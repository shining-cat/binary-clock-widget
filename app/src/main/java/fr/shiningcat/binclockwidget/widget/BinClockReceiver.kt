/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
        val request =
            PeriodicWorkRequestBuilder<WeatherRefreshWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                ).build()
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

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        // The last widget was removed: onDisabled already cancelled the tick, so don't resurrect it.
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_DISABLED) return
        // No widgets placed (e.g. a BOOT_COMPLETED with nothing on the home screen): nothing to arm.
        if (!hasWidgets(context)) return

        // Re-arm the minute tick on ANY live broadcast — not just ACTION_TICK. The AlarmManager
        // chain is dropped on reboot and app replacement, and a bare APPWIDGET_UPDATE never
        // restarts it, which is what froze the clock at a stale time. Rescheduling is idempotent
        // (single alarm, FLAG_UPDATE_CURRENT), so re-arming on every broadcast is safe and cheap.
        TickScheduler.schedule(context)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        // Skip the redraw when the screen is off: inexact alarms still fire in Doze, but an invisible update wastes wakeups.
        if (powerManager?.isInteractive == true) {
            runBlocking { BinClockWidget().updateAll(context) }
        }
    }

    private fun hasWidgets(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val ids = manager.getAppWidgetIds(ComponentName(context, BinClockReceiver::class.java))
        return ids.isNotEmpty()
    }
}
