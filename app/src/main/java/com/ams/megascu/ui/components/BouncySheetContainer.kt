@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.ams.megascu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SheetProgressTracker(
    sheetState: androidx.compose.material3.SheetState,
    onProgress: (Float) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenHeightPx = with(density) { androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx() }

    LaunchedEffect(sheetState) {
        var expandedOffsetPx = -1f
        snapshotFlow {
            runCatching { sheetState.requireOffset() }.getOrNull()
        }.collect { offset ->
            if (offset != null && offset > 0f) {
                if (expandedOffsetPx < 0f || offset < expandedOffsetPx) {
                    expandedOffsetPx = offset
                }
                val totalRange = screenHeightPx - expandedOffsetPx
                if (totalRange > 0f) {
                    val progress = ((screenHeightPx - offset) / totalRange).coerceIn(0f, 1f)
                    onProgress(progress)
                } else {
                    onProgress(1f)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onProgress(0f)
        }
    }
}

@Composable
fun BouncySheetContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
    }

    val targetVisible = appeared && isVisible

    val offscreenY = with(androidx.compose.ui.platform.LocalDensity.current) { 280.dp.toPx() }
    val translationY by animateFloatAsState(
        targetValue = if (targetVisible) 0f else offscreenY,
        animationSpec = androidx.compose.material3.MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "BouncySheetTranslation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (targetVisible) 1f else 0f,
        animationSpec = androidx.compose.material3.MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "BouncySheetAlpha"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                this.translationY = translationY
                this.alpha = alpha
            },
        content = content
    )
}

@Composable
fun BouncyDialogContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    content: @Composable () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
    }

    val targetVisible = appeared && isVisible

    val scale by animateFloatAsState(
        targetValue = if (targetVisible) 1f else 0.85f,
        animationSpec = androidx.compose.material3.MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "BouncyDialogScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (targetVisible) 0f else 60f,
        animationSpec = androidx.compose.material3.MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "BouncyDialogTranslation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (targetVisible) 1f else 0f,
        animationSpec = androidx.compose.material3.MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "BouncyDialogAlpha"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
                this.alpha = alpha
            },
        content = { content() }
    )
}
