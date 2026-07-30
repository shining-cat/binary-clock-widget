/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimeEncoderTest {
    // encodeValue(value, bits) -> 6 booleans, MSB(32) first, unused high cols = false
    @Test fun `13 in 5 bits lights 8+4+1`() {
        assertEquals(listOf(false, false, true, true, false, true), encodeValue(13, 5))
    }

    @Test fun `47 in 6 bits lights 32+8+4+2+1`() {
        assertEquals(listOf(true, false, true, true, true, true), encodeValue(47, 6))
    }

    @Test fun `0 is all off`() {
        assertEquals(List(6) { false }, encodeValue(0, 6))
    }

    @Test fun `59 max minute`() {
        assertEquals(listOf(true, true, true, false, true, true), encodeValue(59, 6))
    }

    @Test fun `31 max day in 5 bits`() {
        assertEquals(listOf(false, true, true, true, true, true), encodeValue(31, 5))
    }

    @Test fun `12 max month in 4 bits`() {
        assertEquals(listOf(false, false, true, true, false, false), encodeValue(12, 4))
    }
}
