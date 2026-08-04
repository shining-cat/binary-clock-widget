/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data

import fr.shiningcat.binclockwidget.data.weather.ForecastDto
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForecastDtoParseTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `parses current and daily`() {
        val raw = """{"current":{"weather_code":3,"is_day":1},"daily":{"weather_code":[61,71]}}"""
        val dto = json.decodeFromString<ForecastDto>(raw)
        assertEquals(3, dto.current.weatherCode)
        assertEquals(1, dto.current.isDay)
        assertEquals(listOf(61, 71), dto.daily.weatherCode)
    }

    @Test fun `parses daily sunrise and sunset arrays`() {
        val raw =
            """{"current":{"weather_code":3,"is_day":1},"daily":{"weather_code":[61,71],""" +
                """"sunrise":["2026-08-04T05:42","2026-08-05T05:44"],""" +
                """"sunset":["2026-08-04T21:30","2026-08-05T21:28"]}}"""
        val dto = json.decodeFromString<ForecastDto>(raw)
        assertEquals(listOf("2026-08-04T05:42", "2026-08-05T05:44"), dto.daily.sunrise)
        assertEquals(listOf("2026-08-04T21:30", "2026-08-05T21:28"), dto.daily.sunset)
    }

    @Test fun `daily without sunrise and sunset still parses`() {
        val raw = """{"current":{"weather_code":3,"is_day":1},"daily":{"weather_code":[61,71]}}"""
        val dto = json.decodeFromString<ForecastDto>(raw)
        assertEquals(emptyList<String>(), dto.daily.sunrise)
        assertEquals(emptyList<String>(), dto.daily.sunset)
    }
}
