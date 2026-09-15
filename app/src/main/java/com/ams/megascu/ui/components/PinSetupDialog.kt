package com.ams.megascu.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun PinSetupDialog(
    onPinSet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var step by remember { mutableStateOf(1) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val flashErrorColor = remember { Animatable(0f) }

    LaunchedEffect(error) {
        if (error) {
            delay(5000)
            error = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .expressiveModalEntrance(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ShieldLockIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (step == 1) "Crear Nuevo PIN" else "Confirmar PIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (step == 1) "Introduce un PIN de 4 dígitos para proteger la app" else "Ingresa nuevamente el PIN para confirmar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))

                fun triggerErrorFeedback() {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    error = true
                    coroutineScope.launch {
                        flashErrorColor.snapTo(1f)
                        flashErrorColor.animateTo(0f, animationSpec = tween(500))
                    }
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(200)
                        if (step == 1) pin = "" else confirmPin = ""
                    }
                }

                val currentInput = if (step == 1) pin else confirmPin

                // Circle PIN Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInput.length
                        val sizeDp by animateDpAsState(
                            targetValue = if (isFilled) 22.dp else 15.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "pinSetupIndicatorSize_$i"
                        )

                        val baseColor = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                        val indicatorColor = lerp(baseColor, MaterialTheme.colorScheme.error, flashErrorColor.value)

                        Surface(
                            shape = CircleShape,
                            color = indicatorColor,
                            modifier = Modifier.size(sizeDp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (error) {
                    Text(
                        "Los PIN no coinciden o tienen menos de 4 dígitos",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Expressive Spacious Numpad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keys) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            for (key in row) {
                                if (key.isEmpty()) {
                                    Spacer(modifier = Modifier.size(68.dp, 52.dp))
                                } else {
                                    val keyInteraction = remember { MutableInteractionSource() }
                                    val keyShape = rememberExpressiveMorphShape(
                                        defaultRadius = 18.dp,
                                        pressedRadius = 8.dp,
                                        interactionSource = keyInteraction
                                    )
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (key == "DEL") {
                                                if (currentInput.isNotEmpty()) {
                                                    val updated = currentInput.dropLast(1)
                                                    if (step == 1) pin = updated else confirmPin = updated
                                                    error = false
                                                }
                                            } else {
                                                if (currentInput.length < 4) {
                                                    val updated = currentInput + key
                                                    if (step == 1) pin = updated else confirmPin = updated
                                                    error = false
                                                }
                                            }
                                        },
                                        interactionSource = keyInteraction,
                                        shape = keyShape,
                                        color = if (key == "DEL") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.50f),
                                        modifier = Modifier
                                            .size(68.dp, 52.dp)
                                            .expressivePressEffect(interactionSource = keyInteraction)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (key == "DEL") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                    contentDescription = "Borrar",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveTextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 46.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    ExpressiveButton(
                        onClick = {
                            if (step == 1) {
                                if (pin.length == 4) step = 2 else triggerErrorFeedback()
                            } else {
                                if (pin == confirmPin && confirmPin.length == 4) {
                                    onPinSet(pin)
                                } else {
                                    triggerErrorFeedback()
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.heightIn(min = 46.dp)
                    ) {
                        Text(if (step == 1) "Siguiente" else "Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
