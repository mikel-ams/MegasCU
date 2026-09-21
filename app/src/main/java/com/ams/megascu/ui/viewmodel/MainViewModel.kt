package com.ams.megascu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ams.megascu.MegasApplication
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.data.ussd.UssdCallback
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.service.EtecsaMonitoringService
import com.ams.megascu.utils.GitHubUpdateChecker
import com.ams.megascu.utils.UpdateCheckResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed interface UssdUiState {
    object Idle : UssdUiState
    data class Executing(val code: String, val title: String? = null) : UssdUiState
    data class Success(val code: String, val rawResponse: String, val message: String, val title: String? = null) : UssdUiState
    data class Error(val code: String, val errorMessage: String, val title: String? = null) : UssdUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as MegasApplication
    private val repository = app.repository
    val ussdExecutor = UssdExecutor(application)

    private val _selectedSimSlot = MutableStateFlow(1)
    val selectedSimSlot: StateFlow<Int> = _selectedSimSlot

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val planStatus: StateFlow<PlanStatusEntity?> = _selectedSimSlot
        .flatMapLatest { slot -> repository.getPlanStatus(slot) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    val smsAlertsEnabled: StateFlow<Boolean> = repository.smsAlertsEnabled
    val dailyLimitAlertsEnabled: StateFlow<Boolean> = repository.dailyLimitAlertsEnabled
    val dataThresholdAlertEnabled: StateFlow<Boolean> = repository.dataThresholdAlertEnabled
    val dataThresholdPercent: StateFlow<Int> = repository.dataThresholdPercent
    val expirationAlertDays: StateFlow<Int> = repository.expirationAlertDays
    val secondExpirationAlertEnabled: StateFlow<Boolean> = repository.secondExpirationAlertEnabled
    val secondExpirationAlertDays: StateFlow<Int> = repository.secondExpirationAlertDays
    val rechargeAlertEnabled: StateFlow<Boolean> = repository.rechargeAlertEnabled
    val onboardingComplete: StateFlow<Boolean> = repository.onboardingComplete
    val coachMarkCompleted: StateFlow<Boolean> = repository.coachMarkCompleted
    val isDarkTheme: StateFlow<Boolean> = repository.isDarkTheme
    val themeMode: StateFlow<String> = repository.themeMode
    val isAmoledMode: StateFlow<Boolean> = repository.isAmoledMode
    val syncIntervalHours: StateFlow<Int> = repository.syncIntervalHours
    val lowDataThresholdMb: StateFlow<Long> = repository.lowDataThresholdMb
    val useMonospaceFont: StateFlow<Boolean> = repository.useMonospaceFont
    val isSimpleMode: StateFlow<Boolean> = repository.isSimpleMode
    val useAlternativeUsageEstimator: StateFlow<Boolean> = repository.useAlternativeUsageEstimator
    val useDynamicColors: StateFlow<Boolean> = repository.useDynamicColors
    val disableBlurEffects: StateFlow<Boolean> = repository.disableBlurEffects
    val paletteStyle: StateFlow<String> = repository.paletteStyle
    val dualSimEnabled: StateFlow<Boolean> = repository.dualSimEnabled

    private val _pendingAction = MutableStateFlow<String?>(null)
    val pendingAction: StateFlow<String?> = _pendingAction

    val securityEnabled: StateFlow<Boolean> = repository.securityEnabled
    val securityPin: StateFlow<String> = repository.securityPin
    val biometricsEnabled: StateFlow<Boolean> = repository.biometricsEnabled
    val protectionScope: StateFlow<String> = repository.protectionScope

    val quickActionCode: StateFlow<String> = repository.quickActionCode
    val quickActionLabel: StateFlow<String> = repository.quickActionLabel

    fun setQuickAction(code: String, label: String) = repository.setQuickAction(code, label)

    fun setSecurityEnabled(enabled: Boolean) = repository.setSecurityEnabled(enabled)
    fun setSecurityPin(pin: String) = repository.setSecurityPin(pin)
    fun setBiometricsEnabled(enabled: Boolean) = repository.setBiometricsEnabled(enabled)
    fun setProtectionScope(scope: String) = repository.setProtectionScope(scope)


    val usageHistory = _selectedSimSlot
        .flatMapLatest { slot -> repository.getUsageHistory(slot) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val smsLogs = _selectedSimSlot
        .flatMapLatest { slot -> repository.getSmsLogs(slot) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _ussdState = MutableStateFlow<UssdUiState>(UssdUiState.Idle)
    val ussdState: StateFlow<UssdUiState> = _ussdState

    private val _isIndividualQueryRunning = MutableStateFlow(false)
    val isIndividualQueryRunning: StateFlow<Boolean> = _isIndividualQueryRunning

    val isAnyUssdRunning: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _isRefreshing,
        _isIndividualQueryRunning
    ) { refreshing, individual ->
        refreshing || individual
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    private val _missingSimAlertSlot = MutableStateFlow<Int?>(null)
    val missingSimAlertSlot: StateFlow<Int?> = _missingSimAlertSlot

    private val _nonCubacelSimAlert = MutableStateFlow<com.ams.megascu.data.ussd.SimOperatorDetails?>(null)
    val nonCubacelSimAlert: StateFlow<com.ams.megascu.data.ussd.SimOperatorDetails?> = _nonCubacelSimAlert

    private fun isConfirmedNonCubacel(details: com.ams.megascu.data.ussd.SimOperatorDetails): Boolean {
        if (details.isCubacel || details.isPendingInfo || details.isAbsent) return false
        val name = details.operatorName.trim().lowercase()
        if (name.isEmpty() ||
            name.startsWith("sim no disponible") ||
            name.startsWith("operadora desconocida") ||
            name.startsWith("obteniendo información") ||
            name.startsWith("sim ausente") ||
            name.startsWith("sim") ||
            name.startsWith("slot") ||
            name.startsWith("card") ||
            name.contains("unknown")
        ) {
            return false
        }
        return true
    }

    fun selectSimSlot(slot: Int, context: android.content.Context? = null): Boolean {
        val appContext = context ?: getApplication()
        val details = com.ams.megascu.data.ussd.SimOperatorUtils.checkSimOperator(appContext, slot, ussdExecutor.isEmulator())
        val isAvailable = com.ams.megascu.data.ussd.SimOperatorUtils.isSimSlotAvailable(appContext, slot, ussdExecutor.isEmulator())
        if (!isAvailable && details.isAbsent) {
            _missingSimAlertSlot.value = slot
            return false
        }
        if (!details.isCubacel && isConfirmedNonCubacel(details)) {
            _nonCubacelSimAlert.value = details
        } else {
            _nonCubacelSimAlert.value = null
        }
        _selectedSimSlot.value = slot
        refreshMidnightDaysOffline()
        return true
    }

    fun dismissMissingSimAlert() {
        _missingSimAlertSlot.value = null
    }

    fun checkSimOperatorCompatibility(simSlot: Int): Boolean {
        if (ussdExecutor.isEmulator()) return true
        val details = com.ams.megascu.data.ussd.SimOperatorUtils.checkSimOperator(getApplication(), simSlot, ussdExecutor.isEmulator())
        if (!details.isCubacel) {
            if (isConfirmedNonCubacel(details)) {
                _nonCubacelSimAlert.value = details
                return false
            }
            _nonCubacelSimAlert.value = null
            return true
        }
        _nonCubacelSimAlert.value = null
        return true
    }

    fun dismissNonCubacelAlert() {
        _nonCubacelSimAlert.value = null
    }

    init {
        checkDailyLimitAlert()
        refreshMidnightDaysOffline()
    }

    fun refreshMidnightDaysOffline() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                repository.checkAndUpdateMidnightDays()
            } catch (e: Exception) {
                android.util.Log.e("MegasCU", "Unhandled exception", e)
            }
        }
    }

    fun checkDailyLimitAlert() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.ams.megascu.service.DailyLimitAlertManager.checkAndNotify(getApplication())
        }
    }

    fun toggleDailyLimitAlerts(enabled: Boolean) {
        repository.setDailyLimitAlertsEnabled(enabled)
        if (enabled) {
            checkDailyLimitAlert()
        }
    }

    fun toggleDataThresholdAlerts(enabled: Boolean) {
        repository.setDataThresholdAlertEnabled(enabled)
        if (enabled) {
            checkDailyLimitAlert()
        }
    }

    fun setDataThresholdPercent(percent: Int) {
        repository.setDataThresholdPercent(percent)
        checkDailyLimitAlert()
    }

    fun setExpirationAlertDays(days: Int) {
        repository.setExpirationAlertDays(days)
        checkDailyLimitAlert()
    }

    fun setSecondExpirationAlertEnabled(enabled: Boolean) {
        repository.setSecondExpirationAlertEnabled(enabled)
        checkDailyLimitAlert()
    }

    fun setSecondExpirationAlertDays(days: Int) {
        repository.setSecondExpirationAlertDays(days)
        checkDailyLimitAlert()
    }

    fun toggleRechargeAlerts(enabled: Boolean) {
        repository.setRechargeAlertEnabled(enabled)
    }

    fun setPendingAction(action: String?) {
        _pendingAction.value = action
    }

    fun clearPendingAction() {
        _pendingAction.value = null
    }

    fun resetOnboarding() {
        repository.resetOnboarding()
    }

    fun resetAllSettings() {
        repository.resetAllSettings()
    }

    fun toggleSimpleMode(enabled: Boolean) {
        repository.setSimpleMode(enabled)
    }

    fun toggleDarkTheme(enabled: Boolean) {
        repository.setDarkTheme(enabled)
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun setAmoledMode(enabled: Boolean) {
        repository.setAmoledMode(enabled)
    }

    private val _easterEggAmoledActive = MutableStateFlow(false)
    val easterEggAmoledActive: StateFlow<Boolean> = _easterEggAmoledActive

    fun setEasterEggAmoledActive(active: Boolean) {
        _easterEggAmoledActive.value = active
    }


    fun setSyncIntervalHours(hours: Int) {
        repository.setSyncIntervalHours(hours)
    }

    fun setLowDataThresholdMb(thresholdMb: Long) {
        repository.setLowDataThresholdMb(thresholdMb)
    }

    fun toggleDynamicColors(enabled: Boolean) {
        repository.setUseDynamicColors(enabled)
    }

    fun setDisableBlurEffects(disabled: Boolean) {
        repository.setDisableBlurEffects(disabled)
    }

    fun setUseAlternativeUsageEstimator(enabled: Boolean) {
        repository.setUseAlternativeUsageEstimator(enabled)
    }

    fun setPaletteStyle(style: String) {
        repository.setPaletteStyle(style)
    }

    fun toggleMonospaceFont(enabled: Boolean) {
        repository.setUseMonospaceFont(enabled)
    }

    fun toggleDualSim(enabled: Boolean, context: android.content.Context? = null) {
        if (enabled) {
            val appContext = context ?: getApplication()
            val sim2Available = com.ams.megascu.data.ussd.SimOperatorUtils.isSimSlotAvailable(appContext, 2, ussdExecutor.isEmulator())
            if (!sim2Available) {
                repository.setDualSimEnabled(false)
                _missingSimAlertSlot.value = 2
                return
            }
            repository.setDualSimEnabled(true)
            val detailsSim2 = com.ams.megascu.data.ussd.SimOperatorUtils.checkSimOperator(appContext, 2, ussdExecutor.isEmulator())
            if (!detailsSim2.isCubacel && isConfirmedNonCubacel(detailsSim2)) {
                _nonCubacelSimAlert.value = detailsSim2
            } else {
                _nonCubacelSimAlert.value = null
            }
        } else {
            repository.setDualSimEnabled(false)
            _selectedSimSlot.value = 1
        }
    }

    fun executePurchaseUssd(code: String, simSlot: Int = _selectedSimSlot.value) {
        if (!checkSimOperatorCompatibility(simSlot)) {
            return
        }
        if (isAnyUssdRunning.value || ussdExecutor.isBusy()) {
            android.widget.Toast.makeText(
                getApplication(),
                "Hay una consulta USSD en progreso. Por favor, espera a que finalice.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        ussdExecutor.executePurchaseUssd(code, simSlot)
    }

    fun executeUssdQuery(code: String, title: String? = null, simSlot: Int = _selectedSimSlot.value, useSimulationIfError: Boolean = true, silent: Boolean = false, showLoading: Boolean = true) {
        if (!checkSimOperatorCompatibility(simSlot)) {
            return
        }
        if (isAnyUssdRunning.value || ussdExecutor.isBusy()) {
            if (!silent) {
                android.widget.Toast.makeText(
                    getApplication(),
                    "Hay una consulta USSD en progreso. Por favor, espera a que finalice.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        if (code == "vigencia_special") {
            viewModelScope.launch {
                _isIndividualQueryRunning.value = true
                try {
                    if (showLoading) {
                        _ussdState.value = UssdUiState.Executing("Consultando...", title)
                    }
                    executeUssdQuerySuspend("*222#", simSlot, useSimulationIfError, true)
                    kotlinx.coroutines.delay(1000)
                    executeUssdQuerySuspend("*222*328#", simSlot, useSimulationIfError, true)
                    kotlinx.coroutines.delay(1000)
                    executeUssdQuerySuspend("*222*266#", simSlot, useSimulationIfError, true)
                    delay(1000)
                    executeUssdQuerySuspend("*222*869#", simSlot, useSimulationIfError, true)
                    kotlinx.coroutines.delay(1000)
                    executeUssdQuerySuspend("*222*767#", simSlot, useSimulationIfError, true)
                    kotlinx.coroutines.delay(1000)
                    executeUssdQuerySuspend("*222*732#", simSlot, useSimulationIfError, true)
                    _ussdState.value = UssdUiState.Success(code, "Vigencia consultada", "Consulta de datos, saldo y vigencias completada.", title)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    _ussdState.value = UssdUiState.Idle
                } catch (e: Exception) {
                    _ussdState.value = UssdUiState.Error(code, e.localizedMessage ?: "Error al consultar vigencias", title)
                } finally {
                    _isIndividualQueryRunning.value = false
                }
            }
            return
        }

        viewModelScope.launch {
            _isIndividualQueryRunning.value = true
            if (!silent && showLoading) {
                _ussdState.value = UssdUiState.Executing(code, title)
            }
            try {
                when (val result = ussdExecutor.executeUssdSuspend(code, simSlot = simSlot, allowDialFallback = false)) {
                    is com.ams.megascu.data.ussd.UssdResult.Success -> {
                        processAndSaveUssdResponse(code, result.response, silent = silent, title = title, simSlot = simSlot)
                    }
                    is com.ams.megascu.data.ussd.UssdResult.Error -> {
                        if (useSimulationIfError && ussdExecutor.isEmulator()) {
                            val simResponse = ussdExecutor.simulateUssdResponse(code)
                            processAndSaveUssdResponse(code, simResponse, isSimulated = true, silent = silent, title = title, simSlot = simSlot)
                        } else if (!silent) {
                            _ussdState.value = UssdUiState.Error(code, result.message, title)
                        }
                    }
                }
            } finally {
                _isIndividualQueryRunning.value = false
            }
        }
    }

    fun processAndSaveUssdResponse(code: String, responseText: String, isSimulated: Boolean = false, silent: Boolean = false, title: String? = null, simSlot: Int = _selectedSimSlot.value) {
        viewModelScope.launch {
            val parsed = EtecsaUssdParser.parseUssdResponse(responseText, code)
            repository.saveParsedData(parsed, responseText, simSlot = simSlot)
            if (!silent) {
                val successMsg = if (isSimulated) {
                    "Simulado (SIM $simSlot):\n$responseText"
                } else {
                    responseText
                }
                _ussdState.value = UssdUiState.Success(code, responseText, successMsg, title)
            }
        }
    }

    fun toggleSmsAlerts(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSmsAlertsEnabled(enabled)
            if (!enabled) {
                EtecsaMonitoringService.stopAlert(app)
            } else {
                // Not generating SMS alert because date is not standard
            }
        }
    }

    fun simulateIncomingSms(smsText: String, simSlot: Int = _selectedSimSlot.value) {
        viewModelScope.launch {
            val extractedDate = EtecsaUssdParser.parseEtecsaSms(smsText)
            val subId = SimOperatorUtils.getSubscriptionIdForSlot(app, simSlot)
            repository.logInterceptedSms("Cubacel-ETECSA", smsText, extractedDate, simSlot = simSlot, subscriptionId = subId)
        }
    }

    fun dismissUssdDialog() {
        _ussdState.value = UssdUiState.Idle
    }

    private var refreshJob: kotlinx.coroutines.Job? = null

    fun refreshAllStatus(simSlot: Int = _selectedSimSlot.value) {
        if (!checkSimOperatorCompatibility(simSlot)) {
            return
        }
        if (isAnyUssdRunning.value || ussdExecutor.isBusy()) {
            android.widget.Toast.makeText(
                getApplication(),
                "Hay una consulta USSD en curso. Por favor, espera a que finalice.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val startTime = System.currentTimeMillis()
        val codes = listOf("*222#", "*222*328#", "*222*266#", "*222*869#", "*222*767#", "*222*732#")
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val isEmu = ussdExecutor.isEmulator()
                codes.forEach { code ->
                    executeUssdQuerySuspend(code, simSlot = simSlot, useSimulationIfError = isEmu, silent = true)
                    kotlinx.coroutines.delay(1000)
                }

                // Verification pass: If key status metrics are 0, perform a secondary retry for data packages
                val currentStatus = repository.getPlanStatusDirect(simSlot)
                if (currentStatus != null) {
                    if (currentStatus.dataMb == 0L || currentStatus.dataLteMb == 0L) {
                        executeUssdQuerySuspend("*222*328#", simSlot = simSlot, useSimulationIfError = isEmu, silent = true)
                    }
                    if (currentStatus.balanceCup == 0.0) {
                        executeUssdQuerySuspend("*222#", simSlot = simSlot, useSimulationIfError = isEmu, silent = true)
                    }
                }

                val elapsedMs = System.currentTimeMillis() - startTime
                val secondsStr = String.format(java.util.Locale.US, "%.1f", elapsedMs / 1000.0)
                android.widget.Toast.makeText(
                    getApplication(),
                    "Actualización SIM $simSlot completada en $secondsStr s",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Cancelled gracefully
            } catch (e: Exception) {
                android.util.Log.e("MegasCU", "Unhandled exception", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private suspend fun executeUssdQuerySuspend(code: String, simSlot: Int = 1, useSimulationIfError: Boolean = true, silent: Boolean = false) {
        if (!silent) {
            _ussdState.value = UssdUiState.Executing(code)
        }

        when (val result = ussdExecutor.executeUssdSuspend(code, simSlot = simSlot, allowDialFallback = false)) {
            is com.ams.megascu.data.ussd.UssdResult.Success -> {
                val parsed = EtecsaUssdParser.parseUssdResponse(result.response, code)
                repository.saveParsedData(parsed, result.response, simSlot = simSlot)
                if (!silent) {
                    _ussdState.value = UssdUiState.Success(code, result.response, result.response)
                }
            }
            is com.ams.megascu.data.ussd.UssdResult.Error -> {
                if (useSimulationIfError && ussdExecutor.isEmulator()) {
                    val simResponse = ussdExecutor.simulateUssdResponse(code)
                    val parsed = EtecsaUssdParser.parseUssdResponse(simResponse, code)
                    repository.saveParsedData(parsed, simResponse, simSlot = simSlot)
                    if (!silent) {
                        _ussdState.value = UssdUiState.Success(code, simResponse, "Simulado (SIM $simSlot):\n$simResponse")
                    }
                } else if (!silent) {
                    _ussdState.value = UssdUiState.Error(code, result.message)
                }
            }
        }
    }

    fun completeOnboarding() {
        repository.setOnboardingComplete()
    }

    fun completeCoachMark() {
        repository.setCoachMarkCompleted(true)
    }

    fun restartCoachMark() {
        repository.setCoachMarkCompleted(false)
    }

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResult?>(null)
    val updateCheckResult: StateFlow<UpdateCheckResult?> = _updateCheckResult

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate

    /**
     * Realiza una comprobación silenciosa en background con el servidor de GitHub al iniciar la app.
     * Si detecta una nueva versión disponible, despliega automáticamente el diálogo de actualización.
     */
    fun checkUpdatesSilentlyOnLaunch() {
        viewModelScope.launch {
            try {
                val prefs = app.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE)
                val autoCheck = prefs.getBoolean(GitHubUpdateChecker.PREF_AUTO_UPDATE_CHECK, true)
                if (!autoCheck) return@launch

                val result = GitHubUpdateChecker.checkForUpdates(app)
                if (result.isSuccess && result.isUpdateAvailable) {
                    val dismissed = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_DISMISSED_VERSION, "")
                    if (result.latestVersionName != dismissed) {
                        _updateCheckResult.value = result
                    }
                }
            } catch (e: Exception) {
                // Comprobación silenciosa: no se interrumpe la navegación del usuario ante fallos de red
            }
        }
    }

    fun checkForAppUpdates(force: Boolean = false) {
        viewModelScope.launch {
            val prefs = app.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE)
            val autoCheck = prefs.getBoolean(GitHubUpdateChecker.PREF_AUTO_UPDATE_CHECK, true)
            if (!force && !autoCheck) return@launch

            val lastCheck = prefs.getLong(GitHubUpdateChecker.PREF_LAST_UPDATE_CHECK_TIME, 0L)
            val now = System.currentTimeMillis()
            // If not forced, only query network if more than 24h passed
            if (!force && (now - lastCheck < 24 * 60 * 60 * 1000L)) {
                // If there is already a saved update available and not dismissed
                val isAvailable = prefs.getBoolean(GitHubUpdateChecker.PREF_UPDATE_AVAILABLE, false)
                val versionName = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_VERSION_NAME, "") ?: ""
                val dismissed = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_DISMISSED_VERSION, "")
                if (isAvailable && versionName.isNotBlank() && versionName != dismissed) {
                    _updateCheckResult.value = UpdateCheckResult(
                        isSuccess = true,
                        isUpdateAvailable = true,
                        latestVersionName = versionName,
                        latestVersionCode = prefs.getInt(GitHubUpdateChecker.PREF_UPDATE_VERSION_CODE, 0),
                        releaseTitle = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_TITLE, "") ?: "",
                        changelog = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_CHANGELOG, "") ?: "",
                        apkDownloadUrl = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_APK_URL, null),
                        releaseHtmlUrl = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_RELEASE_URL, "") ?: "",
                        sha256Checksum = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_SHA256, null)
                    )
                }
                return@launch
            }

            _isCheckingUpdate.value = true
            val result = GitHubUpdateChecker.checkForUpdates(app)
            _isCheckingUpdate.value = false
            if (result.isSuccess && result.isUpdateAvailable) {
                val dismissed = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_DISMISSED_VERSION, "")
                if (force || result.latestVersionName != dismissed) {
                    _updateCheckResult.value = result
                }
            } else if (force) {
                _updateCheckResult.value = result
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateCheckResult.value = null
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            try {
                val prefs = app.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("pref_force_emulator_mode", false).apply()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
