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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag("megas_bottom_bar")
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left - Comprar button
                    val comprarInteraction = remember { MutableInteractionSource() }
                    ExpressiveButton(
                        onClick = onOpenPlanes,
                        interactionSource = comprarInteraction,
                        shape = CircleShape,
                        modifier = Modifier
                            .width(114.dp)
                            .height(48.dp)
                            .testTag("bottom_nav_planes_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            onClick = onRefresh,
                            interactionSource = refreshInteraction,
                            modifier = Modifier
                                .testTag("bottom_nav_refresh_button")
                                .size(52.dp),
                            shape = CircleShape,
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
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(26.dp),
                                            strokeWidth = 3.dp
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
                        onClick = onOpenGuide,
                        interactionSource = consejosInteraction,
                        shape = CircleShape,
                        modifier = Modifier
                            .width(114.dp)
                            .height(48.dp)
                            .testTag("bottom_nav_guide_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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



