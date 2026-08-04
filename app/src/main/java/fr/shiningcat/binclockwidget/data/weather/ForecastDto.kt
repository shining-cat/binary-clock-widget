/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ForecastDto(
    @SerialName("current") val current: CurrentDto,
    @SerialName("daily") val daily: DailyDto,
)

@Serializable data class CurrentDto(
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int, // 1 = day, 0 = night
)

@Serializable data class DailyDto(
    @SerialName("weather_code") val weatherCode: List<Int>, // [today, tomorrow]
    // ISO-local timestamps [today, tomorrow], e.g. "2026-08-04T05:42". Defaulted so a custom
    // endpoint that omits them still parses; day/night then falls back to the cached is_day.
    @SerialName("sunrise") val sunrise: List<String> = emptyList(),
    @SerialName("sunset") val sunset: List<String> = emptyList(),
)
