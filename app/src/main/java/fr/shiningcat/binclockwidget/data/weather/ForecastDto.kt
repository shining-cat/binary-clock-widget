package fr.shiningcat.binclockwidget.data.weather

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable data class ForecastDto(
    @SerialName("current") val current: CurrentDto,
    @SerialName("daily") val daily: DailyDto,
)

@Serializable data class CurrentDto(
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("is_day") val isDay: Int,   // 1 = day, 0 = night
)

@Serializable data class DailyDto(
    @SerialName("weather_code") val weatherCode: List<Int>,  // [today, tomorrow]
)
