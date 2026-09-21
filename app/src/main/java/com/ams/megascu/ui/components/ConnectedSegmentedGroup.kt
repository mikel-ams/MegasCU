@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

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
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
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
        // Material 3 Expressive Connected Button Group con componentes nativos ToggleButton y formas oficiales
        val total = items.size
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            val isAmoled = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.surface == Color.Black
            val unselectedBg = if (isAmoled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            } else if (isDark) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            }

            items.forEachIndexed { index, item ->
                val isSelected = item.value == selectedValue
                val targetWeight = if (isSelected) 1.38f else 1.0f
                val animatedWeight by animateFloatAsState(
                    targetValue = targetWeight,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "btn_weight_$index"
                )

                val buttonShapes = when {
                    total == 1 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    index == total - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }

                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onItemSelected(item.value)
                    },
                    modifier = Modifier
                        .weight(animatedWeight)
                        .fillMaxHeight(),
                    shapes = buttonShapes,
                    colors = ToggleButtonDefaults.colors(
                        containerColor = unselectedBg,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        checkedContainerColor = MaterialTheme.colorScheme.primary,
                        checkedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    // Expressive Animated Dot indicator (•)
                    if (showDotIndicator) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = (scaleIn(
                                initialScale = 0f,
                                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
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
                                    color = MaterialTheme.colorScheme.onPrimary,
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = fontSize
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

