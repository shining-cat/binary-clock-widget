/*
 * SPDX-FileCopyrightText: 2026 shining-cat
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package fr.shiningcat.binclockwidget.domain.model

enum class RowKind { HOURS, MINUTES, DAY, MONTH }

enum class GlyphSlot { ALARM, WEATHER_NOW, WEATHER_TODAY, WEATHER_TOMORROW }

sealed interface Cell {
    data class Bit(
        val placeValue: Int,
        val lit: Boolean,
    ) : Cell // placeValue 32..1

    data class Glyph(
        val slot: GlyphSlot,
    ) : Cell
}

data class FaceRow(
    val kind: RowKind,
    val cells: List<Cell>,
) // always size 6

data class Face(
    val rows: List<FaceRow>,
) // always size 4
