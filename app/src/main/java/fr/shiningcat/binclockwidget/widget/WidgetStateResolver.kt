package fr.shiningcat.binclockwidget.widget

import fr.shiningcat.binclockwidget.data.alarm.AlarmDataSource
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.domain.TimeEncoder
import fr.shiningcat.binclockwidget.widget.model.WidgetRenderState
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class WidgetStateResolver(
    private val now: () -> LocalDateTime,
    private val alarm: AlarmDataSource,
    private val weather: WeatherRepository,
    private val settings: SettingsStore,
) {
    suspend fun resolve(): WidgetRenderState = WidgetRenderState(
        face = TimeEncoder.encode(now()),
        alarmSet = alarm.isAlarmSet(),
        weather = weather.cached(),
        settings = settings.settings().first(),
    )
}
