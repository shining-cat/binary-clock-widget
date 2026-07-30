/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import fr.shiningcat.binclockwidget.widget.ui.DotGrid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BinClockWidget :
    GlanceAppWidget(),
    KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Exact

    // Glance widgets are instantiated by the framework, so dependencies are resolved via Koin.
    private val resolver: WidgetStateResolver by inject()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val state = resolver.resolve()
        provideContent { DotGrid(state) }
    }

    companion object {
        const val ACTION_TICK = "fr.shiningcat.binclockwidget.ACTION_TICK"
    }
}
