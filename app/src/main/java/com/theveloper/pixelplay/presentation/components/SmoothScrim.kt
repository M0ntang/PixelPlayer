package com.theveloper.pixelplay.presentation.components

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

// Precomputed smootherstep stop positions and alpha fractions
private val HEADER_SCRIM_POSITIONS = floatArrayOf(
    0.000f, 0.380f, 0.404f, 0.428f, 0.452f, 0.476f, 0.500f, 0.524f, 0.548f, 0.572f,
    0.596f, 0.620f, 0.644f, 0.668f, 0.692f, 0.716f, 0.740f, 0.764f, 0.788f, 0.812f,
    0.836f, 0.860f, 1.000f
)
private val HEADER_SCRIM_ALPHAS = floatArrayOf(
    0.0000f, 0.0000f, 0.0012f, 0.0086f, 0.0266f, 0.0579f, 0.1035f, 0.1631f, 0.2352f, 0.3174f,
    0.4069f, 0.5000f, 0.5931f, 0.6826f, 0.7648f, 0.8369f, 0.8965f, 0.9421f, 0.9734f, 0.9914f,
    0.9988f, 1.0000f, 1.0000f
)

private val CARD_SCRIM_POSITIONS = floatArrayOf(
    0.000f, 0.500f, 0.520f, 0.540f, 0.560f, 0.580f, 0.600f, 0.620f, 0.640f, 0.660f,
    0.680f, 0.700f, 0.720f, 0.740f, 0.760f, 0.780f, 0.800f, 0.820f, 0.840f, 0.860f,
    0.880f, 0.900f, 1.000f
)
private val CARD_SCRIM_ALPHAS = floatArrayOf(
    0.0000f, 0.0000f, 0.0012f, 0.0086f, 0.0266f, 0.0579f, 0.1035f, 0.1631f, 0.2352f, 0.3174f,
    0.4069f, 0.5000f, 0.5931f, 0.6826f, 0.7648f, 0.8369f, 0.8965f, 0.9421f, 0.9734f, 0.9914f,
    0.9988f, 1.0000f, 1.0000f
)

private val HORIZONTAL_CARD_SCRIM_POSITIONS = floatArrayOf(
    0.000f, 0.500f, 0.521f, 0.542f, 0.563f, 0.584f, 0.605f, 0.626f, 0.647f, 0.668f,
    0.689f, 0.710f, 0.731f, 0.752f, 0.773f, 0.794f, 0.815f, 0.836f, 0.857f, 0.878f,
    0.899f, 0.920f, 1.000f
)
private val HORIZONTAL_CARD_SCRIM_ALPHAS = floatArrayOf(
    0.0000f, 0.0000f, 0.0012f, 0.0086f, 0.0266f, 0.0579f, 0.1035f, 0.1631f, 0.2352f, 0.3174f,
    0.4069f, 0.5000f, 0.5931f, 0.6826f, 0.7648f, 0.8369f, 0.8965f, 0.9421f, 0.9734f, 0.9914f,
    0.9988f, 1.0000f, 1.0000f
)

private val STATUS_BAR_SCRIM_POSITIONS = floatArrayOf(
    0.000f, 0.067f, 0.133f, 0.200f, 0.267f, 0.333f, 0.400f, 0.467f, 0.533f, 0.600f,
    0.667f, 0.733f, 0.800f, 0.867f, 0.933f, 1.000f
)
private val STATUS_BAR_SCRIM_ALPHAS = floatArrayOf(
    1.0000f, 0.9973f, 0.9808f, 0.9421f, 0.8781f, 0.7901f, 0.6826f, 0.5623f, 0.4377f, 0.3174f,
    0.2099f, 0.1219f, 0.0579f, 0.0192f, 0.0027f, 0.0000f
)

/**
 * Color stops representing a gentle, smootherstep easing from 0% alpha to 100% solid [surfaceColor].
 * Starts lower down at 38% height so the majority of the artwork remains clear and vibrant,
 * while reaching sufficient opacity (~50-85%) where album title and subtitle are located,
 * and finishing 100% solid from 86% to 100% to seamlessly merge with the track list.
 */
fun getHeaderScrimStops(surfaceColor: Color): Array<Pair<Float, Color>> {
    val baseAlpha = surfaceColor.alpha
    return Array(HEADER_SCRIM_POSITIONS.size) { i ->
        HEADER_SCRIM_POSITIONS[i] to surfaceColor.copy(alpha = baseAlpha * HEADER_SCRIM_ALPHAS[i])
    }
}

/**
 * Creates a Compose [Brush] with the smooth header scrim stops.
 */
fun createSmoothHeaderScrim(surfaceColor: Color, contentAlpha: Float = 1f): Brush {
    return Brush.verticalGradient(
        colorStops = getHeaderScrimStops(surfaceColor)
    )
}

