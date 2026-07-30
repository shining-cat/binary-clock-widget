/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget.model

import fr.shiningcat.binclockwidget.domain.model.Face
import fr.shiningcat.binclockwidget.domain.model.WeatherSnapshot
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings

data class WidgetRenderState(
    val face: Face,
    val alarmSet: Boolean,
    val weather: WeatherSnapshot?,
    val settings: WidgetSettings,
)
