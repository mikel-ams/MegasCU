package com.ams.megascu.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that subtly flexes the component elastically when pressed, providing
 * fluid M3 Expressive micro-interaction feedback without pure sinking.
 */
fun Modifier.pressScale(
    targetScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val flexX by animateFloatAsState(
        targetValue = if (isPressed) 1.028f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 400f
        ),
        label = "microPressFlexX"
    )
    val flexY by animateFloatAsState(
        targetValue = if (isPressed) 0.972f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 400f
        ),
        label = "microPressFlexY"
    )

    this.graphicsLayer {
        scaleX = flexX
        scaleY = flexY
    }
}

/**
 * Modifier that adds both a click action and a spring bounce micro-interaction.
 */
fun Modifier.bounceClick(
    targetScale: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val flexX by animateFloatAsState(
        targetValue = if (isPressed) 1.032f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 400f
        ),
        label = "bounceClickFlexX"
    )
    val flexY by animateFloatAsState(
        targetValue = if (isPressed) 0.968f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = 400f
        ),
        label = "bounceClickFlexY"
    )

    this
        .graphicsLayer {
            scaleX = flexX
            scaleY = flexY
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            onClick = onClick
        )
}

/**
 * Material 3 Expressive click modifier with spring corner morphing,
 * haptic vibration, and ripple feedback (without distorting label scaling).
 */
fun Modifier.expressiveClick(
    targetScale: Float = 0.93f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "expressiveClickCorner"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    this
        .clip(RoundedCornerShape(cornerRadius))
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            onClick = onClick
        )
}

/**
 * Expressive M3 Morphic press effect modifier with haptic vibration.
 */
fun Modifier.expressivePressEffect(
    targetScale: Float = 0.94f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    this
}

/**
 * Material 3 Expressive Morphic Button components with corner shape morphing
 * and haptic feedback on touch (pure shape morphing without stretching label/content).
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val isPillOrCircle = shape == CircleShape || shape == RoundedCornerShape(percent = 50) || shape == RoundedCornerShape(50)
    val effectiveShape = if (isPillOrCircle) {
        CircleShape
    } else {
        val cornerRadius by animateDpAsState(
            targetValue = if (isPressed) 8.dp else 18.dp,
            animationSpec = spring(
                dampingRatio = 0.52f,
                stiffness = 380f
            ),
            label = "morphicButtonCorner"
        )
        RoundedCornerShape(cornerRadius)
    }

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = effectiveShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicTextButtonCorner"
    )

    TextButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicOutlinedButtonCorner"
    )

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = ButtonDefaults.filledTonalShape,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicFilledTonalButtonCorner"
    )

    FilledTonalButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicIconButtonCorner"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius)),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicFilledTonalIconButtonCorner"
    )

    FilledTonalIconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = FloatingActionButtonDefaults.shape,
    containerColor: androidx.compose.ui.graphics.Color = FloatingActionButtonDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "morphicFabCorner"
    )

    FloatingActionButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun rememberExpressiveMorphShape(
    defaultRadius: Dp = 18.dp,
    pressedRadius: Dp = 8.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): androidx.compose.ui.graphics.Shape {
    val isPressed by interactionSource.collectIsPressedAsState()
    val radius by animateDpAsState(
        targetValue = if (isPressed) pressedRadius else defaultRadius,
        animationSpec = spring(
            dampingRatio = 0.52f,
            stiffness = 380f
        ),
        label = "expressiveMorphShape"
    )
    return RoundedCornerShape(radius)
}

/**
 * M3 Expressive Motion entrance animation for Modal Windows and Alerts.
 * Applies a bouncy scale pop, soft fade, and vertical spring slide.
 */
@Composable
fun Modifier.expressiveModalEntrance(): Modifier = composed {
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateIn = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.78f,
        animationSpec = spring(
            dampingRatio = 0.62f, // Bouncy M3 expressive spring
            stiffness = 340f
        ),
        label = "expressiveModalScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 1.0f,
            stiffness = 380f
        ),
        label = "expressiveModalAlpha"
    )

    val offsetY by animateDpAsState(
        targetValue = if (animateIn) 0.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 360f
        ),
        label = "expressiveModalOffsetY"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        translationY = offsetY.toPx()
    }
}

/**
 * Container wrapper for Modal Windows and Alerts with M3 Expressive entrance physics.
 */
@Composable
fun ExpressiveModalContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.expressiveModalEntrance()
    ) {
        content()
    }
}

const val BOUNCY_DAMPING_RATIO = 0.58f
const val BOUNCY_STIFFNESS = 320f

@Composable
fun bouncyDialogEntrance(
    damping: Float = BOUNCY_DAMPING_RATIO,
    stiffness: Float = BOUNCY_STIFFNESS
): Modifier {
    var isVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = damping,
            stiffness = stiffness
        ),
        label = "bouncyDialogScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncyDialogAlpha"
    )
    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

@Composable
fun bouncySheetSlideUp(
    damping: Float = BOUNCY_DAMPING_RATIO,
    stiffness: Float = BOUNCY_STIFFNESS
): Modifier {
    var isVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 400f,
        animationSpec = spring(
            dampingRatio = damping,
            stiffness = stiffness
        ),
        label = "bouncySheetOffsetY"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncySheetAlpha"
    )
    return Modifier.graphicsLayer {
        translationY = offsetY
        this.alpha = alpha
    }
}

