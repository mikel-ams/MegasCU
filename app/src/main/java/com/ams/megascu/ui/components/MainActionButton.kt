package com.ams.megascu.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SettingsPhone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainActionButton(
    quickActionCode: String,
    quickActionLabel: String,
    onConfigureClick: () -> Unit,
    onExecuteClick: (String) -> Unit,
    isPinProtected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val isConfigured = quickActionCode.isNotBlank()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val contentColor = Color.White

    val buttonGradient = remember(primaryColor, secondaryColor, tertiaryColor) {
        Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor)
        )
    }

    val cardInteraction = remember { MutableInteractionSource() }
    val cardShape = rememberExpressiveMorphShape(
        defaultRadius = 28.dp,
        pressedRadius = 14.dp,
        interactionSource = cardInteraction
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .expressivePressEffect(interactionSource = cardInteraction)
            .clip(cardShape)
            .combinedClickable(
                interactionSource = cardInteraction,
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isConfigured) {
                        onExecuteClick(quickActionCode)
                    } else {
                        onConfigureClick()
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfigureClick()
                }
            )
            .testTag("quick_action_button"),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .background(buttonGradient)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = contentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Rounded.FlashOn else Icons.Rounded.SettingsPhone,
                            contentDescription = "Acción Rápida",
                            tint = contentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        if (isConfigured && isPinProtected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = "Protegido por PIN",
                                    tint = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isConfigured) {
                            if (quickActionLabel.isNotBlank()) quickActionLabel else "Ejecutar $quickActionCode"
                        } else {
                            "Configurar Acción Rápida"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = contentColor
                        )
                    )
                    Text(
                        text = if (isConfigured) {
                            "Código: $quickActionCode • Mantén para cambiar"
                        } else {
                            "Toca para asignar un código USSD para comprar un plan"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }
    }
}


