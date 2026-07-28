package fr.shiningcat.binclockwidget.domain.model

enum class DayNight { DAY, NIGHT }
enum class WeatherCondition { CLEAR, PARTLY_CLOUDY, OVERCAST, FOG, DRIZZLE, RAIN,
                              SNOW, RAIN_SHOWERS, SNOW_SHOWERS, THUNDERSTORM, UNKNOWN }

/** One resolved weather picture for the widget. */
data class WeatherSnapshot(
    val nowCode: Int, val nowIsDay: Boolean,
    val todayCode: Int, val tomorrowCode: Int,
    val fetchedAtEpochMs: Long,
)
