/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.WeatherEndpoint
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Ready(
        val settings: WidgetSettings,
        val locationGranted: Boolean,
        val materialYouAvailable: Boolean,
    ) : SettingsUiState
}

class SettingsViewModel(
    private val store: SettingsStore,
    private val locationGranted: () -> Boolean,
    private val materialYouAvailable: Boolean,
) : ViewModel() {
    private val permission = MutableStateFlow(locationGranted())

    val uiState: StateFlow<SettingsUiState> =
        combine(store.settings(), permission) { settings, granted ->
            SettingsUiState.Ready(settings, granted, materialYouAvailable)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun onColorChanged(argb: Int) = mutate { it.copy(colorArgb = argb) }

    /** Null resets the icon colour to "inherit dots". */
    fun onIconColorChanged(argb: Int?) = mutate { it.copy(iconColorArgb = argb) }

    fun onBackgroundColorChanged(argb: Int) = mutate { it.copy(backgroundColorArgb = argb) }

    fun onMaterialYouToggled(enabled: Boolean) = mutate { it.copy(useMaterialYou = enabled) }

    fun onTapActionChanged(
        zone: TapZone,
        action: TapAction,
    ) = mutate { it.copy(tapActions = it.tapActions + (zone to action)) }

    fun onTapAppPackageChanged(
        zone: TapZone,
        pkg: String?,
    ) = mutate { it.copy(tapAppPackages = it.tapAppPackages + (zone to pkg)) }

    /**
     * Persists the weather service base URL. A blank value turns weather off (opt-in). An invalid,
     * non-blank URL is ignored so the last good value survives while the user is mid-edit; the UI
     * surfaces the error state. Valid values are stored trimmed; the repository normalizes (trailing
     * slash) at fetch time.
     */
    fun onWeatherEndpointChanged(value: String) {
        if (!WeatherEndpoint.isValid(value)) return
        mutate { it.copy(weatherEndpoint = value.trim()) }
    }

    fun refreshPermission() {
        permission.value = locationGranted()
    }

    private fun mutate(transform: (WidgetSettings) -> WidgetSettings) = viewModelScope.launch { store.update(transform) }
}
