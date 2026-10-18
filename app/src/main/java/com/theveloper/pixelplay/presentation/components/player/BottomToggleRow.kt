package com.theveloper.pixelplay.presentation.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.presentation.components.LocalMaterialTheme
import com.theveloper.pixelplay.presentation.components.ToggleSegmentButton
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun BottomToggleRow(
    modifier: Modifier,
    isShuffleEnabled: Boolean,
    isShuffleTransitionInProgress: Boolean = false,
    repeatMode: Int,
    isFavoriteProvider: () -> Boolean,
    isSingleDiscMode: Boolean = false,
    showSingleDiscButton: Boolean = true,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSingleDiscToggle: () -> Unit = {},
    activeColorMain: Color = LocalMaterialTheme.current.primaryFixed,
    activeColorSecondary: Color = LocalMaterialTheme.current.secondaryFixed,
    activeColorTertiary: Color = LocalMaterialTheme.current.tertiaryFixed,
    activeColorQuaternary: Color = LocalMaterialTheme.current.primaryContainer,
    onActiveColorMain: Color = LocalMaterialTheme.current.onPrimaryFixed,
    onActiveColorSecondary: Color = LocalMaterialTheme.current.onSecondaryFixed,
    onActiveColorTertiary: Color = LocalMaterialTheme.current.onTertiaryFixed,
    onActiveColorQuaternary: Color = LocalMaterialTheme.current.onPrimaryContainer,
    inactiveColor: Color = LocalMaterialTheme.current.onSurface.copy(alpha = 0.07f),
    inactiveContentColor: Color = LocalMaterialTheme.current.onSurface,
    containerColor: Color = LocalMaterialTheme.current.surfaceContainerLowest.copy(alpha = 0.7f)
) {
    val isFavorite = isFavoriteProvider()
    val rowCorners = 60.dp

    Box(
        modifier = modifier.background(
            color = containerColor,
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusBL = rowCorners,
                smoothnessAsPercentTR = 60,
                cornerRadiusBR = rowCorners,
                smoothnessAsPercentBL = 60,
                cornerRadiusTL = rowCorners,
                smoothnessAsPercentBR = 60,
                cornerRadiusTR = rowCorners,
                smoothnessAsPercentTL = 60
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .clip(
                    AbsoluteSmoothCornerShape(
                        cornerRadiusBL = rowCorners,
                        smoothnessAsPercentTR = 60,
                        cornerRadiusBR = rowCorners,
                        smoothnessAsPercentBL = 60,
                        cornerRadiusTL = rowCorners,
                        smoothnessAsPercentBR = 60,
                        cornerRadiusTR = rowCorners,
                        smoothnessAsPercentTL = 60
                    )
                )
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val commonModifier = Modifier.weight(1f)

            ToggleSegmentButton(
                modifier = commonModifier,
                active = isShuffleEnabled,
                enabled = !isShuffleTransitionInProgress,
                activeColor = activeColorMain,
                activeCornerRadius = rowCorners,
                activeContentColor = onActiveColorMain,
                inactiveColor = inactiveColor,
                inactiveContentColor = inactiveContentColor,
                onClick = onShuffleToggle,
                iconId = R.drawable.rounded_shuffle_24,
                contentDesc = stringResource(R.string.common_shuffle)
            )
            val repeatActive = repeatMode != Player.REPEAT_MODE_OFF
            val repeatIcon = when (repeatMode) {
                Player.REPEAT_MODE_ONE -> R.drawable.rounded_repeat_one_24
                Player.REPEAT_MODE_ALL -> R.drawable.rounded_repeat_24
                else -> R.drawable.rounded_repeat_24
            }
            ToggleSegmentButton(
                modifier = commonModifier,
                active = repeatActive,
                activeColor = activeColorSecondary,
                activeCornerRadius = rowCorners,
                activeContentColor = onActiveColorSecondary,
                inactiveColor = inactiveColor,
                inactiveContentColor = inactiveContentColor,
                onClick = onRepeatToggle,
                iconId = repeatIcon,
                contentDesc = stringResource(R.string.common_repeat)
            )
            ToggleSegmentButton(
                modifier = commonModifier,
                active = isFavorite,
                activeColor = activeColorTertiary,
                activeCornerRadius = rowCorners,
                activeContentColor = onActiveColorTertiary,
                inactiveColor = inactiveColor,
                inactiveContentColor = inactiveContentColor,
                onClick = onFavoriteToggle,
                iconId = if (isFavorite) R.drawable.round_favorite_24 else R.drawable.rounded_favorite_24,
                contentDesc = stringResource(R.string.common_favorite)
            )
            if (showSingleDiscButton) {
                ToggleSegmentButton(
                    modifier = commonModifier,
                    active = isSingleDiscMode,
                    activeColor = activeColorQuaternary,
                    activeCornerRadius = rowCorners,
                    activeContentColor = onActiveColorQuaternary,
                    inactiveColor = inactiveColor,
                    inactiveContentColor = inactiveContentColor,
                    onClick = onSingleDiscToggle,
                    iconId = R.drawable.rounded_disc_one_24,
                    contentDesc = stringResource(R.string.player_single_disc_mode)
                )
            }
        }
    }
}

