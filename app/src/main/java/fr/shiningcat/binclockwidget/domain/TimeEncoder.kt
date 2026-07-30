/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain

import fr.shiningcat.binclockwidget.domain.model.Cell
import fr.shiningcat.binclockwidget.domain.model.Face
import fr.shiningcat.binclockwidget.domain.model.FaceRow
import fr.shiningcat.binclockwidget.domain.model.GlyphSlot
import fr.shiningcat.binclockwidget.domain.model.RowKind
import java.time.LocalDateTime

/** 6 columns, place values 32·16·8·4·2·1 (index 0 = 32). Columns above [bits] stay false. */
fun encodeValue(
    value: Int,
    bits: Int,
): List<Boolean> = (5 downTo 0).map { col -> col < bits && (value shr col) and 1 == 1 }

object TimeEncoder {
    private val PLACE = listOf(32, 16, 8, 4, 2, 1)

    fun encode(dt: LocalDateTime): Face {
        fun bits(
            value: Int,
            count: Int,
        ) = encodeValue(value, count).mapIndexed { i, lit -> Cell.Bit(PLACE[i], lit) }

        val hours =
            FaceRow(
                RowKind.HOURS,
                listOf(Cell.Glyph(GlyphSlot.ALARM)) + bits(dt.hour, 5).drop(1),
            )
        val minutes = FaceRow(RowKind.MINUTES, bits(dt.minute, 6))
        val day =
            FaceRow(
                RowKind.DAY,
                listOf(Cell.Glyph(GlyphSlot.WEATHER_NOW)) + bits(dt.dayOfMonth, 5).drop(1),
            )
        val month =
            FaceRow(
                RowKind.MONTH,
                listOf(Cell.Glyph(GlyphSlot.WEATHER_TODAY), Cell.Glyph(GlyphSlot.WEATHER_TOMORROW)) +
                    bits(dt.monthValue, 4).drop(2),
            )
        return Face(listOf(hours, minutes, day, month))
    }
}
