/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.AlarmClock
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import fr.shiningcat.binclockwidget.R
import fr.shiningcat.binclockwidget.domain.WeatherGlyphMapper
import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.Cell
import fr.shiningcat.binclockwidget.domain.model.DayNight
import fr.shiningcat.binclockwidget.domain.model.GlyphSlot
import fr.shiningcat.binclockwidget.domain.model.RowKind
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WeatherCondition
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import fr.shiningcat.binclockwidget.widget.model.BatteryIndicator
import fr.shiningcat.binclockwidget.widget.model.WidgetRenderState

/**
 * Glance 1.1.x marks every runtime-Color ColorProvider factory @RestrictTo; the public
 * ColorProvider interface is the only unrestricted way to build one from a user-chosen ARGB.
 */
internal data class ArgbColorProvider(
    val color: Color,
) : ColorProvider {
    override fun getColor(context: Context): Color = color
}

// Nullable: an unmappable condition renders nothing (empty cell) rather than a placeholder dash.
@DrawableRes
internal fun weatherDrawable(
    condition: WeatherCondition,
    dayNight: DayNight,
): Int? =
    when (condition) {
        WeatherCondition.CLEAR -> {
            if (dayNight == DayNight.DAY) R.drawable.ic_light_mode else R.drawable.ic_clear_night
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            if (dayNight ==
                DayNight.DAY
            ) {
                R.drawable.ic_partly_cloudy_day
            } else {
                R.drawable.ic_partly_cloudy_night
            }
        }
        WeatherCondition.OVERCAST -> {
            R.drawable.ic_cloud
        }
        WeatherCondition.FOG -> {
            R.drawable.ic_foggy
        }
        WeatherCondition.DRIZZLE -> {
            R.drawable.ic_rainy_light
        }
        WeatherCondition.RAIN -> {
            R.drawable.ic_rainy
        }
        WeatherCondition.SNOW -> {
            R.drawable.ic_weather_snowy
        }
        WeatherCondition.RAIN_SHOWERS -> {
            R.drawable.ic_rainy_heavy
        }
        WeatherCondition.SNOW_SHOWERS -> {
            R.drawable.ic_snowing
        }
        WeatherCondition.THUNDERSTORM -> {
            R.drawable.ic_thunderstorm
        }
        WeatherCondition.UNKNOWN -> {
            null
        }
    }

// Nullable: weather slots render nothing when weather is disabled/unavailable or the code is
// unmappable. ALARM always resolves to a drawable, so a null return means "draw no glyph here".
@DrawableRes
internal fun resolveGlyph(
    slot: GlyphSlot,
    state: WidgetRenderState,
): Int? {
    val weather = state.weather
    return when (slot) {
        GlyphSlot.ALARM -> {
            if (state.alarmSet) R.drawable.ic_alarm else R.drawable.ic_alarm_off
        }
        GlyphSlot.WEATHER_NOW -> {
            weather?.let {
                weatherDrawable(
                    WeatherGlyphMapper.toCondition(it.nowCode),
                    if (it.nowIsDay) DayNight.DAY else DayNight.NIGHT,
                )
            }
        }
        GlyphSlot.WEATHER_TODAY -> {
            weather?.let {
                weatherDrawable(WeatherGlyphMapper.toCondition(it.todayCode), DayNight.DAY)
            }
        }
        GlyphSlot.WEATHER_TOMORROW -> {
            weather?.let {
                weatherDrawable(WeatherGlyphMapper.toCondition(it.tomorrowCode), DayNight.DAY)
            }
        }
    }
}

@DrawableRes
internal fun batteryGlyphDrawable(glyph: BatteryGlyph): Int? =
    when (glyph) {
        BatteryGlyph.NONE -> null
        BatteryGlyph.LOW -> R.drawable.ic_warning
        BatteryGlyph.VERY_LOW -> R.drawable.ic_warning_filled
        BatteryGlyph.CHARGING -> R.drawable.ic_bolt
    }

