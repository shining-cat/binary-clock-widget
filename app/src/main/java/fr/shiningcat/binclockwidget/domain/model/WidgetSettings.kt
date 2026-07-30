package fr.shiningcat.binclockwidget.domain.model

enum class TapZone { ALARM, TIME, DATE, WEATHER }
enum class TapAction { NONE, OPEN_ALARMS, OPEN_CLOCK, OPEN_CALENDAR, OPEN_APP }

data class WidgetSettings(
    val useMaterialYou: Boolean = false,
    val colorArgb: Int = 0xFFFFFFFF.toInt(), // dots colour, default white
    // Icon (glyph) colour. Null = inherit the dots colour; a value overrides it. Under Material You
    // icons are driven by the wallpaper's secondary tone regardless of this field.
    val iconColorArgb: Int? = null,
    // Widget background. Default opaque AMOLED black; alpha channel allows a translucent background.
    // Material You never overrides this, so pure black is preserved when Material You is on.
    val backgroundColorArgb: Int = 0xFF000000.toInt(),
    val hairline: Boolean = false,
    val tapActions: Map<TapZone, TapAction> = mapOf(
        TapZone.ALARM to TapAction.OPEN_ALARMS,
        TapZone.TIME to TapAction.OPEN_CLOCK,
        TapZone.DATE to TapAction.OPEN_CALENDAR,
        TapZone.WEATHER to TapAction.NONE,
    ),
    val tapAppPackages: Map<TapZone, String?> = emptyMap(),
)
