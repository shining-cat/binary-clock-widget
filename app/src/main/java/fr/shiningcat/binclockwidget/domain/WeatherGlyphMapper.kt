/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.WeatherCondition

object WeatherGlyphMapper {
    fun toCondition(wmo: Int): WeatherCondition =
        when (wmo) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.OVERCAST
            45, 48 -> WeatherCondition.FOG
            in 51..57 -> WeatherCondition.DRIZZLE
            in 61..67 -> WeatherCondition.RAIN
            in 71..77 -> WeatherCondition.SNOW
            in 80..82 -> WeatherCondition.RAIN_SHOWERS
            85, 86 -> WeatherCondition.SNOW_SHOWERS
            in 95..99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.UNKNOWN
        }
}
