package fr.shiningcat.binclockwidget.widget.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider
import fr.shiningcat.binclockwidget.R
import fr.shiningcat.binclockwidget.domain.WeatherGlyphMapper
import fr.shiningcat.binclockwidget.domain.model.Cell
import fr.shiningcat.binclockwidget.domain.model.DayNight
import fr.shiningcat.binclockwidget.domain.model.GlyphSlot
import fr.shiningcat.binclockwidget.domain.model.RowKind
import fr.shiningcat.binclockwidget.domain.model.TapAction
import fr.shiningcat.binclockwidget.domain.model.TapZone
import fr.shiningcat.binclockwidget.domain.model.WeatherCondition
import fr.shiningcat.binclockwidget.domain.model.WidgetSettings
import fr.shiningcat.binclockwidget.widget.model.WidgetRenderState

private val hairlineHeight = 2.dp

/**
 * Glance 1.1.x marks every runtime-Color ColorProvider factory @RestrictTo; the public
 * ColorProvider interface is the only unrestricted way to build one from a user-chosen ARGB.
 */
internal data class ArgbColorProvider(val color: Color) : ColorProvider {
    override fun getColor(context: Context): Color = color
}

@DrawableRes
internal fun weatherDrawable(condition: WeatherCondition, dayNight: DayNight): Int = when (condition) {
    WeatherCondition.CLEAR -> if (dayNight == DayNight.DAY) R.drawable.ic_light_mode else R.drawable.ic_clear_night
    WeatherCondition.PARTLY_CLOUDY -> if (dayNight == DayNight.DAY) R.drawable.ic_partly_cloudy_day else R.drawable.ic_partly_cloudy_night
    WeatherCondition.OVERCAST -> R.drawable.ic_cloud
    WeatherCondition.FOG -> R.drawable.ic_foggy
    WeatherCondition.DRIZZLE -> R.drawable.ic_rainy_light
    WeatherCondition.RAIN -> R.drawable.ic_rainy
    WeatherCondition.SNOW -> R.drawable.ic_weather_snowy
    WeatherCondition.RAIN_SHOWERS -> R.drawable.ic_rainy_heavy
    WeatherCondition.SNOW_SHOWERS -> R.drawable.ic_snowing
    WeatherCondition.THUNDERSTORM -> R.drawable.ic_thunderstorm
    WeatherCondition.UNKNOWN -> R.drawable.ic_remove
}

@DrawableRes
internal fun resolveGlyph(slot: GlyphSlot, state: WidgetRenderState): Int {
    val weather = state.weather
    return when (slot) {
        GlyphSlot.ALARM -> if (state.alarmSet) R.drawable.ic_alarm else R.drawable.ic_alarm_off
        GlyphSlot.WEATHER_NOW -> weather?.let {
            weatherDrawable(
                WeatherGlyphMapper.toCondition(it.nowCode),
                if (it.nowIsDay) DayNight.DAY else DayNight.NIGHT,
            )
        } ?: R.drawable.ic_remove
        GlyphSlot.WEATHER_TODAY -> weather?.let {
            weatherDrawable(WeatherGlyphMapper.toCondition(it.todayCode), DayNight.DAY)
        } ?: R.drawable.ic_remove
        GlyphSlot.WEATHER_TOMORROW -> weather?.let {
            weatherDrawable(WeatherGlyphMapper.toCondition(it.tomorrowCode), DayNight.DAY)
        } ?: R.drawable.ic_remove
    }
}

