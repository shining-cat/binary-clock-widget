package fr.shiningcat.binclockwidget.widget

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking

class BinClockReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BinClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        TickScheduler.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        TickScheduler.cancel(context)
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
