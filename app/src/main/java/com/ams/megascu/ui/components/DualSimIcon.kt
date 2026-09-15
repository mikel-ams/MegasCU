package com.ams.megascu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ícono de Doble SIM compuesto por dos tarjetas SIM superpuestas con profundidad:
 * - Tarjeta posterior desplazada hacia arriba a la izquierda.
 * - Contorno perimetral sólido sin transparencia alrededor de la tarjeta frontal, con el color exacto de la tarjeta.
 * - Tarjeta frontal en primer plano desplazada hacia abajo a la derecha con color pleno.
 * - Adaptable dinámicamente al estilo global de iconografía: Nativo (Vector M3), M3 Filled y M3 Outlined.
 */
@Composable
fun DualSimIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    contourColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 24.dp
) {
    val simSize = size * 0.76f
    val shiftX = size * 0.16f
    val shiftY = size * 0.08f

    // Garantizar que el color del contorno sea 100% sólido y opaco (sin transparencia)
    // componiéndolo sobre la superficie si el color de la tarjeta tiene canal alfa.
    val baseSurface = MaterialTheme.colorScheme.surface
    val solidContourColor = remember(contourColor, baseSurface) {
        if (contourColor.alpha < 1f) {
            contourColor.compositeOver(baseSurface).copy(alpha = 1f)
        } else {
            contourColor.copy(alpha = 1f)
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 1. Tarjeta SIM trasera (desplazada a la izquierda y arriba)
        AppIcon(
            imageVector = Icons.Rounded.SimCard,
            contentDescription = null,
            tint = tint.copy(alpha = 0.72f),
            modifier = Modifier
                .size(simSize)
                .offset(x = -shiftX, y = -shiftY)
        )

        // 2. Borde / Contorno de profundidad alrededor de la tarjeta frontal (línea sólida sin transparencia)
        if (contourColor != Color.Transparent) {
            val strokeRadius = (size.value * 0.075f).coerceIn(1.5f, 2.2f)
            val strokeOffsets = remember(strokeRadius) {
                (0 until 16).map { i ->
                    val angle = (i * 22.5) * (Math.PI / 180.0)
                    Pair(
                        (strokeRadius * Math.cos(angle)).toFloat(),
                        (strokeRadius * Math.sin(angle)).toFloat()
                    )
                }
            }
            for (offset in strokeOffsets) {
                AppIcon(
                    imageVector = Icons.Rounded.SimCard,
                    contentDescription = null,
                    tint = solidContourColor,
                    modifier = Modifier
                        .size(simSize)
                        .offset(
                            x = shiftX + offset.first.dp,
                            y = shiftY + offset.second.dp
                        )
                )
            }
        }

        // 3. Tarjeta SIM frontal (desplazada a la derecha y abajo)
        AppIcon(
            imageVector = Icons.Rounded.SimCard,
            contentDescription = "Doble SIM",
            tint = tint,
            modifier = Modifier
                .size(simSize)
                .offset(x = shiftX, y = shiftY)
        )
    }
}
