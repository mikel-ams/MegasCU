package com.ams.megascu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Handle de arrastre minimalista y expresivo para BottomSheets.
 * Renders a clean pill shape with no Surface selection highlight, ripple, or shading.
 */
@Composable
fun ExpressiveDragHandle(
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
) {
    Box(
        modifier = modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .size(width = width, height = height)
            .background(
                color = color,
                shape = RoundedCornerShape(2.dp)
            )
    )
}
