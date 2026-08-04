/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain.model

enum class DayNight { DAY, NIGHT }

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    OVERCAST,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    RAIN_SHOWERS,
    SNOW_SHOWERS,
    THUNDERSTORM,
    UNKNOWN,
}

/** One resolved weather picture for the widget. */
data class WeatherSnapshot(
    val nowCode: Int,
    val nowIsDay: Boolean,
    val todayCode: Int,
    val tomorrowCode: Int,
    val fetchedAtEpochMs: Long,
    // Today's sunrise/sunset as Open-Meteo ISO-local strings; null when unavailable (old cache or an
    // endpoint that omits them). Used to derive nowIsDay at render time — see DayNightResolver.
    val sunriseToday: String? = null,
    val sunsetToday: String? = null,
)
