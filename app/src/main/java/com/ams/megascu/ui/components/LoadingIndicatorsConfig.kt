package com.ams.megascu.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf

object LoadingIndicatorsConfig {
    // Linear Wavy Parameters
    var linearWavyHeight by mutableFloatStateOf(10f)
    var linearWavyAmplitude by mutableFloatStateOf(0.6f)
    var linearWavyWaveLength by mutableFloatStateOf(20f)
    var linearWavyStrokeWidth by mutableFloatStateOf(4f)

    // Circular Wavy Parameters
    var circularWavySize by mutableFloatStateOf(30f)
    var circularWavyStroke by mutableFloatStateOf(4f)
    var circularWavyAmplitude by mutableFloatStateOf(2.0f)
    var circularWavyWavesCount by mutableIntStateOf(10)

    // Linear Determinate (M3)
    var linearDetHeight by mutableFloatStateOf(8f)
    var linearDetCornerRadius by mutableFloatStateOf(4f)

    // Linear Indeterminate (M3)
    var linearIndetHeight by mutableFloatStateOf(8f)
    var linearIndetCornerRadius by mutableFloatStateOf(4f)

    // Circular Standard (M3)
    var circularStdSize by mutableFloatStateOf(30f)
    var circularStdStroke by mutableFloatStateOf(4f)

    // Shape Morphing
    var shapeMorphSize by mutableFloatStateOf(40f)
}
