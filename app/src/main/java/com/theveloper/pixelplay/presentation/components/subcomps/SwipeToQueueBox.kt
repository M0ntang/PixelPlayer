package com.theveloper.pixelplay.presentation.components.subcomps

import android.view.View
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.presentation.utils.AppHapticsConfig
import com.theveloper.pixelplay.presentation.utils.LocalAppHapticsConfig
import com.theveloper.pixelplay.presentation.utils.performAppCompatHapticFeedback
import com.theveloper.pixelplay.presentation.viewmodel.ThemeStateHolder
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

private enum class SwipeToQueueDragPhase { IDLE, TENSION, SNAPPING, FREE_DRAG }

/**
 * Manages bidirectional swipe-to-queue gesture mirroring the physics, state machine,
 * and haptics of QueueItemDismissGestureHandler:
 *
 * 1. TENSION: drag up to 60dp with dampened visual feedback (max 20dp offset).
 * 2. SNAPPING: haptic fires (GESTURE_THRESHOLD_ACTIVATE) and item snaps with spring to drag position.
 * 3. FREE_DRAG: 1:1 tracking; crossing 40% item width threshold fires activate/deactivate haptics.
 * 4. RELEASE: if released past threshold, fires GESTURE_END, triggers onSwipeToQueue,
 *    and smoothly springs back to 0.
 *
 * Supports bidirectional swipes (left-to-right and right-to-left).
 */
