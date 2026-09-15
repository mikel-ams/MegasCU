package com.ams.megascu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.core.graphics.PathParser
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onAnimationFinished: () -> Unit) {
    val shieldScale = remember { Animatable(0f) }
    val bar1Alpha = remember { Animatable(0f) }
    val bar2Alpha = remember { Animatable(0f) }
    val bar3Alpha = remember { Animatable(0f) }
    val bar4Alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Espera inicial acelerada a 1.5x (500ms / 1.5 = 333ms)
        delay(333)

        // 1. Escudo aparece con rebote (acelerado 1.5x)
        shieldScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium * 1.5f
            )
        )
        
        // 2. Barras de señal se encienden progresivamente (50ms / 1.5 = 33ms)
        val barAnimationSpec = tween<Float>(durationMillis = 33, easing = LinearEasing)
        bar1Alpha.animateTo(1f, animationSpec = barAnimationSpec)
        bar2Alpha.animateTo(1f, animationSpec = barAnimationSpec)
        bar3Alpha.animateTo(1f, animationSpec = barAnimationSpec)
        bar4Alpha.animateTo(1f, animationSpec = barAnimationSpec)
        
        // Espera final acelerada (200ms / 1.5 = 133ms)
        delay(133) 
        onAnimationFinished()
    }

    val shieldPath = remember { PathParser.createPathFromPathData("M12,3.5 L5,6.2 V12 C5,16.5 8.1,20.2 12,21.5 C15.9,20.2 19,16.5 19,12 V6.2 L12,3.5 Z") }
    val bar1Path = remember { PathParser.createPathFromPathData("M 8.25,15.5 V 13.5") }
    val bar2Path = remember { PathParser.createPathFromPathData("M 10.75,15.5 V 11.5") }
    val bar3Path = remember { PathParser.createPathFromPathData("M 13.25,15.5 V 9.5") }
    val bar4Path = remember { PathParser.createPathFromPathData("M 15.75,15.5 V 7.5") }
    
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(288.dp)) {
            val scaleFactor = size.width / 108f
            
            withTransform({
                scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
                translate(left = 25.2f, top = 25.2f)
                scale(scaleX = 2.4f, scaleY = 2.4f, pivot = Offset.Zero)
            }) {
                // Escudo animado (Escala)
                withTransform({
                    scale(shieldScale.value, shieldScale.value, pivot = Offset(12f, 12f))
                }) {
                    drawPath(
                        path = shieldPath.asComposePath(),
                        color = primaryColor,
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                
                // Barras animadas (Opacidad)
                drawPath(
                    path = bar1Path.asComposePath(),
                    color = primaryColor.copy(alpha = bar1Alpha.value),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = bar2Path.asComposePath(),
                    color = primaryColor.copy(alpha = bar2Alpha.value),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = bar3Path.asComposePath(),
                    color = primaryColor.copy(alpha = bar3Alpha.value),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = bar4Path.asComposePath(),
                    color = primaryColor.copy(alpha = bar4Alpha.value),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
        
        androidx.compose.material3.Text(
            text = androidx.compose.ui.res.stringResource(id = com.ams.megascu.R.string.app_name),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = primaryColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(shieldScale.value)
        )
    }
}
