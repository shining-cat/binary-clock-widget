package fr.shiningcat.binclockwidget.data.weather

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.shiningcat.binclockwidget.data.location.LocationDataSource
import fr.shiningcat.binclockwidget.widget.BinClockWidget
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params), KoinComponent {
    // Workers are instantiated by WorkManager, so dependencies are resolved via Koin.
    private val location: LocationDataSource by inject()
    private val weatherRepository: WeatherRepository by inject()

    override suspend fun doWork(): Result {
        // No permission / no fix means there is nothing to fetch; succeed so WorkManager keeps the schedule.
        val fix = location.lastKnown() ?: return Result.success()
        return runCatching {
            weatherRepository.refresh(fix.first, fix.second)
            BinClockWidget().updateAll(applicationContext)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val UNIQUE_NAME = "weather-refresh"
    }
}
