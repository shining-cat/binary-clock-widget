/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.widget

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class TickSchedulerTimeTest {
    private fun ms(
        y: Int,
        mo: Int,
        d: Int,
        h: Int,
        mi: Int,
        s: Int,
        milli: Int,
    ): Long =
        LocalDateTime
            .of(y, mo, d, h, mi, s, milli * 1_000_000)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

    @Test fun `mid-minute rounds up and zeroes seconds and millis`() {
        val now = ms(2026, 7, 28, 13, 47, 23, 500)
        val expected = ms(2026, 7, 28, 13, 48, 0, 0)
        assertEquals(expected, nextMinuteBoundaryMs(now))
    }

    @Test fun `exactly on a minute boundary advances to the next minute`() {
        val now = ms(2026, 7, 28, 13, 47, 0, 0)
        val expected = ms(2026, 7, 28, 13, 48, 0, 0)
        assertEquals(expected, nextMinuteBoundaryMs(now))
    }

    @Test fun `one milli before a boundary rounds up to that boundary`() {
        val now = ms(2026, 7, 28, 13, 47, 59, 999)
        val expected = ms(2026, 7, 28, 13, 48, 0, 0)
        assertEquals(expected, nextMinuteBoundaryMs(now))
    }
}
