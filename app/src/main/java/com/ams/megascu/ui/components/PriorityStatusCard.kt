package com.ams.megascu.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ams.megascu.data.db.PlanStatusEntity
import java.util.Locale

data class SimStatusState(
    val hideSelector: Boolean = false,
    val isSim1Active: Boolean = true,
    val isSim2Active: Boolean = true
)

@SuppressLint("MissingPermission")
fun computeSimStatusState(context: Context): SimStatusState {
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    
    val phoneCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        telephonyManager?.activeModemCount ?: 1
    } else {
        @Suppress("DEPRECATION")
        telephonyManager?.phoneCount ?: 1
    }
    
    var sim1Active = true
    var sim2Active = true
    var activeCount = phoneCount
    
    try {
        if (subscriptionManager != null) {
            val activeList = subscriptionManager.activeSubscriptionInfoList
            if (activeList != null) {
                activeCount = activeList.size
                var found1 = false
                var found2 = false
                for (sub in activeList) {
                    if (sub.simSlotIndex == 0) found1 = true
                    if (sub.simSlotIndex == 1) found2 = true
                }
                sim1Active = found1
                sim2Active = found2
            } else {
                activeCount = 0
                sim1Active = false
                sim2Active = false
            }
        }
    } catch (e: Exception) {
        // Permission fallback
    }
    
    val hideSelector = (phoneCount <= 1) || (activeCount < 2) || (!sim1Active || !sim2Active)
    return SimStatusState(
        hideSelector = hideSelector,
        isSim1Active = sim1Active,
        isSim2Active = sim2Active
    )
}

@Composable
fun rememberSimStatusState(): SimStatusState {
    val context = LocalContext.current.applicationContext
    var state by remember { mutableStateOf(computeSimStatusState(context)) }

    DisposableEffect(context) {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        
        val listener = object : SubscriptionManager.OnSubscriptionsChangedListener() {
            override fun onSubscriptionsChanged() {
                state = computeSimStatusState(context)
            }
        }

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: android.content.Intent?) {
                state = computeSimStatusState(context)
            }
        }

        try {
            subscriptionManager?.addOnSubscriptionsChangedListener(listener)
        } catch (e: Exception) {
            // Ignored
        }

        val filter = android.content.IntentFilter().apply {
            addAction("android.intent.action.SIM_STATE_CHANGED")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                addAction(SubscriptionManager.ACTION_DEFAULT_SUBSCRIPTION_CHANGED)
            }
        }
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            // Ignored
        }

        state = computeSimStatusState(context)

        onDispose {
            try {
                subscriptionManager?.removeOnSubscriptionsChangedListener(listener)
            } catch (e: Exception) {
                // Ignored
            }
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    return state
}

@Composable
fun AnimatedAnnotatedText(
    targetText: androidx.compose.ui.text.AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = targetText,
        transitionSpec = {
            (slideInVertically { height -> height / 2 } + fadeIn(tween(300)))
                .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(300)))
        },
        label = "AnimatedAnnotatedText"
    ) { animatedText ->
        Text(
            text = animatedText,
            style = style,
            color = color,
            maxLines = 1,
            softWrap = false,
            modifier = modifier
        )
    }
}

