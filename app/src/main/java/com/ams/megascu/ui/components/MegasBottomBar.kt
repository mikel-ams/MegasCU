package com.ams.megascu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun MegasBottomBar(
    onOpenPlanes: () -> Unit,
    onOpenGuide: () -> Unit = {},
    onShowAbout: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onRefresh: () -> Unit = {},
    isRefreshing: Boolean = false,
    useWavyProgress: Boolean = true,
    refreshIndicatorType: String = "circular_wavy",
    disableBlur: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val primaryColor = MaterialTheme.colorScheme.primary

    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val extraBottomPadding = if (navBottomPadding > 20.dp) 2.dp else 6.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = extraBottomPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        clip = false
                    )
                    .graphicsLayer {
                        shape = CircleShape
                        clip = false
                    }
                    .testTag("megas_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left - Comprar button
                    val comprarInteraction = remember { MutableInteractionSource() }
                    ExpressiveButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenPlanes()
                        },
                        interactionSource = comprarInteraction,
                        modifier = Modifier
                            .width(114.dp)
                            .height(40.dp)
                            .testTag("bottom_nav_planes_button")
                            .expressivePressEffect(interactionSource = comprarInteraction),
                        shape = rememberExpressiveMorphShape(
                            defaultRadius = 20.dp,
                            pressedRadius = 10.dp,
                            interactionSource = comprarInteraction
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = "Comprar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Comprar",
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Center item - Actualizar (Enlarged)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.zIndex(2f)
                    ) {
                        val refreshInteraction = remember { MutableInteractionSource() }
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRefresh()
                            },
                            interactionSource = refreshInteraction,
                            modifier = Modifier
                                .testTag("bottom_nav_refresh_button")
                                .expressivePressEffect(interactionSource = refreshInteraction)
                                .size(52.dp),
                            shape = rememberExpressiveMorphShape(
                                defaultRadius = 26.dp,
                                pressedRadius = 14.dp,
                                interactionSource = refreshInteraction
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shadowElevation = 0.dp
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (isRefreshing) {
                                    if (!useWavyProgress) {
                                        CircularWavyProgressIndicator(
                                            color = Color.White,
                                            trackColor = Color.White.copy(alpha = 0.25f),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    } else {
                                        when (refreshIndicatorType) {
                                            "loading_indicator" -> {
                                                ShapeMorphingLoadingIndicator(
                                                    color = Color.White,
                                                    strokeWidth = 2.5.dp,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                            "circular_wavy" -> {
                                                CircularWavyProgressIndicator(
                                                    color = Color.White,
                                                    trackColor = Color.White.copy(alpha = 0.25f),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                            else -> {
                                                CircularWavyProgressIndicator(
                                                    color = Color.White,
                                                    trackColor = Color.White.copy(alpha = 0.25f),
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Actualizar",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Right - Consejos button
                    val consejosInteraction = remember { MutableInteractionSource() }
                    ExpressiveButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenGuide()
                        },
                        interactionSource = consejosInteraction,
                        modifier = Modifier
                            .width(114.dp)
                            .height(40.dp)
                            .testTag("bottom_nav_guide_button")
                            .expressivePressEffect(interactionSource = consejosInteraction),
                        shape = rememberExpressiveMorphShape(
                            defaultRadius = 20.dp,
                            pressedRadius = 10.dp,
                            interactionSource = consejosInteraction
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = "Consejos",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Consejos",
                            maxLines = 1,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}



