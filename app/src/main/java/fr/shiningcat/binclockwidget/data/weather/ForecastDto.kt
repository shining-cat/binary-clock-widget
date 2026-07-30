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
)
