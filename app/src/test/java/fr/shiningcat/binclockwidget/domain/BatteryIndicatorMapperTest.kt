/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.BatteryLevel
import fr.shiningcat.binclockwidget.domain.model.BatteryStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BatteryIndicatorMapperTest {
    @Test fun `level boundaries`() {
        assertEquals(BatteryLevel.NORMAL, BatteryIndicatorMapper.level(100))
        assertEquals(BatteryLevel.NORMAL, BatteryIndicatorMapper.level(21))
        assertEquals(BatteryLevel.LOW, BatteryIndicatorMapper.level(20))
        assertEquals(BatteryLevel.LOW, BatteryIndicatorMapper.level(11))
        assertEquals(BatteryLevel.VERY_LOW, BatteryIndicatorMapper.level(10))
        assertEquals(BatteryLevel.VERY_LOW, BatteryIndicatorMapper.level(0))
    }

    @Test fun `discharging glyph follows level`() {
        assertEquals(BatteryGlyph.NONE, BatteryIndicatorMapper.glyph(BatteryStatus(50, isCharging = false)))
        assertEquals(BatteryGlyph.LOW, BatteryIndicatorMapper.glyph(BatteryStatus(20, isCharging = false)))
        assertEquals(BatteryGlyph.VERY_LOW, BatteryIndicatorMapper.glyph(BatteryStatus(10, isCharging = false)))
    }

    @Test fun `charging overrides low and very low`() {
        assertEquals(BatteryGlyph.CHARGING, BatteryIndicatorMapper.glyph(BatteryStatus(5, isCharging = true)))
        assertEquals(BatteryGlyph.CHARGING, BatteryIndicatorMapper.glyph(BatteryStatus(90, isCharging = true)))
    }
}
