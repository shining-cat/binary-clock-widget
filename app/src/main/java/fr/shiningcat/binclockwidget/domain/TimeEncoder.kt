package fr.shiningcat.binclockwidget.domain

/** 6 columns, place values 32·16·8·4·2·1 (index 0 = 32). Columns above [bits] stay false. */
fun encodeValue(value: Int, bits: Int): List<Boolean> =
    (5 downTo 0).map { col -> col < bits && (value shr col) and 1 == 1 }
