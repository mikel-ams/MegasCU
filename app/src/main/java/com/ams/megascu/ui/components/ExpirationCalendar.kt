package com.ams.megascu.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.service.PlanExpiration
import com.ams.megascu.service.upcomingExpirations
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ExpirationCalendarIconButton(plan: PlanStatusEntity?) {
    var open by remember { mutableStateOf(false) }
    FilledTonalIconButton(
        onClick = { open = true },
        modifier = Modifier.size(40.dp),
        shape = CircleShape
    ) {
        Icon(
            imageVector = Icons.Rounded.DateRange,
            contentDescription = "Calendario",
            modifier = Modifier.size(20.dp)
        )
    }
    if (!open) return

    val today = LocalDate.now()
    var month by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDay by remember { mutableStateOf(today) }
    val spanish = Locale.forLanguageTag("es")
    val events = plan?.upcomingExpirations(today).orEmpty() + listOfNotNull(
        plan?.nextRechargeDateStr?.takeIf { it.any(Char::isDigit) }?.let {
            EtecsaUssdParser.evaluateExpiration(it).first
        }?.takeIf { it >= 0 }?.let { PlanExpiration("Recarga disponible", today.plusDays(it), "") }
    )
    val monthEvents = events.filter { YearMonth.from(it.date) == month }.sortedBy { it.date }
    val byDate = monthEvents.groupBy { it.date }
    val start = month.atDay(1).dayOfWeek.value % 7
    val haptic = LocalHapticFeedback.current
    Dialog(onDismissRequest = { open = false }) {
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }
        val entrance by animateFloatAsState(if (entered) 1f else 0f, label = "calendarEntrance")
        Surface(
            modifier = Modifier.fillMaxWidth().graphicsLayer {
                alpha = entrance
                scaleX = 0.94f + 0.06f * entrance
                scaleY = scaleX
                translationY = 12.dp.toPx() * (1f - entrance)
            },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                Icon(Icons.Rounded.DateRange, null, Modifier.align(Alignment.CenterHorizontally),
                    tint = MaterialTheme.colorScheme.primary)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { month = month.minusMonths(1) }) {
                        Text("‹", Modifier.semantics { contentDescription = "Mes anterior" })
                    }
                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", spanish))
                        .replaceFirstChar { it.titlecase(spanish) }, Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { month = month.plusMonths(1) }) {
                        Text("›", Modifier.semantics { contentDescription = "Mes siguiente" })
                    }
                }
                Row {
                    for (day in listOf("D", "L", "M", "X", "J", "V", "S")) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(day) }
                    }
                }
                repeat(6) { week ->
                    Row {
                        repeat(7) { index ->
                            val number = week * 7 + index - start + 1
                            Box(Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                                if (number in 1..month.lengthOfMonth()) {
                                    val date = month.atDay(number)
                                    val dayEvents = byDate[date].orEmpty()
                                    val interaction = remember { MutableInteractionSource() }
                                    val pressed by interaction.collectIsPressedAsState()
                                    val scale by animateFloatAsState(if (pressed) 0.86f else 1f, label = "calendarPress")
                                    val chosen = selectedDay == date
                                    Column(
                                        Modifier.fillMaxSize().scale(scale)
                                            .semantics {
                                                selected = chosen
                                                contentDescription = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", spanish)) +
                                                    (if (date == today) ", hoy" else "") +
                                                    dayEvents.joinToString("") { ", ${it.label}" }
                                            }
                                            .clickable(interactionSource = interaction, indication = ripple()) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDay = date
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(Modifier.size(32.dp)
                                            .background(if (chosen) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                            .then(if (date == today) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                                            contentAlignment = Alignment.Center) {
                                            Text("$number", color = if (chosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            dayEvents.forEach { Box(Modifier.size(5.dp).background(eventColor(it.label), CircleShape)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                val shown = byDate[selectedDay] ?: monthEvents
                if (shown.isEmpty()) Text("Sin fechas registradas para este mes")
                shown.forEach {
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(eventColor(it.label), CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("${it.label} · ${it.date.format(DateTimeFormatter.ofPattern("d MMM", spanish))}")
                    }
                }
                TextButton(onClick = { open = false }, modifier = Modifier.align(Alignment.End)) { Text("Cerrar") }
            }
        }
    }
}

private fun eventColor(label: String): Color = when (label) {
    "Datos" -> Color(0xFF3593ED)
    "Llamadas" -> Color(0xFFEE657A)
    "SMS" -> Color(0xFFEAB415)
    else -> Color(0xFF27B96C)
}