@Composable
fun DotGrid(state: WidgetRenderState) {
    GlanceTheme {
        // One base colour drives lit, dim, and separator so all three follow the same source —
        // Material You's primary when enabled, otherwise the user's ARGB. Deriving dim/separator
        // from a fixed colorArgb here was the bug where off-rings stayed white under Material You.
        val context = LocalContext.current
        val baseColor: Color =
            if (state.settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors.primary.getColor(context)
            } else {
                Color(state.settings.colorArgb)
            }
        val litColor: ColorProvider = ArgbColorProvider(baseColor)
        // Off-rings share the lit colour: the hollow-ring vs. filled-dot shape already signals
        // on/off, so a dimmer tint just read as "wrong colour" against the rest of the grid.
        val dimColor: ColorProvider = litColor
        // Separator is deliberately softer than the dots so the divider line doesn't compete.
        // A concrete Color (not a custom ColorProvider): Glance's background(ColorProvider) overload
        // doesn't paint a runtime-built provider, which is why the separator stayed invisible.
        val separatorColor: Color = baseColor.copy(alpha = 0.55f)

        val size = LocalSize.current
        val cell = minOf(size.width / 6, size.height / 4)
        val dot = cell * 0.55f

        Box(
            modifier = GlanceModifier.fillMaxSize().background(Color.Black),
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
                        row.cells.forEach { c -> CellImage(c, row.kind, state, litColor, dimColor, cell, dot) }
                    }
                    if (state.settings.hairline && index == 1) {
                        // A Box, not a Spacer: Glance Spacer backgrounds don't paint, which is why
                        // the separator was invisible however it was toggled.
                        // Width cell*5 (not cell*6): inset half a cell each side so the line aligns
                        // with the dot columns instead of running to the widget edges. The Column
                        // centres it horizontally.
                        Box(
                            modifier = GlanceModifier
                                .height(hairlineHeight)
                                .width(cell * 5)
                                .background(separatorColor),
                            content = {},
                        )
                    }
                }
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
    cellSize: Dp,
    dotSize: Dp,
) {
    val context = LocalContext.current
    val action = actionForZone(zoneOf(cell, rowKind), state.settings, context)
    val boxModifier = GlanceModifier.size(cellSize).let {
        if (action != null) it.clickable(onClick = action) else it
    }
    val imageModifier = GlanceModifier.size(dotSize)
    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        when (cell) {
            is Cell.Bit -> Image(
                provider = ImageProvider(if (cell.lit) R.drawable.ic_dot_filled else R.drawable.ic_dot_ring),
                contentDescription = null,
                modifier = imageModifier,
                colorFilter = ColorFilter.tint(if (cell.lit) litColor else dimColor),
            )
            is Cell.Glyph -> Image(
                provider = ImageProvider(resolveGlyph(cell.slot, state)),
                contentDescription = null,
                modifier = imageModifier,
                colorFilter = ColorFilter.tint(litColor),
            )
        }
    }
}

private fun zoneOf(cell: Cell, rowKind: RowKind): TapZone = when (cell) {
    is Cell.Glyph -> when (cell.slot) {
        GlyphSlot.ALARM -> TapZone.ALARM
        GlyphSlot.WEATHER_NOW, GlyphSlot.WEATHER_TODAY, GlyphSlot.WEATHER_TOMORROW -> TapZone.WEATHER
    }
    is Cell.Bit -> when (rowKind) {
        RowKind.HOURS, RowKind.MINUTES -> TapZone.TIME
        RowKind.DAY, RowKind.MONTH -> TapZone.DATE
    }
}

private fun actionForZone(zone: TapZone, settings: WidgetSettings, context: Context): Action? =
    when (settings.tapActions[zone] ?: TapAction.NONE) {
        TapAction.NONE -> null
        TapAction.OPEN_ALARMS -> actionStartActivity(
            Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        TapAction.OPEN_CALENDAR -> actionStartActivity(
            Intent(
                Intent.ACTION_VIEW,
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                    .appendPath(System.currentTimeMillis().toString()).build(),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        TapAction.OPEN_CLOCK -> clockAction(context)
        TapAction.OPEN_APP -> {
            val pkg = settings.tapAppPackages[zone]
            pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
                ?.let { actionStartActivity(it) }
        }
    }

/**
 * "Open clock" has no stored package, so resolve the default clock app's launcher entry
 * (its main clock/time screen), falling back to the system alarms screen. Package visibility
 * for the resolve is covered by the launcher <queries> block in the manifest.
 */
private fun clockAction(context: Context): Action {
    val showAlarms = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val clockPackage = context.packageManager
        .resolveActivity(showAlarms, 0)?.activityInfo?.packageName
    val launch = clockPackage
        ?.let { context.packageManager.getLaunchIntentForPackage(it) }
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return actionStartActivity(launch ?: showAlarms)
}
