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
 */
object BatteryIndicatorMapper {
    const val LOW_THRESHOLD = 20
    const val VERY_LOW_THRESHOLD = 10

    fun level(percent: Int): BatteryLevel =
        when {
            percent <= VERY_LOW_THRESHOLD -> BatteryLevel.VERY_LOW
            percent <= LOW_THRESHOLD -> BatteryLevel.LOW
            else -> BatteryLevel.NORMAL
        }

    fun glyph(status: BatteryStatus): BatteryGlyph =
        if (status.isCharging) {
            BatteryGlyph.CHARGING
        } else {
            when (level(status.percent)) {
                BatteryLevel.VERY_LOW -> BatteryGlyph.VERY_LOW
                BatteryLevel.LOW -> BatteryGlyph.LOW
                BatteryLevel.NORMAL -> BatteryGlyph.NONE
            }
        }
}
