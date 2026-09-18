package com.ams.megascu.ui.components

import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.*
import androidx.compose.animation.*


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ams.megascu.ui.theme.CyberCyan
import com.ams.megascu.ui.theme.ElectricIndigo
import com.ams.megascu.ui.theme.EmeraldGreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild

import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.luminance

import com.ams.megascu.BuildConfig
import com.ams.megascu.utils.GitHubUpdateChecker
import com.ams.megascu.utils.UpdateCheckResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    smsAlertsEnabled: Boolean,
    dailyLimitAlertsEnabled: Boolean = true,
    dataThresholdAlertEnabled: Boolean = true,
    dataThresholdPercent: Int = 80,
    expirationAlertDays: Int = 5,
    secondExpirationAlertEnabled: Boolean = false,
    secondExpirationAlertDays: Int = 2,
    rechargeAlertEnabled: Boolean = true,
    isSimpleMode: Boolean = false,
    useMonospace: Boolean,
    useDynamicColors: Boolean,
    disableBlurEffects: Boolean = false,
    paletteStyle: String = "Tonal Spot",
    dualSimEnabled: Boolean = false,
    themeMode: String = "SYSTEM",
    isAmoledMode: Boolean = false,
    syncIntervalHours: Int = 6,
    lowDataThresholdMb: Long = 200L,
    onToggleSmsAlerts: (Boolean) -> Unit,
    onToggleDailyLimitAlerts: (Boolean) -> Unit = {},
    onToggleDataThresholdAlerts: (Boolean) -> Unit = {},
    onSelectDataThresholdPercent: (Int) -> Unit = {},
    onSelectExpirationAlertDays: (Int) -> Unit = {},
    onToggleSecondExpirationAlert: (Boolean) -> Unit = {},
    onSelectSecondExpirationAlertDays: (Int) -> Unit = {},
    onToggleRechargeAlerts: (Boolean) -> Unit = {},
    onToggleSimpleMode: (Boolean) -> Unit = {},
    onToggleMonospace: (Boolean) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onToggleDisableBlurEffects: (Boolean) -> Unit = {},
    onSelectPaletteStyle: (String) -> Unit = {},
    onToggleDualSim: (Boolean) -> Unit = {},
    onSelectThemeMode: (String) -> Unit = {},
    onToggleAmoledMode: (Boolean) -> Unit = {},
    onSelectSyncInterval: (Int) -> Unit = {},
    onSelectLowDataThreshold: (Long) -> Unit = {},
    onClearData: () -> Unit,
    onResetCoachMark: () -> Unit = {},
    onStartTutorial: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onRequestPermissions: () -> Unit,
    onRequestNotificationPermission: () -> Unit = {},
    onRequestUsageStatsPermission: () -> Unit = {},
    hasPhonePermission: Boolean,
    hasNotificationPermission: Boolean,
    hasUsageStatsPermission: Boolean = false,
    useAlternativeUsageEstimator: Boolean = true,
    onToggleAlternativeUsageEstimator: (Boolean) -> Unit = {},
    onExecuteConsulta: ((String, String) -> Unit)? = null,
    onExecutePurchase: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    onProgress: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    var localExpirationAlertDays by remember(expirationAlertDays) { mutableStateOf(expirationAlertDays) }
    var localSecondExpirationAlertDays by remember(secondExpirationAlertDays) { mutableStateOf(secondExpirationAlertDays) }
    var showExpirationConfirmDialog by remember { mutableStateOf(false) }
    var lastSavedPrimaryDays by remember { mutableStateOf(expirationAlertDays) }
    var lastSavedSecondDays by remember { mutableStateOf(secondExpirationAlertDays) }

    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var autoUpdateCheck by remember { mutableStateOf(prefs.getBoolean(GitHubUpdateChecker.PREF_AUTO_UPDATE_CHECK, true)) }
    var lastCheckTimeStr by remember { mutableStateOf(GitHubUpdateChecker.getFormattedLastCheck(context)) }
    var updateResultToShow by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var lastCheckedResult by remember { mutableStateOf<UpdateCheckResult?>(null) }
    var showUpToDateDialog by remember { mutableStateOf(false) }
    var updateErrorMessage by remember { mutableStateOf<String?>(null) }

    fun performUpdateCheck() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isCheckingUpdate = true
        updateErrorMessage = null
        coroutineScope.launch {
            val result = GitHubUpdateChecker.checkForUpdates(context)
            isCheckingUpdate = false
            lastCheckedResult = result
            lastCheckTimeStr = GitHubUpdateChecker.getFormattedLastCheck(context)
            if (result.isSuccess) {
                if (result.isUpdateAvailable) {
                    updateResultToShow = result
                } else {
                    showUpToDateDialog = true
                }
            } else {
                updateErrorMessage = result.errorMessage
            }
        }
    }

    if (showExpirationConfirmDialog) {
        AlertDialog(
            onDismissRequest = { 
                localExpirationAlertDays = lastSavedPrimaryDays
                localSecondExpirationAlertDays = lastSavedSecondDays
                showExpirationConfirmDialog = false 
            },
            modifier = Modifier.expressiveModalEntrance(),
            title = { Text(if (secondExpirationAlertEnabled) "Avisos de Vencimiento" else "Aviso de Vencimiento") },
            text = { 
                Text(
                    if (secondExpirationAlertEnabled) 
                        "¿Deseas programar las alertas de vencimiento para $localExpirationAlertDays y $localSecondExpirationAlertDays días antes del vencimiento?" 
                    else 
                        "¿Deseas programar el aviso de vencimiento para $localExpirationAlertDays días antes del vencimiento?"
                ) 
            },
            confirmButton = {
                ExpressiveTextButton(
                    onClick = {
                        lastSavedPrimaryDays = localExpirationAlertDays
                        lastSavedSecondDays = localSecondExpirationAlertDays
                        onSelectExpirationAlertDays(localExpirationAlertDays)
                        if (secondExpirationAlertEnabled) {
                            onSelectSecondExpirationAlertDays(localSecondExpirationAlertDays)
                        }
                        showExpirationConfirmDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                ExpressiveTextButton(
                    onClick = {
                        localExpirationAlertDays = lastSavedPrimaryDays
                        localSecondExpirationAlertDays = lastSavedSecondDays
                        showExpirationConfirmDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            modifier = Modifier.expressiveModalEntrance(),
            title = { Text("Borrar Datos") },
            text = { Text("¿Estás seguro de que deseas borrar todos los datos de la aplicación? Esta acción no se puede deshacer.") },
            confirmButton = {
                ExpressiveButton(
                    onClick = {
                        onClearData()
                        showClearDataConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                ExpressiveTextButton(
                    onClick = {
                        showClearDataConfirm = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (updateResultToShow != null) {
        UpdateAvailableDialog(
            updateResult = updateResultToShow!!,
            onDismiss = { updateResultToShow = null }
        )
    }

    if (showUpToDateDialog) {
        val hasReleases = lastCheckedResult?.hasReleasesFound ?: true
        AlertDialog(
            onDismissRequest = { showUpToDateDialog = false },
            modifier = Modifier.expressiveModalEntrance(),
            icon = {
                Icon(
                    imageVector = if (hasReleases) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(if (hasReleases) "¡Estás al día!" else "Sin versiones en GitHub")
            },
            text = {
                Text(
                    text = if (hasReleases) {
                        "Tienes instalada la versión más reciente de MegasCU (v${BuildConfig.VERSION_NAME}). No hay nuevas actualizaciones en GitHub."
                    } else {
                        "El repositorio '${GitHubUpdateChecker.DEFAULT_REPO}' no cuenta con publicaciones públicas en GitHub actualmente. Tienes instalada la versión v${BuildConfig.VERSION_NAME}."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                ExpressiveButton(onClick = { showUpToDateDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (updateErrorMessage != null) {
        AlertDialog(
            onDismissRequest = { updateErrorMessage = null },
            modifier = Modifier.expressiveModalEntrance(),
            icon = {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Comprobación de actualización") },
            text = {
                Text(
                    text = updateErrorMessage ?: "Ocurrió un error al consultar GitHub.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                ExpressiveButton(onClick = { updateErrorMessage = null }) {
                    Text("Cerrar")
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

    var isDeveloperMode by remember { mutableStateOf(prefs.getBoolean("developer_mode_enabled", false)) }
    var showDeveloperSheet by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val settingsCardColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    }

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
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Configuración",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val scrollState = rememberScrollState()
            val fadeAlpha by remember { derivedStateOf { (scrollState.value / 40f).coerceIn(0f, 1f) } }

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "Interfaz y Experiencia",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )

                    // 0. Modo Simple (Contenedor Separado en la parte Superior)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = settingsCardColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    contentDescription = "Modo Simple",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Modo Simple",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Vista minimalista enfocada únicamente en los datos y saldos principales",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            ExpressiveSwitch(
                                checked = isSimpleMode,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleSimpleMode(it)
                                }
                            )
                        }
                    }

                    Text(
                        text = "Alertas y Sincronización",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    )

                    if (!hasNotificationPermission) {
                        val notifInteraction = remember { MutableInteractionSource() }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .expressivePressEffect(interactionSource = notifInteraction)
                                .clickable(
                                    interactionSource = notifInteraction,
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onRequestNotificationPermission()
                                },
                            shape = rememberExpressiveMorphShape(
                                defaultRadius = 20.dp,
                                pressedRadius = 10.dp,
                                interactionSource = notifInteraction
                            ),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsOff,
                                    contentDescription = "Permiso de Notificaciones Desactivado",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Permiso de Notificaciones Desactivado",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Toca aquí para conceder el permiso y activar las alertas de vencimientos y consumo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Stacked Cards Layout for Alertas y Sincronización
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // 1. Alerta de Vencimiento (Top Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Timer,
                                            contentDescription = "Alerta Vencimiento",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Alerta de Vencimiento",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                AnimatedContent(
                                    targetState = secondExpirationAlertEnabled,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(250)) + slideInVertically { it / 3 }) togetherWith
                                        (fadeOut(animationSpec = androidx.compose.animation.core.tween(200)) + slideOutVertically { -it / 3 })
                                    },
                                    label = "ExpirationAlertSliderAnimation"
                                ) { isDouble ->
                                    if (isDouble) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Avisos:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                AnimatedContent(
                                                    targetState = "${localExpirationAlertDays}_${localSecondExpirationAlertDays}",
                                                    transitionSpec = {
                                                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + scaleIn(initialScale = 0.92f)) togetherWith
                                                        (fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) + scaleOut(targetScale = 0.92f))
                                                    },
                                                    label = "DoubleDaysTextAnimation"
                                                ) { _ ->
                                                    Text(
                                                        "1º: ${localExpirationAlertDays} días | 2º: ${localSecondExpirationAlertDays} días",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            RangeSlider(
                                                value = localExpirationAlertDays.coerceAtMost(localSecondExpirationAlertDays).toFloat()..localSecondExpirationAlertDays.coerceAtLeast(localExpirationAlertDays).toFloat(),
                                                onValueChange = { range ->
                                                    var startVal = range.start.toInt().coerceAtLeast(1)
                                                    var endVal = range.endInclusive.toInt().coerceAtLeast(1)
                                                    if (startVal == endVal) {
                                                        if (startVal < 15) endVal = startVal + 1 else startVal = endVal - 1
                                                    }
                                                    if (startVal != localExpirationAlertDays || endVal != localSecondExpirationAlertDays) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                    localExpirationAlertDays = startVal
                                                    localSecondExpirationAlertDays = endVal
                                                },
                                                onValueChangeFinished = {
                                                    showExpirationConfirmDialog = true
                                                },
                                                valueRange = 1f..15f,
                                                steps = 13,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .expressivePressEffect(),
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                                )
                                            )
                                        }
                                    } else {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Avisar faltando:",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                AnimatedContent(
                                                    targetState = localExpirationAlertDays,
                                                    transitionSpec = {
                                                        (fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) + scaleIn(initialScale = 0.92f)) togetherWith
                                                        (fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) + scaleOut(targetScale = 0.92f))
                                                    },
                                                    label = "SingleDayTextAnimation"
                                                ) { days ->
                                                    Text(
                                                        if (days == 1) "1 día" else "$days días",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Slider(
                                                value = localExpirationAlertDays.toFloat(),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .expressivePressEffect(),
                                                onValueChange = {
                                                    val newVal = it.toInt()
                                                    if (newVal != localExpirationAlertDays) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        localExpirationAlertDays = newVal
                                                    }
                                                },
                                                onValueChangeFinished = { showExpirationConfirmDialog = true },
                                                valueRange = 1f..15f,
                                                steps = 13,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.NotificationAdd,
                                            contentDescription = "Segunda Alerta",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Segunda Alerta de Vencimiento",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    ExpressiveSwitch(
                                        checked = secondExpirationAlertEnabled,
                                        onCheckedChange = { checked ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (checked && localSecondExpirationAlertDays == localExpirationAlertDays) {
                                                localSecondExpirationAlertDays = if (localExpirationAlertDays > 1) localExpirationAlertDays - 1 else 2
                                                onSelectSecondExpirationAlertDays(localSecondExpirationAlertDays)
                                            }
                                            onToggleSecondExpirationAlert(checked)
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Alerta de Umbral de Plan (Middle Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Notifications,
                                            contentDescription = "Alerta Umbral",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Alerta de Umbral de Plan",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Avisar automáticamente al consumir el $dataThresholdPercent% del total de tu paquete",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    ExpressiveSwitch(
                                        checked = dataThresholdAlertEnabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleDataThresholdAlerts(it)
                                        },
                                        modifier = Modifier.testTag("data_threshold_alert_switch")
                                    )
                                }
                                AnimatedVisibility(
                                    visible = dataThresholdAlertEnabled,
                                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 3 }),
                                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 3 })
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Avisar al llegar al:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                "$dataThresholdPercent%",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Slider(
                                            value = dataThresholdPercent.toFloat(),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .expressivePressEffect(),
                                            onValueChange = {
                                                val newVal = it.toInt()
                                                if (newVal != dataThresholdPercent) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    onSelectDataThresholdPercent(newVal)
                                                }
                                            },
                                            valueRange = 50f..95f,
                                            steps = 8,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Alerta de Consumo Diario (Middle Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = "Alertas de Consumo Diario",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Alerta de Consumo Diario",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Recibir notificaciones diarias al acercarte o superar el límite proyectado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = dailyLimitAlertsEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleDailyLimitAlerts(it)
                                    },
                                    modifier = Modifier.testTag("daily_limit_alerts_switch")
                                )
                            }
                        }

                        // 4. Alerta de Recarga de Saldo (Middle Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationImportant,
                                        contentDescription = "Alerta de Recarga de Saldo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Alerta de Recarga de Saldo",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Notificar automáticamente cuando llegue el día en que puedas recargar tu saldo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = rechargeAlertEnabled,
                                    onCheckedChange = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleRechargeAlerts(it)
                                    },
                                    modifier = Modifier.testTag("recharge_alert_switch")
                                )
                            }
                        }

                        // 5. Umbral de Datos Bajos (Middle Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.HourglassEmpty,
                                            contentDescription = "Umbral Alerta Datos",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Umbral de Datos Bajos",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Emitir alerta cuando el saldo restante sea menor a ${if (lowDataThresholdMb >= 1000L) "1 GB" else "$lowDataThresholdMb MB"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val thresholdOptions = listOf(
                                    SegmentOption(100L, "100MB"),
                                    SegmentOption(200L, "200MB"),
                                    SegmentOption(500L, "500MB"),
                                    SegmentOption(1000L, "1 GB")
                                )
                                ConnectedSegmentedGroup(
                                    items = thresholdOptions,
                                    selectedValue = lowDataThresholdMb,
                                    onItemSelected = { mb ->
                                        onSelectLowDataThreshold(mb)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // 5. Sincronización en Segundo Plano (Bottom Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = "Intervalo Sincronización",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Sincronización en Segundo Plano",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Frecuencia de actualización en segundo plano (cada $syncIntervalHours hrs)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val intervalOptions = listOf(
                                    SegmentOption(1, "1h"),
                                    SegmentOption(3, "3h"),
                                    SegmentOption(6, "6h"),
                                    SegmentOption(12, "12h"),
                                    SegmentOption(24, "24h")
                                )
                                ConnectedSegmentedGroup(
                                    items = intervalOptions,
                                    selectedValue = syncIntervalHours,
                                    onItemSelected = { hours ->
                                        onSelectSyncInterval(hours)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Sección independiente: Gráficos e Indicadores (Stacked Cards Layout)
                    Text(
                        text = "Gráficos e Indicadores",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
                    )

                    var isLineChartMainPref by remember { mutableStateOf(prefs.getBoolean("pref_use_line_chart", true)) }
                    var showDailyLimitBarPref by remember { mutableStateOf(prefs.getBoolean("pref_show_daily_recommendation_bar", true)) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // 1. Gráfica Lineal con Puntos (Top Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Gráfica Lineal con Puntos",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Líneas suaves con puntos e indicadores numéricos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = isLineChartMainPref,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isLineChartMainPref = checked
                                        prefs.edit().putBoolean("pref_use_line_chart", checked).apply()
                                    }
                                )
                            }
                        }

                        // 2. Estimador Alternativo de Consumo (Middle Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.QueryStats,
                                        contentDescription = "Estimador Alternativo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Estimador Alternativo de Consumo",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Calcula el ritmo diario y consumo a partir del historial USSD si no se otorga el permiso de Android",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = useAlternativeUsageEstimator,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onToggleAlternativeUsageEstimator(checked)
                                    }
                                )
                            }
                        }

                        // 3. Barra de Límite Diario (Bottom Card)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.HorizontalRule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Barra de Límite Diario",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Mostrar barra de progreso diario sugerido en la pantalla principal",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                ExpressiveSwitch(
                                    checked = showDailyLimitBarPref,
                                    onCheckedChange = { checked ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showDailyLimitBarPref = checked
                                        prefs.edit().putBoolean("pref_show_daily_recommendation_bar", checked).apply()
                                        onToggleDailyLimitAlerts(checked)
                                    }
                                )
                            }
                        }
                    }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conectividad y Seguridad",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            // Soporte Doble SIM (Tarjeta independiente)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = settingsCardColor)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Badge "Experimental" posicionado en la esquina superior derecha
                    Surface(
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomStart = 10.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = if (isDark) 0.30f else 0.22f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "Experimental",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DualSimIcon(
                                tint = MaterialTheme.colorScheme.primary,
                                contourColor = settingsCardColor,
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = "Soporte Doble SIM",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Mostrar selector e indicador de tarjeta SIM en la barra superior",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = dualSimEnabled,
                            onCheckedChange = { targetChecked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleDualSim(targetChecked)
                            },
                            modifier = Modifier.testTag("dual_sim_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seguridad y Control Parental (Tarjeta independiente)
            val secInteraction = remember { MutableInteractionSource() }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .expressivePressEffect(interactionSource = secInteraction)
                    .clickable(
                        interactionSource = secInteraction,
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSecurity()
                    },
                shape = rememberExpressiveMorphShape(
                    defaultRadius = 24.dp,
                    pressedRadius = 12.dp,
                    interactionSource = secInteraction
                ),
                colors = CardDefaults.cardColors(containerColor = settingsCardColor)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ShieldLockIcon,
                        contentDescription = "Seguridad",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Seguridad y Control Parental",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "PIN y Biometría para proteger el acceso y compras",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Abrir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Plantilla de Diseño del Widget 4x2

            Text(
                text = "Personalización Visual",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            // 1. Tarjeta Seguridad (Saldo vs Datos) - Tarjeta independiente al inicio
            var primaryCardFirstField by remember {
                mutableStateOf(prefs.getString("pref_primary_card_first_field", "SALDO") ?: "SALDO")
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = settingsCardColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DashboardCustomize,
                            contentDescription = "Tarjeta Seguridad",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tarjeta Seguridad",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (primaryCardFirstField == "SALDO")
                                    "Muestra el Saldo Principal como dato destacado inicial"
                                else
                                    "Muestra los Datos Disponibles como dato destacado inicial",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val cardFieldOptions = listOf(
                        SegmentOption("SALDO", "Saldo", Icons.Rounded.AttachMoney),
                        SegmentOption("DATOS", "Datos", Icons.Rounded.SwapVert)
                    )
                    ConnectedSegmentedGroup(
                        items = cardFieldOptions,
                        selectedValue = primaryCardFirstField,
                        onItemSelected = { selected ->
                            primaryCardFirstField = selected
                            prefs.edit().putString("pref_primary_card_first_field", selected).apply()
                        },
                        showDotIndicator = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // 2. Modo de Tema Oscuro/Claro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = "Modo de Tema",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Modo de Tema Oscuro/Claro",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when (themeMode) {
                                            "LIGHT" -> "Forzar tema claro brillante"
                                            "DARK" -> "Forzar tema oscuro de alto contraste"
                                            else -> "Coincidir con la preferencia del sistema"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val activeThemeId = if (themeMode == "AMOLED") "DARK" else themeMode
                        val themeOptions = listOf(
                            SegmentOption("SYSTEM", "Sistema", Icons.Rounded.Smartphone),
                            SegmentOption("LIGHT", "Claro", Icons.Rounded.LightMode),
                            SegmentOption("DARK", "Oscuro", Icons.Rounded.DarkMode)
                        )
                        ConnectedSegmentedGroup(
                            items = themeOptions,
                            selectedValue = activeThemeId,
                            onItemSelected = { mode ->
                                onSelectThemeMode(mode)
                            },
                            showDotIndicator = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 3. Tema Negro AMOLED
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Contrast,
                                contentDescription = "Tema Negro AMOLED",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Tema Negro AMOLED",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Aplica negro puro cuando el modo oscuro está activo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = isAmoledMode,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleAmoledMode(it)
                            }
                        )
                    }
                }

                // 3. Fuente personalizada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FontDownload,
                                contentDescription = "Fuente personalizada",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Fuente personalizada",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tipografía de ancho fijo para mejor lectura de cifras y saldos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = useMonospace,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleMonospace(it)
                            }
                        )
                    }
                }

                // 4. Colores Dinámicos
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Colores Dinámicos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Colores Dinámicos",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                                        "Armonizar paleta de colores con el fondo de pantalla del sistema (Android 12+)"
                                    else
                                        "No disponible en esta versión de Android (requiere Android 12+)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                            enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                            onCheckedChange = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleDynamicColors(it)
                                }
                            }
                        )
                    }
                }

                // 5. Desactivar Efectos Blur
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = settingsCardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BlurOff,
                                contentDescription = "Desactivar efectos blur",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Desactivar Efectos Blur",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Desactiva los desenfoques de fondo para optimizar el rendimiento y ahorrar batería",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = disableBlurEffects,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleDisableBlurEffects(it)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Permisos y Accesos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )

            // 4. Permisos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = settingsCardColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Permisos",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estado de Permisos de la App",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PermissionItem("Llamadas directas USSD (*222#)", hasPhonePermission)
                    PermissionItem("Notificaciones en primer plano", hasNotificationPermission)
                    PermissionItem("Acceso a uso de datos (Android)", hasUsageStatsPermission)

                    if (!hasUsageStatsPermission) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Permiso restringido o no concedido",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (useAlternativeUsageEstimator) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (useAlternativeUsageEstimator)
                                        "El estimador alternativo está activo y calcula el consumo según tus consultas USSD sin requerir acceso al sistema."
                                    else
                                        "Sin este permiso ni el estimador alternativo, las estadísticas de consumo diario no se mostrarán.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Estimador alternativo",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    ExpressiveSwitch(
                                        checked = useAlternativeUsageEstimator,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onToggleAlternativeUsageEstimator(it)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (!hasPhonePermission || !hasNotificationPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ExpressiveButton(
                            onClick = onRequestPermissions,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conceder Permisos Requeridos")
                        }
                    }
                    if (!hasUsageStatsPermission) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ExpressiveButton(
                            onClick = onRequestUsageStatsPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conceder Permiso de Uso de Datos en Ajustes")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            





                if (showDeveloperSheet) {
                    val scope = rememberCoroutineScope()
                    var showMaterialIconsSheet by remember { mutableStateOf(false) }
                    var showWcagInspectorSheet by remember { mutableStateOf(false) }
                    var showSimulatedUpdateDialog by remember { mutableStateOf(false) }
                    var showSimulatedWhatsNewDialog by remember { mutableStateOf(false) }
                    var showChangelogFromDev by remember { mutableStateOf(false) }
                    var isWcagPaletteActive by remember { mutableStateOf(prefs.getBoolean("pref_use_wcag_palette", false)) }
                    val devView = androidx.compose.ui.platform.LocalView.current
                    SideEffect {
                        var parent = devView.parent
                        while (parent != null) {
                            if (parent is androidx.compose.ui.window.DialogWindowProvider) {
                                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(parent.window, false)
                                break
                            }
                            parent = parent.parent
                        }
                    }

                    ModalBottomSheet(
                        onDismissRequest = { showDeveloperSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.statusBarsPadding().padding(top = 40.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        dragHandle = { ExpressiveDragHandle() },
                        scrimColor = Color.Transparent,
                        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Rounded.BugReport,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Opciones de Desarrollador",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Herramientas M3 Expressive",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                ExpressiveFilledTonalIconButton(
                                    onClick = {
                                        isDeveloperMode = false
                                        showDeveloperSheet = false
                                        prefs.edit().putBoolean("developer_mode_enabled", false).apply()
                                        Toast.makeText(context, "Modo de prueba desactivado", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PowerSettingsNew,
                                        contentDescription = "Desactivar modo prueba",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
        text = "🎨 Temas y Estilos",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    // Sección 0: Paleta Expresiva de Colores Globales
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Palette,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Colores Expresivos WCAG",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isWcagPaletteActive) "Activado: Alto contraste en toda la app" else "Desactivado: Paleta Estándar",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    ExpressiveSwitch(
                                        checked = isWcagPaletteActive,
                                        onCheckedChange = { checked ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            isWcagPaletteActive = checked
                                            prefs.edit().putBoolean("pref_use_wcag_palette", checked).apply()
                                            Toast.makeText(
                                                context,
                                                if (checked) "Paleta de Colores WCAG aplicada en toda la app" else "Paleta Estándar restaurada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sección: Estilo de Paleta
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Estilo de paleta",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Elige el estilo de colores para la interfaz de la aplicación.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    val paletteOptions = listOf(
                                        Triple("Tonal Spot", "Equilibrado y calmado.", listOf(Color(0xFFFFD97D), Color(0xFFE5D7C1), Color(0xFFA6CDB0), Color(0xFF48473A))),
                                        Triple("Vibrante", "Acentos de alta saturación.", listOf(Color(0xFFFFB732), Color(0xFFEFE0C1), Color(0xFFD3C570), Color(0xFF474332))),
                                        Triple("Expresivo", "Cambios de tono y contraste marcados.", listOf(Color(0xFFD9B9FF), Color(0xFFC6E0B4), Color(0xFFD5C46E), Color(0xFF474332))),
                                        Triple("Ensalada de frutas", "Acentos juguetones y rotados.", listOf(Color(0xFFFFB4AB), Color(0xFFFFB4AB), Color(0xFFFFC062), Color(0xFF474332)))
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        paletteOptions.forEach { (option, _, colors) ->
                                            val selected = paletteStyle == option
                                            val cornerRadius by androidx.compose.animation.core.animateDpAsState(targetValue = if (selected) 16.dp else 32.dp, label = "shape")
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .expressivePressEffect()
                                                    .clickable(
                                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            onSelectPaletteStyle(option)
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Outline
                                                if (selected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .border(
                                                                width = 2.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = RoundedCornerShape(cornerRadius + 6.dp)
                                                            )
                                                    )
                                                }
                                                
                                                // Color quadrants
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(cornerRadius))
                                                ) {
                                                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val w = size.width
                                                        val h = size.height
                                                        drawRect(color = colors[0], topLeft = androidx.compose.ui.geometry.Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w/2, h/2))
                                                        drawRect(color = colors[1], topLeft = androidx.compose.ui.geometry.Offset(w/2, 0f), size = androidx.compose.ui.geometry.Size(w/2, h/2))
                                                        drawRect(color = colors[2], topLeft = androidx.compose.ui.geometry.Offset(0f, h/2), size = androidx.compose.ui.geometry.Size(w/2, h/2))
                                                        drawRect(color = colors[3], topLeft = androidx.compose.ui.geometry.Offset(w/2, h/2), size = androidx.compose.ui.geometry.Size(w/2, h/2))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(28.dp))
                                    
                                    val currentDesc = paletteOptions.find { it.first == paletteStyle }?.second ?: ""
                                    Text(
                                        text = paletteStyle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = currentDesc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Toggle de Forzar Modo Simulación / Emulador
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Modo Simulación / Emulador", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                            Text("Simula respuestas USSD de red Cubacel sin llamadas reales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    var forceEmu by remember { mutableStateOf(prefs.getBoolean("pref_force_emulator_mode", false)) }
                                    ExpressiveSwitch(
                                        checked = forceEmu,
                                        onCheckedChange = {
                                            forceEmu = it
                                            prefs.edit().putBoolean("pref_force_emulator_mode", it).apply()
                                            Toast.makeText(context, if (it) "Modo simulación forzado activado" else "Modo simulación forzado desactivado", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Sección: Simulador de Menús y Códigos USSD ETECSA
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Dialpad,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Simulador de Menús y Códigos USSD",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Prueba de menús interactivos, compras y consultas de la red Cubacel con respuesta instantánea.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Campo de código personalizado
                                    var customUssdCode by remember { mutableStateOf("") }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = customUssdCode,
                                            onValueChange = { customUssdCode = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("*133*1# o *222#", style = MaterialTheme.typography.bodySmall) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ExpressiveButton(
                                            onClick = {
                                                if (customUssdCode.isNotBlank()) {
                                                    val codeToRun = customUssdCode.trim()
                                                    onExecuteConsulta?.invoke(codeToRun, "Simulación USSD")
                                                    Toast.makeText(context, "Ejecutando $codeToRun...", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Ejecutar")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Menús Interactivos ETECSA (*133# y *234#):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val interactiveMenus = listOf(
                                            "*133#" to "Menú Compra",
                                            "*133*1#" to "Datos Móviles",
                                            "*133*1*1#" to "Tarifa Consumo",
                                            "*133*3#" to "Planes Voz",
                                            "*133*2#" to "Planes SMS",
                                            "*133*4#" to "Plan Amigos",
                                            "*234#" to "Transf. Saldo",
                                            "*662#" to "Recarga Cupón"
                                        )
                                        items(interactiveMenus) { (code, label) ->
                                            ExpressiveOutlinedButton(
                                                onClick = {
                                                    onExecuteConsulta?.invoke(code, label)
                                                    Toast.makeText(context, "Llamando a $label ($code)...", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("$label ($code)", fontSize = 11.5.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Consultas Directas de Recursos (*222#):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val queryMenus = listOf(
                                            "*222#" to "Saldo",
                                            "*222*328#" to "Datos LTE",
                                            "*222*266#" to "Bono .cu",
                                            "*222*869#" to "Minutos",
                                            "*222*767#" to "SMS",
                                            "*222*732#" to "Bono Int.",
                                            "*222*468#" to "Habilitar Net"
                                        )
                                        items(queryMenus) { (code, label) ->
                                            ExpressiveOutlinedButton(
                                                onClick = {
                                                    onExecuteConsulta?.invoke(code, label)
                                                    Toast.makeText(context, "Consultando $label ($code)...", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("$label ($code)", fontSize = 11.5.sp)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Simulación Directa de Compras:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ExpressiveOutlinedButton(
                                            onClick = {
                                                onExecutePurchase?.invoke("*133*1*4*1#")
                                                Toast.makeText(context, "Simulando compra: Plan 4.5 GB...", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("Comprar 4.5 GB", fontSize = 11.sp)
                                        }
                                        ExpressiveOutlinedButton(
                                            onClick = {
                                                onExecutePurchase?.invoke("*133*1*4*4#")
                                                Toast.makeText(context, "Simulando compra: Combo 6 GB...", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text("Comprar Combo 6GB", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
        text = "🧪 Pruebas de Funcionalidad",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    // Sección: Simulación de Datos y Estados
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoGraph,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Simulación de Datos y Estados",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                                                val current = db.planDao().getPlanStatusDirect() ?: com.ams.megascu.data.db.PlanStatusEntity()
                                                val updated = current.copy(
                                                    dataDays = 0,
                                                    dataExpirationTimestamp = System.currentTimeMillis() + 3600_000L
                                                )
                                                db.planDao().insertOrUpdatePlanStatus(updated)
                                            }
                                            Toast.makeText(context, "Simulación: Plan vence en 1 hora", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.HourglassEmpty, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Plan Por Vencer (1 Hora)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                                                val current = db.planDao().getPlanStatusDirect() ?: com.ams.megascu.data.db.PlanStatusEntity()
                                                val updated = current.copy(
                                                    dataMb = 50L,
                                                    dataLteMb = 100L,
                                                    bonusDataMb = 0L
                                                )
                                                db.planDao().insertOrUpdatePlanStatus(updated)
                                            }
                                            Toast.makeText(context, "Simulación: Alerta de Datos Bajos (< 150 MB)", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Datos Bajos (< 200 MB)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            try {
                                                com.ams.megascu.service.DailyLimitAlertManager.sendNotification(
                                                    context,
                                                    "Límite Diario Superado",
                                                    "Has alcanzado el 95% del consumo diario recomendado de tu plan."
                                                )
                                                Toast.makeText(context, "Simulación: Límite diario activado", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Límite Diario Superado (95%)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            try {
                                                com.ams.megascu.service.DailyLimitAlertManager.sendNotification(
                                                    context,
                                                    "2da Alerta: Plan por vencer",
                                                    "Tu plan expira pronto según la 2da alerta configurada."
                                                )
                                                Toast.makeText(context, "Simulación: 2da alerta de vencimiento activada", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular 2da Alerta de Vencimiento")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            try {
                                                com.ams.megascu.service.DailyLimitAlertManager.sendNotification(
                                                    context,
                                                    "Límite de Datos Alcanzado",
                                                    "Has alcanzado el porcentaje límite de datos configurado."
                                                )
                                                Toast.makeText(context, "Simulación: Límite de datos activado", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Límite de Datos (Porcentaje)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                                                val current = db.planDao().getPlanStatusDirect() ?: com.ams.megascu.data.db.PlanStatusEntity()
                                                val updated = current.copy(
                                                    dataMb = 0L,
                                                    dataLteMb = 0L,
                                                    bonusDataMb = 0L,
                                                    balanceCup = 0.0
                                                )
                                                db.planDao().insertOrUpdatePlanStatus(updated)
                                            }
                                            Toast.makeText(context, "Simulación: Plan totalmente agotado (0 MB)", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Plan Agotado (0 MB)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                                                val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(context, 1)
                                                val reset = com.ams.megascu.data.db.PlanStatusEntity(
                                                    id = 1,
                                                    subscriptionId = currentSubId,
                                                    balanceCup = 125.50,
                                                    dataMb = 1024L,
                                                    dataLteMb = 2560L,
                                                    bonusDataMb = 512L,
                                                    minutesStr = "45 mins",
                                                    smsCount = 120,
                                                    dataDays = 28,
                                                    minutesDays = 28,
                                                    smsDays = 28,
                                                    lastUpdatedTimestamp = System.currentTimeMillis()
                                                )
                                                db.planDao().insertOrUpdatePlanStatus(reset)
                                            }
                                            Toast.makeText(context, "Valores de prueba restablecidos", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Restablecer Datos Iniciales")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Disparadores de Alertas y Fondo",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    ExpressiveButton(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                                                val plan = db.planDao().getPlanStatusDirect()
                                                if (plan != null) {
                                                    prefs.edit()
                                                        .remove("last_daily_exceeded_alert_date")
                                                        .remove("last_daily_near_alert_date")
                                                        .remove("last_exp_alert_date_1day")
                                                        .remove("last_exp_alert_count_1day")
                                                        .remove("last_exp_alert_date_5days")
                                                        .remove("last_exp_alert_date_2days")
                                                        .apply()

                                                    com.ams.megascu.service.PlanExpirationAlertManager.checkAndNotifyExpiration(context, plan)
                                                    com.ams.megascu.service.DailyLimitAlertManager.checkAndNotify(context)
                                                }
                                            }
                                            Toast.makeText(context, "Evaluando y disparando alertas configuradas...", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Disparar: Evaluar Alertas Configuradas")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            try {
                                                com.ams.megascu.widget.MegasWidgetProvider.updateAllWidgets(context)
                                                Toast.makeText(context, "Sincronización de Widget ejecutada", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error Sync: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Forzar Actualización de Widget")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            try {
                                                com.ams.megascu.service.DailyLimitAlertManager.sendNotification(
                                                    context,
                                                    "Notificación de Prueba",
                                                    "Prueba de Desarrollador: Sistema de alertas y notificaciones MegasCU activo."
                                                )
                                                Toast.makeText(context, "Notificación de prueba enviada", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Prueba enviada: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Lanzar Notificación de Prueba")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
        text = "🛠️ Herramientas de UI",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
    // Sección 3: Lanzadores de Pantallas e Indicadores
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.RocketLaunch,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Lanzadores e Iconos",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            prefs.edit().putBoolean("pref_coach_mark_completed", false).apply()
                                            onResetCoachMark()
                                            showDeveloperSheet = false
                                            onDismiss()
                                            Toast.makeText(context, "Indicador 'Toca aquí para actualizar' activado", Toast.LENGTH_LONG).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.TouchApp, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Mostrar Indicador 'Toca aquí'")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveButton(
                                        onClick = {
                                            showDeveloperSheet = false
                                            onDismiss()
                                            onStartTutorial()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Ver Tutorial Interactivo (Paso a Paso)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveButton(
                                        onClick = {
                                            showDeveloperSheet = false
                                            onDismiss()
                                            onResetOnboarding()
                                            Toast.makeText(context, "Mostrando Pantalla de Bienvenida", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Rounded.RocketLaunch, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Visualizar Pantalla de Bienvenida (Onboarding)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveButton(
                                        onClick = {
                                            showWcagInspectorSheet = true
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                    ) {
                                        Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Comprobación de Contraste WCAG")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            showMaterialIconsSheet = true
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Palette, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Catálogo de Iconos Material M3")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveButton(
                                        onClick = {
                                            showSimulatedUpdateDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    ) {
                                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Descarga de Nueva Versión")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ExpressiveOutlinedButton(
                                        onClick = {
                                            showSimulatedWhatsNewDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Rounded.Celebration, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Simular Alerta de Novedades (What's New)")
                                    }

                                    if (showWcagInspectorSheet) {
                                        WcagContrastInspectorBottomSheet(
                                            onDismissRequest = { showWcagInspectorSheet = false }
                                        )
                                    }

                                    if (showMaterialIconsSheet) {
                                        MaterialIconsBottomSheet(
                                            onDismissRequest = { showMaterialIconsSheet = false }
                                        )
                                    }
                                }
                            }

                            if (showSimulatedUpdateDialog) {
                                UpdateAvailableDialog(
                                    updateResult = UpdateCheckResult(
                                        isSuccess = true,
                                        isUpdateAvailable = true,
                                        latestVersionName = "0.9.0-beta_(250)",
                                        latestVersionCode = 250,
                                        releaseTitle = "MegasCU v0.9.0-beta (Build 250)",
                                        changelog = "• Nuevo simulador interactivo de descargas y actualizaciones en opciones de desarrollo.\n• Optimización integral de márgenes y scroll en ventana de descarga de GitHub.\n• Sistema de alerta 'Novedades de la versión' al iniciar la app tras una actualización.",
                                        apkDownloadUrl = "https://github.com/mikel-ams/MegasCU/releases/download/v0.9.0-beta/MegasCU_0.9.0-beta_(250).apk",
                                        releaseHtmlUrl = "https://github.com/mikel-ams/MegasCU/releases",
                                        apkSizeMb = 14.5f,
                                        publishedAt = "Hoy",
                                        isPrerelease = true
                                    ),
                                    isSimulation = true,
                                    onDismiss = { showSimulatedUpdateDialog = false }
                                )
                            }

                            if (showSimulatedWhatsNewDialog) {
                                WhatsNewDialog(
                                    onDismiss = { showSimulatedWhatsNewDialog = false },
                                    onViewFullChangelog = {
                                        showSimulatedWhatsNewDialog = false
                                        showChangelogFromDev = true
                                    }
                                )
                            }

                            if (showChangelogFromDev) {
                                ChangelogBottomSheet(
                                    onDismiss = { showChangelogFromDev = false }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón Destacado de Desactivación con Icono Coherente (PowerSettingsNew)
                            ExpressiveFilledTonalButton(
                                onClick = {
                                    isDeveloperMode = false
                                    showDeveloperSheet = false
                                    prefs.edit().putBoolean("developer_mode_enabled", false).apply()
                                    Toast.makeText(context, "Modo de prueba desactivado", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PowerSettingsNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Desactivar Opciones de Desarrollador",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                }

            Text(
                text = "Gestor de Descarga y Actualizaciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = "Actualizaciones",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Actualizaciones desde GitHub",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Versión instalada: v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Última comprobación:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastCheckTimeStr,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch for automatic 24h background checks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Comprobación automática (cada 24h)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Avisar en segundo plano cuando haya una nueva APK en GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        ExpressiveSwitch(
                            checked = autoUpdateCheck,
                            onCheckedChange = { checked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                autoUpdateCheck = checked
                                prefs.edit().putBoolean(GitHubUpdateChecker.PREF_AUTO_UPDATE_CHECK, checked).apply()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button to check for updates now
                    ExpressiveButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            performUpdateCheck() 
                        },
                        enabled = !isCheckingUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isCheckingUpdate) {
                            CircularWavyProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackThickness = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Comprobando repositorio...")
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar Actualización")
                        }
                    }
                }
            }

            Text(
                text = "Gestión de Datos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
            )

            // 3. Borrar Datos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = "Borrar Datos",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Borrar Datos de la App",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Limpiar todos los registros y dejar valores en 0",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ExpressiveButton(
                        onClick = { showClearDataConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Borrar Datos")
                    }
                }
            }

            // 🧪 Opciones de Prueba (Modo Desarrollador) - Tarjeta al pie de la ventana, debajo de Gestión de Datos
            if (isDeveloperMode) {
                Spacer(modifier = Modifier.height(16.dp))
                val devInteraction = remember { MutableInteractionSource() }
                Card(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showDeveloperSheet = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .expressivePressEffect(interactionSource = devInteraction),
                    interactionSource = devInteraction,
                    shape = rememberExpressiveMorphShape(
                        defaultRadius = 24.dp,
                        pressedRadius = 12.dp,
                        interactionSource = devInteraction
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BugReport,
                                contentDescription = "Menú Secreto",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Opciones de Desarrollador",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Herramientas avanzadas de prueba, depuración y personalización del sistema.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "Abrir",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                .height(20.dp)
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
private fun PermissionItem(label: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (isGranted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Concedido",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Activo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
    }
}
