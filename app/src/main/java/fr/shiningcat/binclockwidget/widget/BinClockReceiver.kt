package fr.shiningcat.binclockwidget.widget

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import fr.shiningcat.binclockwidget.data.weather.WeatherRefreshWorker
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class BinClockReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BinClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        TickScheduler.schedule(context)
        val request = PeriodicWorkRequestBuilder<WeatherRefreshWorker>(30, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WeatherRefreshWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        TickScheduler.cancel(context)
        WorkManager.getInstance(context).cancelUniqueWork(WeatherRefreshWorker.UNIQUE_NAME)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == BinClockWidget.ACTION_TICK) {
            TickScheduler.schedule(context)
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            // Skip the redraw when the screen is off: inexact alarms still fire in Doze, but an invisible update wastes wakeups.
            if (powerManager?.isInteractive == true) {
                runBlocking { BinClockWidget().updateAll(context) }
            }
        }
    }
}
