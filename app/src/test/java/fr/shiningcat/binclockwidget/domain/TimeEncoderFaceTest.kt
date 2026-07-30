/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.Cell
import fr.shiningcat.binclockwidget.domain.model.GlyphSlot
import fr.shiningcat.binclockwidget.domain.model.RowKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TimeEncoderFaceTest {
    private val dt = LocalDateTime.of(2026, 7, 28, 13, 47)

    @Test fun `hours row has alarm glyph at col32 then 13 in 5 bits`() {
        val row = TimeEncoder.encode(dt).rows[0]
        assertEquals(RowKind.HOURS, row.kind)
        assertEquals(Cell.Glyph(GlyphSlot.ALARM), row.cells[0])
        assertEquals(listOf(16, 8, 4, 2, 1), row.cells.drop(1).map { (it as Cell.Bit).placeValue })
        assertEquals(
            listOf(false, true, true, false, true),
            row.cells.drop(1).map { (it as Cell.Bit).lit },
        ) // 16,8,4,2,1 -> 8+4+1
    }

    @Test fun `minutes row is all six bits, 47`() {
        val row = TimeEncoder.encode(dt).rows[1]
        assertTrue(row.cells.all { it is Cell.Bit })
        assertEquals(listOf(32, 16, 8, 4, 2, 1), row.cells.map { (it as Cell.Bit).placeValue })
        assertEquals(listOf(true, false, true, true, true, true), row.cells.map { (it as Cell.Bit).lit })
    }

    @Test fun `day row has weather-now glyph at col32 then 28`() {
        val row = TimeEncoder.encode(dt).rows[2]
        assertEquals(Cell.Glyph(GlyphSlot.WEATHER_NOW), row.cells[0])
        assertEquals(listOf(16, 8, 4, 2, 1), row.cells.drop(1).map { (it as Cell.Bit).placeValue })
        assertEquals(listOf(true, true, true, false, false), row.cells.drop(1).map { (it as Cell.Bit).lit })
    }

    @Test fun `month row has today+tomorrow glyphs then 07`() {
        val row = TimeEncoder.encode(dt).rows[3]
        assertEquals(Cell.Glyph(GlyphSlot.WEATHER_TODAY), row.cells[0])
        assertEquals(Cell.Glyph(GlyphSlot.WEATHER_TOMORROW), row.cells[1])
        assertEquals(listOf(8, 4, 2, 1), row.cells.drop(2).map { (it as Cell.Bit).placeValue })
        assertEquals(listOf(false, true, true, true), row.cells.drop(2).map { (it as Cell.Bit).lit })
    }
}
