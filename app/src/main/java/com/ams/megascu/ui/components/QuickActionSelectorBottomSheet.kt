package com.ams.megascu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class QuickActionOption(
    val title: String,
    val subtitle: String,
    val code: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionSelectorBottomSheet(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelectCode: (code: String, label: String) -> Unit,
    onProgress: (Float) -> Unit = {}
) {
    val options = listOf(
        // Combinados / Datos
        QuickActionOption("Plan 240 CUP (Solo Datos)", "4.5 GB (Vigencia 35 días)", "*133*1*4*1#", "Paquetes Combinados"),
        QuickActionOption("Plan 120 CUP", "2 GB + 15 min + 20 SMS", "*133*1*4*2#", "Paquetes Combinados"),
        QuickActionOption("Plan 240 CUP", "4 GB + 35 min + 40 SMS", "*133*1*4*3#", "Paquetes Combinados"),
        QuickActionOption("Plan 360 CUP", "6 GB + 60 min + 70 SMS", "*133*1*4*4#", "Paquetes Combinados"),
        QuickActionOption("Plan toDus (600 MB)", "25 CUP (ToDus / Nauta)", "*133*1*2#", "Paquetes Combinados"),
        QuickActionOption("Bolsa LTE Diaria (200 MB)", "25 CUP", "*133*1*3#", "Paquetes Combinados"),
        QuickActionOption("Menú General Comprar Datos", "Menú interactivo *133*1#", "*133*1#", "Paquetes Combinados"),

        // Voz
        QuickActionOption("5 Minutos (37.50 CUP)", "Vigencia 35 días", "*133*3*1#", "Planes de Voz"),
        QuickActionOption("10 Minutos (72.50 CUP)", "Vigencia 35 días", "*133*3*2#", "Planes de Voz"),
        QuickActionOption("15 Minutos (105 CUP)", "Vigencia 35 días", "*133*3*3#", "Planes de Voz"),
        QuickActionOption("25 Minutos (162.50 CUP)", "Vigencia 35 días", "*133*3*4#", "Planes de Voz"),
        QuickActionOption("40 Minutos (250 CUP)", "Vigencia 35 días", "*133*3*5#", "Planes de Voz"),
        QuickActionOption("Menú General Comprar Voz", "Menú interactivo *133*3#", "*133*3#", "Planes de Voz"),

        // SMS
        QuickActionOption("20 SMS (15 CUP)", "Vigencia 35 días", "*133*2*1#", "Planes de SMS"),
        QuickActionOption("50 SMS (30 CUP)", "Vigencia 35 días", "*133*2*2#", "Planes de SMS"),
        QuickActionOption("90 SMS (50 CUP)", "Vigencia 35 días", "*133*2*3#", "Planes de SMS"),
        QuickActionOption("120 SMS (60 CUP)", "Vigencia 35 días", "*133*2*4#", "Planes de SMS"),
        QuickActionOption("Menú General Comprar SMS", "Menú interactivo *133*2#", "*133*2#", "Planes de SMS"),

        // Consultas
        QuickActionOption("Consulta Saldo Principal", "*222#", "*222#", "Consultas Frecuentes"),
        QuickActionOption("Consulta Datos (DAT)", "*222*328#", "*222*328#", "Consultas Frecuentes"),
        QuickActionOption("Consulta Minutos (VOZ)", "*222*869#", "*222*869#", "Consultas Frecuentes"),
        QuickActionOption("Consulta Mensajes (SMS)", "*222*767#", "*222*767#", "Consultas Frecuentes")
    )

    val groupedOptions = options.groupBy { it.category }

    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        var parent = view.parent
        while (parent != null) {
            if (parent is androidx.compose.ui.window.DialogWindowProvider) {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(parent.window, false)
                break
            }
            parent = parent.parent
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    SheetProgressTracker(sheetState = sheetState, onProgress = onProgress)

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isDismissing by remember { mutableStateOf(false) }
    var selectedOptionCode by remember { mutableStateOf(currentCode) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 40.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = { ExpressiveDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.35f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Configuración de Acción Rápida",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                groupedOptions.forEach { (category, items) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(items) { option ->
                        val isSelected = option.code == selectedOptionCode
                        val itemInteraction = remember { MutableInteractionSource() }
                        val unselectedBgColor = if (isDark) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        }
                        Surface(
                            shape = rememberExpressiveMorphShape(
                                defaultRadius = 20.dp,
                                pressedRadius = 10.dp,
                                interactionSource = itemInteraction
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else unselectedBgColor,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            onClick = {
                                if (!isDismissing) {
                                    isDismissing = true
                                    selectedOptionCode = option.code
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    scope.launch {
                                        try {
                                            sheetState.hide()
                                        } catch (_: Exception) {
                                            // Ignore
                                        } finally {
                                            onSelectCode(option.code, option.title)
                                            onDismiss()
                                        }
                                    }
                                }
                            },
                            interactionSource = itemInteraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressivePressEffect(interactionSource = itemInteraction)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "${option.subtitle} • Código: ${option.code}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
