package com.ams.megascu
import android.content.Context
import kotlinx.coroutines.delay
import com.ams.megascu.utils.PermissionUtils
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.ams.megascu.utils.PurchaseAlertHelper

import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.view.WindowManager
import com.ams.megascu.ui.components.AuthDialog
import com.ams.megascu.ui.components.ShapeMorphingLoadingIndicator
import com.ams.megascu.ui.components.AuthScreen
import com.ams.megascu.ui.components.UpdateAvailableDialog
import com.ams.megascu.ui.components.AppLaunchUpdateChecker
import com.ams.megascu.utils.GitHubUpdateChecker
import com.ams.megascu.utils.UpdateCheckResult
import androidx.fragment.app.FragmentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ams.megascu.ui.components.*
import com.ams.megascu.ui.theme.MegasTheme
import com.ams.megascu.ui.viewmodel.MainViewModel
import com.ams.megascu.ui.viewmodel.UssdUiState

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var onPermissionsUpdated: (() -> Unit)? = null
    private var showUsageAccessDialogState by mutableStateOf(false)

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onPermissionsUpdated?.invoke()
        if (!checkUsageStatsPermission()) {
            requestUsageStatsPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShortcutIntent(intent)
        
        setContent {
            
            val securityEnabled by viewModel.securityEnabled.collectAsStateWithLifecycle()
            val protectionScope by viewModel.protectionScope.collectAsStateWithLifecycle()
            val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
            val biometricsEnabled by viewModel.biometricsEnabled.collectAsStateWithLifecycle()
            
            var showSplash by remember { mutableStateOf(true) }
            var isAppUnlocked by remember { mutableStateOf(false) }
            var lastBackgroundTimestamp by remember { mutableLongStateOf(0L) }
            var resetToHomeScreenTrigger by remember { mutableIntStateOf(0) }
            var pendingPurchaseCode by remember { mutableStateOf<String?>(null) }
            
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            lastBackgroundTimestamp = System.currentTimeMillis()
                        }
                        Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME -> {
                            if (lastBackgroundTimestamp != 0L) {
                                val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
                                if (elapsed >= 2 * 60 * 1000L) {
                                    isAppUnlocked = false
                                    resetToHomeScreenTrigger++
                                }
                                lastBackgroundTimestamp = 0L
                            }
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()

            val needsAppUnlock = onboardingComplete && securityEnabled && 
                (protectionScope == "APP_ACCESS" || protectionScope == "BOTH") && 
                !isAppUnlocked

            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isAmoledMode by viewModel.isAmoledMode.collectAsStateWithLifecycle()
            val isEasterEggAmoled by viewModel.easterEggAmoledActive.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = if (isEasterEggAmoled) true else when (themeMode) {
                "DARK", "AMOLED" -> true
                "LIGHT" -> false
                else -> systemInDark
            }
            val isAmoled = isEasterEggAmoled || isAmoledMode || themeMode == "AMOLED"
            val useMonospace by viewModel.useMonospaceFont.collectAsStateWithLifecycle()
            val useDynamicColors by viewModel.useDynamicColors.collectAsStateWithLifecycle()
            val paletteStyle by viewModel.paletteStyle.collectAsStateWithLifecycle()
            
            var permissionsTrigger by remember { mutableIntStateOf(0) }
            
            LaunchedEffect(Unit) {
                onPermissionsUpdated = {
                    permissionsTrigger++
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        permissionsTrigger++
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            val hasPhonePermission = remember(permissionsTrigger) { checkPermission(Manifest.permission.CALL_PHONE) }
            val hasNotificationPermission = remember(permissionsTrigger) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkPermission(Manifest.permission.POST_NOTIFICATIONS)
                } else true
            }
            val hasUsageStatsPermission = remember(permissionsTrigger) { checkUsageStatsPermission() }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }
            
            MegasTheme(darkTheme = isDarkTheme, isAmoled = isAmoled, useMonospace = useMonospace, useDynamicColors = useDynamicColors, paletteStyle = paletteStyle) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val appState = if (needsAppUnlock) 2 else if (!onboardingComplete) 0 else 1
                        
                        AnimatedContent(
                            modifier = Modifier.fillMaxSize(),
                            targetState = appState,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400)))
                                    .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically(targetOffsetY = { -it / 3 }, animationSpec = tween(300)))
                            },
                            label = "app_startup_transition"
                        ) { state ->
                            when (state) {
                                2 -> {
                                    com.ams.megascu.ui.components.AuthScreen(
                                        correctPin = securityPin,
                                        biometricsEnabled = biometricsEnabled,
                                        onAuthSuccess = { isAppUnlocked = true },
                                        onResetApp = { viewModel.resetAllSettings() }
                                    )
                                }
                                0 -> {
                                    OnboardingScreen(
                                        onRequestPermissions = { requestRequiredPermissions() },
                                        onRequestPhonePermission = { requestPhonePermission() },
                                        onRequestNotificationPermission = { requestNotificationPermission() },
                                        onRequestUsageStatsPermission = { requestUsageStatsPermission() },
                                        hasPhonePermission = hasPhonePermission,
                                        hasNotificationPermission = hasNotificationPermission,
                                        hasUsageStatsPermission = hasUsageStatsPermission,
                                        themeMode = themeMode,
                                        useDynamicColors = useDynamicColors,
                                        useMonospace = useMonospace,
                                        isSimpleMode = viewModel.isSimpleMode.collectAsStateWithLifecycle().value,
                                        dailyLimitAlertsEnabled = viewModel.dailyLimitAlertsEnabled.collectAsStateWithLifecycle().value,
                                        dataThresholdAlertEnabled = viewModel.dataThresholdAlertEnabled.collectAsStateWithLifecycle().value,
                                        dataThresholdPercent = viewModel.dataThresholdPercent.collectAsStateWithLifecycle().value,
                                        expirationAlertDays = viewModel.expirationAlertDays.collectAsStateWithLifecycle().value,
                                        secondExpirationAlertEnabled = viewModel.secondExpirationAlertEnabled.collectAsStateWithLifecycle().value,
                                        secondExpirationAlertDays = viewModel.secondExpirationAlertDays.collectAsStateWithLifecycle().value,
                                        syncIntervalHours = viewModel.syncIntervalHours.collectAsStateWithLifecycle().value,
                                        dualSimEnabled = viewModel.dualSimEnabled.collectAsStateWithLifecycle().value,
                                        securityEnabled = viewModel.securityEnabled.collectAsStateWithLifecycle().value,
                                        securityPin = viewModel.securityPin.collectAsStateWithLifecycle().value,
                                        biometricsEnabled = viewModel.biometricsEnabled.collectAsStateWithLifecycle().value,
                                        onSelectThemeMode = { viewModel.setThemeMode(it) },
                                        onToggleDynamicColors = { viewModel.toggleDynamicColors(it) },
                                        onToggleMonospace = { viewModel.toggleMonospaceFont(it) },
                                        onToggleSimpleMode = { viewModel.toggleSimpleMode(it) },
                                        onToggleDailyLimitAlerts = { viewModel.toggleDailyLimitAlerts(it) },
                                        onToggleDataThresholdAlerts = { viewModel.toggleDataThresholdAlerts(it) },
                                        onSetDataThresholdPercent = { viewModel.setDataThresholdPercent(it) },
                                        onSetExpirationAlertDays = { viewModel.setExpirationAlertDays(it) },
                                        onToggleSecondExpirationAlert = { viewModel.setSecondExpirationAlertEnabled(it) },
                                        onSetSecondExpirationAlertDays = { viewModel.setSecondExpirationAlertDays(it) },
                                        onSetSyncIntervalHours = { viewModel.setSyncIntervalHours(it) },
                                        onToggleDualSim = { viewModel.toggleDualSim(it) },
                                        onToggleSecurity = { viewModel.setSecurityEnabled(it) },
                                        onSetSecurityPin = { viewModel.setSecurityPin(it) },
                                        onToggleBiometrics = { viewModel.setBiometricsEnabled(it) },
                                        onFinish = { 
                                            isAppUnlocked = true
                                            viewModel.completeOnboarding() 
                                        }
                                    )
                                }
                                1 -> {
                                    MegasMainApp(
                                        viewModel = viewModel,
                                        isDarkTheme = isDarkTheme,
                                        resetToHomeScreenTrigger = resetToHomeScreenTrigger,
                                        onSecurityUnlocked = { isAppUnlocked = true },
                                        onRequestPermissions = { requestRequiredPermissions() },
                                        onRequestNotificationPermission = { requestNotificationPermission() },
                                        onRequestUsageStatsPermission = { requestUsageStatsPermission() },
                                        hasPhonePermission = hasPhonePermission,
                                        hasNotificationPermission = hasNotificationPermission,
                                        hasUsageStatsPermission = hasUsageStatsPermission
                                    )
                                }
                            }
                        }

                        // Splash Screen Overlay (se muestra siempre al iniciar o reanudar)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showSplash,
                            enter = fadeIn(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(300)),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            com.ams.megascu.ui.components.AnimatedSplashScreen(onAnimationFinished = { showSplash = false })
                        }

                        // Diálogo explicativo previo a abrir los Ajustes del Sistema para el permiso de uso
                        if (showUsageAccessDialogState) {
                            UsageAccessExplanationDialog(
                                onDismiss = { showUsageAccessDialogState = false },
                                onConfirm = {
                                    showUsageAccessDialogState = false
                                    PermissionUtils.openUsageAccessSettings(this@MainActivity)
                                },
                                onUseAlternativeEstimator = {
                                    viewModel.setUseAlternativeUsageEstimator(true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_SHOW_UPDATE", false) == true) {
            viewModel.setPendingAction("show_update")
        }
        val notifAction = intent?.getStringExtra("action")
        if (notifAction != null) {
            viewModel.setPendingAction(notifAction)
        }
        if (intent?.action == "com.ams.megascu.ACTION_WIDGET_REFRESH" || intent?.getBooleanExtra("from_widget_refresh", false) == true) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                viewModel.refreshAllStatus()
            }
            return
        }
        val action = intent?.getStringExtra("shortcut_action") ?: return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            when (action) {
                "check_balance" -> viewModel.executeUssdQuery("*222#")
                "check_data" -> viewModel.executeUssdQuery("*222*328#")
                "open_history" -> viewModel.refreshAllStatus()
            }
        }
    }

    private fun requestPhonePermission() {
        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE
            )
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }

    private fun requestRequiredPermissions() {
        val missingPermissions = mutableListOf<String>()
        if (!checkPermission(Manifest.permission.CALL_PHONE)) {
            missingPermissions.add(Manifest.permission.CALL_PHONE)
        }
        if (!checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            missingPermissions.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !checkPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (missingPermissions.isNotEmpty()) {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else if (!checkUsageStatsPermission()) {
            requestUsageStatsPermission()
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkUsageStatsPermission(): Boolean {
        return PermissionUtils.hasUsageStatsPermission(this)
    }

    private fun requestUsageStatsPermission() {
        showUsageAccessDialogState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegasMainApp(
    viewModel: MainViewModel,
    isDarkTheme: Boolean,
    resetToHomeScreenTrigger: Int = 0,
    onSecurityUnlocked: () -> Unit = {},
    onRequestPermissions: () -> Unit,
    onRequestNotificationPermission: () -> Unit = {},
    onRequestUsageStatsPermission: () -> Unit,
    hasPhonePermission: Boolean,
    hasNotificationPermission: Boolean,
    hasUsageStatsPermission: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val planStatus by viewModel.planStatus.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()
    val coachMarkCompleted by viewModel.coachMarkCompleted.collectAsStateWithLifecycle()

    val securityEnabled by viewModel.securityEnabled.collectAsStateWithLifecycle()
    val protectionScope by viewModel.protectionScope.collectAsStateWithLifecycle()
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsStateWithLifecycle()
    
    var showPhonePermissionRequiredDialog by remember { mutableStateOf(false) }

    fun safeRunUssd(action: () -> Unit) {
        val isEmu = viewModel.ussdExecutor.isEmulator()
        if (hasPhonePermission || isEmu) {
            action()
        } else {
            showPhonePermissionRequiredDialog = true
        }
    }
    var pendingPurchaseCode by remember { mutableStateOf<String?>(null) }
    val isSimpleMode by viewModel.isSimpleMode.collectAsStateWithLifecycle()
    val useAlternativeUsageEstimator by viewModel.useAlternativeUsageEstimator.collectAsStateWithLifecycle()
    val smsAlertsEnabled by viewModel.smsAlertsEnabled.collectAsStateWithLifecycle()

    // Controla la alerta de colocación del widget si el usuario no lo tiene aún en inicio
    com.ams.megascu.ui.components.WidgetPromptController()
    val dailyLimitAlertsEnabled by viewModel.dailyLimitAlertsEnabled.collectAsStateWithLifecycle()
    val dataThresholdAlertEnabled by viewModel.dataThresholdAlertEnabled.collectAsStateWithLifecycle()
    val dataThresholdPercent by viewModel.dataThresholdPercent.collectAsStateWithLifecycle()
    val expirationAlertDays by viewModel.expirationAlertDays.collectAsStateWithLifecycle()
    val secondExpirationAlertEnabled by viewModel.secondExpirationAlertEnabled.collectAsStateWithLifecycle()
    val secondExpirationAlertDays by viewModel.secondExpirationAlertDays.collectAsStateWithLifecycle()
    val rechargeAlertEnabled by viewModel.rechargeAlertEnabled.collectAsStateWithLifecycle()
    val pendingAction by viewModel.pendingAction.collectAsStateWithLifecycle()
    val usageHistory by viewModel.usageHistory.collectAsStateWithLifecycle()
    val ussdState by viewModel.ussdState.collectAsStateWithLifecycle()
    val useMonospace by viewModel.useMonospaceFont.collectAsStateWithLifecycle()
    val useDynamicColors by viewModel.useDynamicColors.collectAsStateWithLifecycle()
    val disableBlurEffects by viewModel.disableBlurEffects.collectAsStateWithLifecycle()
    val paletteStyle by viewModel.paletteStyle.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isAmoledMode by viewModel.isAmoledMode.collectAsStateWithLifecycle()
    val syncIntervalHours by viewModel.syncIntervalHours.collectAsStateWithLifecycle()
    val lowDataThresholdMb by viewModel.lowDataThresholdMb.collectAsStateWithLifecycle()
    val dualSimEnabled by viewModel.dualSimEnabled.collectAsStateWithLifecycle()

    val quickActionCode by viewModel.quickActionCode.collectAsStateWithLifecycle()
    val quickActionLabel by viewModel.quickActionLabel.collectAsStateWithLifecycle()

    val hasPendingUpdateBadge by viewModel.hasPendingUpdateBadge.collectAsStateWithLifecycle()
    val updateCheckResult by viewModel.updateCheckResult.collectAsStateWithLifecycle()

    val nonCubacelSimAlert by viewModel.nonCubacelSimAlert.collectAsStateWithLifecycle()
    val missingSimAlertSlot by viewModel.missingSimAlertSlot.collectAsStateWithLifecycle()

    val selectedSimSlot by viewModel.selectedSimSlot.collectAsStateWithLifecycle()
    var showPlanesSheet by remember { mutableStateOf(false) }
    var showGuideSheet by remember { mutableStateOf(false) }
    var showUssdTutorial by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }
    var showMatrixTerminal by remember { mutableStateOf(false) }
    var showQuickActionSelector by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    var isPullRefreshing by remember { mutableStateOf(false) }

    // Comprobación de actualizaciones en background al abrir la aplicación
    AppLaunchUpdateChecker(viewModel = viewModel)

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) {
        val lastSeenVersion = prefs.getInt("pref_last_seen_version_code", -1)
        val currentVersion = com.ams.megascu.BuildConfig.VERSION_CODE
        if (lastSeenVersion == -1) {
            prefs.edit().putInt("pref_last_seen_version_code", currentVersion).apply()
        } else if (currentVersion > lastSeenVersion) {
            prefs.edit().putInt("pref_last_seen_version_code", currentVersion).apply()
            showWhatsNewDialog = true
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            isPullRefreshing = false
        }
    }

    LaunchedEffect(resetToHomeScreenTrigger) {
        if (resetToHomeScreenTrigger > 0) {
            showPlanesSheet = false
            showGuideSheet = false
            showUssdTutorial = false
            showSettingsSheet = false
            showSecuritySettings = false
            showAbout = false
            showChangelogSheet = false
            showMatrixTerminal = false
            showQuickActionSelector = false
            showWhatsNewDialog = false
        }
    }
    var useWavyProgress by remember {
        val dev = prefs.getBoolean("developer_mode_enabled", false)
        val wavy = prefs.getBoolean("pref_use_wavy_progress", false)
        mutableStateOf(dev && wavy)
    }
    var refreshIndicatorType by remember { mutableStateOf(prefs.getString("pref_refresh_indicator_type", "circular_wavy") ?: "circular_wavy") }

    androidx.compose.runtime.DisposableEffect(prefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "pref_use_wavy_progress" || key == "developer_mode_enabled") {
                val dev = p.getBoolean("developer_mode_enabled", false)
                val wavy = p.getBoolean("pref_use_wavy_progress", false)
                useWavyProgress = dev && wavy
            } else if (key == "pref_refresh_indicator_type") {
                refreshIndicatorType = p.getString("pref_refresh_indicator_type", "circular_wavy") ?: "circular_wavy"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    LaunchedEffect(pendingAction) {
        pendingAction?.let { action ->
            when (action) {
                "comprar" -> {
                    showPlanesSheet = true
                }
                "menu", "open_settings_update" -> {
                    showSettingsSheet = true
                }
                "show_update" -> {
                    viewModel.checkForAppUpdates(force = true)
                }
            }
            viewModel.clearPendingAction()
        }
    }

    LaunchedEffect(selectedSimSlot) {
        viewModel.checkSimOperatorCompatibility(selectedSimSlot)
    }

    var activeSheetProgress by remember { mutableFloatStateOf(0f) }

    val purchaseAlertState = remember(planStatus, prefs) {
        PurchaseAlertHelper.calculateAlertState(planStatus, prefs)
    }

    val isUpdateModalOpen = updateCheckResult != null && updateCheckResult!!.isUpdateAvailable

    val isAnyDialogOnlyOpen = showAbout || showMatrixTerminal || showUssdTutorial || showWhatsNewDialog || pendingPurchaseCode != null || nonCubacelSimAlert != null || missingSimAlertSlot != null || isUpdateModalOpen ||
            (ussdState !is UssdUiState.Idle && ussdState !is UssdUiState.Executing)

    val dialogBlurFraction by animateFloatAsState(
        targetValue = if (isAnyDialogOnlyOpen) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "DialogBlurFraction"
    )

    val effectiveBlurAmount = maxOf(dialogBlurFraction, activeSheetProgress)
    val backgroundBlurRadius = if (disableBlurEffects) 0.dp else (24.dp * effectiveBlurAmount).coerceIn(0.dp, 24.dp)
    val isDarkScrimDialog = (showMatrixTerminal || showUssdTutorial || showWhatsNewDialog || pendingPurchaseCode != null || nonCubacelSimAlert != null || missingSimAlertSlot != null || isUpdateModalOpen ||
            (ussdState !is UssdUiState.Idle && ussdState !is UssdUiState.Executing)) && !showAbout
    val backgroundOverlayAlpha = if (isDarkScrimDialog) {
        (0.25f * dialogBlurFraction).coerceIn(0f, 0.25f)
    } else if (showAbout) {
        (0.35f * dialogBlurFraction).coerceIn(0f, 0.35f)
    } else {
        (0.25f * activeSheetProgress).coerceIn(0f, 0.25f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backgroundBlurRadius > 0.dp) Modifier.blur(backgroundBlurRadius) else Modifier)
        ) {
            PullToRefreshBox(
                isRefreshing = isPullRefreshing,
                onRefresh = {
                    isPullRefreshing = true
                    safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) }
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    val distanceFraction = if (isPullRefreshing) 1f else pullRefreshState.distanceFraction
                    val dynamicScale = if (isPullRefreshing) 1f else distanceFraction.coerceIn(0.12f, 1f)
                    val dynamicAlpha = if (isPullRefreshing) 1f else (distanceFraction * 2.2f).coerceIn(0f, 1f)
                    PullToRefreshDefaults.Indicator(
                        state = pullRefreshState,
                        isRefreshing = isPullRefreshing,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 0.dp)
                            .graphicsLayer {
                                scaleX = dynamicScale
                                scaleY = dynamicScale
                                alpha = dynamicAlpha
                            }
                            .zIndex(50f),
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            ) {
                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                    val isDynamicColorActive = useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    val iconBgColor = if (isDynamicColorActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                    val iconTintColor = if (isDynamicColorActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                    var isMegasBold by remember { mutableStateOf(false) }

                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                isMegasBold = !isMegasBold
                                            },
                                            onLongPress = { showMatrixTerminal = true }
                                        )
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .size(42.dp)
                                        .graphicsLayer { rotationZ = -5f }
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(13.dp),
                                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                        .background(
                                            color = iconBgColor,
                                            shape = RoundedCornerShape(13.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                        contentDescription = "App Icon",
                                        tint = iconTintColor,
                                        modifier = Modifier
                                            .requiredSize(68.dp)
                                            .graphicsLayer { rotationZ = 5f }
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                AnimatedContent(
                                    targetState = isMegasBold,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                                fadeOut(animationSpec = tween(durationMillis = 300))
                                    },
                                    label = "TitleEmphasisTransition"
                                ) { boldMegas ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Megas",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = if (boldMegas) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 22.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "CU",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = if (boldMegas) FontWeight.Medium else FontWeight.Bold,
                                                fontSize = 22.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ExpressiveIconButton(
                                    onClick = { showAbout = true },
                                    modifier = Modifier.testTag("btn_about")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Info,
                                        contentDescription = "Acerca de",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                ExpressiveIconButton(
                                    onClick = {
                                        val nextMode = when (themeMode) {
                                            "LIGHT" -> "DARK"
                                            "DARK" -> "SYSTEM"
                                            else -> "LIGHT"
                                        }
                                        viewModel.setThemeMode(nextMode)
                                    },
                                    modifier = Modifier.testTag("btn_theme_toggle")
                                ) {
                                    val iconRes = when (themeMode) {
                                        "LIGHT" -> Icons.Rounded.LightMode
                                        "DARK" -> Icons.Rounded.DarkMode
                                        else -> Icons.Rounded.BrightnessAuto
                                    }
                                    Icon(
                                        imageVector = iconRes,
                                        contentDescription = "Cambiar Tema",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                Box(contentAlignment = Alignment.TopEnd) {
                                    ExpressiveIconButton(
                                        onClick = { showSettingsSheet = true },
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .testTag("btn_settings")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = "Configuración",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }

                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = hasPendingUpdateBadge,
                                        enter = fadeIn(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ) + scaleIn(
                                            initialScale = 0.2f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ),
                                        exit = fadeOut(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ) + scaleOut(
                                            targetScale = 0.2f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 8.dp, end = 9.dp)
                                                .size(9.dp)
                                                .background(Color(0xFFE53935), CircleShape)
                                                .testTag("settings_update_badge_dot")
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    if (!isSimpleMode) {
                        MegasBottomBar(
                            onOpenPlanes = { showPlanesSheet = true },
                            onOpenGuide = { showGuideSheet = true },
                            onRefresh = {
                                safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) }
                            },
                            isRefreshing = isRefreshing,
                            useWavyProgress = useWavyProgress,
                            refreshIndicatorType = refreshIndicatorType,
                            disableBlur = disableBlurEffects,
                            hasPurchaseAlert = purchaseAlertState.hasAlert,
                            onOpenSettings = { showSettingsSheet = true },
                            onShowAbout = { showAbout = true }
                        )
                    }
                },
                floatingActionButton = {
                    if (isSimpleMode) {
                        SimpleModeRefreshFab(
                            onClick = { safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) } },
                            isRefreshing = isRefreshing,
                            useWavyProgress = useWavyProgress,
                            refreshIndicatorType = refreshIndicatorType
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)
                    ) {
                        if (isSimpleMode) {
                        // MODO SIMPLE: Solo bloque principal con tipografía grande y sin distracciones
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                                .padding(top = innerPadding.calculateTopPadding() + 8.dp, bottom = innerPadding.calculateBottomPadding() + 24.dp)
                        ) {
                            com.ams.megascu.ui.components.SimplePriorityStatusCard(
                                planStatus = planStatus,
                                selectedSimSlot = selectedSimSlot,
                                dualSimEnabled = dualSimEnabled,
                                hasPurchaseAlert = purchaseAlertState.hasAlert,
                                onSelectSimSlot = { viewModel.selectSimSlot(it, context) },
                                onExecuteConsulta = { code, title -> safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot) } },
                                onRefresh = { safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) } },
                                onOpenPlanes = { showPlanesSheet = true }
                            )

                            // Loading Bar
                            AnimatedVisibility(visible = isRefreshing || ussdState is UssdUiState.Executing) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (useWavyProgress) {
                                        com.ams.megascu.ui.components.LinearWavyProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        )
                                    } else {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (ussdState is UssdUiState.Executing) {
                                            "Ejecutando consulta USSD: ${(ussdState as UssdUiState.Executing).code}..."
                                        } else {
                                            "Actualizando datos..."
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else {
                        // MODO EXPERTO: Vista completa con gráficos y consultas
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = innerPadding.calculateTopPadding() + 8.dp, bottom = 4.dp)
                        ) {
                            PriorityStatusCard(
                                planStatus = planStatus,
                                selectedSimSlot = selectedSimSlot,
                                dualSimEnabled = dualSimEnabled,
                                onSelectSimSlot = { viewModel.selectSimSlot(it, context) },
                                onExecuteConsulta = { code, title -> safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot) } },
                                onCardClick = { safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) } }
                            )
                        }

                        // Loading Bar under Main Card when loading/refreshing
                        AnimatedVisibility(visible = isRefreshing || ussdState is UssdUiState.Executing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (useWavyProgress) {
                                    com.ams.megascu.ui.components.LinearWavyProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                } else {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (ussdState is UssdUiState.Executing) {
                                        "Ejecutando consulta USSD: ${(ussdState as UssdUiState.Executing).code}..."
                                    } else {
                                        "Actualizando datos USSD..."
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                UsageHistoryChart(
                                    usageHistory = usageHistory,
                                    planStatus = planStatus,
                                    onRequestRefresh = { safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) } },
                                    hasUsagePermission = hasUsageStatsPermission,
                                    onRequestPermission = onRequestUsageStatsPermission,
                                    useAlternativeEstimator = useAlternativeUsageEstimator,
                                    onToggleAlternativeEstimator = { viewModel.setUseAlternativeUsageEstimator(it) }
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Consultas Section
                                ConsultasSection(
                                    planStatus = planStatus,
                                    onExecuteConsulta = { code, title -> safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot) } }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Main Action Area: Botón de acción rápida configurable
                                val isQuickActionProtected = securityEnabled && (protectionScope == "PURCHASES" || protectionScope == "BOTH") && quickActionCode.contains("*133*")
                                MainActionButton(
                                    quickActionCode = quickActionCode,
                                    quickActionLabel = quickActionLabel,
                                    isPinProtected = isQuickActionProtected,
                                    onConfigureClick = { showQuickActionSelector = true },
                                    onExecuteClick = { code ->
                                        if (securityEnabled && (protectionScope == "PURCHASES" || protectionScope == "BOTH") && code.contains("*133*")) {
                                            pendingPurchaseCode = code
                                        } else {
                                            val title = if (quickActionLabel.isNotBlank()) quickActionLabel else "Consulta Rápida"
                                            safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot, showLoading = false) }
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 24.dp))
                            }
                            
                            // Fade effect
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.background,
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
                }
            }
        }
        }

        // Capa de oscurecimiento de fondo (atrás de hojas y diálogos)
        if (backgroundOverlayAlpha > 0.001f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = backgroundOverlayAlpha))
                    .pointerInput(Unit) {}
            )
        }

        // Sheets and Dialogs
            if (showPlanesSheet) {
                PlanesBottomSheet(
                    planStatus = planStatus,
                    onDismiss = { showPlanesSheet = false },
                    onSelectPlan = { code -> 
                        if (securityEnabled && (protectionScope == "PURCHASES" || protectionScope == "BOTH")) {
                            pendingPurchaseCode = code
                        } else {
                            safeRunUssd { viewModel.executePurchaseUssd(code, selectedSimSlot) }
                        }
                        showPlanesSheet = false 
                    },
                    onRequestRefresh = { safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) } },
                    disableBlur = disableBlurEffects,
                    onProgress = { activeSheetProgress = it }
                )
            }
            if (showGuideSheet) {
                GuideBottomSheet(
                    onDismiss = { showGuideSheet = false },
                    onStartInteractiveTutorial = {
                        showGuideSheet = false
                        showUssdTutorial = true
                    },
                    onExecuteConsulta = { code, title ->
                        safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot) }
                        showGuideSheet = false
                    },
                    onProgress = { activeSheetProgress = it }
                )
            }
            if (showQuickActionSelector) {
                QuickActionSelectorBottomSheet(
                    currentCode = quickActionCode,
                    onDismiss = { showQuickActionSelector = false },
                    onSelectCode = { code, label ->
                        viewModel.setQuickAction(code, label)
                    },
                    onProgress = { activeSheetProgress = it }
                )
            }
            
            if (pendingPurchaseCode != null) {
                AuthDialog(
                    correctPin = securityPin,
                    biometricsEnabled = biometricsEnabled,
                    onAuthSuccess = {
                        viewModel.executePurchaseUssd(pendingPurchaseCode!!, selectedSimSlot)
                        pendingPurchaseCode = null
                    },
                    onDismiss = { pendingPurchaseCode = null }
                )
            }

            
            if (showSettingsSheet) {
                SettingsBottomSheet(
                    smsAlertsEnabled = smsAlertsEnabled,
                    dailyLimitAlertsEnabled = dailyLimitAlertsEnabled,
                    dataThresholdAlertEnabled = dataThresholdAlertEnabled,
                    dataThresholdPercent = dataThresholdPercent,
                    expirationAlertDays = expirationAlertDays,
                    secondExpirationAlertEnabled = secondExpirationAlertEnabled,
                    secondExpirationAlertDays = secondExpirationAlertDays,
                    rechargeAlertEnabled = rechargeAlertEnabled,
                    isSimpleMode = isSimpleMode,
                    useMonospace = useMonospace,
                    useDynamicColors = useDynamicColors,
                    disableBlurEffects = disableBlurEffects,
                    paletteStyle = paletteStyle,
                    dualSimEnabled = dualSimEnabled,
                    themeMode = themeMode,
                    isAmoledMode = isAmoledMode,
                    syncIntervalHours = syncIntervalHours,
                    lowDataThresholdMb = lowDataThresholdMb,
                    onToggleDynamicColors = { viewModel.toggleDynamicColors(it) },
                    onToggleDisableBlurEffects = { viewModel.setDisableBlurEffects(it) },
                    onSelectPaletteStyle = { viewModel.setPaletteStyle(it) },
                    onToggleAmoledMode = { viewModel.setAmoledMode(it) },
                    onToggleSmsAlerts = { viewModel.toggleSmsAlerts(it) },
                    onToggleDailyLimitAlerts = { viewModel.toggleDailyLimitAlerts(it) },
                    onToggleDataThresholdAlerts = { viewModel.toggleDataThresholdAlerts(it) },
                    onSelectDataThresholdPercent = { viewModel.setDataThresholdPercent(it) },
                    onSelectExpirationAlertDays = { viewModel.setExpirationAlertDays(it) },
                    onToggleSecondExpirationAlert = { viewModel.setSecondExpirationAlertEnabled(it) },
                    onSelectSecondExpirationAlertDays = { viewModel.setSecondExpirationAlertDays(it) },
                    onToggleRechargeAlerts = { viewModel.toggleRechargeAlerts(it) },
                    onToggleSimpleMode = { viewModel.toggleSimpleMode(it) },
                    onToggleMonospace = { viewModel.toggleMonospaceFont(it) },
                    onToggleDualSim = { viewModel.toggleDualSim(it, context) },
                    onSelectThemeMode = { viewModel.setThemeMode(it) },
                    onSelectSyncInterval = { viewModel.setSyncIntervalHours(it) },
                    onSelectLowDataThreshold = { viewModel.setLowDataThresholdMb(it) },
                    onClearData = { viewModel.clearAllData() },
                    onResetCoachMark = { viewModel.restartCoachMark() },
                    onStartTutorial = { showUssdTutorial = true },
                    onResetOnboarding = { viewModel.resetOnboarding() },
                    onOpenSecurity = { showSecuritySettings = true },
                    onRequestPermissions = onRequestPermissions,
                    onRequestNotificationPermission = onRequestNotificationPermission,
                    onRequestUsageStatsPermission = onRequestUsageStatsPermission,
                    hasPhonePermission = hasPhonePermission,
                    hasNotificationPermission = hasNotificationPermission,
                    hasUsageStatsPermission = hasUsageStatsPermission,
                    useAlternativeUsageEstimator = useAlternativeUsageEstimator,
                    onToggleAlternativeUsageEstimator = { viewModel.setUseAlternativeUsageEstimator(it) },
                    onExecuteConsulta = { code, title ->
                        safeRunUssd { viewModel.executeUssdQuery(code, title = title, simSlot = selectedSimSlot) }
                    },
                    onExecutePurchase = { code ->
                        safeRunUssd { viewModel.executePurchaseUssd(code, selectedSimSlot) }
                    },
                    onDismiss = { showSettingsSheet = false },
                    onProgress = { activeSheetProgress = it }
                )
            }

            if (showSecuritySettings) {
                com.ams.megascu.ui.components.SecuritySettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showSecuritySettings = false },
                    onSecurityUnlocked = onSecurityUnlocked,
                    onProgress = { activeSheetProgress = it }
                )
            }
            if (showAbout) {
                AboutBottomSheet(
                    onDismiss = { showAbout = false },
                    onOpenChangelog = {
                        showAbout = false
                        showChangelogSheet = true
                    },
                    onProgress = { activeSheetProgress = it }
                )
            }
            if (showChangelogSheet) {
                ChangelogBottomSheet(
                    onDismiss = { showChangelogSheet = false },
                    onProgress = { activeSheetProgress = it }
                )
            }
            if (showMatrixTerminal) {
                com.ams.megascu.ui.components.MatrixTerminalDialog(
                    onAmoledChange = { active -> viewModel.setEasterEggAmoledActive(active) },
                    onDismiss = { showMatrixTerminal = false }
                )
            }

            if (showWhatsNewDialog) {
                com.ams.megascu.ui.components.WhatsNewDialog(
                    onDismiss = { showWhatsNewDialog = false },
                    onViewFullChangelog = {
                        showWhatsNewDialog = false
                        showChangelogSheet = true
                    }
                )
            }

            if (updateCheckResult != null && updateCheckResult!!.isUpdateAvailable) {
                UpdateAvailableDialog(
                    updateResult = updateCheckResult!!,
                    onDismiss = { viewModel.dismissUpdateDialog() }
                )
            }

            if (showUssdTutorial) {
                com.ams.megascu.ui.components.UssdTutorialOverlay(onDismiss = { showUssdTutorial = false })
            }

            // Onboarding Refresh Coach-Mark Overlay for first-time users (works for Normal & Simple Mode)
            if (onboardingComplete && !coachMarkCompleted) {
                RefreshCoachMarkOverlay(
                    onDismiss = { viewModel.completeCoachMark() },
                    onRefreshClick = {
                        viewModel.completeCoachMark()
                        safeRunUssd { viewModel.refreshAllStatus(simSlot = selectedSimSlot) }
                    },
                    isSimpleMode = isSimpleMode
                )
            }

            // Full-screen overlay removed so system USSD prompts (e.g. reply 1 to buy) remain visible and unobscured.

            // Success/Error Dialogs
            when (ussdState) {
                is UssdUiState.Success -> {
                    val state = ussdState as UssdUiState.Success
                    val responseText = state.message.ifBlank { state.rawResponse }
                    val isRechargeHistoryQuery = state.code == "*222*732#" ||
                            state.title?.contains("Historial", ignoreCase = true) == true
                    val isRechargeAvailable = isRechargeHistoryQuery &&
                            (responseText.contains("puede recargar un", ignoreCase = true) ||
                             responseText.contains("UD puede recargar", ignoreCase = true) ||
                             responseText.contains("monto de recarga", ignoreCase = true) ||
                             responseText.contains("puede recargar", ignoreCase = true))

                    AlertDialog(
                        onDismissRequest = { viewModel.dismissUssdDialog() },
                        modifier = Modifier.expressiveModalEntrance(),
                        shape = RoundedCornerShape(28.dp),
                        icon = {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        },
                        title = { 
                            Column {
                                val displayTitle = if (!state.title.isNullOrBlank()) "Respuesta USSD para ${state.title}" else "Respuesta USSD para ${state.code}"
                                Text(
                                    text = displayTitle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Código USSD: ${state.code}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        text = { 
                            Text(
                                text = responseText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        confirmButton = {
                            ExpressiveButton(
                                onClick = {
                                    viewModel.dismissUssdDialog()
                                },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Aceptar", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = if (isRechargeAvailable) {
                            {
                                ExpressiveButton(
                                    onClick = {
                                        viewModel.dismissUssdDialog()
                                        showPlanesSheet = true
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ir a Compras", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else null,
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
                is UssdUiState.Error -> {
                    val state = ussdState as UssdUiState.Error
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissUssdDialog() },
                        modifier = Modifier.expressiveModalEntrance(),
                        shape = RoundedCornerShape(28.dp),
                        icon = {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        },
                        title = { 
                            Column {
                                val displayTitle = if (!state.title.isNullOrBlank()) "Respuesta USSD para ${state.title}" else "Error USSD"
                                Text(
                                    text = displayTitle,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Código USSD: ${state.code}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        text = { 
                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        confirmButton = {
                            ExpressiveButton(
                                onClick = {
                                    viewModel.dismissUssdDialog()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Aceptar", fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
                else -> {}
            }

            // Alerta de SIM no compatible con Cubacel
            nonCubacelSimAlert?.let { alertDetails ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissNonCubacelAlert() },
                    modifier = Modifier.expressiveModalEntrance(),
                    shape = RoundedCornerShape(28.dp),
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.SimCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "SIM no compatible con Cubacel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "La tarjeta SIM en la Ranura ${alertDetails.simSlot} ('${alertDetails.operatorName}') no pertenece a la operadora Cubacel (ETECSA).\n\nMegasCU es una aplicación diseñada exclusivamente para la red móvil de Cuba. Se ha bloqueado la ejecución de códigos USSD en esta SIM para evitar llamadas o cobros no deseados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        ExpressiveButton(
                            onClick = {
                                viewModel.dismissNonCubacelAlert()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // Alerta de SIM no detectada o no instalada
            missingSimAlertSlot?.let { slot ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMissingSimAlert() },
                    modifier = Modifier.expressiveModalEntrance(),
                    shape = RoundedCornerShape(28.dp),
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                DualSimIcon(
                                    tint = MaterialTheme.colorScheme.error,
                                    contourColor = MaterialTheme.colorScheme.errorContainer,
                                    size = 32.dp
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "Segunda SIM no detectada",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "No se detectó una segunda tarjeta SIM (SIM $slot) instalada o activa en el dispositivo.\n\nAsegúrate de tener una tarjeta SIM insertada en la ranura $slot para poder consultar o gestionar esta línea.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        ExpressiveButton(
                            onClick = {
                                viewModel.dismissMissingSimAlert()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // Alerta de permiso telefónico faltante para ejecutar consultas
            if (showPhonePermissionRequiredDialog) {
                val context = LocalContext.current
                AlertDialog(
                    onDismissRequest = { showPhonePermissionRequiredDialog = false },
                    modifier = Modifier.expressiveModalEntrance(),
                    shape = RoundedCornerShape(28.dp),
                    icon = {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "Permiso de Teléfono Necesario",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = "MegasCU requiere permisos de Teléfono para consultar automáticamente tus saldos, megas y planes mediante la red móvil de Cubacel.\n\nPuedes conceder el permiso ahora o abrir los ajustes del sistema.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        ExpressiveButton(
                            onClick = {
                                showPhonePermissionRequiredDialog = false
                                onRequestPermissions()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("dialog_grant_phone_permission")
                        ) {
                            Text("Conceder", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        ExpressiveTextButton(
                            onClick = {
                                showPhonePermissionRequiredDialog = false
                                PermissionUtils.openAppSettings(context)
                            },
                            modifier = Modifier.testTag("dialog_open_app_settings")
                        ) {
                            Text("Abrir Ajustes")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }

@Composable
fun SimpleModeRefreshFab(
    onClick: () -> Unit,
    isRefreshing: Boolean,
    useWavyProgress: Boolean,
    refreshIndicatorType: String,
    modifier: Modifier = Modifier
) {
    var showText by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3500)
        showText = false
    }

    ExpressiveFloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        ),
        modifier = modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRefreshing) {
                if (!useWavyProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    when (refreshIndicatorType) {
                        "loading_indicator" -> {
                            ShapeMorphingLoadingIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        "circular_wavy" -> {
                            com.ams.megascu.ui.components.CircularWavyProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        else -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Actualizar",
                    modifier = Modifier.size(28.dp)
                )
            }

            AnimatedVisibility(
                visible = showText && !isRefreshing,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Actualizar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
