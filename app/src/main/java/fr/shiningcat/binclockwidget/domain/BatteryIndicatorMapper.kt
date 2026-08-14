/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.BatteryLevel
import fr.shiningcat.binclockwidget.domain.model.BatteryStatus

/**
 * Pure thresholds + glyph selection for the battery indicator. Charging wins over any low state:
 * a charging phone is not "in trouble", so the bolt takes precedence over the warning escalation.
 *
 * Thresholds are configurable to allow diagnostic testing with real battery (set higher thresholds
 * like 80%/60% to see indicators without waiting for battery to drain).
 */
object BatteryIndicatorMapper {
    const val DEFAULT_LOW_THRESHOLD = 20
    const val DEFAULT_VERY_LOW_THRESHOLD = 10

    fun level(
        percent: Int,
        lowThreshold: Int = DEFAULT_LOW_THRESHOLD,
        veryLowThreshold: Int = DEFAULT_VERY_LOW_THRESHOLD,
    ): BatteryLevel =
        when {
            percent <= veryLowThreshold -> BatteryLevel.VERY_LOW
            percent <= lowThreshold -> BatteryLevel.LOW
            else -> BatteryLevel.NORMAL
        }

    fun glyph(
        status: BatteryStatus,
        lowThreshold: Int = DEFAULT_LOW_THRESHOLD,
        veryLowThreshold: Int = DEFAULT_VERY_LOW_THRESHOLD,
    ): BatteryGlyph =
        if (status.isCharging) {
            BatteryGlyph.CHARGING
        } else {
            when (level(status.percent, lowThreshold, veryLowThreshold)) {
                BatteryLevel.VERY_LOW -> BatteryGlyph.VERY_LOW
                BatteryLevel.LOW -> BatteryGlyph.LOW
                BatteryLevel.NORMAL -> BatteryGlyph.NONE
            }
        }
}
