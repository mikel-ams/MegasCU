package com.ams.megascu.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.ams.megascu.R
import com.ams.megascu.data.db.PlanStatusEntity
import java.util.Locale

data class ConsultaItem(
    val title: String,
    val ussdCode: String,
    val icon: ImageVector,
    val tag: String,
    val getValueText: (PlanStatusEntity?) -> String?
)

@Composable
fun ConsultasSection(
    planStatus: PlanStatusEntity?,
    onExecuteConsulta: (code: String, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val items = remember {
        listOf(
            ConsultaItem(
                title = "Saldo",
                ussdCode = "*222#",
                icon = Icons.Rounded.MonetizationOn,
                tag = "consulta_saldo",
                getValueText = { status ->
                    status?.let { "${String.format(Locale.US, "%.2f", it.balanceCup)} CUP" }
                }
            ),
            ConsultaItem(
                title = "Vigencia",
                ussdCode = "*222#",
                icon = Icons.Rounded.Event,
                tag = "consulta_vigencia",
                getValueText = { status ->
                    val dateStr = status?.nextRechargeDateStr
                    dateStr?.let {
                        try {
                            val cleanDate = it.trim().replace("/", "-").replace(".", "-")
                            val pattern = if (cleanDate.length > 8) "dd-MM-yyyy" else "dd-MM-yy"
                            val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
                            val expDate = java.time.LocalDate.parse(cleanDate, formatter)
                            val today = java.time.LocalDate.now()
                            val days = java.time.temporal.ChronoUnit.DAYS.between(today, expDate)
                            when {
                                days < 0 -> "Expirado ($it)"
                                days == 0L -> "Hoy ($it) ⚠️"
                                days <= 3 -> "$days días ($it) ⚠️"
                                else -> "$days días ($it)"
                            }
                        } catch (e: Exception) {
                            it
                        }
                    }
                }
            ),
            ConsultaItem(
                title = "Datos",
                ussdCode = "*222*328#",
                icon = Icons.Rounded.SwapVert,
                tag = "consulta_datos",
                getValueText = { status ->
                    status?.let {
                        val totalMb = it.dataMb + it.dataLteMb
                        if (totalMb > 0) {
                            if (totalMb >= 1024) "${String.format(Locale.US, "%.2f", totalMb / 1024.0)} GB" else "$totalMb MB"
                        } else null
                    }
                }
            ),
            ConsultaItem(
                title = "Bono",
                ussdCode = "*222*266#",
                icon = Icons.Rounded.CardGiftcard,
                tag = "consulta_bono",
                getValueText = { status ->
                    status?.let {
                        if (it.bonusDataMb > 0) {
                            if (it.bonusDataMb >= 1024) "${String.format(Locale.US, "%.2f", it.bonusDataMb / 1024.0)} GB" else "${it.bonusDataMb} MB"
                        } else null
                    }
                }
            ),
            ConsultaItem(
                title = "Minutos",
                ussdCode = "*222*869#",
                icon = Icons.Rounded.PhoneInTalk,
                tag = "consulta_llamadas",
                getValueText = { status ->
                    status?.let { if (it.minutesStr.isNotEmpty()) "${it.minutesStr} Min" else null }
                }
            ),
            ConsultaItem(
                title = "Mensajes",
                ussdCode = "*222*767#",
                icon = Icons.AutoMirrored.Rounded.Message,
                tag = "consulta_mensajeria",
                getValueText = { status ->
                    status?.let { if (it.smsCount > 0) "${it.smsCount} SMS" else null }
                }
            ),
            ConsultaItem(
                title = "Plan amigos",
                ussdCode = "*222*264#",
                icon = Icons.Rounded.Group,
                tag = "consulta_plan_amigos",
                getValueText = { _ -> null }
            ),
            ConsultaItem(
                title = "Acceso Internet",
                ussdCode = "*222*468#",
                icon = Icons.Rounded.NetworkCheck,
                tag = "consulta_acceso_internet",
                getValueText = { _ -> null }
            ),
            ConsultaItem(
                title = "Historial Recargas",
                ussdCode = "*222*732#",
                icon = Icons.Rounded.History,
                tag = "consulta_historial_recargas",
                getValueText = { _ -> null }
            ),
        )
    }

    var showAll by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.section_quick_queries),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Saldo, Vigencia, Datos, Mensajes, Llamadas, Bono
        val mainItems = listOf(items[0], items[1], items[2], items[4], items[5], items[3])
        val extraItems = items.drop(6)

        for (i in mainItems.indices step 2) {
            if (i > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (i + 1 < mainItems.size) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ConsultaCard(
                        item = mainItems[i],
                        valueText = mainItems[i].getValueText(planStatus),
                        onClick = { onExecuteConsulta(mainItems[i].ussdCode, mainItems[i].title) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    ConsultaCard(
                        item = mainItems[i + 1],
                        valueText = mainItems[i + 1].getValueText(planStatus),
                        onClick = { onExecuteConsulta(mainItems[i + 1].ussdCode, mainItems[i + 1].title) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ConsultaCard(
                        item = mainItems[i],
                        valueText = mainItems[i].getValueText(planStatus),
                        onClick = { onExecuteConsulta(mainItems[i].ussdCode, mainItems[i].title) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        AnimatedVisibility(
            visible = showAll,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                for (i in extraItems.indices step 2) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (i + 1 < extraItems.size) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ConsultaCard(
                                item = extraItems[i],
                                valueText = extraItems[i].getValueText(planStatus),
                                onClick = { onExecuteConsulta(extraItems[i].ussdCode, extraItems[i].title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            ConsultaCard(
                                item = extraItems[i + 1],
                                valueText = extraItems[i + 1].getValueText(planStatus),
                                onClick = { onExecuteConsulta(extraItems[i + 1].ussdCode, extraItems[i + 1].title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ConsultaCard(
                                item = extraItems[i],
                                valueText = extraItems[i].getValueText(planStatus),
                                onClick = { onExecuteConsulta(extraItems[i].ussdCode, extraItems[i].title) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ExpressiveTextButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showAll = !showAll
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("btn_toggle_more_queries")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (showAll) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showAll) stringResource(R.string.btn_show_less_queries) else stringResource(R.string.btn_show_more_queries),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
fun ConsultaCard(
    item: ConsultaItem,
    valueText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val cardInteraction = remember { MutableInteractionSource() }
    val cardShape = rememberExpressiveMorphShape(
        defaultRadius = 24.dp,
        pressedRadius = 12.dp,
        interactionSource = cardInteraction
    )

    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = cardInteraction,
        modifier = modifier
            .testTag(item.tag)
            .defaultMinSize(minHeight = 48.dp)
            .expressivePressEffect(interactionSource = cardInteraction),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = item.ussdCode,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = "Ejecutar ${item.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
