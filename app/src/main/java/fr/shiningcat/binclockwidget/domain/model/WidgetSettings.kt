package fr.shiningcat.binclockwidget.domain.model

enum class TapZone { ALARM, TIME, DATE, WEATHER }
enum class TapAction { NONE, OPEN_ALARMS, OPEN_CLOCK, OPEN_CALENDAR, OPEN_APP }

data class WidgetSettings(
    val useMaterialYou: Boolean = false,
    val colorArgb: Int = 0xFFFFFFFF.toInt(), // default white
    val hairline: Boolean = false,
    val tapActions: Map<TapZone, TapAction> = mapOf(
        TapZone.ALARM to TapAction.OPEN_ALARMS,
        TapZone.TIME to TapAction.OPEN_CLOCK,
        TapZone.DATE to TapAction.OPEN_CALENDAR,
        TapZone.WEATHER to TapAction.NONE,
    ),
    val tapAppPackages: Map<TapZone, String?> = emptyMap(),
)
