/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data

import fr.shiningcat.binclockwidget.data.weather.CurrentDto
import fr.shiningcat.binclockwidget.data.weather.DailyDto
import fr.shiningcat.binclockwidget.data.weather.ForecastDto
import fr.shiningcat.binclockwidget.data.weather.OpenMeteoApi
import fr.shiningcat.binclockwidget.data.weather.WeatherCache
import fr.shiningcat.binclockwidget.data.weather.WeatherRepositoryImpl
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

private class FakeWeatherCache : WeatherCache {
    private var stored: WeatherSnapshot? = null

    override suspend fun read(): WeatherSnapshot? = stored

    override suspend fun write(snapshot: WeatherSnapshot) {
        stored = snapshot
    }
}

class WeatherRepositoryTest {
    private val api = mockk<OpenMeteoApi>()
    private val cache = FakeWeatherCache()
    private val repo = WeatherRepositoryImpl(api, cache) { 5000L }

    @Test fun `refresh fetches, caches, returns`() =
        runTest {
            coEvery { api.forecast(any(), any(), any(), any(), any(), any()) } returns
                ForecastDto(CurrentDto(0, 1), DailyDto(listOf(1, 3)))
            val snap = repo.refresh(59.9, 10.7)
            assertEquals(0, snap.nowCode)
            assertEquals(5000L, snap.fetchedAtEpochMs)
            assertEquals(snap, cache.read())
        }

    @Test fun `cached returns last written`() =
        runTest {
            assertNull(repo.cached())
            cache.write(WeatherSnapshot(1, true, 1, 1, 100L))
            assertEquals(1, repo.cached()?.nowCode)
        }
}
