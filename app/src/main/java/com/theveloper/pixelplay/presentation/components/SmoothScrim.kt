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

/**
 * Color stops representing a gentle, smootherstep easing from 0% alpha to 100% solid [surfaceColor].
 * Starts lower down at 38% height so the majority of the artwork remains clear and vibrant,
 * while reaching sufficient opacity (~50-85%) where album title and subtitle are located,
 * and finishing 100% solid from 86% to 100% to seamlessly merge with the track list.
 */
fun getHeaderScrimStops(surfaceColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.000f to surfaceColor.copy(alpha = 0.0000f),
        0.380f to surfaceColor.copy(alpha = 0.0000f),
        0.404f to surfaceColor.copy(alpha = 0.0012f),
        0.428f to surfaceColor.copy(alpha = 0.0086f),
        0.452f to surfaceColor.copy(alpha = 0.0266f),
        0.476f to surfaceColor.copy(alpha = 0.0579f),
        0.500f to surfaceColor.copy(alpha = 0.1035f),
        0.524f to surfaceColor.copy(alpha = 0.1631f),
        0.548f to surfaceColor.copy(alpha = 0.2352f),
        0.572f to surfaceColor.copy(alpha = 0.3174f),
        0.596f to surfaceColor.copy(alpha = 0.4069f),
        0.620f to surfaceColor.copy(alpha = 0.5000f),
        0.644f to surfaceColor.copy(alpha = 0.5931f),
        0.668f to surfaceColor.copy(alpha = 0.6826f),
        0.692f to surfaceColor.copy(alpha = 0.7648f),
        0.716f to surfaceColor.copy(alpha = 0.8369f),
        0.740f to surfaceColor.copy(alpha = 0.8965f),
        0.764f to surfaceColor.copy(alpha = 0.9421f),
        0.788f to surfaceColor.copy(alpha = 0.9734f),
        0.812f to surfaceColor.copy(alpha = 0.9914f),
        0.836f to surfaceColor.copy(alpha = 0.9988f),
        0.860f to surfaceColor,
        1.000f to surfaceColor
    )
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
    val stops = getHeaderScrimStops(surfaceColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            positions,
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
    return arrayOf(
        0.000f to containerColor.copy(alpha = 0.0000f),
        0.500f to containerColor.copy(alpha = 0.0000f),
        0.520f to containerColor.copy(alpha = 0.0012f),
        0.540f to containerColor.copy(alpha = 0.0086f),
        0.560f to containerColor.copy(alpha = 0.0266f),
        0.580f to containerColor.copy(alpha = 0.0579f),
        0.600f to containerColor.copy(alpha = 0.1035f),
        0.620f to containerColor.copy(alpha = 0.1631f),
        0.640f to containerColor.copy(alpha = 0.2352f),
        0.660f to containerColor.copy(alpha = 0.3174f),
        0.680f to containerColor.copy(alpha = 0.4069f),
        0.700f to containerColor.copy(alpha = 0.5000f),
        0.720f to containerColor.copy(alpha = 0.5931f),
        0.740f to containerColor.copy(alpha = 0.6826f),
        0.760f to containerColor.copy(alpha = 0.7648f),
        0.780f to containerColor.copy(alpha = 0.8369f),
        0.800f to containerColor.copy(alpha = 0.8965f),
        0.820f to containerColor.copy(alpha = 0.9421f),
        0.840f to containerColor.copy(alpha = 0.9734f),
        0.860f to containerColor.copy(alpha = 0.9914f),
        0.880f to containerColor.copy(alpha = 0.9988f),
        0.900f to containerColor,
        1.000f to containerColor
    )
}

/**
 * Hardware-dithered card scrim overlay modifier with GPU anti-aliasing and smootherstep gradient.
 * Ideal for grid album/playlist cards.
 */
fun Modifier.ditheredCardScrim(containerColor: Color): Modifier = this.drawWithCache {
    val stops = getCardScrimStops(containerColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            positions,
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
    return arrayOf(
        0.000f to containerColor.copy(alpha = 0.0000f),
        0.500f to containerColor.copy(alpha = 0.0000f),
        0.521f to containerColor.copy(alpha = 0.0012f),
        0.542f to containerColor.copy(alpha = 0.0086f),
        0.563f to containerColor.copy(alpha = 0.0266f),
        0.584f to containerColor.copy(alpha = 0.0579f),
        0.605f to containerColor.copy(alpha = 0.1035f),
        0.626f to containerColor.copy(alpha = 0.1631f),
        0.647f to containerColor.copy(alpha = 0.2352f),
        0.668f to containerColor.copy(alpha = 0.3174f),
        0.689f to containerColor.copy(alpha = 0.4069f),
        0.710f to containerColor.copy(alpha = 0.5000f),
        0.731f to containerColor.copy(alpha = 0.5931f),
        0.752f to containerColor.copy(alpha = 0.6826f),
        0.773f to containerColor.copy(alpha = 0.7648f),
        0.794f to containerColor.copy(alpha = 0.8369f),
        0.815f to containerColor.copy(alpha = 0.8965f),
        0.836f to containerColor.copy(alpha = 0.9421f),
        0.857f to containerColor.copy(alpha = 0.9734f),
        0.878f to containerColor.copy(alpha = 0.9914f),
        0.899f to containerColor.copy(alpha = 0.9988f),
        0.920f to containerColor,
        1.000f to containerColor
    )
}

/**
 * Hardware-dithered horizontal card scrim overlay modifier.
 */
fun Modifier.ditheredHorizontalCardScrim(containerColor: Color): Modifier = this.drawWithCache {
    val stops = getHorizontalCardScrimStops(containerColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, size.width, 0f,
            colors,
            positions,
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
    return arrayOf(
        0.000f to statusBarColor,
        0.067f to statusBarColor.copy(alpha = baseAlpha * 0.9973f),
        0.133f to statusBarColor.copy(alpha = baseAlpha * 0.9808f),
        0.200f to statusBarColor.copy(alpha = baseAlpha * 0.9421f),
        0.267f to statusBarColor.copy(alpha = baseAlpha * 0.8781f),
        0.333f to statusBarColor.copy(alpha = baseAlpha * 0.7901f),
        0.400f to statusBarColor.copy(alpha = baseAlpha * 0.6826f),
        0.467f to statusBarColor.copy(alpha = baseAlpha * 0.5623f),
        0.533f to statusBarColor.copy(alpha = baseAlpha * 0.4377f),
        0.600f to statusBarColor.copy(alpha = baseAlpha * 0.3174f),
        0.667f to statusBarColor.copy(alpha = baseAlpha * 0.2099f),
        0.733f to statusBarColor.copy(alpha = baseAlpha * 0.1219f),
        0.800f to statusBarColor.copy(alpha = baseAlpha * 0.0579f),
        0.867f to statusBarColor.copy(alpha = baseAlpha * 0.0192f),
        0.933f to statusBarColor.copy(alpha = baseAlpha * 0.0027f),
        1.000f to statusBarColor.copy(alpha = 0f)
    )
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
    val stops = getStatusBarScrimStops(statusBarColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        shader = LinearGradient(
            0f, 0f, 0f, size.height,
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, gradientPaint)
        }
    }
}
