package com.ams.megascu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    checkedIcon: ImageVector? = null,
    uncheckedIcon: ImageVector? = null
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val haptic = LocalHapticFeedback.current

    // Animated colors for super fluid transitions
    val animatedTrackBgColor by animateColorAsState(
        targetValue = if (checked) primaryColor.copy(alpha = 0.85f) else primaryColor.copy(alpha = 0.08f),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 650f),
        label = "trackBgColor"
    )

    val animatedThumbColor by animateColorAsState(
        targetValue = if (checked) (if (isDark) Color.White else onPrimaryColor) else primaryColor.copy(alpha = 0.55f),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 650f),
        label = "thumbColor"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (checked) (if (isDark) primaryColor else primaryColor) else (if (isDark) Color.White else onPrimaryColor),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 650f),
        label = "iconColor"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (checked) primaryColor.copy(alpha = 0.15f) else primaryColor.copy(alpha = 0.35f),
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 650f),
        label = "borderColor"
    )

    // Dimensions
    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 24.dp
    val maxDragOffset = 20.dp // trackWidth (52) - thumbSize (24) - padding (4) - padding (4)

    // Animated thumb position offset with smooth spring physics
    val thumbOffsetAnim by animateDpAsState(
        targetValue = if (checked) maxDragOffset else 0.dp,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 450f),
        label = "thumbOffset"
    )

    // Slide-to-toggle and tap interaction
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(animatedTrackBgColor)
            .border(
                width = 1.5.dp,
                color = animatedBorderColor,
                shape = CircleShape
            )
            .pointerInput(enabled, checked) {
                if (enabled && onCheckedChange != null) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 6 && !checked) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCheckedChange(true)
                        } else if (dragAmount < -6 && checked) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCheckedChange(false)
                        }
                    }
                }
            }
            .semantics {
                role = Role.Switch
                stateDescription = if (checked) "Activado" else "Desactivado"
            }
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange?.invoke(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding Thumb
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .offset(x = thumbOffsetAnim)
                .size(thumbSize)
                .clip(CircleShape)
                .background(animatedThumbColor),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedContent(
                targetState = checked,
                transitionSpec = {
                    androidx.compose.animation.fadeIn(animationSpec = spring(stiffness = 500f)) + 
                    androidx.compose.animation.scaleIn(initialScale = 0.5f, animationSpec = spring(stiffness = 500f)) togetherWith 
                    androidx.compose.animation.fadeOut(animationSpec = spring(stiffness = 500f)) + 
                    androidx.compose.animation.scaleOut(targetScale = 0.5f, animationSpec = spring(stiffness = 500f))
                },
                label = "switchIconAnim"
            ) { isChecked ->
                if (checkedIcon != null && uncheckedIcon != null) {
                    Icon(
                        imageVector = if (isChecked) checkedIcon else uncheckedIcon,
                        contentDescription = if (isChecked) "Activo" else "Inactivo",
                        tint = animatedIconColor,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    Canvas(
                        modifier = Modifier
                            .size(13.dp)
                            .semantics {
                                contentDescription = if (isChecked) "Activo" else "Inactivo"
                            }
                    ) {
                        val strokeWidthPx = 2.4.dp.toPx()
                        if (isChecked) {
                            val path = Path().apply {
                                moveTo(size.width * 0.18f, size.height * 0.52f)
                                lineTo(size.width * 0.42f, size.height * 0.76f)
                                lineTo(size.width * 0.84f, size.height * 0.24f)
                            }
                            drawPath(
                                path = path,
                                color = animatedIconColor,
                                style = Stroke(
                                    width = strokeWidthPx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else {
                            val startOffset = size.width * 0.24f
                            val endOffset = size.width * 0.76f
                            drawLine(
                                color = animatedIconColor,
                                start = Offset(startOffset, startOffset),
                                end = Offset(endOffset, endOffset),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = animatedIconColor,
                                start = Offset(endOffset, startOffset),
                                end = Offset(startOffset, endOffset),
                                strokeWidth = strokeWidthPx,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}
