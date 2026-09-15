package com.ams.megascu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Material 3 Expressive Shape Morphing Loading Indicator.
 * Smoothly morphs between Circle -> Clover -> Rounded Square -> Star -> Shield while rotating.
 */
@Composable
fun ShapeMorphingLoadingIndicator(
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = Color.White,
    strokeWidth: Dp = 2.5.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShapeMorphingLoading")

    // Continuous rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Morph cycle parameter (0 to 5)
    val morphProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphProgress"
    )

    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val center = size.minDimension / 2f
        val baseRadius = (center - strokeWidthPx / 2f - 1.5.dp.toPx()).coerceAtLeast(1f)

        val currentIdx = morphProgress.toInt() % 5
        val nextIdx = (currentIdx + 1) % 5
        val rawFraction = morphProgress - morphProgress.toInt()
        
        // Easing for smooth morph transition
        val morphFraction = FastOutSlowInEasing.transform(rawFraction)

        fun radiusForShape(shapeIdx: Int, theta: Double): Double {
            return when (shapeIdx) {
                0 -> 1.0 // Circle
                1 -> 0.82 + 0.18 * cos(4.0 * theta) // 4-lobe clover
                2 -> {
                    // Smooth Rounded Square / Superellipse
                    val c = abs(cos(theta)).pow(3.0)
                    val s = abs(sin(theta)).pow(3.0)
                    val factor = (c + s).pow(-1.0 / 3.0)
                    0.80 * factor.coerceIn(0.8, 1.15)
                }
                3 -> 0.82 + 0.18 * cos(5.0 * theta) // 5-point star
                4 -> 0.84 + 0.16 * cos(3.0 * theta) // 3-lobe shield
                else -> 1.0
            }
        }

        val path = Path()
        val steps = 180

        for (i in 0..steps) {
            val theta = (i.toDouble() / steps) * 2.0 * PI
            val r1 = radiusForShape(currentIdx, theta)
            val r2 = radiusForShape(nextIdx, theta)
            val interpolatedRadiusRatio = r1 * (1.0 - morphFraction) + r2 * morphFraction

            val r = baseRadius * interpolatedRadiusRatio
            val x = center + (r * cos(theta)).toFloat()
            val y = center + (r * sin(theta)).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        rotate(degrees = rotationAngle, pivot = androidx.compose.ui.geometry.Offset(center, center)) {
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
