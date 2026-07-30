package fr.shiningcat.binclockwidget.config.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.roundToInt

// Compact sizing: the picker lives in a bottom sheet, so it stays small and hex-free. The live
// preview is rendered by the caller (in the sheet header), keeping it clear of the slider stack.
private val PANEL_HEIGHT = 120.dp
private val SLIDER_HEIGHT = 20.dp

/**
 * Dependency-free HSV colour picker with an optional alpha channel.
 *
 * Reusable across dots / icon / background controls. Emits ARGB [Int] values (0xAARRGGBB) via
 * [onColorChanged] whenever the user edits any component.
 *
 * State (hue/saturation/value/alpha) is seeded ONCE from the initial [color] and thereafter owned
 * internally, so the picker's own emits — which flow back down as a new [color] — never reset the
 * in-flight interaction (drag). The remember is deliberately NOT keyed on [color].
 *
 * @param color initial ARGB colour (0xAARRGGBB).
 * @param onColorChanged invoked with the new ARGB colour on every user interaction.
 * @param showAlpha when true, shows the alpha slider; otherwise the colour is always opaque.
 */
@Composable
fun ColorPicker(
    color: Int,
    onColorChanged: (Int) -> Unit,
    showAlpha: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Seed HSV + alpha once from the incoming colour. Not keyed on `color` on purpose.
    val seed = remember { FloatArray(3).also { AndroidColor.colorToHSV(color, it) } }
    var hue by remember { mutableFloatStateOf(seed[0]) }
    var sat by remember { mutableFloatStateOf(seed[1]) }
    var value by remember { mutableFloatStateOf(seed[2]) }
    var alpha by remember { mutableFloatStateOf(if (showAlpha) AndroidColor.alpha(color) / 255f else 1f) }

    fun emit() {
        val a = (alpha * 255f).roundToInt().coerceIn(0, 255)
        onColorChanged(AndroidColor.HSVToColor(a, floatArrayOf(hue, sat, value)))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SaturationValuePanel(
            hue = hue,
            saturation = sat,
            value = value,
            onChange = { s, v ->
                sat = s
                value = v
                emit()
            },
        )

        HueSlider(
            hue = hue,
            onHueChange = {
                hue = it
                emit()
            },
        )

        if (showAlpha) {
            AlphaSlider(
                hue = hue,
                saturation = sat,
                value = value,
                alpha = alpha,
                onAlphaChange = {
                    alpha = it
                    emit()
                },
            )
        }
    }
}

@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (saturation: Float, value: Float) -> Unit,
) {
    var panelSize by remember { mutableStateOf(IntSize.Zero) }

    fun handle(position: Offset) {
        val w = panelSize.width.toFloat()
        val h = panelSize.height.toFloat()
        if (w <= 0f || h <= 0f) return
        val s = (position.x / w).coerceIn(0f, 1f)
        val v = (1f - position.y / h).coerceIn(0f, 1f)
        onChange(s, v)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(PANEL_HEIGHT)
            .onSizeChanged { panelSize = it }
            .pointerInput(Unit) {
                detectTapGestures { handle(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { handle(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        handle(change.position)
                    },
                )
            },
    ) {
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color.White, Color.hsv(hue, 1f, 1f)),
            ),
        )
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black),
            ),
        )
        val cx = (saturation * size.width).coerceIn(0f, size.width)
        val cy = ((1f - value) * size.height).coerceIn(0f, size.height)
        drawThumb(Offset(cx, cy))
    }
}

@Composable
private fun HueSlider(hue: Float, onHueChange: (Float) -> Unit) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    val hueColors = remember {
        (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) }
    }

    fun handle(position: Offset) {
        val w = sliderSize.width.toFloat()
        if (w <= 0f) return
        onHueChange((position.x / w).coerceIn(0f, 1f) * 360f)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(SLIDER_HEIGHT)
            .onSizeChanged { sliderSize = it }
            .pointerInput(Unit) {
                detectTapGestures { handle(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { handle(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        handle(change.position)
                    },
                )
            },
    ) {
        drawRect(brush = Brush.horizontalGradient(hueColors))
        val cx = ((hue / 360f) * size.width).coerceIn(0f, size.width)
        drawThumb(Offset(cx, size.height / 2f))
    }
}

@Composable
private fun AlphaSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
) {
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    val opaque = Color.hsv(hue, saturation, value)

    fun handle(position: Offset) {
        val w = sliderSize.width.toFloat()
        if (w <= 0f) return
        onAlphaChange((position.x / w).coerceIn(0f, 1f))
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(SLIDER_HEIGHT)
            .onSizeChanged { sliderSize = it }
            .pointerInput(Unit) {
                detectTapGestures { handle(it) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { handle(it) },
                    onDrag = { change, _ ->
                        change.consume()
                        handle(change.position)
                    },
                )
            },
    ) {
        drawCheckerboard()
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(opaque.copy(alpha = 0f), opaque.copy(alpha = 1f)),
            ),
        )
        val cx = (alpha * size.width).coerceIn(0f, size.width)
        drawThumb(Offset(cx, size.height / 2f))
    }
}

private fun DrawScope.drawThumb(center: Offset) {
    drawCircle(
        color = Color.White,
        radius = 8f,
        center = center,
        style = Stroke(width = 3f),
    )
    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = center,
        style = Stroke(width = 1f),
    )
}

internal fun DrawScope.drawCheckerboard(
    cell: Float = 12f,
    light: Color = Color(0xFFFFFFFF),
    dark: Color = Color(0xFFCCCCCC),
) {
    val cols = ceil(size.width / cell).toInt()
    val rows = ceil(size.height / cell).toInt()
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val x = col * cell
            val y = row * cell
            drawRect(
                color = if ((row + col) % 2 == 0) light else dark,
                topLeft = Offset(x, y),
                size = Size(
                    width = minOf(cell, size.width - x),
                    height = minOf(cell, size.height - y),
                ),
            )
        }
    }
}

/** Format an ARGB [Int] as `#AARRGGBB` (or `#RRGGBB` when [showAlpha] is false). */
internal fun formatHex(argb: Int, showAlpha: Boolean): String {
    val a = AndroidColor.alpha(argb)
    val r = AndroidColor.red(argb)
    val g = AndroidColor.green(argb)
    val b = AndroidColor.blue(argb)
    return if (showAlpha) {
        "#%02X%02X%02X%02X".format(a, r, g, b)
    } else {
        "#%02X%02X%02X".format(r, g, b)
    }
}

/**
 * Parse a 6- or 8-digit hex string into an ARGB [Int], or return null for partial/invalid input.
 * A 6-digit value reuses [fallbackAlpha] for the alpha channel.
 */
internal fun parseHexToArgb(input: String, fallbackAlpha: Int): Int? {
    val clean = input.trim().removePrefix("#")
    if (clean.length != 6 && clean.length != 8) return null
    if (clean.any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
    val v = clean.toLongOrNull(16) ?: return null
    return when (clean.length) {
        6 -> (((fallbackAlpha.toLong() and 0xFF) shl 24) or v).toInt()
        8 -> v.toInt()
        else -> null
    }
}
