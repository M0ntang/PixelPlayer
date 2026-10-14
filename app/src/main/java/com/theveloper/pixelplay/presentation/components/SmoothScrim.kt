package com.theveloper.pixelplay.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

/**
 * Color stops representing a wide, gentle, smootherstep easing from 0% alpha to 100% solid [surfaceColor].
 * Reaches 100% solid [surfaceColor] at 86%, and stays 100% solid from 86% to 100%.
 *
 * This ensures:
 * 1. The title, subtitle, and play FAB sit on pure solid surfaceColor.
 * 2. The artwork is completely hidden 40+ dp before the bottom of the header.
 * 3. During scrolling/collapsing, the bottom of the header is always 100% solid surfaceColor,
 *    eliminating any visible seam line against the content below.
 */
fun getHeaderScrimStops(surfaceColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.00f to surfaceColor.copy(alpha = 0.000f),
        0.08f to surfaceColor.copy(alpha = 0.000f),
        0.13f to surfaceColor.copy(alpha = 0.007f),
        0.19f to surfaceColor.copy(alpha = 0.027f),
        0.24f to surfaceColor.copy(alpha = 0.065f),
        0.30f to surfaceColor.copy(alpha = 0.122f),
        0.35f to surfaceColor.copy(alpha = 0.198f),
        0.41f to surfaceColor.copy(alpha = 0.290f),
        0.47f to surfaceColor.copy(alpha = 0.400f),
        0.52f to surfaceColor.copy(alpha = 0.512f),
        0.58f to surfaceColor.copy(alpha = 0.626f),
        0.63f to surfaceColor.copy(alpha = 0.735f),
        0.69f to surfaceColor.copy(alpha = 0.832f),
        0.74f to surfaceColor.copy(alpha = 0.908f),
        0.79f to surfaceColor.copy(alpha = 0.960f),
        0.83f to surfaceColor.copy(alpha = 0.988f),
        0.86f to surfaceColor,
        1.00f to surfaceColor
    )
}

/**
 * Creates a Compose [Brush] with the smooth scrim stops.
 */
fun createSmoothHeaderScrim(surfaceColor: Color, contentAlpha: Float = 1f): Brush {
    // Note: To prevent seam lines during collapse, the solid bottom of the scrim
    // must always remain 100% surfaceColor (never multiplied by contentAlpha).
    return Brush.verticalGradient(
        colorStops = getHeaderScrimStops(surfaceColor)
    )
}

/**
 * Hardware-dithered scrim overlay modifier.
 * Uses native Skia GPU dithering (paint.isDither = true) to prevent 8-bit color banding
 * on dark covers in light theme.
 */
fun Modifier.ditheredHeaderScrim(surfaceColor: Color): Modifier = this.drawWithCache {
    val stops = getHeaderScrimStops(surfaceColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val paint = android.graphics.Paint().apply {
        isDither = true
        isAntiAlias = true
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            positions,
            android.graphics.Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
        }
    }
}

/**
 * Color stops for status bar scrim.
 */
fun getStatusBarScrimStops(statusBarColor: Color): Array<Pair<Float, Color>> {
    val baseAlpha = statusBarColor.alpha
    return arrayOf(
        0.00f to statusBarColor,
        0.30f to statusBarColor.copy(alpha = baseAlpha * 0.75f),
        0.55f to statusBarColor.copy(alpha = baseAlpha * 0.45f),
        0.75f to statusBarColor.copy(alpha = baseAlpha * 0.18f),
        0.90f to statusBarColor.copy(alpha = baseAlpha * 0.05f),
        1.00f to statusBarColor.copy(alpha = 0f)
    )
}

/**
 * Creates a smooth scrim brush for the status bar fading downwards from [statusBarColor]
 * to transparent [statusBarColor].
 * Never uses [Color.Transparent] (black 0 alpha) so RGB stays pure and does not produce gray cast.
 */
fun createSmoothStatusBarScrim(statusBarColor: Color): Brush {
    return Brush.verticalGradient(
        colorStops = getStatusBarScrimStops(statusBarColor)
    )
}

/**
 * Hardware-dithered status bar scrim overlay modifier.
 */
fun Modifier.ditheredStatusBarScrim(statusBarColor: Color): Modifier = this.drawWithCache {
    val stops = getStatusBarScrimStops(statusBarColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val paint = android.graphics.Paint().apply {
        isDither = true
        isAntiAlias = true
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            positions,
            android.graphics.Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
        }
    }
}
