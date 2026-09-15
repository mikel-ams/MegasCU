package com.ams.megascu.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator as M3LinearWavyProgressIndicator
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Indeterminate Linear Wavy Progress Indicator.
 * Acts as a wrapper adapter around official Material 3 [M3LinearWavyProgressIndicator].
 *
 * @param modifier Layout and size modifier.
 * @param color Active stroke color.
 * @param trackColor Background track color.
 * @param strokeWidth Width of the wavy stroke line (default 3.dp).
 * @param amplitude Height offset for wave crests (default 2.5.dp).
 * @param waveLength Distance between crests in Dp (default 18.dp).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinearWavyProgressIndicator(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(10.dp),
    color: Color = Color.White,
    trackColor: Color = Color.Transparent,
    strokeWidth: Dp = 3.dp,
    amplitude: Dp = 2.5.dp,
    waveLength: Dp = 18.dp
) {
    val density = LocalDensity.current
    val stroke = Stroke(
        width = with(density) { strokeWidth.toPx() },
        cap = StrokeCap.Round
    )
    M3LinearWavyProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = stroke,
        amplitude = 1.0f,
        wavelength = waveLength
    )
}


