package com.theveloper.pixelplay.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color as AndroidColor
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
 * 8x8 Bayer dithering matrix (values 0..63).
 * When tiled across the canvas with an alternating micro-luminance offset (+/- 1-2 levels),
 * it converts discrete 8-bit quantization steps and Mach bands into continuous, seamless tone.
 */
private val BayerMatrix8x8 = intArrayOf(
     0, 32,  8, 40,  2, 34, 10, 42,
    48, 16, 56, 24, 50, 18, 58, 26,
    12, 44,  4, 36, 14, 46,  6, 38,
    60, 28, 52, 20, 62, 30, 54, 22,
     3, 35, 11, 43,  1, 33,  9, 41,
    51, 19, 59, 27, 49, 17, 57, 25,
    15, 47,  7, 39, 13, 45,  5, 37,
    63, 31, 55, 23, 61, 29, 53, 21
)

/**
 * Shared Bayer dither paint. Allocated once and reused everywhere.
 * Uses isFilterBitmap = false to preserve pixel-exact checkerboard dither pattern.
 */
private val sharedDitherPaint: Paint by lazy {
    val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
    for (y in 0 until 8) {
        for (x in 0 until 8) {
            val v = BayerMatrix8x8[y * 8 + x]
            // Normalized offset in [-1.0 .. +1.0]
            val offset = (v - 31.5f) / 31.5f
            val color = if (offset > 0f) {
                // Subtle white nudge: max alpha ~ 2.4% (approx +2 discrete 8-bit levels)
                val alpha = (offset * 7f).toInt().coerceIn(0, 255)
                AndroidColor.argb(alpha, 255, 255, 255)
            } else {
                // Subtle dark nudge: max alpha ~ 2.4% (approx -2 discrete 8-bit levels)
                val alpha = (-offset * 7f).toInt().coerceIn(0, 255)
                AndroidColor.argb(alpha, 0, 0, 0)
            }
            bitmap.setPixel(x, y, color)
        }
    }
    Paint().apply {
        isFilterBitmap = false
        shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }
}

/**
 * Color stops representing a wide, gentle, smootherstep easing from 0% alpha to 100% solid [surfaceColor].
 * Reaches 100% solid [surfaceColor] at 89%, and stays 100% solid from 89% to 100%.
 */
fun getHeaderScrimStops(surfaceColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.00f to surfaceColor.copy(alpha = 0.000f),
        0.06f to surfaceColor.copy(alpha = 0.000f),
        0.11f to surfaceColor.copy(alpha = 0.006f),
        0.17f to surfaceColor.copy(alpha = 0.018f),
        0.23f to surfaceColor.copy(alpha = 0.042f),
        0.29f to surfaceColor.copy(alpha = 0.080f),
        0.35f to surfaceColor.copy(alpha = 0.135f),
        0.41f to surfaceColor.copy(alpha = 0.205f),
        0.47f to surfaceColor.copy(alpha = 0.290f),
        0.53f to surfaceColor.copy(alpha = 0.390f),
        0.59f to surfaceColor.copy(alpha = 0.500f),
        0.65f to surfaceColor.copy(alpha = 0.610f),
        0.70f to surfaceColor.copy(alpha = 0.710f),
        0.75f to surfaceColor.copy(alpha = 0.800f),
        0.80f to surfaceColor.copy(alpha = 0.880f),
        0.84f to surfaceColor.copy(alpha = 0.940f),
        0.87f to surfaceColor.copy(alpha = 0.980f),
        0.89f to surfaceColor,
        1.00f to surfaceColor
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
 * Hardware-dithered header scrim overlay modifier with Bayer matrix dithering.
 * Completely eliminates 8-bit color banding, lines, and quantization blocks.
 */
fun Modifier.ditheredHeaderScrim(surfaceColor: Color): Modifier = this.drawWithCache {
    val stops = getHeaderScrimStops(surfaceColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
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
            // Dither the active transition zone (from 6% to 89% height)
            val top = size.height * 0.06f
            val bottom = size.height * 0.89f
            canvas.nativeCanvas.drawRect(0f, top, size.width, bottom, sharedDitherPaint)
        }
    }
}

/**
 * Color stops for card scrim overlay (e.g. in grid items).
 * Upper 26% is left completely transparent for artwork clarity.
 * Fades smoothly into [containerColor] from 26% to 96%, then stays solid 100%.
 */
fun getCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.00f to containerColor.copy(alpha = 0.000f),
        0.26f to containerColor.copy(alpha = 0.000f),
        0.36f to containerColor.copy(alpha = 0.018f),
        0.46f to containerColor.copy(alpha = 0.065f),
        0.56f to containerColor.copy(alpha = 0.150f),
        0.65f to containerColor.copy(alpha = 0.275f),
        0.73f to containerColor.copy(alpha = 0.435f),
        0.80f to containerColor.copy(alpha = 0.620f),
        0.86f to containerColor.copy(alpha = 0.795f),
        0.91f to containerColor.copy(alpha = 0.925f),
        0.94f to containerColor.copy(alpha = 0.985f),
        0.96f to containerColor,
        1.00f to containerColor
    )
}

/**
 * Hardware-dithered card scrim overlay modifier with Bayer matrix dithering.
 * Ideal for grid album/playlist cards.
 */
fun Modifier.ditheredCardScrim(containerColor: Color): Modifier = this.drawWithCache {
    val stops = getCardScrimStops(containerColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
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
            // Dither the active transition zone (from 26% to 96% height)
            val top = size.height * 0.26f
            val bottom = size.height * 0.96f
            canvas.nativeCanvas.drawRect(0f, top, size.width, bottom, sharedDitherPaint)
        }
    }
}

/**
 * Color stops for horizontal card scrim overlay (e.g. in list items).
 */
fun getHorizontalCardScrimStops(containerColor: Color): Array<Pair<Float, Color>> {
    return arrayOf(
        0.00f to containerColor.copy(alpha = 0.000f),
        0.25f to containerColor.copy(alpha = 0.000f),
        0.40f to containerColor.copy(alpha = 0.035f),
        0.55f to containerColor.copy(alpha = 0.135f),
        0.68f to containerColor.copy(alpha = 0.320f),
        0.79f to containerColor.copy(alpha = 0.570f),
        0.88f to containerColor.copy(alpha = 0.810f),
        0.94f to containerColor.copy(alpha = 0.960f),
        0.97f to containerColor,
        1.00f to containerColor
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
            // Dither the active transition zone (from 25% to 97% width)
            val left = size.width * 0.25f
            val right = size.width * 0.97f
            canvas.nativeCanvas.drawRect(left, 0f, right, size.height, sharedDitherPaint)
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
        0.25f to statusBarColor.copy(alpha = baseAlpha * 0.78f),
        0.50f to statusBarColor.copy(alpha = baseAlpha * 0.48f),
        0.70f to statusBarColor.copy(alpha = baseAlpha * 0.22f),
        0.88f to statusBarColor.copy(alpha = baseAlpha * 0.06f),
        1.00f to statusBarColor.copy(alpha = 0f)
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
 * Hardware-dithered status bar scrim overlay modifier with Bayer matrix dithering.
 */
fun Modifier.ditheredStatusBarScrim(statusBarColor: Color): Modifier = this.drawWithCache {
    val stops = getStatusBarScrimStops(statusBarColor)
    val colors = IntArray(stops.size) { stops[it].second.toArgb() }
    val positions = FloatArray(stops.size) { stops[it].first }
    val gradientPaint = Paint().apply {
        isAntiAlias = true
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
            canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, sharedDitherPaint)
        }
    }
}