// Layout fractions, shared by the height budget and BatteryIndicatorRow so they stay in sync.
private const val DOT_CELL_FRACTION = 0.55f // dot diameter as a fraction of its square cell
private const val GLYPH_DOT_FRACTION = 0.8f // battery glyph size as a fraction of the dot

// The battery band is one full cell — the same footprint as a dot row — so the gauge, centred in
// it, floats with the same air a visible dot has in its cell, and the widget reads as five even
// rows. This holds regardless of the gauge height (see gaugeHeight == dot/2 in BatteryIndicatorRow).
// The empty-track outline weight is baked into ic_gauge_track to match the off-dot ring; see that
// drawable for the rim and aspect-ratio derivation.
private const val BATTERY_ROW_CELL_FRACTION = 1f

@Composable
fun DotGrid(state: WidgetRenderState) {
    GlanceTheme {
        // One base colour drives lit, dim, and gauge so all three follow the same source —
        // Material You's primary when enabled, otherwise the user's ARGB. Deriving dim/gauge
        // from a fixed colorArgb here was the bug where off-rings stayed white under Material You.
        val context = LocalContext.current
        val useMaterialYou =
            state.settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        // Dots follow Material You's primary (the vivid wallpaper accent) or the user's dots colour.
        val baseColor: Color =
            if (useMaterialYou) {
                GlanceTheme.colors.primary.getColor(context)
            } else {
                Color(state.settings.colorArgb)
            }
        // Icons are deliberately a *different* tone from the dots for readability: under Material You
        // that's the secondary companion tint; otherwise the user's icon colour, defaulting to the
        // dots colour when unset (inherit).
        val iconColor: Color =
            if (useMaterialYou) {
                GlanceTheme.colors.secondary.getColor(context)
            } else {
                state.settings.iconColorArgb?.let { Color(it) } ?: baseColor
            }
        val litColor: ColorProvider = ArgbColorProvider(baseColor)
        // Off-rings share the lit colour: the hollow-ring vs. filled-dot shape already signals
        // on/off, so a dimmer tint just read as "wrong colour" against the rest of the grid.
        val dimColor: ColorProvider = litColor
        val glyphColor: ColorProvider = ArgbColorProvider(iconColor)
        // Gauge shares the icon tone (not the dot tone): it's secondary info that groups visually
        // with the battery glyph beside it, so they read as one unit distinct from the clock dots.
        // Concrete Color, not a runtime ColorProvider — Glance's background(ColorProvider) overload
        // doesn't paint a runtime-built provider (the gotcha the old separator hit).
        val gaugeColor: Color = iconColor
        // User-chosen background; Material You never overrides it, so pure AMOLED black is preserved.
        // The alpha channel is honoured, allowing a translucent widget over the launcher wallpaper.
        val backgroundColor: Color = Color(state.settings.backgroundColorArgb)

        val size = LocalSize.current
        // Height is shared by five stacked components — four dot rows plus the battery band between
        // minutes and day. Reserve the band's fraction so it doesn't overflow and make the launcher
        // squeeze the bottom (date) rows; the old 2.dp hairline was negligible, this band isn't.
        val cell = minOf(size.width / 6, size.height / (4 + BATTERY_ROW_CELL_FRACTION))
        val dot = cell * DOT_CELL_FRACTION

        Box(
            modifier = GlanceModifier.fillMaxSize().background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = GlanceModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                state.face.rows.forEachIndexed { index, row ->
                    Row(
                        modifier = GlanceModifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        row.cells.forEach { c -> CellImage(c, row.kind, state, litColor, dimColor, glyphColor, cell, dot) }
                    }
                    if (index == 1) {
                        BatteryIndicatorRow(
                            indicator = state.battery,
                            gaugeColor = gaugeColor,
                            glyphColor = glyphColor,
                            cell = cell,
                            dot = dot,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryIndicatorRow(
    indicator: BatteryIndicator?,
    gaugeColor: Color,
    glyphColor: ColorProvider,
    cell: Dp,
    dot: Dp,
) {
    val fraction = indicator?.fraction ?: 0f
    val glyph = indicator?.glyph ?: BatteryGlyph.NONE
    // Dots sit (cell - dot)/2 inside their square cells, so a bar filling the row from its edge
    // juts out past the leftmost dot toward the widget border. Inset the gauge by that same amount
    // so its left edge lines up with the dots; the glyph fills the 6th column, centred under the
    // 6th dot. The gauge height matches the *visible* dot (dot/2 — the dot circle is radius 6 of a
    // 24-unit viewport, so it only fills half its box); the battery glyph is a touch smaller than
    // the grid icons (it's secondary information). cornerRadius on the fill only rounds on API 31+;
    // older versions fall back to square corners by design (the outline vector stays rounded
    // regardless, so old APIs show a rounded track with a squared fill — acceptable).
    val edgeInset = (cell - dot) / 2
    val trackWidth = cell * 5 - edgeInset
    val gaugeHeight = dot / 2
    val gaugeRadius = gaugeHeight / 2
    val glyphSize = dot * GLYPH_DOT_FRACTION
    // The band is one full cell — identical footprint to a dot row — so the gauge, centred in it,
    // floats with the same air above and below that a visible dot has in its own cell. Every row in
    // the Column is then cell-tall and the grid reads as five even rows.
    val rowHeight = cell
    Row(
        modifier = GlanceModifier.width(cell * 6).height(rowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = GlanceModifier.width(edgeInset))
        // Pill gauge in the icon tone, echoing the grid's ring-vs-filled-dot language: the empty
        // portion is an outline with a genuinely transparent centre (so it never blots a translucent
        // widget), the charged portion a solid fill growing left→right. The outline is a fill-based
        // capsule annulus (ic_gauge_track), tinted like the dots; the solid fill then layers on top,
        // covering the outline wherever the battery is charged.
        Box(
            modifier = GlanceModifier.width(trackWidth).height(gaugeHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_gauge_track),
                contentDescription = null,
                modifier = GlanceModifier.width(trackWidth).height(gaugeHeight),
                contentScale = ContentScale.FillBounds,
                colorFilter = ColorFilter.tint(glyphColor),
            )
            Box(
                modifier =
                    GlanceModifier
                        .width(trackWidth * fraction)
                        .height(gaugeHeight)
                        .background(gaugeColor)
                        .cornerRadius(gaugeRadius),
                content = {},
            )
        }
        // Glyph slot: the 6th column (cell wide), icon centred under the 6th dot. Height tracks the
        // pinned row so the glyph never stretches the row past the dot rhythm.
        Box(
            modifier = GlanceModifier.width(cell).height(rowHeight),
            contentAlignment = Alignment.Center,
        ) {
            batteryGlyphDrawable(glyph)?.let { res ->
                Image(
                    provider = ImageProvider(res),
                    contentDescription = null,
                    modifier = GlanceModifier.size(glyphSize),
                    colorFilter = ColorFilter.tint(glyphColor),
                )
            }
        }
    }
}

@Composable
private fun CellImage(
    cell: Cell,
    rowKind: RowKind,
    state: WidgetRenderState,
    litColor: ColorProvider,
    dimColor: ColorProvider,
    glyphColor: ColorProvider,
    cellSize: Dp,
    dotSize: Dp,
) {
    val context = LocalContext.current
    val action = actionForZone(zoneOf(cell, rowKind), state.settings, context)
    val boxModifier =
        GlanceModifier.size(cellSize).let {
            if (action != null) it.clickable(onClick = action) else it
        }
    val imageModifier = GlanceModifier.size(dotSize)
    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        when (cell) {
            is Cell.Bit -> {
                Image(
                    provider = ImageProvider(if (cell.lit) R.drawable.ic_dot_filled else R.drawable.ic_dot_ring),
                    contentDescription = null,
                    modifier = imageModifier,
                    colorFilter = ColorFilter.tint(if (cell.lit) litColor else dimColor),
                )
            }
            is Cell.Glyph -> {
                // A null glyph (weather disabled/unavailable) draws nothing — the tap zone stays,
                // the cell is simply empty rather than showing a placeholder dash.
                resolveGlyph(cell.slot, state)?.let { glyph ->
                    Image(
                        provider = ImageProvider(glyph),
                        contentDescription = null,
                        modifier = imageModifier,
                        colorFilter = ColorFilter.tint(glyphColor),
                    )
                }
            }
        }
    }
}

private fun zoneOf(
    cell: Cell,
    rowKind: RowKind,
): TapZone =
    when (cell) {
        is Cell.Glyph -> {
            when (cell.slot) {
                GlyphSlot.ALARM -> TapZone.ALARM
                GlyphSlot.WEATHER_NOW, GlyphSlot.WEATHER_TODAY, GlyphSlot.WEATHER_TOMORROW -> TapZone.WEATHER
            }
        }
        is Cell.Bit -> {
            when (rowKind) {
                RowKind.HOURS, RowKind.MINUTES -> TapZone.TIME
                RowKind.DAY, RowKind.MONTH -> TapZone.DATE
            }
        }
    }

private fun actionForZone(
    zone: TapZone,
    settings: WidgetSettings,
    context: Context,
): Action? =
    when (settings.tapActions[zone] ?: TapAction.NONE) {
        TapAction.NONE -> {
            null
        }
        TapAction.OPEN_ALARMS -> {
            actionStartActivity(alarmsIntent(context.packageManager))
        }
        TapAction.OPEN_CALENDAR -> {
            // Launch the default calendar app via CATEGORY_APP_CALENDAR rather than an
            // ACTION_VIEW on content://.../calendar. A content URI in the widget's PendingIntent
            // makes the AppWidget host attempt a URI-permission grant against CalendarProvider2 on
            // every update; with no READ_CALENDAR permission that logs a Permission Denial burst
            // each refresh. CATEGORY_APP_CALENDAR carries no content URI, so the check never runs.
            actionStartActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_APP_CALENDAR)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        TapAction.OPEN_CLOCK -> {
            clockAction(context)
        }
        TapAction.OPEN_APP -> {
            val pkg = settings.tapAppPackages[zone]
            pkg
                ?.let { context.packageManager.getLaunchIntentForPackage(it) }
                ?.let { actionStartActivity(it) }
        }
    }

/**
 * Resolve ACTION_SHOW_ALARMS to an explicit component. An *implicit* ACTION_SHOW_ALARMS fired
 * through a widget PendingIntent is silently dropped by many launchers (no chooser, no toast);
 * pinning it to the resolved component — as clockAction does for the clock launcher entry — makes
 * the tap reliable. Falls back to the implicit intent when nothing resolves. Package visibility
 * for the resolve is covered by the SHOW_ALARMS <queries> entry in the manifest.
 */
internal fun alarmsIntent(pm: PackageManager): Intent {
    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    pm.resolveActivity(intent, 0)?.activityInfo?.let { intent.setClassName(it.packageName, it.name) }
    return intent
}

/**
 * "Open clock" has no stored package, so resolve the default clock app's launcher entry
 * (its main clock/time screen), falling back to the system alarms screen. Package visibility
 * for the resolve is covered by the launcher <queries> block in the manifest.
 */
private fun clockAction(context: Context): Action {
    val showAlarms = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val clockPackage =
        context.packageManager
            .resolveActivity(showAlarms, 0)
            ?.activityInfo
            ?.packageName
    val launch =
        clockPackage
            ?.let { context.packageManager.getLaunchIntentForPackage(it) }
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return actionStartActivity(launch ?: showAlarms)
}
