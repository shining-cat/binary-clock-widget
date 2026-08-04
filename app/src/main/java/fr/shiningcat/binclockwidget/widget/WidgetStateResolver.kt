/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import fr.shiningcat.binclockwidget.data.alarm.AlarmDataSource
import fr.shiningcat.binclockwidget.data.battery.BatteryDataSource
import fr.shiningcat.binclockwidget.data.settings.SettingsStore
import fr.shiningcat.binclockwidget.data.weather.WeatherRepository
import fr.shiningcat.binclockwidget.domain.BatteryIndicatorMapper
import fr.shiningcat.binclockwidget.domain.DayNightResolver
import fr.shiningcat.binclockwidget.domain.TimeEncoder
import fr.shiningcat.binclockwidget.widget.model.BatteryIndicator
import fr.shiningcat.binclockwidget.widget.model.WidgetRenderState
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class WidgetStateResolver(
    private val now: () -> LocalDateTime,
    private val alarm: AlarmDataSource,
    private val weather: WeatherRepository,
    private val settings: SettingsStore,
    private val battery: BatteryDataSource,
) {
    suspend fun resolve(): WidgetRenderState {
        val current = settings.settings().first()
        val nowTime = now()
        return WidgetRenderState(
            face = TimeEncoder.encode(nowTime),
            alarmSet = alarm.isAlarmSet(),
            // Weather is opt-in: a blank endpoint means disabled, so surface no weather even if a
            // stale snapshot is still cached — the glyphs disappear the instant weather is turned off.
            // day/night is derived from the render clock (not the fetch-time is_day) so the sun/moon
            // glyph self-corrects between the ≤30-min weather refreshes — see DayNightResolver.
            weather =
                if (current.weatherEndpoint.isBlank()) {
                    null
                } else {
                    weather.cached()?.let {
                        it.copy(nowIsDay = DayNightResolver.isDay(nowTime, it.sunriseToday, it.sunsetToday, it.nowIsDay))
                    }
                },
            settings = current,
            battery =
                battery.read()?.let { status ->
                    BatteryIndicator(
                        fraction = (status.percent / 100f).coerceIn(0f, 1f),
                        glyph = BatteryIndicatorMapper.glyph(status),
                    )
                },
        )
    }
}
