package fr.shiningcat.binclockwidget.data.weather

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.shiningcat.binclockwidget.widget.BinClockWidget
import fr.shiningcat.binclockwidget.widget.WidgetGraph

class WeatherRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        // No permission / no fix means there is nothing to fetch; succeed so WorkManager keeps the schedule.
        val location = WidgetGraph.location(applicationContext).lastKnown() ?: return Result.success()
        return runCatching {
            WidgetGraph.weatherRepository(applicationContext).refresh(location.first, location.second)
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
