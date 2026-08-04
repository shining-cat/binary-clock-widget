/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import fr.shiningcat.binclockwidget.data.alarm.AlarmDataSource
import fr.shiningcat.binclockwidget.data.battery.BatteryDataSource
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.BatteryStatus
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

private const val OPEN_METEO = "https://api.open-meteo.com/"

private class FakeWeatherRepo(
    private val snapshot: WeatherSnapshot?,
) : WeatherRepository {
    override suspend fun cached(): WeatherSnapshot? = snapshot

    override suspend fun refresh(
        lat: Double,
        lon: Double,
    ): WeatherSnapshot = snapshot ?: error("no snapshot")
}

private class FakeSettingsStore(
    private val value: WidgetSettings,
) : SettingsStore {
    override fun settings(): Flow<WidgetSettings> = flowOf(value)

    override suspend fun update(transform: (WidgetSettings) -> WidgetSettings) = Unit
}

class WidgetStateResolverTest {
    @Test
    fun `resolves face, passes through alarm+weather+settings, maps battery`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { true },
                    weather = FakeWeatherRepo(WeatherSnapshot(0, true, 1, 3, 1L)),
                    settings = FakeSettingsStore(WidgetSettings(weatherEndpoint = OPEN_METEO)),
                    battery = BatteryDataSource { BatteryStatus(percent = 50, isCharging = false) },
                )

            val s = resolver.resolve()

            assertEquals(4, s.face.rows.size)
            assertTrue(s.alarmSet)
            assertEquals(0, s.weather?.nowCode)
            assertEquals(WidgetSettings(weatherEndpoint = OPEN_METEO), s.settings)
            assertEquals(0.5f, s.battery?.fraction)
            assertEquals(BatteryGlyph.NONE, s.battery?.glyph)
        }

    @Test
    fun `charging battery maps to the charging glyph`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    weather = FakeWeatherRepo(null),
                    settings = FakeSettingsStore(WidgetSettings()),
                    battery = BatteryDataSource { BatteryStatus(percent = 5, isCharging = true) },
                )

            val s = resolver.resolve()

            assertEquals(BatteryGlyph.CHARGING, s.battery?.glyph)
        }

    @Test
    fun `resolve clamps over-100 percent to full fraction`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    weather = FakeWeatherRepo(null),
                    settings = FakeSettingsStore(WidgetSettings()),
                    battery = BatteryDataSource { BatteryStatus(percent = 150, isCharging = false) },
                )

            val s = resolver.resolve()

            assertEquals(1.0f, s.battery?.fraction)
        }

    @Test
    fun `a blank weather endpoint disables weather even when a snapshot is cached`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    // Cache still holds a snapshot from before weather was turned off...
                    weather = FakeWeatherRepo(WeatherSnapshot(0, true, 1, 3, 1L)),
                    // ...but the endpoint is blank (disabled), so the widget must show no weather.
                    settings = FakeSettingsStore(WidgetSettings(weatherEndpoint = "")),
                    battery = BatteryDataSource { null },
                )

            assertNull(resolver.resolve().weather)
        }

    @Test
    fun `a configured weather endpoint surfaces the cached snapshot`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    weather = FakeWeatherRepo(WeatherSnapshot(2, true, 1, 3, 1L)),
                    settings = FakeSettingsStore(WidgetSettings(weatherEndpoint = "https://api.open-meteo.com/")),
                    battery = BatteryDataSource { null },
                )

            assertEquals(2, resolver.resolve().weather?.nowCode)
        }

    @Test
    fun `stale night snapshot is corrected to day from cached sunrise-sunset at render time`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 8, 4, 13, 47) },
                    alarm = AlarmDataSource { false },
                    // Cached at dawn with is_day=false; sunrise/sunset bracket the render time.
                    weather =
                        FakeWeatherRepo(
                            WeatherSnapshot(
                                nowCode = 0,
                                nowIsDay = false,
                                todayCode = 1,
                                tomorrowCode = 3,
                                fetchedAtEpochMs = 1L,
                                sunriseToday = "2026-08-04T05:42",
                                sunsetToday = "2026-08-04T21:30",
                            ),
                        ),
                    settings = FakeSettingsStore(WidgetSettings(weatherEndpoint = OPEN_METEO)),
                    battery = BatteryDataSource { null },
                )

            assertTrue(resolver.resolve().weather?.nowIsDay == true)
        }

    @Test
    fun `without cached sunrise-sunset the fetch-time is_day is kept`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 8, 4, 13, 47) },
                    alarm = AlarmDataSource { false },
                    // No sunrise/sunset (old cache / endpoint omits them) → fall back to cached is_day.
                    weather = FakeWeatherRepo(WeatherSnapshot(0, false, 1, 3, 1L)),
                    settings = FakeSettingsStore(WidgetSettings(weatherEndpoint = OPEN_METEO)),
                    battery = BatteryDataSource { null },
                )

            assertFalse(resolver.resolve().weather?.nowIsDay == true)
        }

    @Test
    fun `passes through null weather, unset alarm and unavailable battery`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    weather = FakeWeatherRepo(null),
                    settings = FakeSettingsStore(WidgetSettings()),
                    battery = BatteryDataSource { null },
                )

            val s = resolver.resolve()

            assertFalse(s.alarmSet)
            assertNull(s.weather)
            assertNull(s.battery)
            assertEquals(4, s.face.rows.size)
        }
}
