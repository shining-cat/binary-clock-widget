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
        return WidgetRenderState(
            face = TimeEncoder.encode(now()),
            alarmSet = alarm.isAlarmSet(),
            // Weather is opt-in: a blank endpoint means disabled, so surface no weather even if a
            // stale snapshot is still cached — the glyphs disappear the instant weather is turned off.
            weather = if (current.weatherEndpoint.isBlank()) null else weather.cached(),
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
