package com.ams.megascu.ui.components

import com.ams.megascu.utils.PermissionUtils
import com.ams.megascu.utils.MobileDataUsageReader
import com.ams.megascu.utils.UsageHistoryEstimator
import java.time.format.DateTimeFormatter
import android.content.Context
import kotlin.math.roundToInt
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.ams.megascu.R
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.db.UsageHistoryEntity
import com.ams.megascu.ui.theme.CustomMonoFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UsageHistoryChart(
    usageHistory: List<UsageHistoryEntity>,
    planStatus: PlanStatusEntity? = null,
    onRequestRefresh: (() -> Unit)? = null,
    hasUsagePermission: Boolean = false,
    onRequestPermission: (() -> Unit)? = null,
    useAlternativeEstimator: Boolean = true,
    onToggleAlternativeEstimator: ((Boolean) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var systemDailyTrends by remember(planStatus?.id, planStatus?.subscriptionId) { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var effectivePermission by remember { mutableStateOf(hasUsagePermission) }

    LaunchedEffect(hasUsagePermission, planStatus?.subscriptionId, planStatus?.lastUpdatedTimestamp) {
        val granted = hasUsagePermission || PermissionUtils.hasUsageStatsPermission(context)
        effectivePermission = granted
        systemDailyTrends = if (granted) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                getDailyMobileDataUsageLast7Days(context, planStatus?.subscriptionId)
            }
        } else emptyList()
    }

    val lineHistory = remember(usageHistory, planStatus?.id, planStatus?.subscriptionId) {
        usageHistory.filter { it.simSlot == (planStatus?.id ?: 1) && it.subscriptionId == planStatus?.subscriptionId }
    }
    val (chartData, avgDailyMb, totalConsumedMb, recommendedDailyMb) = remember(lineHistory, planStatus, systemDailyTrends) {
        val dateFormat = DateTimeFormatter.ofPattern("EEE dd", Locale.getDefault())
        val estimates = UsageHistoryEstimator.dailyUsage(lineHistory, planStatus?.id ?: 1, planStatus?.subscriptionId)
        val displayData = if (systemDailyTrends.isNotEmpty()) systemDailyTrends else {
            estimates.map { dateFormat.format(it.date) to (it.consumedMb / 1024f) }
        }
        val totalMb = displayData.sumOf { it.second.toDouble() * 1024.0 }.toFloat()
        val avgMb = if (displayData.isNotEmpty()) totalMb / displayData.size else 0f
        val remainingMb = (planStatus?.dataMb ?: 0L) + (planStatus?.dataLteMb ?: 0L)
        val dataDays = planStatus?.dataDays ?: 0
        val recommendedMb = if (dataDays > 0 && remainingMb > 0) remainingMb.toFloat() / dataDays else null
        Quadruple(displayData, avgMb, totalMb, recommendedMb)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner de estado de permiso / estimador alternativo cuando falta PACKAGE_USAGE_STATS
        if (!effectivePermission || systemDailyTrends.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (useAlternativeEstimator) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (useAlternativeEstimator) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (useAlternativeEstimator) Icons.Rounded.Info else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (useAlternativeEstimator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (effectivePermission) "Estadísticas no disponibles para esta SIM" else if (useAlternativeEstimator) "Estimador Alternativo Activo" else "Permiso de Estadísticas No Concedido",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (useAlternativeEstimator) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (useAlternativeEstimator)
                                "Comparamos saldos de datos de la misma SIM: se necesitan consultas en al menos 2 días. Las recargas no cuentan como consumo. El acceso de uso permite consultar el tráfico móvil registrado por Android; algunas SIM pueden restringirlo."
                            else
                                "El permiso de uso de Android está denegado o restringido. Puedes concederlo en Ajustes del Sistema o activar el Estimador Alternativo.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 15.sp),
                            color = if (useAlternativeEstimator) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ExpressiveFilledTonalButton(
                        onClick = {
                            if (useAlternativeEstimator) {
                                onRequestPermission?.invoke() ?: PermissionUtils.openUsageAccessSettings(context)
                            } else {
                                onToggleAlternativeEstimator?.invoke(true)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (useAlternativeEstimator) "Permiso" else "Activar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Bloque 1: Tasa de Consumo
        TasaConsumoCard(
            avgDailyMb = avgDailyMb,
            recommendedDailyMb = recommendedDailyMb,
            planStatus = planStatus,
            hasUsagePermission = effectivePermission,
            useAlternativeEstimator = useAlternativeEstimator,
            onToggleAlternativeEstimator = onToggleAlternativeEstimator
        )

        // Bloque 2: Consumo y Registros (Combinado: Gráficas + Historial)
        ConsumoYRegistrosCard(
            chartData = if (effectivePermission || useAlternativeEstimator) chartData else emptyList(),
            totalConsumedMb = if (effectivePermission || useAlternativeEstimator) totalConsumedMb else 0f,
            hasPermission = effectivePermission,
            usageHistory = lineHistory,
            planStatus = planStatus
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TasaConsumoCard(
    avgDailyMb: Float,
    recommendedDailyMb: Float?,
    planStatus: PlanStatusEntity?,
    hasUsagePermission: Boolean = true,
    useAlternativeEstimator: Boolean = true,
    onToggleAlternativeEstimator: ((Boolean) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var realDataUsedToday by remember(planStatus?.id, planStatus?.subscriptionId) { mutableStateOf<Long?>(null) }
    var effectivePermission by remember { mutableStateOf(hasUsagePermission) }
    var showExplanationDialog by remember { mutableStateOf(false) }

    if (showExplanationDialog) {
        UsageAccessExplanationDialog(
            onDismiss = { showExplanationDialog = false },
            onConfirm = {
                showExplanationDialog = false
                PermissionUtils.openUsageAccessSettings(context)
            },
            onUseAlternativeEstimator = {
                onToggleAlternativeEstimator?.invoke(true)
            }
        )
    }

    LaunchedEffect(hasUsagePermission, planStatus, avgDailyMb) {
        val granted = hasUsagePermission || PermissionUtils.hasUsageStatsPermission(context)
        effectivePermission = granted
        if (granted) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val used = MobileDataUsageReader.lastDays(context, planStatus?.subscriptionId, 1)?.lastOrNull()?.bytes
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        realDataUsedToday = used
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MegasCU", "Unhandled exception", e)
                }
            }
        } else {
            realDataUsedToday = null
        }
    }

    val dataDays = planStatus?.dataDays ?: 0
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val onContainerColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val textSecondaryColor = onContainerColor.copy(alpha = 0.8f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tasa de Consumo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = onContainerColor
                    )
                    Text(
                        text = if (dataDays > 0) "Calculado para los $dataDays días restantes" else "Ritmo de uso según historial",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = textSecondaryColor
                    )
                }

                if (recommendedDailyMb != null) {
                    val isOptimal = avgDailyMb <= recommendedDailyMb
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOptimal) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOptimal) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = if (isOptimal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (isOptimal) "Consumo" else "Alerta de",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isOptimal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        lineHeight = 12.sp
                                    )
                                )
                                Text(
                                    text = if (isOptimal) "Óptimo" else "Consumo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isOptimal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        lineHeight = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            val todayMb = if (effectivePermission && realDataUsedToday != null) {
                (realDataUsedToday ?: 0L) / (1024f * 1024f)
            } else if (useAlternativeEstimator) {
                avgDailyMb
            } else {
                0f
            }
            val targetRecMb = if (recommendedDailyMb != null && recommendedDailyMb > 0f) recommendedDailyMb else 500f

            var isAnimStarted by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(1000)
                isAnimStarted = true
            }

            val animatedTodayMbInt by androidx.compose.animation.core.animateIntAsState(
                targetValue = if (isAnimStarted) todayMb.roundToInt() else 0,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                label = "animatedTodayMb"
            )

            val animatedTargetRecMbInt by androidx.compose.animation.core.animateIntAsState(
                targetValue = if (isAnimStarted) (recommendedDailyMb ?: 0f).roundToInt() else 0,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                label = "animatedTargetRecMb"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Consumo Recomendado
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Límite Diario Recomendado",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = textSecondaryColor,
                        maxLines = 2,
                        softWrap = true
                    )
                    val recText = if (recommendedDailyMb != null) {
                        val currentVal = animatedTargetRecMbInt
                        if (currentVal >= 1024) {
                            String.format(Locale.US, "%.2f\u00A0GB/día", currentVal / 1024f)
                        } else {
                            "$currentVal\u00A0MB/día"
                        }
                    } else {
                        "Sin plan activo"
                    }
                    Text(
                        text = recText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = onContainerColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Column 2: Consumo Real Hoy o Estimado
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .expressiveClick {
                            if (!hasUsagePermission) {
                                showExplanationDialog = true
                            }
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (hasUsagePermission) "Consumo Hoy" else if (useAlternativeEstimator) "Consumo Estimado" else "Consumo Hoy",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = textSecondaryColor,
                        maxLines = 2,
                        softWrap = true
                    )
                    if (hasUsagePermission) {
                        val currentVal = animatedTodayMbInt
                        val realText = if (currentVal >= 1024) {
                            String.format(Locale.US, "%.2f\u00A0GB", currentVal / 1024f)
                        } else {
                            "$currentVal\u00A0MB"
                        }
                        Text(
                            text = realText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = onContainerColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else if (useAlternativeEstimator) {
                        val currentVal = animatedTodayMbInt
                        val estText = if (currentVal >= 1024) {
                            String.format(Locale.US, "%.2f\u00A0GB", currentVal / 1024f)
                        } else {
                            "$currentVal\u00A0MB"
                        }
                        Text(
                            text = estText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = onContainerColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "Historial USSD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Text(
                            text = "Sin permiso",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "Toca p/ Activar",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            ),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            // Experimental Daily Recommendation Limit Progress Bar
            val prefs = remember(context) { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }
            var showDailyBar by remember { mutableStateOf(prefs.getBoolean("pref_show_daily_recommendation_bar", true)) }

            DisposableEffect(prefs) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "pref_show_daily_recommendation_bar") {
                        showDailyBar = p.getBoolean("pref_show_daily_recommendation_bar", true)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            if (showDailyBar) {
                val targetProgress = (todayMb / targetRecMb).coerceIn(0f, 1f)
                val animatedProgress = remember { Animatable(0f) }
                LaunchedEffect(targetProgress, isAnimStarted) {
                    if (isAnimStarted) {
                        animatedProgress.animateTo(
                            targetValue = targetProgress,
                            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
                        )
                    }
                }
                val isExceeded = todayMb > targetRecMb
                val barColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

                Spacer(modifier = Modifier.height(14.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progreso Límite Recomendado:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            ),
                            color = textSecondaryColor,
                            modifier = Modifier.weight(1f, fill = false),
                            softWrap = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val formattedToday = if (todayMb >= 1024f) String.format(Locale.US, "%.2f\u00A0GB", todayMb / 1024f) else "${todayMb.roundToInt()}\u00A0MB"
                        val formattedRec = if (targetRecMb >= 1024f) String.format(Locale.US, "%.2f\u00A0GB", targetRecMb / 1024f) else "${targetRecMb.roundToInt()}\u00A0MB"
                        Text(
                            text = "$formattedToday / $formattedRec",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp,
                                fontFamily = CustomMonoFont
                            ),
                            color = barColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(LoadingIndicatorsConfig.linearDetHeight.dp)
                            .clip(RoundedCornerShape(LoadingIndicatorsConfig.linearDetCornerRadius.dp)),
                        color = barColor,
                        trackColor = onContainerColor.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Composable
fun ConsumoYRegistrosCard(
    chartData: List<Pair<String, Float>>,
    totalConsumedMb: Float,
    hasPermission: Boolean = true,
    usageHistory: List<UsageHistoryEntity>,
    planStatus: PlanStatusEntity? = null
) {
    var isExpanded by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }
    var isLineChartMode by remember { mutableStateOf(prefs.getBoolean("pref_use_line_chart", true)) }

    DisposableEffect(context) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "pref_use_line_chart") {
                isLineChartMode = p.getBoolean("pref_use_line_chart", true)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    val spaceMonoTypeface = remember(context) {
        try {
            ResourcesCompat.getFont(context, R.font.space_mono_bold)
                ?: android.graphics.Typeface.create("monospace", android.graphics.Typeface.BOLD)
        } catch (e: Exception) {
            android.graphics.Typeface.create("monospace", android.graphics.Typeface.BOLD)
        }
    }
    LaunchedEffect(chartData, isLineChartMode, isExpanded) {
        if (isExpanded) {
            animProgress.snapTo(0f)
            kotlinx.coroutines.delay(1000)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 1400,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            )
        }
    }

    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    var isRegistrosExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val weeklyTotalGb = chartData.sumOf { it.second.toDouble() }.toFloat()
    val weeklyTotalMb = weeklyTotalGb * 1024f
    val weeklyFormatted = if (weeklyTotalGb >= 1.0f) {
        String.format(Locale.US, "%.2f GB", weeklyTotalGb)
    } else {
        String.format(Locale.US, "%.0f MB", weeklyTotalMb)
    }

    val sortedHistory = remember(usageHistory) { usageHistory.sortedByDescending { it.timestamp } }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Consumo y Registros
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .expressiveClick { isExpanded = !isExpanded }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = "Gráfico",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Consumo y Registros",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    // Section 1: Charts
                    if (chartData.isNotEmpty()) {
                        val maxGb = chartData.maxOfOrNull { it.second }?.coerceAtLeast(0.1f) ?: 1.0f

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 0.dp)
                        ) {
                            // ZONE B: Graph Area (Bars or Line Chart with floating values sitting right above bar tops / points)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(135.dp)
                            ) {
                                if (isLineChartMode) {
                                    // Smooth Line Chart with Points
                                    val primaryColor = MaterialTheme.colorScheme.primary
                                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        if (chartData.isEmpty()) return@Canvas

                                        val numPoints = chartData.size
                                        val leftMargin = 54.dp.toPx()
                                        val chartW = w - leftMargin
                                        val padX = if (numPoints > 1) chartW / (numPoints * 2f) else chartW / 2f
                                        val availW = chartW - 2 * padX
                                        val stepX = if (numPoints > 1) availW / (numPoints - 1) else 0f

                                        val maxDataMb = (maxGb * 1024f).coerceAtLeast(1f)
                                        val topRefMb = (Math.ceil((maxDataMb + 1.0) / 50.0) * 50.0).toFloat().coerceAtLeast(50f)
                                        val midRefMb = topRefMb / 2f

                                        val padYTop = 18.dp.toPx()
                                        val padYBottom = 4.dp.toPx()
                                        val chartAreaHeight = h - padYTop - padYBottom

                                        val points = chartData.mapIndexed { idx, item ->
                                            val x = leftMargin + padX + idx * stepX
                                            val itemMb = item.second * 1024f
                                            val fraction = (itemMb / topRefMb).coerceIn(0f, 1f)
                                            val animatedFraction = fraction * animProgress.value
                                            val y = h - padYBottom - (animatedFraction * chartAreaHeight)
                                            androidx.compose.ui.geometry.Offset(x, y)
                                        }

                                        // Draw numerical reference guidelines
                                        val guideColor = primaryColor.copy(alpha = 0.18f)

                                        val textPaint = android.graphics.Paint().apply {
                                            color = primaryColor.toArgb()
                                            textSize = 10.dp.toPx()
                                            alpha = (0.75f * 255).toInt()
                                            isAntiAlias = true
                                            typeface = spaceMonoTypeface
                                        }

                                        fun formatRefLabel(mb: Float): String {
                                            return if (mb >= 1024f && mb % 1024f == 0f) {
                                                String.format(java.util.Locale.US, "%.0fGB", mb / 1024f)
                                            } else if (mb >= 1024f) {
                                                String.format(java.util.Locale.US, "%.1fGB", mb / 1024f)
                                            } else {
                                                String.format(java.util.Locale.US, "%.0fMB", mb)
                                            }
                                        }

                                        val topLabel = formatRefLabel(topRefMb)
                                        val midLabel = formatRefLabel(midRefMb)
                                        val botLabel = "0MB"

                                        val fontMetrics = textPaint.fontMetrics
                                        val textYOffset = (fontMetrics.ascent + fontMetrics.descent) / 2f

                                        fun drawRefLineAndLabel(label: String, lineY: Float) {
                                            val textX = 6.dp.toPx()
                                            drawContext.canvas.nativeCanvas.drawText(label, textX, lineY - textYOffset, textPaint)

                                            val textWidth = textPaint.measureText(label)
                                            val lineStartX = textX + textWidth + 8.dp.toPx()

                                            if (lineStartX < w) {
                                                drawLine(
                                                    color = guideColor,
                                                    start = androidx.compose.ui.geometry.Offset(lineStartX, lineY),
                                                    end = androidx.compose.ui.geometry.Offset(w, lineY),
                                                    strokeWidth = 1.dp.toPx(),
                                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                                )
                                            }
                                        }

                                        val topY = padYTop
                                        val midY = padYTop + chartAreaHeight / 2f
                                        val botY = h - padYBottom

                                        drawRefLineAndLabel(topLabel, topY)
                                        drawRefLineAndLabel(midLabel, midY)
                                        drawRefLineAndLabel(botLabel, botY)

                                        if (points.size >= 2) {
                                            val linePath = androidx.compose.ui.graphics.Path().apply {
                                                moveTo(points[0].x, points[0].y)
                                                for (i in 0 until points.size - 1) {
                                                    val p1 = points[i]
                                                    val p2 = points[i + 1]
                                                    val controlX1 = (p1.x + p2.x) / 2f
                                                    val controlY1 = p1.y
                                                    val controlX2 = (p1.x + p2.x) / 2f
                                                    val controlY2 = p2.y
                                                    cubicTo(controlX1, controlY1, controlX2, controlY2, p2.x, p2.y)
                                                }
                                            }

                                            val fillPath = androidx.compose.ui.graphics.Path().apply {
                                                addPath(linePath)
                                                lineTo(points.last().x, h - padYBottom)
                                                lineTo(points.first().x, h - padYBottom)
                                                close()
                                            }

                                            // Gradient fill under line
                                            drawPath(
                                                path = fillPath,
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                                                    startY = 0f,
                                                    endY = h - padYBottom
                                                )
                                            )

                                            // Smooth line stroke
                                            drawPath(
                                                path = linePath,
                                                color = primaryColor,
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                    width = 3.dp.toPx(),
                                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                                )
                                            )
                                        }

                                        // Draw Point Dots on each day
                                        points.forEachIndexed { idx, pt ->
                                            val isSelected = selectedBarIndex == idx
                                            val outerR = if (isSelected) 8.dp.toPx() else 6.dp.toPx()
                                            val innerR = if (isSelected) 4.dp.toPx() else 3.dp.toPx()

                                            drawCircle(
                                                color = primaryColor.copy(alpha = 0.25f),
                                                radius = outerR + 3.dp.toPx(),
                                                center = pt
                                            )
                                            drawCircle(
                                                color = primaryColor,
                                                radius = outerR,
                                                center = pt
                                            )
                                            drawCircle(
                                                color = surfaceVariantColor,
                                                radius = innerR,
                                                center = pt
                                            )
                                        }
                                    }

                                    // Interactive overlay with value text sitting right above point
                                    Row(modifier = Modifier.fillMaxSize()) {
                                        Spacer(modifier = Modifier.width(54.dp))
                                        chartData.forEachIndexed { idx, (_, gbVal) ->
                                            val maxDataMb = (maxGb * 1024f).coerceAtLeast(1f)
                                            val topRefMb = (Math.ceil((maxDataMb + 1.0) / 50.0) * 50.0).toFloat().coerceAtLeast(50f)
                                            val itemMb = gbVal * 1024f
                                            val fraction = (itemMb / topRefMb).coerceIn(0f, 1f)
                                            val animatedFraction = fraction * animProgress.value
                                            val valueText = if (gbVal >= 1f) String.format(Locale.US, "%.1fGB", gbVal) else String.format(Locale.US, "%.0fMB", itemMb)
                                            val isSelected = selectedBarIndex == idx

                                            val chipAlpha by androidx.compose.animation.core.animateFloatAsState(
                                                targetValue = if (isSelected) 1f else 0f,
                                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                label = "lineChipAlpha_$idx"
                                            )
                                            val chipTranslationY by androidx.compose.animation.core.animateFloatAsState(
                                                targetValue = if (isSelected) 0f else 12f,
                                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                label = "lineChipTranslationY_$idx"
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clickable(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        selectedBarIndex = if (selectedBarIndex == idx) null else idx
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                // Value Label directly above point on line only when selected
                                                if (chipAlpha > 0.001f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .wrapContentWidth(unbounded = true)
                                                            .graphicsLayer {
                                                                alpha = chipAlpha
                                                                translationY = chipTranslationY
                                                            }
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                            .padding(horizontal = 9.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = valueText,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontFamily = CustomMonoFont
                                                            ),
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(17.dp + (animatedFraction * 113).dp))
                                            }
                                        }
                                    }
                                } else {
                                    // Stadium Bar Chart Mode
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        chartData.forEachIndexed { idx, (_, gbVal) ->
                                            val fraction = (gbVal / maxGb).coerceIn(0f, 1f)
                                            val animatedFraction = (fraction * animProgress.value).coerceIn(0f, 1f)
                                            val mbVal = gbVal * 1024f
                                            val valueText = if (gbVal >= 1f) String.format(Locale.US, "%.1fGB", gbVal) else String.format(Locale.US, "%.0fMB", mbVal)
                                            val isSelected = selectedBarIndex == idx

                                            val chipAlpha by androidx.compose.animation.core.animateFloatAsState(
                                                targetValue = if (isSelected) 1f else 0f,
                                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                label = "barChipAlpha_$idx"
                                            )
                                            val chipTranslationY by androidx.compose.animation.core.animateFloatAsState(
                                                targetValue = if (isSelected) 0f else 12f,
                                                animationSpec = androidx.compose.animation.core.tween(durationMillis = 220, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                                label = "barChipTranslationY_$idx"
                                            )

                                            val minBarHeightDp = 14.dp
                                            val maxBarHeightDp = 110.dp
                                            val currentBarHeight = minBarHeightDp + (maxBarHeightDp - minBarHeightDp) * animatedFraction

                                            val barColorStart by androidx.compose.animation.animateColorAsState(
                                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                                animationSpec = androidx.compose.animation.core.tween(280),
                                                label = "barColorStart"
                                            )
                                            val barColorEnd by androidx.compose.animation.animateColorAsState(
                                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.95f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                animationSpec = androidx.compose.animation.core.tween(280),
                                                label = "barColorEnd"
                                            )

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .padding(horizontal = 1.dp)
                                                    .clickable(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        selectedBarIndex = if (isSelected) null else idx
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Bottom
                                            ) {
                                                // Value Label sitting right above the top of the bar only when selected
                                                if (chipAlpha > 0.001f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .wrapContentWidth(unbounded = true)
                                                            .padding(bottom = 5.dp)
                                                            .graphicsLayer {
                                                                alpha = chipAlpha
                                                                translationY = chipTranslationY
                                                            }
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                            .padding(horizontal = 9.dp, vertical = 3.dp)
                                                    ) {
                                                        Text(
                                                            text = valueText,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontFamily = CustomMonoFont
                                                            ),
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .width(32.dp)
                                                        .height(currentBarHeight)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .then(
                                                            if (gbVal <= 0.001f) {
                                                                Modifier.border(
                                                                    width = 0.5.dp,
                                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                                                    shape = RoundedCornerShape(16.dp)
                                                                )
                                                            } else Modifier
                                                        )
                                                        .background(
                                                            Brush.verticalGradient(listOf(barColorStart, barColorEnd))
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ZONE C: Day Labels Row (Fixed height 22.dp, perfectly aligned dates)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLineChartMode) {
                                    Spacer(modifier = Modifier.width(54.dp))
                                }
                                val isNarrowScreen = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 360
                                val labelFontSize = if (isNarrowScreen || chartData.size > 7) 8.5.sp else 10.sp
                                chartData.forEachIndexed { idx, (dayLabel, _) ->
                                    val isSelected = selectedBarIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayLabel.replace(" ", "").replace(".", "").trim(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = labelFontSize
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Weekly Total Row
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Total últimos 7 días",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(
                                        text = weeklyFormatted,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Se necesitan consultas de datos en al menos 2 días distintos para estimar el consumo. Con el acceso de uso se consulta el tráfico registrado por Android cuando está disponible.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Section 2: Registros (History Records)
                    if (sortedHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .expressiveClick { isRegistrosExpanded = !isRegistrosExpanded }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Registros",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    Text(
                                        text = "${sortedHistory.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isRegistrosExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = if (isRegistrosExpanded) "Contraer registros" else "Expandir registros",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(visible = isRegistrosExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 2.dp)
                                ) {
                                    items(sortedHistory) { item ->
                                        val dateString = sdf.format(Date(item.timestamp))
                                        val totalMb = item.dataMb + item.dataLteMb + item.bonusDataMb
                                        val dataStr = if (totalMb >= 1024) String.format(Locale.US, "%.2f GB", totalMb / 1024f) else "$totalMb MB"
                                        val saldoStr = String.format(Locale.US, "%.2f CUP", item.balanceCup)
                                        val minutosStr = planStatus?.minutesStr?.ifEmpty { "0 min" } ?: "0 min"
                                        val smsStr = "${planStatus?.smsCount ?: 0}"

                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = dateString,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    text = "Saldo: $saldoStr • Datos: $dataStr • Minutos: $minutosStr • SMS: $smsStr",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                                )
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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

@Composable
fun EmptyUsageState(onRequestRefresh: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onRequestRefresh != null) {
                    Modifier.expressiveClick { onRequestRefresh() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin Historial Aún",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Consulta los datos en al menos 2 días distintos para estimar el consumo, o concede el acceso de uso para consultar el tráfico de Android.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getMobileDataUsageToday(context: Context, subscriptionId: Int? = null): Long =
    MobileDataUsageReader.lastDays(context, subscriptionId, 1)?.lastOrNull()?.bytes ?: 0L

fun getDailyMobileDataUsageLast7Days(context: Context, subscriptionId: Int? = null): List<Pair<String, Float>> {
    val format = DateTimeFormatter.ofPattern("EEE dd", Locale.getDefault())
    return MobileDataUsageReader.lastDays(context, subscriptionId)?.map {
        format.format(it.date) to (it.bytes / (1024f * 1024f * 1024f))
    } ?: emptyList()
}
