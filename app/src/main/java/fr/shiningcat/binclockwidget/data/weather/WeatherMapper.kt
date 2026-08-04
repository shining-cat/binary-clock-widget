/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot

object WeatherMapper {
    fun toSnapshot(
        dto: ForecastDto,
        fetchedAtEpochMs: Long,
    ) = WeatherSnapshot(
        nowCode = dto.current.weatherCode,
        nowIsDay = dto.current.isDay == 1,
        todayCode = dto.daily.weatherCode.getOrElse(0) { dto.current.weatherCode },
        tomorrowCode = dto.daily.weatherCode.getOrElse(1) { dto.daily.weatherCode.getOrElse(0) { dto.current.weatherCode } },
        fetchedAtEpochMs = fetchedAtEpochMs,
        sunriseToday = dto.daily.sunrise.getOrNull(0),
        sunsetToday = dto.daily.sunset.getOrNull(0),
    )
}
