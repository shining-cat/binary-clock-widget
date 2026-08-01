/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsStore {
    fun settings(): Flow<WidgetSettings>

    suspend fun update(transform: (WidgetSettings) -> WidgetSettings)
}

class DataStoreSettingsStore(
    private val dataStore: DataStore<Preferences>,
) : SettingsStore {
    override fun settings(): Flow<WidgetSettings> = dataStore.data.map { it.toSettings() }

    override suspend fun update(transform: (WidgetSettings) -> WidgetSettings) {
        val current = dataStore.data.first().toSettings()
        val updated = transform(current)
        dataStore.edit { prefs -> updated.writeInto(prefs) }
    }

    private fun Preferences.toSettings(): WidgetSettings {
        val d = WidgetSettings()
        return WidgetSettings(
            useMaterialYou = this[USE_MATERIAL_YOU] ?: d.useMaterialYou,
            colorArgb = this[COLOR_ARGB] ?: d.colorArgb,
            iconColorArgb = this[ICON_COLOR_ARGB], // absent = inherit dots colour
            backgroundColorArgb = this[BACKGROUND_COLOR_ARGB] ?: d.backgroundColorArgb,
            tapActions =
                TapZone.entries.associateWith { zone ->
                    this[actionKey(zone)]?.let { runCatching { TapAction.valueOf(it) }.getOrNull() }
                        ?: d.tapActions.getValue(zone)
                },
            tapAppPackages =
                TapZone.entries
                    .mapNotNull { zone ->
                        this[packageKey(zone)]?.let { zone to it }
                    }.toMap(),
            weatherEndpoint = this[WEATHER_ENDPOINT] ?: d.weatherEndpoint, // absent = weather disabled
        )
    }

    private fun WidgetSettings.writeInto(prefs: MutablePreferences) {
        prefs[USE_MATERIAL_YOU] = useMaterialYou
        prefs[COLOR_ARGB] = colorArgb
        if (iconColorArgb != null) prefs[ICON_COLOR_ARGB] = iconColorArgb else prefs.remove(ICON_COLOR_ARGB)
        prefs[BACKGROUND_COLOR_ARGB] = backgroundColorArgb
        TapZone.entries.forEach { zone ->
            tapActions[zone]?.let { prefs[actionKey(zone)] = it.name }
            val pkg = tapAppPackages[zone]
            if (pkg != null) prefs[packageKey(zone)] = pkg else prefs.remove(packageKey(zone))
        }
        if (weatherEndpoint.isNotBlank()) prefs[WEATHER_ENDPOINT] = weatherEndpoint else prefs.remove(WEATHER_ENDPOINT)
    }

    private companion object {
        val USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        val COLOR_ARGB = intPreferencesKey("color_argb")
        val ICON_COLOR_ARGB = intPreferencesKey("icon_color_argb")
        val BACKGROUND_COLOR_ARGB = intPreferencesKey("background_color_argb")
        val WEATHER_ENDPOINT = stringPreferencesKey("weather_endpoint")

        fun actionKey(zone: TapZone) = stringPreferencesKey("tap_action_${zone.name}")

        fun packageKey(zone: TapZone) = stringPreferencesKey("tap_pkg_${zone.name}")
    }
}
