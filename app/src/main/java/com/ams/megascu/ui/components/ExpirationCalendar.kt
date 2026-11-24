package com.ams.megascu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ams.megascu.R
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.service.PlanExpiration
import com.ams.megascu.service.upcomingExpirations
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ExpirationCalendarIconButton(plan: PlanStatusEntity?, onOpenChanged: (Boolean) -> Unit) {
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

    val reportOpen by rememberUpdatedState(onOpenChanged)
    DisposableEffect(Unit) {
        reportOpen(true)
        onDispose { reportOpen(false) }
    }

    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    var requestedMonth by remember { mutableStateOf(currentMonth) }
    val month = maxOf(requestedMonth, currentMonth)
    var selectedDay by remember { mutableStateOf(today) }
    val spanish = Locale.forLanguageTag("es")
    val events = plan?.upcomingExpirations(today).orEmpty() + listOfNotNull(
        plan?.nextRechargeDateStr?.takeIf { it.any(Char::isDigit) }?.let {
            EtecsaUssdParser.evaluateExpiration(it).first
        }?.takeIf { it >= 0 }?.let { PlanExpiration("Recarga disponible", today.plusDays(it), "") }
    )
    val monthEvents = events.filter { YearMonth.from(it.date) == month }.sortedBy { it.date }
    val byDate = monthEvents.groupBy { it.date }
    val haptic = LocalHapticFeedback.current
    Dialog(
        onDismissRequest = { open = false },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            var entered by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { entered = true }
            val entrance by animateFloatAsState(if (entered) 1f else 0f, label = "calendarEntrance")
            Card(
                modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth().graphicsLayer {
                    alpha = entrance
                    scaleX = 0.94f + 0.06f * entrance
                    scaleY = scaleX
                    translationY = 12.dp.toPx() * (1f - entrance)
                },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp)) {
                    Icon(Icons.Rounded.DateRange, null, Modifier.align(Alignment.CenterHorizontally),
                        tint = MaterialTheme.colorScheme.primary)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            enabled = month > currentMonth,
                            onClick = { if (month > currentMonth) requestedMonth = month.minusMonths(1) }
                        ) {
                            Icon(Icons.Rounded.ChevronLeft, "Mes anterior")
                        }
                        AnimatedContent(
                            targetState = month,
                            modifier = Modifier.weight(1f),
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "calendarTitle"
                        ) { titleMonth ->
                            Text(
                                titleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", spanish))
                                    .replaceFirstChar { it.titlecase(spanish) },
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(onClick = { requestedMonth = month.plusMonths(1) }) {
                            Icon(Icons.Rounded.ChevronRight, "Mes siguiente")
                        }
                    }
                    Row {
                        for (day in listOf("D", "L", "M", "X", "J", "V", "S")) {
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(day) }
                        }
                    }
                    AnimatedContent(
                        targetState = month,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally { direction * it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { -direction * it / 4 } + fadeOut())
                        },
                        label = "calendarGrid"
                    ) { gridMonth ->
                        val start = gridMonth.atDay(1).dayOfWeek.value % 7
                        val gridEvents = if (gridMonth == month) byDate else
                            events.filter { YearMonth.from(it.date) == gridMonth }.groupBy { it.date }
                        Column {
                            repeat(6) { week ->
                                Row {
                                    repeat(7) { index ->
                                        val number = week * 7 + index - start + 1
                                        Box(Modifier.weight(1f).height(64.dp), contentAlignment = Alignment.Center) {
                                            if (number in 1..gridMonth.lengthOfMonth()) {
                                                val date = gridMonth.atDay(number)
                                                val dayEvents = gridEvents[date].orEmpty()
                                                val interaction = remember { MutableInteractionSource() }
                                                val pressed by interaction.collectIsPressedAsState()
                                                val scale by animateFloatAsState(if (pressed) 0.86f else 1f, label = "calendarPress")
                                                val chosen = selectedDay == date
                                                Column(
                                                    Modifier.fillMaxSize()
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
                                                    val colors = MaterialTheme.colorScheme
                                                    val shape = MaterialTheme.shapes.extraLarge
                                                    val background by animateColorAsState(
                                                        when {
                                                            chosen -> colors.primaryContainer
                                                            dayEvents.size == 1 -> eventColor(dayEvents.first().label).copy(alpha = 0.22f)
                                                            dayEvents.isNotEmpty() -> colors.surfaceContainerHighest
                                                            else -> Color.Transparent
                                                        },
                                                        label = "calendarSelection"
                                                    )
                                                    Column(
                                                        Modifier.scale(scale)
                                                            .then(
                                                                if (dayEvents.isEmpty()) Modifier else
                                                                    Modifier.widthIn(min = 36.dp).heightIn(min = 54.dp)
                                                            )
                                                            .background(background, shape)
                                                            .then(if (date == today) Modifier.border(1.dp, colors.primary, shape) else Modifier)
                                                            .padding(horizontal = 3.dp, vertical = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            "$number",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            color = if (chosen) colors.onPrimaryContainer else colors.onSurface
                                                        )
                                                        dayEvents.chunked(2).forEach { group ->
                                                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                                group.forEach { event ->
                                                                    Icon(eventIcon(event.label), null, Modifier.size(if (dayEvents.size == 1) 20.dp else 13.dp), eventColor(event.label))
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
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    val shown = byDate[selectedDay] ?: monthEvents
                    AnimatedContent(
                        targetState = shown,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "calendarDetails"
                    ) { details ->
                        if (details.isEmpty()) {
                            Text("Sin fechas registradas para este mes")
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                details.forEach { event ->
                                    val accent = eventColor(event.label)
                                    InputChip(
                                        selected = selectedDay == event.date,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedDay = event.date
                                        },
                                        label = {
                                            Text("${event.label} · ${event.date.format(DateTimeFormatter.ofPattern("dd MMM", spanish))}")
                                        },
                                        leadingIcon = {
                                            val shape = MaterialTheme.shapes.extraLarge
                                            Box(Modifier.background(accent.copy(alpha = 0.14f), shape).padding(4.dp)) {
                                                Icon(eventIcon(event.label), null, Modifier.size(14.dp), accent)
                                            }
                                        },
                                        shape = MaterialTheme.shapes.extraLarge,
                                        colors = InputChipDefaults.inputChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    ExpressiveButton(
                        onClick = { open = false },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_close),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
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

private fun eventIcon(label: String) = when (label) {
    "Datos" -> Icons.Rounded.DataUsage
    "Bono" -> Icons.Rounded.CardGiftcard
    "Llamadas" -> Icons.Rounded.Call
    "SMS" -> Icons.Rounded.Sms
    else -> Icons.Rounded.AttachMoney
}
