/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class DayNightResolverTest {
    private val sunrise = "2026-08-04T05:42"
    private val sunset = "2026-08-04T21:30"

    private fun at(
        hour: Int,
        minute: Int,
    ) = LocalDateTime.of(2026, 8, 4, hour, minute)

    @Test fun `midday is day`() {
        assertTrue(DayNightResolver.isDay(at(13, 44), sunrise, sunset, fallback = false))
    }

    @Test fun `before sunrise is night`() {
        assertFalse(DayNightResolver.isDay(at(4, 0), sunrise, sunset, fallback = true))
    }

    @Test fun `after sunset is night`() {
        assertFalse(DayNightResolver.isDay(at(22, 15), sunrise, sunset, fallback = true))
    }

    @Test fun `exactly at sunrise is day (inclusive)`() {
        assertTrue(DayNightResolver.isDay(at(5, 42), sunrise, sunset, fallback = false))
    }

    @Test fun `exactly at sunset is night (exclusive)`() {
        assertFalse(DayNightResolver.isDay(at(21, 30), sunrise, sunset, fallback = true))
    }

    @Test fun `null sunrise falls back`() {
        assertTrue(DayNightResolver.isDay(at(13, 44), null, sunset, fallback = true))
        assertFalse(DayNightResolver.isDay(at(13, 44), null, sunset, fallback = false))
    }

    @Test fun `null sunset falls back`() {
        assertTrue(DayNightResolver.isDay(at(13, 44), sunrise, null, fallback = true))
        assertFalse(DayNightResolver.isDay(at(13, 44), sunrise, null, fallback = false))
    }

    @Test fun `unparseable timestamp falls back`() {
        assertTrue(DayNightResolver.isDay(at(13, 44), "not-a-date", sunset, fallback = true))
        assertFalse(DayNightResolver.isDay(at(13, 44), sunrise, "garbage", fallback = false))
    }

    // The bug this fix addresses: a stale fetch-time is_day=false (moon) at 13:44 must render as day.
    @Test fun `stale night fallback is corrected to day within the window`() {
        assertTrue(DayNightResolver.isDay(at(13, 44), sunrise, sunset, fallback = false))
    }
}
