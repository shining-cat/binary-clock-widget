package fr.shiningcat.binclockwidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import fr.shiningcat.binclockwidget.widget.ui.DotGrid

class BinClockWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = WidgetGraph.resolver(context).resolve()
        provideContent { DotGrid(state) }
    }

    companion object {
        const val ACTION_TICK = "fr.shiningcat.binclockwidget.ACTION_TICK"
    }
}
