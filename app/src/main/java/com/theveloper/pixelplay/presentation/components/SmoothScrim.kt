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
 * Color stops representing a wide, gentle, smootherstep easing from 0% alpha to 100% solid [surfaceColor].
 * Uses C^2 continuous quintic smootherstep S(t) = 6t^5 - 15t^4 + 10t^3 with zero 1st and 2nd derivatives
 * at both boundaries, eliminating Mach bands, gradient cutoff lines, and color banding.
 * Reaches 100% solid [surfaceColor] at 88%, and stays 100% solid from 88% to 100%.
 */
fun getHeaderScrimStops(surfaceColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.000f to surfaceColor.copy(alpha = 0.0000f),
        0.050f to surfaceColor.copy(alpha = 0.0000f),
        0.083f to surfaceColor.copy(alpha = 0.0006f),
        0.116f to surfaceColor.copy(alpha = 0.0045f),
        0.150f to surfaceColor.copy(alpha = 0.0143f),
        0.183f to surfaceColor.copy(alpha = 0.0318f),
        0.216f to surfaceColor.copy(alpha = 0.0579f),
        0.249f to surfaceColor.copy(alpha = 0.0933f),
        0.282f to surfaceColor.copy(alpha = 0.1376f),
        0.316f to surfaceColor.copy(alpha = 0.1905f),
        0.349f to surfaceColor.copy(alpha = 0.2509f),
        0.382f to surfaceColor.copy(alpha = 0.3174f),
        0.415f to surfaceColor.copy(alpha = 0.3886f),
        0.448f to surfaceColor.copy(alpha = 0.4625f),
        0.482f to surfaceColor.copy(alpha = 0.5375f),
        0.515f to surfaceColor.copy(alpha = 0.6114f),
        0.548f to surfaceColor.copy(alpha = 0.6826f),
        0.581f to surfaceColor.copy(alpha = 0.7491f),
        0.614f to surfaceColor.copy(alpha = 0.8095f),
        0.648f to surfaceColor.copy(alpha = 0.8624f),
        0.681f to surfaceColor.copy(alpha = 0.9067f),
        0.714f to surfaceColor.copy(alpha = 0.9421f),
        0.747f to surfaceColor.copy(alpha = 0.9682f),
        0.780f to surfaceColor.copy(alpha = 0.9857f),
        0.814f to surfaceColor.copy(alpha = 0.9955f),
        0.847f to surfaceColor.copy(alpha = 0.9994f),
        0.880f to surfaceColor,
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
 * Upper 15% is left completely transparent for artwork clarity.
 * Fades smoothly into [containerColor] from 15% to 92% using quintic smootherstep,
 * then stays solid 100% from 92% to 100% to seamlessly merge with the card body.
 */
fun getCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.000f to containerColor.copy(alpha = 0.0000f),
        0.150f to containerColor.copy(alpha = 0.0000f),
        0.181f to containerColor.copy(alpha = 0.0006f),
        0.212f to containerColor.copy(alpha = 0.0045f),
        0.242f to containerColor.copy(alpha = 0.0143f),
        0.273f to containerColor.copy(alpha = 0.0318f),
        0.304f to containerColor.copy(alpha = 0.0579f),
        0.335f to containerColor.copy(alpha = 0.0933f),
        0.366f to containerColor.copy(alpha = 0.1376f),
        0.396f to containerColor.copy(alpha = 0.1905f),
        0.427f to containerColor.copy(alpha = 0.2509f),
        0.458f to containerColor.copy(alpha = 0.3174f),
        0.489f to containerColor.copy(alpha = 0.3886f),
        0.520f to containerColor.copy(alpha = 0.4625f),
        0.550f to containerColor.copy(alpha = 0.5375f),
        0.581f to containerColor.copy(alpha = 0.6114f),
        0.612f to containerColor.copy(alpha = 0.6826f),
        0.643f to containerColor.copy(alpha = 0.7491f),
        0.674f to containerColor.copy(alpha = 0.8095f),
        0.704f to containerColor.copy(alpha = 0.8624f),
        0.735f to containerColor.copy(alpha = 0.9067f),
        0.766f to containerColor.copy(alpha = 0.9421f),
        0.797f to containerColor.copy(alpha = 0.9682f),
        0.828f to containerColor.copy(alpha = 0.9857f),
        0.858f to containerColor.copy(alpha = 0.9955f),
        0.889f to containerColor.copy(alpha = 0.9994f),
        0.920f to containerColor,
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
 * Fades smoothly into [containerColor] from 20% to 95% using quintic smootherstep,
 * then stays solid 100% to blend into the adjacent text column.
 */
fun getHorizontalCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.000f to containerColor.copy(alpha = 0.0000f),
        0.200f to containerColor.copy(alpha = 0.0000f),
        0.230f to containerColor.copy(alpha = 0.0006f),
        0.260f to containerColor.copy(alpha = 0.0045f),
        0.290f to containerColor.copy(alpha = 0.0143f),
        0.320f to containerColor.copy(alpha = 0.0318f),
        0.350f to containerColor.copy(alpha = 0.0579f),
        0.380f to containerColor.copy(alpha = 0.0933f),
        0.410f to containerColor.copy(alpha = 0.1376f),
        0.440f to containerColor.copy(alpha = 0.1905f),
        0.470f to containerColor.copy(alpha = 0.2509f),
        0.500f to containerColor.copy(alpha = 0.3174f),
        0.530f to containerColor.copy(alpha = 0.3886f),
        0.560f to containerColor.copy(alpha = 0.4625f),
        0.590f to containerColor.copy(alpha = 0.5375f),
        0.620f to containerColor.copy(alpha = 0.6114f),
        0.650f to containerColor.copy(alpha = 0.6826f),
        0.680f to containerColor.copy(alpha = 0.7491f),
        0.710f to containerColor.copy(alpha = 0.8095f),
        0.740f to containerColor.copy(alpha = 0.8624f),
        0.770f to containerColor.copy(alpha = 0.9067f),
        0.800f to containerColor.copy(alpha = 0.9421f),
        0.830f to containerColor.copy(alpha = 0.9682f),
        0.860f to containerColor.copy(alpha = 0.9857f),
        0.890f to containerColor.copy(alpha = 0.9955f),
        0.920f to containerColor.copy(alpha = 0.9994f),
        0.950f to containerColor,
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
