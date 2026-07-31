/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain.model

/** A single battery reading: charge percent (0..100) and whether it is currently charging. */
data class BatteryStatus(
    val percent: Int,
    val isCharging: Boolean,
)

/** Discharge severity, derived from percent by [fr.shiningcat.binclockwidget.domain.BatteryIndicatorMapper]. */
enum class BatteryLevel { NORMAL, LOW, VERY_LOW }

/** The mutually-exclusive state glyph shown beside the gauge. NONE = empty slot (bar only). */
enum class BatteryGlyph { NONE, LOW, VERY_LOW, CHARGING }
