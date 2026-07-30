package fr.shiningcat.binclockwidget.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import fr.shiningcat.binclockwidget.data.settings.DataStoreSettingsStore
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.DataStoreWeatherCache
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStorePersistenceTest {
    private fun CoroutineScope.store(dir: File, name: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = this,
            produceFile = { dir.resolve(name) },
        )

    @Test
    fun `weather cache read returns null on empty store`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val cache = DataStoreWeatherCache(backgroundScope.store(dir, "weather.preferences_pb"))
            assertNull(cache.read())
        }

    @Test
    fun `weather cache round-trips a snapshot`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val cache = DataStoreWeatherCache(backgroundScope.store(dir, "weather.preferences_pb"))
            val snap = WeatherSnapshot(
                nowCode = 3,
                nowIsDay = false,
                todayCode = 61,
                tomorrowCode = 71,
                fetchedAtEpochMs = 1_700_000_000_000L,
            )
            cache.write(snap)
            assertEquals(snap, cache.read())
        }

    @Test
    fun `settings on empty store equals defaults`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val settings: SettingsStore =
                DataStoreSettingsStore(backgroundScope.store(dir, "settings.preferences_pb"))
            assertEquals(WidgetSettings(), settings.settings().first())
        }

    @Test
    fun `settings update reflects every change`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val settings: SettingsStore =
                DataStoreSettingsStore(backgroundScope.store(dir, "settings.preferences_pb"))
            settings.update {
                it.copy(
                    hairline = true,
                    useMaterialYou = true,
                    colorArgb = 0xFF00FF00.toInt(),
                    tapActions = it.tapActions + (TapZone.WEATHER to TapAction.OPEN_APP),
                    tapAppPackages = mapOf(TapZone.WEATHER to "com.x"),
                )
            }
            val result = settings.settings().first()
            assertEquals(true, result.hairline)
            assertEquals(true, result.useMaterialYou)
            assertEquals(0xFF00FF00.toInt(), result.colorArgb)
            assertEquals(TapAction.OPEN_APP, result.tapActions[TapZone.WEATHER])
            assertEquals(TapAction.OPEN_ALARMS, result.tapActions[TapZone.ALARM])
            assertEquals("com.x", result.tapAppPackages[TapZone.WEATHER])
        }

    @Test
    fun `icon colour round-trips and null removes the key`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val settings: SettingsStore =
                DataStoreSettingsStore(backgroundScope.store(dir, "settings.preferences_pb"))

            // A set icon colour persists and reads back.
            settings.update { it.copy(iconColorArgb = 0xFF112233.toInt()) }
            assertEquals(0xFF112233.toInt(), settings.settings().first().iconColorArgb)

            // Resetting to "inherit dots" must REMOVE the key, not leave the previous value behind —
            // otherwise "reset to dots colour" would silently keep the old icon colour.
            settings.update { it.copy(iconColorArgb = null) }
            assertNull(settings.settings().first().iconColorArgb)
        }

    @Test
    fun `background colour round-trips including alpha`(@TempDir dir: File) =
        runTest(UnconfinedTestDispatcher()) {
            val settings: SettingsStore =
                DataStoreSettingsStore(backgroundScope.store(dir, "settings.preferences_pb"))
            // Translucent value exercises the alpha channel that is the whole point of the field.
            settings.update { it.copy(backgroundColorArgb = 0x80FF0000.toInt()) }
            assertEquals(0x80FF0000.toInt(), settings.settings().first().backgroundColorArgb)
        }
}
