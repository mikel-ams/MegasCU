@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.ams.megascu.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope

/**
 * Lightweight press-scale feedback driven by the app's Material 3 Expressive motion scheme.
 * The scale is uniform, so content is never stretched on one axis.
 */
fun Modifier.pressScale(
    targetScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.pressScale(
    targetScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.pressScale(targetScale, interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

fun Modifier.bounceClick(
    targetScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = pressScale(targetScale, interactionSource)

fun Modifier.bounceClick(
    targetScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = pressScale(targetScale, onClick)

fun Modifier.expressiveClick(
    interactionSource: MutableInteractionSource,
    cornerRadius: Dp = 18.dp
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "expressiveClickScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.expressiveClick(
    cornerRadius: Dp = 18.dp,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.expressiveClick(interactionSource, cornerRadius)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

fun Modifier.expressiveClick(
    onClick: () -> Unit
): Modifier = expressiveClick(18.dp, onClick)

/**
 * Material 3 Expressive press scale animation with spring bounce.
 */
fun Modifier.expressivePressEffect(
    interactionSource: MutableInteractionSource? = null,
    targetScale: Float = 0.94f
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "expressivePressScale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private fun expressiveButtonShape(requested: Shape): Shape = requested

private fun expressivePressedShape(requested: Shape): Shape =
    when (requested) {
        CircleShape -> RoundedCornerShape(12.dp)
        is RoundedCornerShape -> RoundedCornerShape(12.dp)
        else -> RoundedCornerShape(12.dp)
    }

@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = expressiveButtonShape(shape),
            pressedShape = expressivePressedShape(shape)
        ),
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
    shape: Shape = ButtonDefaults.textShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = expressiveButtonShape(shape),
            pressedShape = expressivePressedShape(shape)
        ),
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
    shape: Shape = ButtonDefaults.outlinedShape,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = expressiveButtonShape(shape),
            pressedShape = expressivePressedShape(shape)
        ),
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
    shape: Shape = ButtonDefaults.filledTonalShape,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(
            shape = expressiveButtonShape(shape),
            pressedShape = expressivePressedShape(shape)
        ),
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
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = IconButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(12.dp)
        ),
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
    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shapes = IconButtonDefaults.shapes(
            shape = RoundedCornerShape(18.dp),
            pressedShape = RoundedCornerShape(12.dp)
        ),
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun ExpressiveFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: androidx.compose.ui.graphics.Color = FloatingActionButtonDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.material3.contentColorFor(containerColor),
    elevation: androidx.compose.material3.FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = expressiveButtonShape(shape),
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
    pressedRadius: Dp = 12.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Shape {
    val isPressed by interactionSource.collectIsPressedAsState()
    val radius by animateDpAsState(
        targetValue = if (isPressed) pressedRadius else defaultRadius,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expressiveMorphShape"
    )
    return RoundedCornerShape(radius)
}

@Composable
fun Modifier.expressiveModalEntrance(): Modifier {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expressiveModalScale"
    )
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "expressiveModalAlpha"
    )
    val offsetY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "expressiveModalOffsetY"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        translationY = offsetY.toPx()
    }
}

@Composable
fun ExpressiveModalContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.expressiveModalEntrance()
    ) {
        content()
    }
}

const val BOUNCY_DAMPING_RATIO = 0.8f
const val BOUNCY_STIFFNESS = 400f

@Composable
fun bouncyDialogEntrance(
    damping: Float = BOUNCY_DAMPING_RATIO,
    stiffness: Float = BOUNCY_STIFFNESS
): Modifier = Modifier.expressiveModalEntrance()

@Composable
fun bouncySheetSlideUp(
    damping: Float = BOUNCY_DAMPING_RATIO,
    stiffness: Float = BOUNCY_STIFFNESS
): Modifier = Modifier.expressiveModalEntrance()
