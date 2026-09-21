package com.ams.megascu.ui.components

import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.SettingsPhone
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.ams.megascu.R
import com.ams.megascu.ui.theme.CyberCyan
import com.ams.megascu.ui.theme.ElectricIndigo
import com.ams.megascu.data.db.PlanStatusEntity

data class EtecsaPlan(
    val title: String,
    val description: String,
    val price: String,
    val ussdCode: String
)

data class EtecsaCategory(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector?,
    val iconText: String? = null,
    val plans: List<EtecsaPlan>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanesBottomSheet(
    planStatus: PlanStatusEntity?,
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onRequestRefresh: (() -> Unit)? = null,
    onProgress: (Float) -> Unit = {}
) {
    val categories = listOf(
        EtecsaCategory(
            title = "Planes de Datos y Combos Combinados",
            description = "Vigencia 35 días",
            icon = Icons.Rounded.SwapVert,
            plans = listOf(
                EtecsaPlan("Plan 240 CUP", "4.5 GB", "240 CUP", "*133*1*4*1#"),
                EtecsaPlan("Plan 120 CUP", "2 GB + 15 min + 20 SMS", "120 CUP", "*133*1*4*2#"),
                EtecsaPlan("Plan 240 CUP", "4 GB + 35 min + 40 SMS", "240 CUP", "*133*1*4*3#"),
                EtecsaPlan("Plan 360 CUP", "6 GB + 60 min + 70 SMS", "360 CUP", "*133*1*4*4#"),
                EtecsaPlan("Plan toDus (600 MB)", "ToDus / Nauta", "25 CUP", "*133*1*2#"),
                EtecsaPlan("Bolsa LTE Diaria (200 MB)", "200 MB", "25 CUP", "*133*1*3#")
            )
        ),
        EtecsaCategory(
            title = "Planes de Voz / Minutos",
            description = "Vigencia 35 días",
            icon = Icons.Rounded.PhoneInTalk,
            plans = listOf(
                EtecsaPlan("5 Minutos", "", "37.50 CUP", "*133*3*1#"),
                EtecsaPlan("10 Minutos", "", "72.50 CUP", "*133*3*2#"),
                EtecsaPlan("15 Minutos", "", "105.00 CUP", "*133*3*3#"),
                EtecsaPlan("25 Minutos", "", "162.50 CUP", "*133*3*4#"),
                EtecsaPlan("40 Minutos", "", "250.00 CUP", "*133*3*5#")
            )
        ),
        EtecsaCategory(
            title = "Planes de Mensajería / SMS",
            description = "Vigencia 35 días",
            icon = Icons.AutoMirrored.Rounded.Message,
            plans = listOf(
                EtecsaPlan("20 SMS", "", "15.00 CUP", "*133*2*1#"),
                EtecsaPlan("50 SMS", "", "30.00 CUP", "*133*2*2#"),
                EtecsaPlan("90 SMS", "", "50.00 CUP", "*133*2*3#"),
                EtecsaPlan("120 SMS", "", "60.00 CUP", "*133*2*4#")
            )
        )
    )

    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var planToConfirm by remember { mutableStateOf<EtecsaPlan?>(null) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    if (planToConfirm != null) {
        val plan = planToConfirm!!
        val priceStr = plan.price.replace(Regex("[^0-9.]"), "")
        val priceVal = priceStr.toDoubleOrNull() ?: 0.0
        val currentBalance = planStatus?.balanceCup ?: -1.0
        
        val canAfford = currentBalance == -1.0 || currentBalance >= priceVal
        
        AlertDialog(
            onDismissRequest = { planToConfirm = null },
            modifier = Modifier.expressiveModalEntrance(),
            title = { Text(if (canAfford) stringResource(R.string.confirm_purchase_title) else stringResource(R.string.insufficient_balance_title)) },
            text = { 
                if (currentBalance == -1.0) {
                    Text("Se va a comprar el paquete ${plan.title} por ${plan.price}. Asegúrate de tener saldo suficiente.")
                } else if (!canAfford) {
                    Text("No tienes saldo suficiente para comprar el paquete ${plan.title}. Cuesta ${plan.price} y tienes ${currentBalance} CUP.")
                } else {
                    Text("Se va a comprar el paquete ${plan.title} por ${plan.price}. Tienes saldo suficiente (${currentBalance} CUP). ¿Deseas continuar?")
                }
            },
            confirmButton = {
                if (canAfford) {
                    ExpressiveButton(
                        onClick = {
                            onSelectPlan(plan.ussdCode)
                            planToConfirm = null
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.btn_buy))
                    }
                }
            },
            dismissButton = {
                ExpressiveTextButton(
                    onClick = {
                        planToConfirm = null
                    }
                ) {
                    Text(if (canAfford) stringResource(R.string.btn_cancel) else stringResource(R.string.btn_understood), color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

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
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("planes_bottom_sheet")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Planes Cubacel",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            val listState = rememberLazyListState()
            val fadeAlpha by remember { derivedStateOf { 
                if (listState.firstVisibleItemIndex > 0) 1f 
                else (listState.firstVisibleItemScrollOffset / 40f).coerceIn(0f, 1f) 
            } }

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    item {
                        ActivePlanHeaderCard(
                            planStatus = planStatus,
                            onRequestRefresh = onRequestRefresh
                        )
                    }

                    item {
                        Text(
                            text = "Catálogo de Planes Disponibles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(categories) { category ->
                    val isExpanded = expandedCategory == category.title
                    val categoryInteraction = remember { MutableInteractionSource() }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = rememberExpressiveMorphShape(
                            defaultRadius = 24.dp,
                            pressedRadius = 12.dp,
                            interactionSource = categoryInteraction
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .expressivePressEffect(interactionSource = categoryInteraction)
                                    .clickable(
                                        interactionSource = categoryInteraction,
                                        indication = null
                                    ) {
                                        expandedCategory = if (isExpanded) null else category.title
                                    }
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (category.icon != null) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = category.title,
                                                tint = Color.White,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        } else if (category.iconText != null) {
                                            Text(
                                                text = category.iconText,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (category.description.isNotBlank()) {
                                        Text(
                                            text = category.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = "Expandir",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 1.dp, modifier = Modifier.padding(bottom = 6.dp))
                                    category.plans.forEach { plan ->
                                        Surface(
                                            onClick = {
                                                if (plan.ussdCode.startsWith("*")) {
                                                    onSelectPlan(plan.ussdCode)
                                                    onDismiss()
                                                }
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = plan.title,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (plan.description.isNotBlank()) {
                                                        Text(
                                                            text = plan.description,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = plan.price,
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
            }

            // Top fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = fadeAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                // Bottom fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun ActivePlanHeaderCard(
    planStatus: PlanStatusEntity?,
    onRequestRefresh: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan Activo Actual",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                if (onRequestRefresh != null) {
                    ExpressiveIconButton(
                        onClick = onRequestRefresh,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            val totalDataMb = (planStatus?.dataMb ?: 0L) + (planStatus?.dataLteMb ?: 0L)
            val dataFormatted = if (totalDataMb >= 1024L) {
                String.format(java.util.Locale.US, "%.2f GB", totalDataMb.toFloat() / 1024f)
            } else {
                String.format(java.util.Locale.US, "%.0f MB", totalDataMb.toFloat())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Datos Restantes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = dataFormatted,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                val bonusMb = planStatus?.bonusDataMb ?: 0L
                if (bonusMb > 0L) {
                    val bonusFormatted = if (bonusMb >= 1024L) {
                        String.format(java.util.Locale.US, "%.2f GB", bonusMb.toFloat() / 1024f)
                    } else {
                        String.format(java.util.Locale.US, "%.0f MB", bonusMb.toFloat())
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Bono .CU",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = bonusFormatted,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (!planStatus?.nextRechargeDateStr.isNullOrBlank() || (planStatus?.dataDays ?: 0) > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateStr = planStatus?.nextRechargeDateStr.orEmpty().ifBlank { "Vigente" }
                    Text(
                        text = "Vencimiento: $dateStr",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if ((planStatus?.dataDays ?: 0) > 0) {
                        Text(
                            text = "${planStatus?.dataDays} días restantes",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
