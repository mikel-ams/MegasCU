package com.ams.megascu.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator as M3CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Indeterminate Circular Wavy Progress Indicator (Expressive Material 3 design).
 * Acts as a wrapper adapter around official Material 3 [M3CircularWavyProgressIndicator].
 *
 * @param modifier Size and layout modifiers.
 * @param color Active stroke color of the progress indicator.
 * @param trackColor Color for the background circular track.
 * @param trackThickness Stroke width for the path (default 2.dp).
 * @param cornerSize Wave amplitude / crest height offset (default 1.dp).
 * @param gapSize Wavelength separation parameter (default 3.dp).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CircularWavyProgressIndicator(
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = Color.White,
    trackColor: Color = color.copy(alpha = 0.25f),
    trackThickness: Dp = 2.dp,
    cornerSize: Dp = 1.dp,
    gapSize: Dp = 3.dp
) {
    val density = LocalDensity.current
    val stroke = Stroke(
        width = with(density) { trackThickness.toPx() },
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )
    M3CircularWavyProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = stroke,
        gapSize = gapSize,
        amplitude = 1.0f
    )
}



