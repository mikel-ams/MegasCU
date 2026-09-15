package com.ams.megascu.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SegmentOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ConnectedSegmentedGroup(
    items: List<SegmentOption<T>>,
    selectedValue: T,
    onItemSelected: (T) -> Unit,
    useConnectedStyle: Boolean = true,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    height: Dp = 44.dp,
    showDotIndicator: Boolean = true
) {
    val haptic = LocalHapticFeedback.current

    if (!useConnectedStyle) {
        // Standard M3 SingleChoiceSegmentedButtonRow
        SingleChoiceSegmentedButtonRow(
            modifier = modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = item.value == selectedValue
                SegmentedButton(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onItemSelected(item.value)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f),
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        activeBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    ),
                    icon = {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            SegmentedButtonDefaults.Icon(active = isSelected)
                        }
                    }
                ) {
                    Text(
                        text = item.label,
                        fontSize = fontSize,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    } else {
        // Material 3 Expressive Connected Button Group con metamorfosis de forma dinámica
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val density = LocalDensity.current.density
            val total = items.size
            val largeRadius = 24.dp
            val smallRadius = 6.dp

            val interactionSources = remember(total) { List(total) { MutableInteractionSource() } }
            val pressStates = interactionSources.map { source -> source.collectIsPressedAsState().value }

            items.forEachIndexed { index, item ->
                val isSelected = item.value == selectedValue
                val interactionSource = interactionSources[index]
                val isPressed = pressStates[index]

                // Determine if adjacent neighbors (index - 1 or index + 1) are currently pressed
                val isLeftNeighborPressed = index > 0 && pressStates[index - 1]
                val isRightNeighborPressed = index < total - 1 && pressStates[index + 1]

                val targetPushOffset by animateFloatAsState(
                    targetValue = when {
                        isLeftNeighborPressed -> 6f // pushed right
                        isRightNeighborPressed -> -6f // pushed left
                        else -> 0f
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "adjacent_push_$index"
                )

                val targetScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.93f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "btn_scale_$index"
                )

                // Cálculo dinámico de esquinas con transición de forma expresiva
                val targetTopStart = if (isSelected || isPressed) largeRadius else if (index == 0) largeRadius else smallRadius
                val targetBottomStart = if (isSelected || isPressed) largeRadius else if (index == 0) largeRadius else smallRadius
                val targetTopEnd = if (isSelected || isPressed) largeRadius else if (index == total - 1) largeRadius else smallRadius
                val targetBottomEnd = if (isSelected || isPressed) largeRadius else if (index == total - 1) largeRadius else smallRadius

                val cornerSpringSpec = spring<Dp>(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )

                val topStart by animateDpAsState(targetTopStart, cornerSpringSpec, label = "shape_ts_$index")
                val topEnd by animateDpAsState(targetTopEnd, cornerSpringSpec, label = "shape_te_$index")
                val bottomStart by animateDpAsState(targetBottomStart, cornerSpringSpec, label = "shape_bs_$index")
                val bottomEnd by animateDpAsState(targetBottomEnd, cornerSpringSpec, label = "shape_be_$index")

                val targetWeight = if (isSelected) 1.38f else 1.0f
                val animatedWeight by animateFloatAsState(
                    targetValue = targetWeight,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "btn_weight_$index"
                )

                val dynamicShape = RoundedCornerShape(
                    topStart = topStart,
                    topEnd = topEnd,
                    bottomEnd = bottomEnd,
                    bottomStart = bottomStart
                )

                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val isAmoled = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.surface == Color.Black
                val targetBgColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    if (isAmoled) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    } else if (isDark) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    }
                }

                val targetContentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                val animatedBgColor by animateColorAsState(
                    targetValue = targetBgColor,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "btn_bg_$index"
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = targetContentColor,
                    animationSpec = tween(160),
                    label = "btn_content_$index"
                )

                val unselectedBorderModifier = if (!isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.50f else 0.35f),
                        shape = dynamicShape
                    )
                } else {
                    Modifier
                }

                Box(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .fillMaxHeight()
                        .graphicsLayer {
                            translationX = targetPushOffset * density
                            scaleX = targetScale
                            scaleY = targetScale
                        }
                        .clip(dynamicShape)
                        .background(animatedBgColor)
                        .then(unselectedBorderModifier)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item.value)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        // Expressive Animated Dot indicator (•)
                        if (showDotIndicator) {
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = (scaleIn(
                                    initialScale = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                ) + fadeIn(animationSpec = tween(150))),
                                exit = (scaleOut(
                                    targetScale = 0f,
                                    animationSpec = tween(120)
                                ) + fadeOut(animationSpec = tween(100)))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Black,
                                        fontSize = (fontSize.value + 4).sp,
                                        color = animatedContentColor,
                                        lineHeight = (fontSize.value + 4).sp
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                            }
                        }

                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = animatedContentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = fontSize,
                                color = animatedContentColor
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