internal class SwipeToQueueGestureHandler(
    private val scope: CoroutineScope,
    private val density: Density,
    private val hapticView: View,
    private val appHapticsConfig: AppHapticsConfig,
    private val offsetAnimatable: Animatable<Float, AnimationVector1D>,
    private val getItemWidthPx: () -> Float,
    private val onSwipeToQueue: () -> Unit
) {
    private var dragPhase: SwipeToQueueDragPhase = SwipeToQueueDragPhase.IDLE
    private var accumulatedDragX: Float = 0f

    var isInActionZone: Boolean by mutableStateOf(false)
        private set

    var isActionRunning: Boolean by mutableStateOf(false)
        private set

    fun onDragStart() {
        if (isActionRunning) return
        dragPhase = SwipeToQueueDragPhase.TENSION
        accumulatedDragX = 0f
        isInActionZone = false
        scope.launch { offsetAnimatable.stop() }
    }

    fun onHorizontalDrag(dragAmount: Float) {
        if (isActionRunning) return
        accumulatedDragX += dragAmount

        when (dragPhase) {
            SwipeToQueueDragPhase.TENSION -> {
                val tensionThresholdPx = 60f * density.density
                if (abs(accumulatedDragX) < tensionThresholdPx) {
                    val maxTensionOffsetPx = 20f * density.density
                    val dragFraction = (abs(accumulatedDragX) / tensionThresholdPx).coerceIn(0f, 1f)
                    val tensionOffset = maxTensionOffsetPx * dragFraction
                    val sign = if (accumulatedDragX >= 0f) 1f else -1f
                    scope.launch {
                        offsetAnimatable.snapTo(sign * tensionOffset)
                    }
                } else {
                    performAppCompatHapticFeedback(
                        hapticView,
                        appHapticsConfig,
                        HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE
                    )
                    scope.launch {
                        offsetAnimatable.animateTo(
                            targetValue = accumulatedDragX,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    dragPhase = SwipeToQueueDragPhase.FREE_DRAG
                }
            }

            SwipeToQueueDragPhase.SNAPPING -> {
                performAppCompatHapticFeedback(
                    hapticView,
                    appHapticsConfig,
                    HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE
                )
                scope.launch {
                    offsetAnimatable.animateTo(
                        targetValue = accumulatedDragX,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                dragPhase = SwipeToQueueDragPhase.FREE_DRAG
            }

            SwipeToQueueDragPhase.FREE_DRAG -> {
                val itemWidth = getItemWidthPx()
                val actionThreshold = if (itemWidth > 0f) itemWidth * 0.40f else 72f * density.density
                val nowInZone = abs(accumulatedDragX) > actionThreshold
                if (nowInZone != isInActionZone) {
                    isInActionZone = nowInZone
                    performAppCompatHapticFeedback(
                        hapticView,
                        appHapticsConfig,
                        if (nowInZone) HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_ACTIVATE
                        else HapticFeedbackConstantsCompat.GESTURE_THRESHOLD_DEACTIVATE
                    )
                }
                scope.launch {
                    offsetAnimatable.animateTo(
                        targetValue = accumulatedDragX,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }
            }

            SwipeToQueueDragPhase.IDLE -> Unit
        }
    }

    fun onDragEnd() {
        if (isActionRunning) return
        dragPhase = SwipeToQueueDragPhase.IDLE
        val itemWidth = getItemWidthPx()
        val actionThreshold = if (itemWidth > 0f) itemWidth * 0.40f else 72f * density.density

        if (abs(accumulatedDragX) > actionThreshold) {
            isActionRunning = true
            performAppCompatHapticFeedback(
                hapticView,
                appHapticsConfig,
                HapticFeedbackConstantsCompat.GESTURE_END
            )
            onSwipeToQueue()
            scope.launch {
                offsetAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                isActionRunning = false
                isInActionZone = false
            }
        } else {
            isInActionZone = false
            scope.launch {
                offsetAnimatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }

    fun onDragCancel() {
        if (isActionRunning) return
        dragPhase = SwipeToQueueDragPhase.IDLE
        isInActionZone = false
        scope.launch {
            offsetAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }
}

/**
 * Swipeable container that reveals an "Add to Queue" action with dynamic colors
 * matching the album / playlist cover theme, using the exact physics, haptics, and appearance
 * as the queue item dismissal gesture handler in QueueBottomSheet.
 * Supports bidirectional swipes (both left-to-right and right-to-left).
 */
@Composable
fun SwipeToQueueBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    swipeStateIdentity: Any? = null,
    coverArtUri: String? = null,
    themeStateHolder: ThemeStateHolder? = null,
    backgroundHorizontalPadding: Dp = 0.dp,
    capsuleShape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onSwipeToQueue: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val hapticView = LocalView.current
    val appHapticsConfig = LocalAppHapticsConfig.current
    val scope = rememberCoroutineScope()
    val isDark = LocalPixelPlayDarkTheme.current
    val currentOnSwipeToQueue by rememberUpdatedState(onSwipeToQueue)

    val offsetAnimatable = remember(swipeStateIdentity) { Animatable(0f) }
    var itemWidthPx by remember { mutableStateOf(0f) }
    var surfaceHeightPx by remember { mutableStateOf(0f) }

    // Resolve dynamic palette from cover artwork if available
    val dynamicSchemePair = remember(coverArtUri, themeStateHolder) {
        if (!coverArtUri.isNullOrBlank() && themeStateHolder != null) {
            themeStateHolder.getAlbumColorSchemeFlow(coverArtUri, eager = false)
        } else null
    }?.collectAsStateWithLifecycle()?.value

    val dynamicColorScheme = remember(dynamicSchemePair, isDark) {
        dynamicSchemePair?.let { if (isDark) it.dark else it.light }
    }

    val resolvedContainerColor = dynamicColorScheme?.primaryContainer ?: containerColor
    val resolvedContentColor = dynamicColorScheme?.onPrimaryContainer ?: contentColor

    val gestureHandler = remember(swipeStateIdentity, enabled) {
        if (enabled) {
            SwipeToQueueGestureHandler(
                scope = scope,
                density = density,
                hapticView = hapticView,
                appHapticsConfig = appHapticsConfig,
                offsetAnimatable = offsetAnimatable,
                getItemWidthPx = { itemWidthPx },
                onSwipeToQueue = { currentOnSwipeToQueue() }
            )
        } else null
    }

    val isSwipeTargeted = gestureHandler?.isInActionZone == true
    val currentOffsetPx = offsetAnimatable.value
    val isSwipingRight = currentOffsetPx > 0f
    val revealWidthPx = abs(currentOffsetPx)
    val revealProgress = if (density.density > 0f) {
        (revealWidthPx / (56.dp.value * density.density)).coerceIn(0f, 1f)
    } else 0f

    val actionBackgroundColor by animateColorAsState(
        targetValue = if (isSwipeTargeted) resolvedContainerColor else resolvedContainerColor.copy(alpha = 0.82f),
        animationSpec = tween(durationMillis = 150),
        label = "actionBackgroundColor"
    )
    val actionIconAlpha by animateFloatAsState(
        targetValue = revealProgress * if (isSwipeTargeted) 1f else 0.88f,
        animationSpec = tween(durationMillis = 120),
        label = "actionIconAlpha"
    )
    val actionIconScale by animateFloatAsState(
        targetValue = if (isSwipeTargeted) 1.08f else 0.95f,
        animationSpec = tween(durationMillis = 120),
        label = "actionIconScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                val measuredWidth = coordinates.size.width.toFloat()
                if (measuredWidth != itemWidthPx) itemWidthPx = measuredWidth
            }
    ) {
        // Background action indicator (capsule matching QueueBottomSheet dismissal)
        if (revealWidthPx > 0f && surfaceHeightPx > 0f) {
            val revealWidthDp = with(density) { revealWidthPx.toDp() }
            val surfaceHeightDp = with(density) { surfaceHeightPx.toDp() }
            val alignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            val capsulePadding = if (isSwipingRight) {
                Modifier.padding(start = backgroundHorizontalPadding)
            } else {
                Modifier.padding(end = backgroundHorizontalPadding)
            }
            val iconPadding = if (isSwipingRight) {
                Modifier.padding(start = 16.dp)
            } else {
                Modifier.padding(end = 16.dp)
            }

            Box(
                modifier = Modifier
                    .align(alignment)
                    .then(capsulePadding)
                    .height(surfaceHeightDp)
                    .width(revealWidthDp)
                    .clip(capsuleShape)
                    .background(actionBackgroundColor),
                contentAlignment = alignment
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                    contentDescription = stringResource(R.string.library_toast_added_to_queue),
                    modifier = iconPadding
                        .size(24.dp)
                        .graphicsLayer {
                            alpha = actionIconAlpha
                            scaleX = actionIconScale
                            scaleY = actionIconScale
                        },
                    tint = resolvedContentColor
                )
            }
        }

        // Foreground content with translation and horizontal drag gesture
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = currentOffsetPx
                }
                .onGloballyPositioned { coordinates ->
                    val h = coordinates.size.height.toFloat()
                    if (h != surfaceHeightPx) surfaceHeightPx = h
                }
                .then(
                    if (enabled && gestureHandler != null) {
                        Modifier.pointerInput(swipeStateIdentity, gestureHandler) {
                            detectHorizontalDragGestures(
                                onDragStart = { gestureHandler.onDragStart() },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    gestureHandler.onHorizontalDrag(dragAmount)
                                },
                                onDragEnd = { gestureHandler.onDragEnd() },
                                onDragCancel = { gestureHandler.onDragCancel() }
                            )
                        }
                    } else Modifier
                )
        ) {
            content()
        }
    }
}
