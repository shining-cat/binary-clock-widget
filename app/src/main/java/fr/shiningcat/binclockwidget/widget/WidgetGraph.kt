package fr.shiningcat.binclockwidget.widget

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import fr.shiningcat.binclockwidget.data.alarm.AndroidAlarmDataSource
import fr.shiningcat.binclockwidget.data.settings.DataStoreSettingsStore
import fr.shiningcat.binclockwidget.data.weather.DataStoreWeatherCache
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import java.time.LocalDateTime

private val Context.settingsDataStore by preferencesDataStore(name = "settings")
private val Context.weatherDataStore by preferencesDataStore(name = "weather")

object WidgetGraph {
    // Temporary manual wiring; replaced by dependency injection in a later task.
    fun resolver(context: Context): WidgetStateResolver {
        val app = context.applicationContext
        val cache = DataStoreWeatherCache(app.weatherDataStore)
        val weather = object : WeatherRepository {
            override suspend fun cached(): WeatherSnapshot? = cache.read()
            override suspend fun refresh(lat: Double, lon: Double): WeatherSnapshot =
                error("refresh is handled by the weather worker")
        }
        return WidgetStateResolver(
            now = { LocalDateTime.now() },
            alarm = AndroidAlarmDataSource(app),
            weather = weather,
            settings = DataStoreSettingsStore(app.settingsDataStore),
        )
    }
}
