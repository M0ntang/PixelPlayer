package com.theveloper.pixelplay.presentation.components.subcomps

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.presentation.utils.LocalAppHapticsConfig
import com.theveloper.pixelplay.presentation.utils.performAppCompatHapticFeedback
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

val SpotifyGreen = Color(0xFF1ED760)
val SpotifyIconDark = Color(0xFF0F1711)

/**
 * Spotify-style swipeable container that reveals a green "Add to Queue" action
 * with an icon and haptic feedback when swiped horizontally.
 * Supports both rightward and leftward swipes.
 */
@Composable
fun SwipeToQueueBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(22.dp),
    threshold: Dp = 72.dp,
    onSwipeToQueue: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val hapticView = LocalView.current
    val appHapticsConfig = LocalAppHapticsConfig.current
    val scope = rememberCoroutineScope()
    val offsetAnimatable = remember { Animatable(0f) }

    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
    ) {
        val currentOffset = offsetAnimatable.value

        // Background action indicator (Spotify-style green action box)
        if (abs(currentOffset) > 0.5f) {
            val isSwipingRight = currentOffset > 0f
            val alignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            val progress = (abs(currentOffset) / thresholdPx).coerceIn(0f, 1f)
            val isActivated = abs(currentOffset) >= thresholdPx
            val iconScale = if (isActivated) 1.15f else 0.7f + (0.3f * progress)
            val iconAlpha = (progress * 1.5f).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(SpotifyGreen)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    contentDescription = stringResource(R.string.library_toast_added_to_queue),
                    tint = SpotifyIconDark,
                    modifier = Modifier
                        .align(alignment)
                        .padding(horizontal = 24.dp)
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            alpha = iconAlpha
                        }
                )
            }
        }

        // Foreground content with horizontal drag gesture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = currentOffset
                }
                .then(
                    if (enabled) {
                        Modifier.pointerInput(thresholdPx) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    scope.launch { offsetAnimatable.stop() }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    val unconstrained = offsetAnimatable.value + dragAmount
                                    val sign = sign(unconstrained)
                                    val magnitude = abs(unconstrained)
                                    // Apply resistance / dampening past threshold
                                    val finalOffset = if (magnitude > thresholdPx) {
                                        val excess = magnitude - thresholdPx
                                        sign * (thresholdPx + excess * 0.35f)
                                    } else {
                                        unconstrained
                                    }

                                    val isNowPastThreshold = abs(finalOffset) >= thresholdPx
                                    if (isNowPastThreshold != hasTriggeredHaptic) {
                                        hasTriggeredHaptic = isNowPastThreshold
                                        performAppCompatHapticFeedback(
                                            hapticView,
                                            appHapticsConfig,
                                            if (isNowPastThreshold) {
                                                HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE
                                            } else {
                                                HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_DEACTIVATE
                                            }
                                        )
                                    }

                                    scope.launch {
                                        offsetAnimatable.snapTo(finalOffset)
                                    }
                                },
                                onDragEnd = {
                                    val triggered = hasTriggeredHaptic
                                    hasTriggeredHaptic = false
                                    scope.launch {
                                        if (triggered) {
                                            performAppCompatHapticFeedback(
                                                hapticView,
                                                appHapticsConfig,
                                                HapticFeedbackConstantsCompat.CONFIRM
                                            )
                                            onSwipeToQueue()
                                        }
                                        offsetAnimatable.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.75f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                },
                                onDragCancel = {
                                    hasTriggeredHaptic = false
                                    scope.launch {
                                        offsetAnimatable.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.75f,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
    }
}
