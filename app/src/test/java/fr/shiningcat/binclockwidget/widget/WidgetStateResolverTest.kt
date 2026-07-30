/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import fr.shiningcat.binclockwidget.data.alarm.AlarmDataSource
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
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
    fun `resolves face for injected time and passes through alarm+weather+settings`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { true },
                    weather = FakeWeatherRepo(WeatherSnapshot(0, true, 1, 3, 1L)),
                    settings = FakeSettingsStore(WidgetSettings()),
                )

            val s = resolver.resolve()

            assertEquals(4, s.face.rows.size)
            assertTrue(s.alarmSet)
            assertEquals(0, s.weather?.nowCode)
            assertEquals(WidgetSettings(), s.settings)
        }

    @Test
    fun `passes through null weather and unset alarm`() =
        runTest {
            val resolver =
                WidgetStateResolver(
                    now = { LocalDateTime.of(2026, 7, 28, 13, 47) },
                    alarm = AlarmDataSource { false },
                    weather = FakeWeatherRepo(null),
                    settings = FakeSettingsStore(WidgetSettings()),
                )

            val s = resolver.resolve()

            assertFalse(s.alarmSet)
            assertNull(s.weather)
            assertEquals(4, s.face.rows.size)
        }
}
