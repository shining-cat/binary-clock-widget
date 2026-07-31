/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget.model

import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.Face
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings

data class WidgetRenderState(
    val face: Face,
    val alarmSet: Boolean,
    val weather: WeatherSnapshot?,
    val settings: WidgetSettings,
    val battery: BatteryIndicator?,
)

/** Pre-resolved battery indicator: [fraction] drives the gauge (0f..1f), [glyph] the state icon. */
data class BatteryIndicator(
    val fraction: Float,
    val glyph: BatteryGlyph,
)
