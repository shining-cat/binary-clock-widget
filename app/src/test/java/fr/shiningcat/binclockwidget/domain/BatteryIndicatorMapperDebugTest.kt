/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.BatteryGlyph
import fr.shiningcat.binclockwidget.domain.model.BatteryStatus
import org.junit.jupiter.api.Test

/**
 * Debug test to verify the exact scenario the user reported:
 * - Battery at ~15-20% (low but not very low)
 * - Not charging
 * - Should show LOW glyph
 */
class BatteryIndicatorMapperDebugTest {
    @Test
    fun `verify user scenario - 15 percent not charging should show LOW`() {
        val status = BatteryStatus(percent = 15, isCharging = false)
        val glyph = BatteryIndicatorMapper.glyph(status)
        println("Battery: 15%, not charging")
        println("Expected: LOW")
        println("Actual: $glyph")
        assert(glyph == BatteryGlyph.LOW) { "Expected LOW but got $glyph" }
    }

    @Test
    fun `verify edge cases around threshold`() {
        listOf(21, 20, 19, 11, 10, 9).forEach { percent ->
            val status = BatteryStatus(percent = percent, isCharging = false)
            val glyph = BatteryIndicatorMapper.glyph(status)
            println("$percent% → $glyph")
        }
    }
}
