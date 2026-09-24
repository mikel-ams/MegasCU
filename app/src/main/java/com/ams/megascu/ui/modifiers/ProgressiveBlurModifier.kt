package com.ams.megascu.ui.modifiers

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ams.megascu.utils.DeviceUtils

enum class BlurDirection {
    TOP,
    BOTTOM,
}

/**
 * Modificador de desenfoque progresivo nativo de Jetpack Compose.
 * Aplica un gradiente gaussiano limpio en la parte superior o inferior,
 * fusionándolo gradualmente con el contenedor de la superficie para eliminar
 * cualquier línea dura, pixelado o corte abrupto.
 */
fun Modifier.progressiveBlur(
    blurRadius: Float,
    height: Float,
    direction: BlurDirection = BlurDirection.TOP,
    showGradientOverlay: Boolean = true,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle,
): Modifier = composed {
    val context = LocalContext.current
    val isPowerSave = remember(context) { DeviceUtils.isPowerSaveMode(context) }
    val overlayColor = MaterialTheme.colorScheme.surface

    val gradientModifier = if (height > 0f) {
        Modifier.drawWithContent {
            drawContent()
            if (showGradientOverlay) {
                val brush = when (direction) {
                    BlurDirection.TOP -> Brush.verticalGradient(
                        colors = listOf(
                            overlayColor,
                            overlayColor.copy(alpha = 0.85f),
                            overlayColor.copy(alpha = 0.40f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                    BlurDirection.BOTTOM -> Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            overlayColor.copy(alpha = 0.40f),
                            overlayColor.copy(alpha = 0.85f),
                            overlayColor
                        ),
                        startY = (size.height - height).coerceAtLeast(0f),
                        endY = size.height
                    )
                }
                drawRect(brush = brush)
            }
        }
    } else {
        Modifier
    }

    this.then(gradientModifier)
}

fun Modifier.progressiveBlur(
    maxRadius: Dp = 24.dp,
    direction: BlurDirection = BlurDirection.TOP,
    showGradientOverlay: Boolean = true,
    edgeTreatment: BlurredEdgeTreatment = BlurredEdgeTreatment.Rectangle,
): Modifier = composed {
    val density = LocalDensity.current
    val heightPx = with(density) { 32.dp.toPx() }
    val blurRadiusPx = with(density) { maxRadius.toPx() }
    progressiveBlur(
        blurRadius = blurRadiusPx,
        height = heightPx,
        direction = direction,
        showGradientOverlay = showGradientOverlay,
        edgeTreatment = edgeTreatment
    )
}
