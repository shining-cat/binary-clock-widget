package fr.shiningcat.binclockwidget.widget.ui

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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
import fr.shiningcat.binclockwidget.domain.model.WeatherCondition
import fr.shiningcat.binclockwidget.widget.model.WidgetRenderState

private val dotSize = 22.dp
private val cellPadding = 1.dp
private val hairlineHeight = 1.dp
private val hairlineWidth = 138.dp

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
        val litColor: ColorProvider =
            if (state.settings.useMaterialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors.primary
            } else {
                ColorProvider(Color(state.settings.colorArgb))
            }
        val dimColor: ColorProvider =
            ColorProvider(Color(state.settings.colorArgb).copy(alpha = 0.14f))

        Column(
            modifier = GlanceModifier.fillMaxSize().background(Color.Black),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.face.rows.forEachIndexed { index, row ->
                Row(
                    modifier = GlanceModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    row.cells.forEach { cell -> CellImage(cell, state, litColor, dimColor) }
                }
                if (state.settings.hairline && index == 1) {
                    Spacer(
                        modifier = GlanceModifier
                            .height(hairlineHeight)
                            .width(hairlineWidth)
                            .background(dimColor),
                    )
                }
            }
        }
    }
}

@Composable
private fun CellImage(
    cell: Cell,
    state: WidgetRenderState,
    litColor: ColorProvider,
    dimColor: ColorProvider,
) {
    val modifier = GlanceModifier.padding(cellPadding).size(dotSize)
    when (cell) {
        is Cell.Bit -> Image(
            provider = ImageProvider(if (cell.lit) R.drawable.ic_dot_filled else R.drawable.ic_dot_ring),
            contentDescription = null,
            modifier = modifier,
            colorFilter = ColorFilter.tint(if (cell.lit) litColor else dimColor),
        )
        is Cell.Glyph -> Image(
            provider = ImageProvider(resolveGlyph(cell.slot, state)),
            contentDescription = null,
            modifier = modifier,
            colorFilter = ColorFilter.tint(litColor),
        )
    }
}
