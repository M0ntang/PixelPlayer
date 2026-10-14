package com.theveloper.pixelplay.presentation.components

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates a smooth, non-linear scrim brush from [surfaceColor] fading in smoothly over artwork.
 * Uses 15 progressive stops with constant RGB values to eliminate color-banding, dirty gray
 * midpoints, and dithering artifacts (especially visible on dark album covers in light theme).
 */
fun createSmoothHeaderScrim(surfaceColor: Color, contentAlpha: Float = 1f): Brush {
    val clampedAlpha = contentAlpha.coerceIn(0f, 1f)
    return Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to surfaceColor.copy(alpha = 0f),
            0.28f to surfaceColor.copy(alpha = 0f),
            0.36f to surfaceColor.copy(alpha = 0.015f * clampedAlpha),
            0.44f to surfaceColor.copy(alpha = 0.040f * clampedAlpha),
            0.52f to surfaceColor.copy(alpha = 0.080f * clampedAlpha),
            0.60f to surfaceColor.copy(alpha = 0.140f * clampedAlpha),
            0.67f to surfaceColor.copy(alpha = 0.220f * clampedAlpha),
            0.74f to surfaceColor.copy(alpha = 0.320f * clampedAlpha),
            0.80f to surfaceColor.copy(alpha = 0.440f * clampedAlpha),
            0.85f to surfaceColor.copy(alpha = 0.570f * clampedAlpha),
            0.90f to surfaceColor.copy(alpha = 0.710f * clampedAlpha),
            0.94f to surfaceColor.copy(alpha = 0.840f * clampedAlpha),
            0.97f to surfaceColor.copy(alpha = 0.930f * clampedAlpha),
            0.99f to surfaceColor.copy(alpha = 0.980f * clampedAlpha),
            1.00f to surfaceColor.copy(alpha = 1.000f * clampedAlpha)
        )
    )
}

/**
 * Creates a smooth scrim brush for the status bar fading downwards from [statusBarColor]
 * to transparent [statusBarColor].
 * Never uses [Color.Transparent] (black 0 alpha) so RGB stays pure and does not produce gray cast.
 */
fun createSmoothStatusBarScrim(statusBarColor: Color): Brush {
    val baseAlpha = statusBarColor.alpha
    return Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to statusBarColor,
            0.30f to statusBarColor.copy(alpha = baseAlpha * 0.80f),
            0.55f to statusBarColor.copy(alpha = baseAlpha * 0.50f),
            0.75f to statusBarColor.copy(alpha = baseAlpha * 0.22f),
            0.90f to statusBarColor.copy(alpha = baseAlpha * 0.06f),
            1.00f to statusBarColor.copy(alpha = 0f)
        )
    )
}
