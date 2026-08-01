/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.data.weather

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.shiningcat.binclockwidget.data.location.LocationDataSource
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.widget.BinClockWidget
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params),
    KoinComponent {
    // Workers are instantiated by WorkManager, so dependencies are resolved via Koin.
    private val location: LocationDataSource by inject()
    private val weatherRepository: WeatherRepository by inject()
    private val settings: SettingsStore by inject()

    override suspend fun doWork(): Result {
        // Weather is opt-in: a blank endpoint means disabled. Bail before touching location so the
        // app makes zero location/network calls when the user hasn't enabled weather.
        if (settings
                .settings()
                .first()
                .weatherEndpoint
                .isBlank()
        ) {
            return Result.success()
        }
        // No permission / no fix means there is nothing to fetch; succeed so WorkManager keeps the schedule.
        val fix = location.currentLocation() ?: return Result.success()
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
