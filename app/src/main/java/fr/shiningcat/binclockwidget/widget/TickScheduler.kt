package fr.shiningcat.binclockwidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

fun nextMinuteBoundaryMs(nowMs: Long): Long = ((nowMs / 60000L) + 1) * 60000L

object TickScheduler {
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = tickPendingIntent(context)
        // Inexact alarm: Doze may coalesce it, which is acceptable for a minute clock and needs no exact-alarm permission.
        alarmManager.set(AlarmManager.RTC, nextMinuteBoundaryMs(System.currentTimeMillis()), pi)
    }

    fun cancel(context: Context) {
        val pi = tickPendingIntent(context)
        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.cancel(pi)
        pi.cancel()
    }

    private fun tickPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BinClockReceiver::class.java).setAction(BinClockWidget.ACTION_TICK)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