@Composable
fun ExpressiveDaysChip(
    days: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    suffix: String = "d"
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = "$days$suffix",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun getDaysBadgeColors(
    days: Int,
    isDark: Boolean,
    defaultBg: Color,
    defaultText: Color
): Pair<Color, Color> {
    return when {
        days <= 0 -> {
            (if (isDark) Color(0x33FF8A80) else Color(0x22B91C1C)) to (if (isDark) Color(0xFFFF8A80) else Color(0xFFB91C1C))
        }
        days in 1..3 -> {
            (if (isDark) Color(0x33FFD54F) else Color(0x24B45309)) to (if (isDark) Color(0xFFFFD54F) else Color(0xFFB45309))
        }
        else -> defaultBg to defaultText
    }
}

@Composable
fun PriorityStatusCard(
    planStatus: PlanStatusEntity?,
    selectedSimSlot: Int = 1,
    dualSimEnabled: Boolean = false,
    onSelectSimSlot: (Int) -> Unit = {},
    onExecuteConsulta: (code: String, title: String?) -> Unit = { _, _ -> },
    onCardClick: () -> Unit = {}
) {
    val showCard = planStatus != null
    val simStatus = rememberSimStatusState()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE) }
    var primaryCardFirstField by remember {
        mutableStateOf(prefs.getString("pref_primary_card_first_field", "SALDO") ?: "SALDO")
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "pref_primary_card_first_field") {
                primaryCardFirstField = p.getString("pref_primary_card_first_field", "SALDO") ?: "SALDO"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(simStatus.isSim1Active, simStatus.isSim2Active, selectedSimSlot) {
        if (selectedSimSlot == 1 && !simStatus.isSim1Active && simStatus.isSim2Active) {
            onSelectSimSlot(2)
        } else if (selectedSimSlot == 2 && !simStatus.isSim2Active && simStatus.isSim1Active) {
            onSelectSimSlot(1)
        }
    }

    AnimatedVisibility(
        visible = showCard,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) +
                scaleIn(
                    initialScale = 0.88f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
    ) {
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val containerColor = MaterialTheme.colorScheme.primaryContainer
        val contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        val secondaryContentColor = if (isDark) Color.White.copy(alpha = 0.9f) else if (containerColor == Color(0xFFE2D1FF) || containerColor == Color(0xFFEDE9FE)) {
            Color(0xFF1E005A) // High-contrast deep dark violet
        } else {
            contentColor.copy(alpha = 0.85f)
        }
        val badgeBgColor = contentColor.copy(alpha = 0.15f)

        val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
        val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
        val isNarrowOrLargeFont = screenWidthDp < 360 || fontScale > 1.15f

        val dataDays = planStatus?.dataDays ?: 0
        // WCAG AA (>= 4.5:1) compliant status colors
        val (daysBadgeBg, daysBadgeText) = when {
            dataDays <= 0 && planStatus != null -> {
                (if (isDark) Color(0x33FF8A80) else Color(0x22B91C1C)) to (if (isDark) Color(0xFFFF8A80) else Color(0xFFB91C1C))
            }
            dataDays in 1..3 -> {
                (if (isDark) Color(0x33FFD54F) else Color(0x24B45309)) to (if (isDark) Color(0xFFFFD54F) else Color(0xFFB45309))
            }
            else -> badgeBgColor to contentColor
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
 
                    val balanceText = buildAnnotatedString {
                        append(String.format(Locale.US, "%.2f", planStatus?.balanceCup ?: 0.0))
                        withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) {
                            append("\u00A0CUP")
                        }
                    }

                    val totalDataMb = (planStatus?.dataMb ?: 0L) + (planStatus?.dataLteMb ?: 0L)
                    val dataFormatted = if (totalDataMb >= 1024L) {
                        buildAnnotatedString {
                            append(String.format(Locale.US, "%.2f", totalDataMb.toFloat() / 1024f))
                            withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0GB") }
                        }
                    } else {
                        buildAnnotatedString {
                            append(String.format(Locale.US, "%.0f", totalDataMb.toFloat()))
                            withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0MB") }
                        }
                    }

                    val bonusMb = planStatus?.bonusDataMb ?: 0L
                    val bonusFormatted = if (bonusMb >= 1024L) {
                        buildAnnotatedString {
                            append(String.format(Locale.US, "%.2f", bonusMb.toFloat() / 1024f))
                            withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0GB") }
                        }
                    } else {
                        buildAnnotatedString {
                            append(String.format(Locale.US, "%.0f", bonusMb.toFloat()))
                            withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0MB") }
                        }
                    }

                    if (primaryCardFirstField == "DATOS") {
                        // ROW 1: Datos Disponibles (Featured Large Display)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = "Datos Disponibles",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = secondaryContentColor
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AnimatedAnnotatedText(
                                    targetText = dataFormatted,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = contentColor,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                if ((planStatus?.dataDays ?: 0) > 0) {
                                    Spacer(modifier = Modifier.width(if (isNarrowOrLargeFont) 4.dp else 6.dp))
                                    ExpressiveDaysChip(
                                        days = planStatus?.dataDays ?: 0,
                                        containerColor = daysBadgeBg,
                                        contentColor = daysBadgeText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // ROW 2: Saldo Principal (Left) & Bono Datos .CU (Right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left: Saldo Principal (*222#)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "Saldo Principal",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = secondaryContentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                AnimatedAnnotatedText(
                                    targetText = balanceText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = contentColor
                                )
                            }

                            // Right: Bono Datos .CU (*222*266#)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 2.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Bono Datos .CU",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = secondaryContentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedAnnotatedText(
                                        targetText = bonusFormatted,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = contentColor,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if ((planStatus?.dataDays ?: 0) > 0 && bonusMb > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        ExpressiveDaysChip(
                                            days = planStatus?.dataDays ?: 0,
                                            containerColor = daysBadgeBg,
                                            contentColor = daysBadgeText
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ROW 1: Saldo Principal (Alone on its line)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = "Saldo Principal",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = secondaryContentColor
                            )
                            AnimatedAnnotatedText(
                                targetText = balanceText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = contentColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // ROW 2: Datos Disponibles (Left) & Bono Datos .CU (Right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left: Datos Disponibles (*222*328#)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "Datos Disponibles",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isNarrowOrLargeFont) 11.5.sp else 13.sp
                                    ),
                                    color = secondaryContentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedAnnotatedText(
                                        targetText = dataFormatted,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = if (isNarrowOrLargeFont) 15.sp else 17.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = contentColor,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if ((planStatus?.dataDays ?: 0) > 0) {
                                        Spacer(modifier = Modifier.width(if (isNarrowOrLargeFont) 3.dp else 4.dp))
                                        ExpressiveDaysChip(
                                            days = planStatus?.dataDays ?: 0,
                                            containerColor = daysBadgeBg,
                                            contentColor = daysBadgeText
                                        )
                                    }
                                }
                            }
                            
                            // Right: Bono Datos .CU (*222*266#)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .padding(vertical = 2.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Bono Datos .CU",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isNarrowOrLargeFont) 11.5.sp else 13.sp
                                    ),
                                    color = secondaryContentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimatedAnnotatedText(
                                        targetText = bonusFormatted,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = if (isNarrowOrLargeFont) 15.sp else 17.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = contentColor,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    if ((planStatus?.dataDays ?: 0) > 0 && bonusMb > 0) {
                                        Spacer(modifier = Modifier.width(if (isNarrowOrLargeFont) 3.dp else 4.dp))
                                        ExpressiveDaysChip(
                                            days = planStatus?.dataDays ?: 0,
                                            containerColor = daysBadgeBg,
                                            contentColor = daysBadgeText
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ROW 3: Llamadas (Voz) (Left) & Mensajes (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left: Llamadas (Voz) (*222*869#)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = "Llamadas (Voz)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = secondaryContentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            val minText = planStatus?.minutesStr.orEmpty().ifBlank { "00:00:00" }
                            val callsText = buildAnnotatedString {
                                append(minText)
                                withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0Min") }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AnimatedAnnotatedText(
                                    targetText = callsText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = contentColor,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                val minDays = planStatus?.minutesDays ?: 0
                                if (minDays > 0) {
                                    val (minBadgeBg, minBadgeText) = getDaysBadgeColors(minDays, isDark, badgeBgColor, contentColor)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    ExpressiveDaysChip(
                                        days = minDays,
                                        containerColor = minBadgeBg,
                                        contentColor = minBadgeText
                                    )
                                }
                            }
                        }

                        // Right: Mensajes (*222*767#)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .padding(vertical = 2.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Mensajes",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = secondaryContentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                            val smsText = buildAnnotatedString {
                                append("${planStatus?.smsCount ?: 0}")
                                withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)) { append("\u00A0SMS") }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AnimatedAnnotatedText(
                                    targetText = smsText,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = contentColor,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                val smsDays = planStatus?.smsDays ?: 0
                                if (smsDays > 0) {
                                    val (smsBadgeBg, smsBadgeText) = getDaysBadgeColors(smsDays, isDark, badgeBgColor, contentColor)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    ExpressiveDaysChip(
                                        days = smsDays,
                                        containerColor = smsBadgeBg,
                                        contentColor = smsBadgeText
                                    )
                                }
                            }
                        }
                    }
                    
                    // Footer: Date on the left + Recharge Days chip next to date
                    if (!planStatus?.nextRechargeDateStr.isNullOrBlank() || (planStatus?.nextRechargeDays ?: 0) > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = "Recarga",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val isRechargeNow = planStatus?.nextRechargeDateStr == "Puede recargar saldo" ||
                                    ((planStatus?.nextRechargeDays ?: 1) <= 0 && !planStatus?.nextRechargeDateStr.isNullOrBlank()) ||
                                    (planStatus?.nextRechargeDateStr?.isNotBlank() == true && !planStatus.nextRechargeDateStr.contains("-") && !planStatus.nextRechargeDateStr.contains("/") && !planStatus.nextRechargeDateStr.contains(".")) ||
                                    (planStatus?.nextRechargeDateStr?.isNotBlank() == true && com.ams.megascu.data.ussd.EtecsaUssdParser.evaluateExpiration(planStatus.nextRechargeDateStr).first?.let { it <= 0 } == true)
                            val rechargeLabel = if (isRechargeNow) "Puede recargar saldo" else "Puede recargar el ${planStatus?.nextRechargeDateStr}"
                            Text(
                                text = rechargeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = secondaryContentColor
                                )
                            )

                            val rechargeDays = planStatus?.nextRechargeDays ?: 0
                            if (rechargeDays > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                ExpressiveDaysChip(
                                    days = rechargeDays,
                                    containerColor = badgeBgColor,
                                    contentColor = contentColor
                                )
                            }
                        }
                    }
                }
                // Dual SIM Selector Button (Top Right)
                if (dualSimEnabled && !simStatus.hideSelector) {
                    val simInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = rememberExpressiveMorphShape(
                            defaultRadius = 12.dp,
                            pressedRadius = 6.dp,
                            interactionSource = simInteraction
                        ),
                        color = badgeBgColor,
                        onClick = {
                            val nextSim = if (selectedSimSlot == 1) 2 else 1
                            onSelectSimSlot(nextSim)
                        },
                        interactionSource = simInteraction,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .zIndex(10f)
                            .expressivePressEffect(interactionSource = simInteraction)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SimCard,
                                contentDescription = "Doble SIM",
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SIM $selectedSimSlot",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePriorityStatusCard(
    planStatus: PlanStatusEntity?,
    selectedSimSlot: Int = 1,
    dualSimEnabled: Boolean = false,
    hasPurchaseAlert: Boolean = false,
    onSelectSimSlot: (Int) -> Unit = {},
    onExecuteConsulta: (code: String, title: String?) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    onOpenPlanes: () -> Unit = {}
) {
    val simStatus = rememberSimStatusState()
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE) }
    var primaryCardFirstField by remember {
        mutableStateOf(prefs.getString("pref_primary_card_first_field", "SALDO") ?: "SALDO")
    }

    DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "pref_primary_card_first_field") {
                primaryCardFirstField = p.getString("pref_primary_card_first_field", "SALDO") ?: "SALDO"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(simStatus.isSim1Active, simStatus.isSim2Active, selectedSimSlot) {
        if (selectedSimSlot == 1 && !simStatus.isSim1Active && simStatus.isSim2Active) {
            onSelectSimSlot(2)
        } else if (selectedSimSlot == 2 && !simStatus.isSim2Active && simStatus.isSim1Active) {
            onSelectSimSlot(1)
        }
    }

    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryContentColor = contentColor.copy(alpha = 0.9f)
    val badgeBgColor = contentColor.copy(alpha = 0.18f)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val screenWidthDp = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp
    val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    val isNarrowOrLargeFont = screenWidthDp < 360 || fontScale > 1.15f

    val dataDays = planStatus?.dataDays ?: 0
    val (daysBadgeBg, daysBadgeText) = when {
        dataDays <= 0 && planStatus != null -> {
            (if (isDark) Color(0x33FF8A80) else Color(0x22B91C1C)) to (if (isDark) Color(0xFFFF8A80) else Color(0xFFB91C1C))
        }
        dataDays in 1..3 -> {
            (if (isDark) Color(0x33FFD54F) else Color(0x24B45309)) to (if (isDark) Color(0xFFFFD54F) else Color(0xFFB45309))
        }
        else -> badgeBgColor to contentColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with SIM selector if enabled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBgColor
                ) {
                    Text(
                        text = "MODO SIMPLE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        ),
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                if (dualSimEnabled && !simStatus.hideSelector) {
                    val simBadgeInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = rememberExpressiveMorphShape(
                            defaultRadius = 12.dp,
                            pressedRadius = 6.dp,
                            interactionSource = simBadgeInteraction
                        ),
                        color = badgeBgColor,
                        modifier = Modifier
                            .expressivePressEffect(interactionSource = simBadgeInteraction)
                            .clickable(
                                interactionSource = simBadgeInteraction,
                                indication = null
                            ) {
                                val nextSim = if (selectedSimSlot == 1) 2 else 1
                                onSelectSimSlot(nextSim)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SimCard,
                                contentDescription = "SIM",
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIM $selectedSimSlot",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = contentColor
                            )
                        }
                    }
                }
            }

            val saldoComposable: @Composable () -> Unit = {
                // SALDO PRINCIPAL
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AttachMoney,
                                contentDescription = "Saldo",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(2.5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saldo Principal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = secondaryContentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f\u00A0CUP", planStatus?.balanceCup ?: 0.0),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            val datosComposable: @Composable () -> Unit = {
                // DATOS DISPONIBLES
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapVert,
                                    contentDescription = "Datos",
                                    tint = secondaryContentColor,
                                    modifier = Modifier.size(18.dp).clip(RoundedCornerShape(2.5.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Datos Disponibles",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = secondaryContentColor
                                )
                            }

                            val dataDays = planStatus?.dataDays ?: 0
                            if (dataDays > 0) {
                                ExpressiveDaysChip(
                                    days = dataDays,
                                    containerColor = daysBadgeBg,
                                    contentColor = daysBadgeText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        val totalDataMb = (planStatus?.dataMb ?: 0L) + (planStatus?.dataLteMb ?: 0L)
                        val dataFormatted = if (totalDataMb >= 1024L) {
                            String.format(Locale.US, "%.2f\u00A0GB", totalDataMb.toFloat() / 1024f)
                        } else {
                            String.format(Locale.US, "%.0f\u00A0MB", totalDataMb.toFloat())
                        }

                        Text(
                            text = dataFormatted,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (primaryCardFirstField == "DATOS") {
                datosComposable()
                saldoComposable()
            } else {
                saldoComposable()
                datosComposable()
            }

            // 3. BONO .CU (If available)
            val bonusMb = planStatus?.bonusDataMb ?: 0L
            if (bonusMb > 0L) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CardGiftcard,
                                contentDescription = "Bono",
                                tint = secondaryContentColor,
                                modifier = Modifier.size(18.dp).clip(RoundedCornerShape(2.5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bono Datos .CU",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = secondaryContentColor
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val bonusFormatted = if (bonusMb >= 1024L) {
                            String.format(Locale.US, "%.2f\u00A0GB", bonusMb.toFloat() / 1024f)
                        } else {
                            String.format(Locale.US, "%.0f\u00A0MB", bonusMb.toFloat())
                        }
                        Text(
                            text = bonusFormatted,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 4. VOZ & SMS (Dividido en dos tarjetas independientes)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarjeta 1: Minutos (Llamadas)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = "Voz",
                                    tint = secondaryContentColor,
                                    modifier = Modifier.size(16.dp).clip(RoundedCornerShape(2.5.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Llamadas",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = secondaryContentColor
                                )
                            }
                            val minutesDays = planStatus?.minutesDays ?: 0
                            if (minutesDays > 0) {
                                val (minBadgeBg, minBadgeText) = getDaysBadgeColors(minutesDays, isDark, badgeBgColor, contentColor)
                                ExpressiveDaysChip(
                                    days = minutesDays,
                                    containerColor = minBadgeBg,
                                    contentColor = minBadgeText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val minText = planStatus?.minutesStr.orEmpty().ifBlank { "00:00:00" }
                        Text(
                            text = minText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Tarjeta 2: Mensajes (SMS)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = contentColor.copy(alpha = 0.08f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Sms,
                                    contentDescription = "SMS",
                                    tint = secondaryContentColor,
                                    modifier = Modifier.size(16.dp).clip(RoundedCornerShape(2.5.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mensajes",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = secondaryContentColor
                                )
                            }
                            val smsDays = planStatus?.smsDays ?: 0
                            if (smsDays > 0) {
                                val (smsBadgeBg, smsBadgeText) = getDaysBadgeColors(smsDays, isDark, badgeBgColor, contentColor)
                                ExpressiveDaysChip(
                                    days = smsDays,
                                    containerColor = smsBadgeBg,
                                    contentColor = smsBadgeText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val smsCount = planStatus?.smsCount ?: 0
                        Text(
                            text = "$smsCount\u00A0SMS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = contentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // 5. FECHA LÍMITE DE RECARGA
            if (!planStatus?.nextRechargeDateStr.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DateRange,
                        contentDescription = "Recarga",
                        tint = secondaryContentColor,
                        modifier = Modifier.size(18.dp).clip(RoundedCornerShape(2.5.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val isRechargeNow = planStatus?.nextRechargeDateStr == "Puede recargar saldo" ||
                            ((planStatus?.nextRechargeDays ?: 1) <= 0 && !planStatus?.nextRechargeDateStr.isNullOrBlank()) ||
                            (planStatus?.nextRechargeDateStr?.isNotBlank() == true && !planStatus.nextRechargeDateStr.contains("-") && !planStatus.nextRechargeDateStr.contains("/") && !planStatus.nextRechargeDateStr.contains(".")) ||
                            (planStatus?.nextRechargeDateStr?.isNotBlank() == true && com.ams.megascu.data.ussd.EtecsaUssdParser.evaluateExpiration(planStatus.nextRechargeDateStr).first?.let { it <= 0 } == true)
                    val rechargeLabel = if (isRechargeNow) "Puede recargar saldo" else "Puede recargar el ${planStatus?.nextRechargeDateStr}"
                    Text(
                        text = rechargeLabel,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = secondaryContentColor
                        )
                    )
                }
            }

            // Action Button to Open Purchase Window
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier.fillMaxWidth()
            ) {
                ExpressiveButton(
                    onClick = onOpenPlanes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = contentColor,
                        contentColor = containerColor
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingCart,
                            contentDescription = "Comprar",
                            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(2.5.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comprar Paquetes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                if (hasPurchaseAlert) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp, end = 8.dp)
                            .size(13.dp)
                            .background(Color(0xFFE53935), CircleShape)
                            .border(2.dp, contentColor, CircleShape)
                            .testTag("simple_mode_purchase_alert_badge_dot")
                    )
                }
            }
        }
    }
}