/**
 * Hardware-dithered header scrim overlay modifier with GPU anti-aliasing and smootherstep gradient.
 * Completely eliminates color banding, lines, and quantization artifacts.
 */
fun Modifier.ditheredHeaderScrim(surfaceColor: Color): Modifier = this.drawWithCache {
    val baseAlpha = surfaceColor.alpha
    val colors = IntArray(HEADER_SCRIM_POSITIONS.size) { i ->
        surfaceColor.copy(alpha = baseAlpha * HEADER_SCRIM_ALPHAS[i]).toArgb()
    }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            HEADER_SCRIM_POSITIONS,
            Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, gradientPaint)
        }
    }
}

/**
 * Color stops for card scrim overlay (e.g. in grid items).
 * Upper 50% is left completely transparent so the artwork is fully visible.
 * Fades smoothly into [containerColor] from 50% to 90% using quintic smootherstep,
 * then stays solid 100% from 90% to 100% to seamlessly merge with the card body.
 */
fun getCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    val baseAlpha = containerColor.alpha
    return Array(CARD_SCRIM_POSITIONS.size) { i ->
        CARD_SCRIM_POSITIONS[i] to containerColor.copy(alpha = baseAlpha * CARD_SCRIM_ALPHAS[i])
    }
}

/**
 * Hardware-dithered card scrim overlay modifier with GPU anti-aliasing and smootherstep gradient.
 * Ideal for grid album/playlist cards.
 */
fun Modifier.ditheredCardScrim(containerColor: Color): Modifier = this.drawWithCache {
    val baseAlpha = containerColor.alpha
    val colors = IntArray(CARD_SCRIM_POSITIONS.size) { i ->
        containerColor.copy(alpha = baseAlpha * CARD_SCRIM_ALPHAS[i]).toArgb()
    }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            CARD_SCRIM_POSITIONS,
            Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, gradientPaint)
        }
    }
}

/**
 * Color stops for horizontal card scrim overlay (e.g. in list items).
 * Fades smoothly into [containerColor] from 50% to 92% using quintic smootherstep,
 * leaving the left half completely clean and vibrant, then stays solid 100% to blend into the adjacent text column.
 */
fun getHorizontalCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    val baseAlpha = containerColor.alpha
    return Array(HORIZONTAL_CARD_SCRIM_POSITIONS.size) { i ->
        HORIZONTAL_CARD_SCRIM_POSITIONS[i] to containerColor.copy(alpha = baseAlpha * HORIZONTAL_CARD_SCRIM_ALPHAS[i])
    }
}

/**
 * Hardware-dithered horizontal card scrim overlay modifier.
 */
fun Modifier.ditheredHorizontalCardScrim(containerColor: Color): Modifier = this.drawWithCache {
    val baseAlpha = containerColor.alpha
    val colors = IntArray(HORIZONTAL_CARD_SCRIM_POSITIONS.size) { i ->
        containerColor.copy(alpha = baseAlpha * HORIZONTAL_CARD_SCRIM_ALPHAS[i]).toArgb()
    }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, size.width, 0f,
            colors,
            HORIZONTAL_CARD_SCRIM_POSITIONS,
            Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, gradientPaint)
        }
    }
}

/**
 * Color stops for status bar scrim with smootherstep easing from [statusBarColor] down to transparent.
 */
fun getStatusBarScrimStops(statusBarColor: Color): Array<Pair<Float, Color>> {
    val baseAlpha = statusBarColor.alpha
    return Array(STATUS_BAR_SCRIM_POSITIONS.size) { i ->
        STATUS_BAR_SCRIM_POSITIONS[i] to statusBarColor.copy(alpha = baseAlpha * STATUS_BAR_SCRIM_ALPHAS[i])
    }
}

/**
 * Creates a smooth scrim brush for the status bar fading downwards from [statusBarColor]
 * to transparent [statusBarColor].
 */
fun createSmoothStatusBarScrim(statusBarColor: Color): Brush {
    return Brush.verticalGradient(
        colorStops = getStatusBarScrimStops(statusBarColor)
    )
}

/**
 * Hardware-dithered status bar scrim overlay modifier with smootherstep gradient.
 */
fun Modifier.ditheredStatusBarScrim(statusBarColor: Color): Modifier = this.drawWithCache {
    val baseAlpha = statusBarColor.alpha
    val colors = IntArray(STATUS_BAR_SCRIM_POSITIONS.size) { i ->
        statusBarColor.copy(alpha = baseAlpha * STATUS_BAR_SCRIM_ALPHAS[i]).toArgb()
    }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            STATUS_BAR_SCRIM_POSITIONS,
            Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, gradientPaint)
        }
    }
}
