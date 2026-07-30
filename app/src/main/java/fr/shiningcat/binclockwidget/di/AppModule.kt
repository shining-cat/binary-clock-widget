package fr.shiningcat.binclockwidget.di

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import fr.shiningcat.binclockwidget.config.SettingsViewModel
import fr.shiningcat.binclockwidget.data.alarm.AlarmDataSource
import fr.shiningcat.binclockwidget.data.alarm.AndroidAlarmDataSource
import fr.shiningcat.binclockwidget.data.location.AndroidLocationDataSource
import fr.shiningcat.binclockwidget.data.location.LocationDataSource
import fr.shiningcat.binclockwidget.data.settings.DataStoreSettingsStore
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.DataStoreWeatherCache
import fr.shiningcat.binclockwidget.data.weather.OpenMeteoApi
import fr.shiningcat.binclockwidget.data.weather.WeatherCache
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.data.weather.WeatherRepositoryImpl
import fr.shiningcat.binclockwidget.widget.WidgetStateResolver
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.LocalDateTime

/**
 * Single Koin module for the app. Every dependency is a [single] so the HTTP stack
 * (OkHttp/Retrofit) is built once and reused; the same [WeatherRepository] serves both the
 * per-minute render path (`cached()`) and the refresh worker (`refresh()`).
 */
val appModule = module {
    single(named(SETTINGS_DATASTORE)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(SETTINGS_DATASTORE)
        }
    }
    single(named(WEATHER_DATASTORE)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(WEATHER_DATASTORE)
        }
    }

    single<SettingsStore> { DataStoreSettingsStore(get(named(SETTINGS_DATASTORE))) }
    single<WeatherCache> { DataStoreWeatherCache(get(named(WEATHER_DATASTORE))) }

    single { Json { ignoreUnknownKeys = true } }
    single { OkHttpClient() }
    single {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(OpenMeteoApi::class.java) }

    single<WeatherRepository> { WeatherRepositoryImpl(get(), get()) }

    single<LocationDataSource> { AndroidLocationDataSource(androidContext()) }
    single<AlarmDataSource> { AndroidAlarmDataSource(androidContext()) }

    single {
        WidgetStateResolver(
            now = { LocalDateTime.now() },
            alarm = get(),
            weather = get(),
            settings = get(),
        )
    }

    viewModel {
        val appContext = androidContext()
        SettingsViewModel(
            store = get(),
            locationGranted = {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            },
            materialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        )
    }
}

private const val SETTINGS_DATASTORE = "settings"
private const val WEATHER_DATASTORE = "weather"
