package com.ams.megascu.ui.components

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Handle de arrastre minimalista y expresivo para BottomSheets usando el componente nativo de Material 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveDragHandle(
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
) {
    BottomSheetDefaults.DragHandle(
        modifier = modifier,
        width = width,
        height = height,
        color = color
    )
}
