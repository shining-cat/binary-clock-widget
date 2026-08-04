/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "weather_code,is_day",
        @Query("daily") daily: String = "weather_code,sunrise,sunset",
        @Query("forecast_days") days: Int = 2,
        @Query("timezone") tz: String = "auto",
    ): ForecastDto
}
