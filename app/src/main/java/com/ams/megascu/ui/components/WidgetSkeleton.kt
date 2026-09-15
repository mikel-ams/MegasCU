package com.ams.megascu.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unified WidgetSkeleton component providing accurate Compose Shimmer / Pulse
 * placeholders mimicking the final 4x2, 2x1, and 3x2 widget layouts.
 */
@Composable
fun WidgetSkeleton(
    type: String = "4x2",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonPulseTransition")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    val skeletonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaPulse * 0.4f)
    val cardBgColor = MaterialTheme.colorScheme.primaryContainer
    val borderBrush = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
    )

    when (type) {
        "2x1" -> WidgetSkeleton2x1(
            skeletonColor = skeletonColor,
            cardBgColor = cardBgColor,
            borderBrush = borderBrush,
            modifier = modifier
        )
        "3x2" -> WidgetSkeletonChart3x2(
            skeletonColor = skeletonColor,
            cardBgColor = cardBgColor,
            borderBrush = borderBrush,
            modifier = modifier
        )
        else -> WidgetSkeleton4x2(
            skeletonColor = skeletonColor,
            cardBgColor = cardBgColor,
            borderBrush = borderBrush,
            modifier = modifier
        )
    }
}

@Composable
private fun WidgetSkeletonBlock(
    width: Dp,
    height: Dp,
    color: Color,
    cornerRadius: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
    )
}

@Composable
fun WidgetSkeleton4x2(
    skeletonColor: Color,
    cardBgColor: Color,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Saldo title & numeric value + Refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    WidgetSkeletonBlock(width = 90.dp, height = 12.dp, color = skeletonColor)
                    WidgetSkeletonBlock(width = 110.dp, height = 22.dp, color = skeletonColor, cornerRadius = 8.dp)
                }

                // Refresh button pill
                WidgetSkeletonBlock(width = 80.dp, height = 28.dp, color = skeletonColor, cornerRadius = 14.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid Metrics (Datos & Llamadas left, Bono & SMS right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 1: Datos
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WidgetSkeletonBlock(width = 85.dp, height = 11.dp, color = skeletonColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetSkeletonBlock(width = 58.dp, height = 16.dp, color = skeletonColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            WidgetSkeletonBlock(width = 28.dp, height = 14.dp, color = skeletonColor, cornerRadius = 4.dp)
                        }
                    }

                    // Item 2: Llamadas
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WidgetSkeletonBlock(width = 75.dp, height = 11.dp, color = skeletonColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetSkeletonBlock(width = 62.dp, height = 16.dp, color = skeletonColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            WidgetSkeletonBlock(width = 28.dp, height = 14.dp, color = skeletonColor, cornerRadius = 4.dp)
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 3: Bono Datos
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WidgetSkeletonBlock(width = 80.dp, height = 11.dp, color = skeletonColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetSkeletonBlock(width = 52.dp, height = 16.dp, color = skeletonColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            WidgetSkeletonBlock(width = 28.dp, height = 14.dp, color = skeletonColor, cornerRadius = 4.dp)
                        }
                    }

                    // Item 4: Mensajes
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WidgetSkeletonBlock(width = 65.dp, height = 11.dp, color = skeletonColor)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WidgetSkeletonBlock(width = 50.dp, height = 16.dp, color = skeletonColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            WidgetSkeletonBlock(width = 28.dp, height = 14.dp, color = skeletonColor, cornerRadius = 4.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer progress bar skeleton
            WidgetSkeletonBlock(width = Dp.Unspecified, height = 6.dp, color = skeletonColor, cornerRadius = 3.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun WidgetSkeleton2x1(
    skeletonColor: Color,
    cardBgColor: Color,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Title label & Pill badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WidgetSkeletonBlock(width = 110.dp, height = 13.dp, color = skeletonColor)
                WidgetSkeletonBlock(width = 36.dp, height = 16.dp, color = skeletonColor, cornerRadius = 8.dp)
            }

            // Middle: Large Numeric Value
            WidgetSkeletonBlock(width = 130.dp, height = 24.dp, color = skeletonColor, cornerRadius = 8.dp)

            // Bottom: Subtitle / Expiration text
            WidgetSkeletonBlock(width = 140.dp, height = 12.dp, color = skeletonColor)
        }
    }
}

@Composable
fun WidgetSkeletonChart3x2(
    skeletonColor: Color,
    cardBgColor: Color,
    borderBrush: Brush,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .border(width = 1.dp, brush = borderBrush, shape = RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Total Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WidgetSkeletonBlock(width = 120.dp, height = 14.dp, color = skeletonColor)
                WidgetSkeletonBlock(width = 70.dp, height = 18.dp, color = skeletonColor, cornerRadius = 6.dp)
            }

            // Middle: Chart Bars Wave Skeleton
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val barHeights = listOf(30.dp, 48.dp, 62.dp, 40.dp, 75.dp, 55.dp, 80.dp)
                barHeights.forEach { h ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        WidgetSkeletonBlock(width = 24.dp, height = 10.dp, color = skeletonColor)
                        WidgetSkeletonBlock(width = 16.dp, height = h, color = skeletonColor, cornerRadius = 8.dp)
                    }
                }
            }

            // Footer date labels skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WidgetSkeletonBlock(width = 80.dp, height = 10.dp, color = skeletonColor)
                WidgetSkeletonBlock(width = 40.dp, height = 12.dp, color = skeletonColor, cornerRadius = 6.dp)
            }
        }
    }
}
