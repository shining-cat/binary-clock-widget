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
            hairline = this[HAIRLINE] ?: d.hairline,
            tapActions = TapZone.entries.associateWith { zone ->
                this[actionKey(zone)]?.let { runCatching { TapAction.valueOf(it) }.getOrNull() }
                    ?: d.tapActions.getValue(zone)
            },
            tapAppPackages = TapZone.entries.mapNotNull { zone ->
                this[packageKey(zone)]?.let { zone to it }
            }.toMap(),
        )
    }

    private fun WidgetSettings.writeInto(prefs: MutablePreferences) {
        prefs[USE_MATERIAL_YOU] = useMaterialYou
        prefs[COLOR_ARGB] = colorArgb
        prefs[HAIRLINE] = hairline
        TapZone.entries.forEach { zone ->
            tapActions[zone]?.let { prefs[actionKey(zone)] = it.name }
            val pkg = tapAppPackages[zone]
            if (pkg != null) prefs[packageKey(zone)] = pkg else prefs.remove(packageKey(zone))
        }
    }

    private companion object {
        val USE_MATERIAL_YOU = booleanPreferencesKey("use_material_you")
        val COLOR_ARGB = intPreferencesKey("color_argb")
        val HAIRLINE = booleanPreferencesKey("hairline")
        fun actionKey(zone: TapZone) = stringPreferencesKey("tap_action_${zone.name}")
        fun packageKey(zone: TapZone) = stringPreferencesKey("tap_pkg_${zone.name}")
    }
}
