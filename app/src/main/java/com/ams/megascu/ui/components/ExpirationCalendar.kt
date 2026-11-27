package com.ams.megascu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
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
        shape = CircleShape,
        colors = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        } else IconButtonDefaults.filledTonalIconButtonColors()
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
        }?.takeIf { it >= 0 }?.let { PlanExpiration("Recargar", today.plusDays(it), "") }
    )
    fun eventsFor(target: YearMonth) = events.mapNotNull { event ->
        val originalMonth = YearMonth.from(event.date)
        when {
            target == originalMonth -> event
            target > originalMonth -> event.copy(
                date = target.atDay(event.date.dayOfMonth.coerceAtMost(target.lengthOfMonth()))
            )
            else -> null
        }
    }.sortedBy { it.date }
    val monthEvents = eventsFor(month)
    val byDate = monthEvents.groupBy { it.date }
    val calendarTextStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 12.dp, top = 24.dp, end = 12.dp, bottom = 12.dp)) {
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
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text(day, style = calendarTextStyle) }
                        }
                    }
                    AnimatedContent(
                        modifier = Modifier.pointerInput(month, currentMonth) {
                            var drag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { drag = 0f },
                                onDragEnd = {
                                    val threshold = 48.dp.toPx()
                                    if (drag < -threshold) requestedMonth = month.plusMonths(1)
                                    if (drag > threshold && month > currentMonth) {
                                        requestedMonth = month.minusMonths(1)
                                    }
                                }
                            ) { change, amount ->
                                change.consume()
                                drag += amount
                            }
                        },
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
                            eventsFor(gridMonth).groupBy { it.date }
                        Column {
                            repeat((start + gridMonth.lengthOfMonth() + 6) / 7) { week ->
                                Row {
                                    repeat(7) { index ->
                                        val number = week * 7 + index - start + 1
                                        Box(Modifier.weight(1f).height(58.dp), contentAlignment = Alignment.Center) {
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
                                                        .clickable(interactionSource = interaction, indication = null) {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            selectedDay = date
                                                        },
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    val colors = MaterialTheme.colorScheme
                                                    val shape = CircleShape
                                                    val background by animateColorAsState(
                                                        when {
                                                            chosen -> colors.primaryContainer
                                                            dayEvents.size == 1 -> eventColor(dayEvents.first().label).copy(alpha = 0.22f)
                                                            dayEvents.isNotEmpty() -> colors.surfaceContainerHighest
                                                            date == today -> colors.secondaryContainer
                                                            else -> Color.Transparent
                                                        },
                                                        animationSpec = tween(durationMillis = 180),
                                                        label = "calendarSelection"
                                                    )
                                                    Column(
                                                        Modifier.scale(scale)
                                                            .widthIn(min = 36.dp)
                                                            .heightIn(min = 54.dp)
                                                            .background(background, shape)
                                                            .then(if (date == today) Modifier.border(1.dp, colors.primary, shape) else Modifier)
                                                            .padding(horizontal = 3.dp, vertical = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            "$number",
                                                            style = calendarTextStyle,
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
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    if (month > currentMonth && monthEvents.isNotEmpty()) {
                        Text("Las fechas repetidas son orientativas", style = calendarTextStyle)
                    }
                    val shown = byDate[selectedDay] ?: monthEvents
                    AnimatedContent(
                        targetState = shown,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "calendarDetails"
                    ) { details ->
                        if (details.isEmpty()) {
                            Text("Sin fechas registradas para este mes", style = calendarTextStyle)
                        } else {
                            BoxWithConstraints(Modifier.fillMaxWidth()) {
                                val fontScale = LocalDensity.current.fontScale
                                val columns = if (maxWidth >= (296 * fontScale).dp) 2 else 1
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    maxItemsInEachRow = columns
                                ) {
                                    details.forEach { event ->
                                        val accent = eventColor(event.label)
                                        val label = when (event.label) {
                                            "Recargar" -> "Recarga"
                                            "Llamadas" -> "Llamada"
                                            else -> event.label
                                        }
                                        InputChip(
                                            selected = selectedDay == event.date,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDay = event.date
                                            },
                                            label = {
                                                Text(
                                                    "$label · ${event.date.format(DateTimeFormatter.ofPattern("dd MMM", spanish))}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                            },
                                            leadingIcon = {
                                                val shape = MaterialTheme.shapes.extraLarge
                                                Box(Modifier.background(accent.copy(alpha = 0.14f), shape).padding(2.dp)) {
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
                    }
                    Spacer(Modifier.height(4.dp))
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
