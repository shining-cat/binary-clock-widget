package fr.shiningcat.binclockwidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

fun nextMinuteBoundaryMs(nowMs: Long): Long = ((nowMs / 60000L) + 1) * 60000L

object TickScheduler {
    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = tickPendingIntent(context)
        val triggerAt = nextMinuteBoundaryMs(System.currentTimeMillis())
        // Exact + non-wakeup RTC: fires on the minute while the screen is on, and never wakes the
        // device just to redraw an unseen widget. Fall back to inexact if exact scheduling isn't
        // permitted (SCHEDULE_EXACT_ALARM is revocable on API 31-32).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC, triggerAt, pi)
        } else {
            alarmManager.set(AlarmManager.RTC, triggerAt, pi)
        }
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
