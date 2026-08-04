/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data

import fr.shiningcat.binclockwidget.data.weather.CurrentDto
import fr.shiningcat.binclockwidget.data.weather.DailyDto
import fr.shiningcat.binclockwidget.data.weather.ForecastDto
import fr.shiningcat.binclockwidget.data.weather.WeatherMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WeatherMapperTest {
    @Test fun `maps dto to snapshot`() {
        val dto = ForecastDto(CurrentDto(3, 0), DailyDto(listOf(61, 71)))
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertEquals(3, snap.nowCode)
        assertFalse(snap.nowIsDay)
        assertEquals(61, snap.todayCode)
        assertEquals(71, snap.tomorrowCode)
        assertEquals(1000L, snap.fetchedAtEpochMs)
    }

    @Test fun `empty daily falls back to current code`() {
        val dto = ForecastDto(CurrentDto(3, 0), DailyDto(emptyList()))
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertEquals(3, snap.todayCode)
        assertEquals(3, snap.tomorrowCode)
    }

    @Test fun `single-element daily falls back tomorrow to today`() {
        val dto = ForecastDto(CurrentDto(3, 0), DailyDto(listOf(80)))
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertEquals(80, snap.todayCode)
        assertEquals(80, snap.tomorrowCode)
    }

    @Test fun `maps is_day one to day`() {
        val dto = ForecastDto(CurrentDto(3, 1), DailyDto(listOf(61, 71)))
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertTrue(snap.nowIsDay)
    }

    @Test fun `maps today sunrise and sunset from daily`() {
        val dto =
            ForecastDto(
                CurrentDto(3, 1),
                DailyDto(
                    weatherCode = listOf(61, 71),
                    sunrise = listOf("2026-08-04T05:42", "2026-08-05T05:44"),
                    sunset = listOf("2026-08-04T21:30", "2026-08-05T21:28"),
                ),
            )
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertEquals("2026-08-04T05:42", snap.sunriseToday)
        assertEquals("2026-08-04T21:30", snap.sunsetToday)
    }

    @Test fun `absent sunrise and sunset map to null`() {
        val dto = ForecastDto(CurrentDto(3, 1), DailyDto(listOf(61, 71)))
        val snap = WeatherMapper.toSnapshot(dto, fetchedAtEpochMs = 1000L)
        assertNull(snap.sunriseToday)
        assertNull(snap.sunsetToday)
    }
}
