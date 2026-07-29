package fr.shiningcat.binclockwidget.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking

class BinClockReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BinClockWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // tick scheduling added in a later task
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // tick cancellation added in a later task
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == BinClockWidget.ACTION_TICK) {
            runBlocking { BinClockWidget().updateAll(context) }
            // reschedule added in a later task
        }
    }
}
