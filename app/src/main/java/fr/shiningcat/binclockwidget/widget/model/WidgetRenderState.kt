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
