package fr.shiningcat.binclockwidget.widget

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import fr.shiningcat.binclockwidget.data.alarm.AndroidAlarmDataSource
import fr.shiningcat.binclockwidget.data.location.AndroidLocationDataSource
import fr.shiningcat.binclockwidget.data.location.LocationDataSource
import fr.shiningcat.binclockwidget.data.settings.DataStoreSettingsStore
import fr.shiningcat.binclockwidget.data.weather.DataStoreWeatherCache
import fr.shiningcat.binclockwidget.data.weather.OpenMeteoApi
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.data.weather.WeatherRepositoryImpl
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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

    fun location(context: Context): LocationDataSource =
        AndroidLocationDataSource(context.applicationContext)

    // Full network stack; kept off the per-minute render path in resolver().
    fun weatherRepository(context: Context): WeatherRepository {
        val app = context.applicationContext
        val json = Json { ignoreUnknownKeys = true }
        val client = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        val api = retrofit.create(OpenMeteoApi::class.java)
        return WeatherRepositoryImpl(api, DataStoreWeatherCache(app.weatherDataStore))
    }
}
