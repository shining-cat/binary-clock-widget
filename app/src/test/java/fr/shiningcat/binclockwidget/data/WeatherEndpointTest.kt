/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data

import fr.shiningcat.binclockwidget.data.weather.WeatherEndpoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WeatherEndpointTest {
    @Test fun `blank is valid — it means weather disabled`() {
        assertTrue(WeatherEndpoint.isValid(""))
        assertTrue(WeatherEndpoint.isValid("   "))
    }

    @Test fun `http and https URLs are valid`() {
        assertTrue(WeatherEndpoint.isValid("https://api.open-meteo.com/"))
        assertTrue(WeatherEndpoint.isValid("http://weather.lan:8080/api"))
    }

    @Test fun `garbage and non-http schemes are invalid`() {
        assertFalse(WeatherEndpoint.isValid("not a url"))
        assertFalse(WeatherEndpoint.isValid("ftp://example.org"))
        assertFalse(WeatherEndpoint.isValid("open-meteo.com")) // no scheme
    }

    @Test fun `normalize guarantees a trailing slash for a host-only URL`() {
        assertEquals("https://api.open-meteo.com/", WeatherEndpoint.normalize("https://api.open-meteo.com"))
    }

    @Test fun `normalize preserves and slash-terminates a path`() {
        assertEquals("https://weather.example.org/api/", WeatherEndpoint.normalize("https://weather.example.org/api"))
        assertEquals("https://weather.example.org/api/", WeatherEndpoint.normalize("https://weather.example.org/api/"))
    }

    @Test fun `normalize trims surrounding whitespace`() {
        assertEquals("https://api.open-meteo.com/", WeatherEndpoint.normalize("  https://api.open-meteo.com/  "))
    }
}
