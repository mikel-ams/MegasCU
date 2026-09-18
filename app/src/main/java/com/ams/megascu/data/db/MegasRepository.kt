package com.ams.megascu.data.db

import kotlinx.coroutines.launch

import android.content.Context
import android.content.SharedPreferences
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ams.megascu.data.ussd.ParsedPlanData
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.service.SyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MegasRepository(
    private val context: Context,
    private val planDao: PlanDao,
    private val smsLogDao: SmsLogDao,
    private val usageHistoryDao: UsageHistoryDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)

    private val _smsAlertsEnabled = MutableStateFlow(prefs.getBoolean(PREF_SMS_ALERTS_ENABLED, true))
    val smsAlertsEnabled: StateFlow<Boolean> = _smsAlertsEnabled

    private val _dailyLimitAlertsEnabled = MutableStateFlow(prefs.getBoolean("daily_limit_alerts_enabled", true))
    val dailyLimitAlertsEnabled: StateFlow<Boolean> = _dailyLimitAlertsEnabled

    private val _dataThresholdAlertEnabled = MutableStateFlow(prefs.getBoolean("pref_data_threshold_alert_enabled", true))
    val dataThresholdAlertEnabled: StateFlow<Boolean> = _dataThresholdAlertEnabled

    private val _dataThresholdPercent = MutableStateFlow(prefs.getInt("pref_data_threshold_percent", 80))
    val dataThresholdPercent: StateFlow<Int> = _dataThresholdPercent

    private val _expirationAlertDays = MutableStateFlow(prefs.getInt("pref_expiration_alert_days", 5))
    val expirationAlertDays: StateFlow<Int> = _expirationAlertDays

    private val _secondExpirationAlertEnabled = MutableStateFlow(prefs.getBoolean("pref_second_expiration_alert_enabled", false))
    val secondExpirationAlertEnabled: StateFlow<Boolean> = _secondExpirationAlertEnabled

    private val _secondExpirationAlertDays = MutableStateFlow(prefs.getInt("pref_second_expiration_alert_days", 2))
    val secondExpirationAlertDays: StateFlow<Int> = _secondExpirationAlertDays

    private val _rechargeAlertEnabled = MutableStateFlow(prefs.getBoolean("pref_recharge_alert_enabled", true))
    val rechargeAlertEnabled: StateFlow<Boolean> = _rechargeAlertEnabled

    private val _onboardingComplete = MutableStateFlow(prefs.getBoolean(PREF_ONBOARDING_COMPLETE, false))
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete

    private val _coachMarkCompleted = MutableStateFlow(prefs.getBoolean("pref_coach_mark_completed", false))
    val coachMarkCompleted: StateFlow<Boolean> = _coachMarkCompleted

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(PREF_DARK_THEME, false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _themeMode = MutableStateFlow(prefs.getString(PREF_THEME_MODE, "LIGHT") ?: "LIGHT")
    val themeMode: StateFlow<String> = _themeMode

    private val _isAmoledMode = MutableStateFlow(prefs.getBoolean("pref_use_amoled", false))
    val isAmoledMode: StateFlow<Boolean> = _isAmoledMode

    private val _dualSimEnabled = MutableStateFlow(prefs.getBoolean("pref_dual_sim_enabled", false))
    val dualSimEnabled: StateFlow<Boolean> = _dualSimEnabled

    private val _syncIntervalHours = MutableStateFlow(prefs.getInt(PREF_SYNC_INTERVAL_HOURS, 6))
    val syncIntervalHours: StateFlow<Int> = _syncIntervalHours

    private val _lowDataThresholdMb = MutableStateFlow(prefs.getLong(PREF_LOW_DATA_THRESHOLD, 200L))
    val lowDataThresholdMb: StateFlow<Long> = _lowDataThresholdMb

    private val _useDynamicColors = MutableStateFlow(prefs.getBoolean("pref_use_dynamic_colors", false))
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors

    private val _disableBlurEffects = MutableStateFlow(prefs.getBoolean("pref_disable_blur_effects", false))
    val disableBlurEffects: StateFlow<Boolean> = _disableBlurEffects

    private val _paletteStyle = MutableStateFlow(prefs.getString("pref_palette_style", "Tonal Spot") ?: "Tonal Spot")
    val paletteStyle: StateFlow<String> = _paletteStyle

    private val _useMonospaceFont = MutableStateFlow(prefs.getBoolean("pref_use_monospace", true))
    val useMonospaceFont: StateFlow<Boolean> = _useMonospaceFont

    private val _isSimpleMode = MutableStateFlow(prefs.getBoolean("pref_is_simple_mode", false))
    val isSimpleMode: StateFlow<Boolean> = _isSimpleMode

    private val _useAlternativeUsageEstimator = MutableStateFlow(prefs.getBoolean("pref_use_alternative_usage_estimator", false))
    val useAlternativeUsageEstimator: StateFlow<Boolean> = _useAlternativeUsageEstimator

    fun setUseAlternativeUsageEstimator(enabled: Boolean) {
        prefs.edit().putBoolean("pref_use_alternative_usage_estimator", enabled).apply()
        _useAlternativeUsageEstimator.value = enabled
    }


    
    private val _securityEnabled = MutableStateFlow(prefs.getBoolean("pref_security_enabled", false))
    val securityEnabled: StateFlow<Boolean> = _securityEnabled

    private val _securityPin = MutableStateFlow(prefs.getString("pref_security_pin", "") ?: "")
    val securityPin: StateFlow<String> = _securityPin

    private val _biometricsEnabled = MutableStateFlow(prefs.getBoolean("pref_biometrics_enabled", false))
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled

    private val _protectionScope = MutableStateFlow(prefs.getString("pref_protection_scope", "BOTH") ?: "PURCHASES")
    val protectionScope: StateFlow<String> = _protectionScope


    private val _quickActionCode = MutableStateFlow(prefs.getString("pref_quick_action_code", "") ?: "")
    val quickActionCode: StateFlow<String> = _quickActionCode

    private val _quickActionLabel = MutableStateFlow(prefs.getString("pref_quick_action_label", "") ?: "")
    val quickActionLabel: StateFlow<String> = _quickActionLabel

    fun setQuickAction(code: String, label: String) {
        prefs.edit()
            .putString("pref_quick_action_code", code)
            .putString("pref_quick_action_label", label)
            .apply()
        _quickActionCode.value = code
        _quickActionLabel.value = label
    }

    fun setSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_security_enabled", enabled).apply()
        _securityEnabled.value = enabled
    }

    fun setSecurityPin(pin: String) {
        prefs.edit().putString("pref_security_pin", pin).apply()
        _securityPin.value = pin
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_biometrics_enabled", enabled).apply()
        _biometricsEnabled.value = enabled
    }

    fun setProtectionScope(scope: String) {
        prefs.edit().putString("pref_protection_scope", scope).apply()
        _protectionScope.value = scope
    }


    val planStatus: Flow<PlanStatusEntity?> = getPlanStatus(1)
    fun getPlanStatus(simSlot: Int = 1): Flow<PlanStatusEntity?> = planDao.getPlanStatus(simSlot).map { entity ->
        if (entity == null) return@map null
        val currentSubId = SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
        if (currentSubId != null) {
            if (entity.subscriptionId != currentSubId) {
                return@map null
            }
        } else {
            if (entity.subscriptionId != null) {
                return@map null
            }
        }
        entity
    }

    suspend fun getPlanStatusDirect(simSlot: Int = 1): PlanStatusEntity? {
        val entity = planDao.getPlanStatusDirect(simSlot) ?: return null
        val currentSubId = SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
        if (currentSubId != null) {
            if (entity.subscriptionId != currentSubId) {
                return null
            }
        } else {
            if (entity.subscriptionId != null) {
                return null
            }
        }
        return entity
    }
    val smsLogs: Flow<List<SmsLogEntity>> = smsLogDao.getAllSmsLogs()

    fun getSmsLogs(simSlot: Int? = null): Flow<List<SmsLogEntity>> {
        return if (simSlot == null) {
            smsLogDao.getAllSmsLogs()
        } else {
            smsLogDao.getSmsLogsForSim(simSlot)
        }
    }
    val usageHistory: Flow<List<UsageHistoryEntity>> = usageHistoryDao.getUsageHistory()

    fun getUsageHistory(simSlot: Int? = null): Flow<List<UsageHistoryEntity>> {
        return if (simSlot == null) {
            usageHistoryDao.getUsageHistory()
        } else {
            usageHistoryDao.getUsageHistoryForSim(simSlot)
        }
    }

    suspend fun checkAndUpdateMidnightDays() {
        try {
            for (slot in 1..2) {
                val plan = planDao.getPlanStatusDirect(slot) ?: continue
                com.ams.megascu.service.PlanExpirationAlertManager.checkAndUpdateMidnightDays(context, plan)
            }
        } catch (e: Exception) {
            // Silencioso ante cierre de conexión o terminación de test
        }
    }

    init {
        scheduleSyncWorker(_syncIntervalHours.value)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                checkAndUpdateMidnightDays()
            } catch (e: Exception) {
                // Silencioso si la BD se cierra durante el ciclo de vida o tests
            }
        }
    }

    fun setSmsAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_SMS_ALERTS_ENABLED, enabled).apply()
        _smsAlertsEnabled.value = enabled
    }

    fun setDailyLimitAlertsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("daily_limit_alerts_enabled", enabled).apply()
        _dailyLimitAlertsEnabled.value = enabled
    }

    fun setDataThresholdAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_data_threshold_alert_enabled", enabled).apply()
        _dataThresholdAlertEnabled.value = enabled
    }

    fun setDataThresholdPercent(percent: Int) {
        prefs.edit().putInt("pref_data_threshold_percent", percent).apply()
        _dataThresholdPercent.value = percent
    }

    fun setExpirationAlertDays(days: Int) {
        prefs.edit().putInt("pref_expiration_alert_days", days).apply()
        _expirationAlertDays.value = days
    }

    fun setSecondExpirationAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_second_expiration_alert_enabled", enabled).apply()
        _secondExpirationAlertEnabled.value = enabled
    }

    fun setSecondExpirationAlertDays(days: Int) {
        prefs.edit().putInt("pref_second_expiration_alert_days", days).apply()
        _secondExpirationAlertDays.value = days
    }

    fun setRechargeAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_recharge_alert_enabled", enabled).apply()
        _rechargeAlertEnabled.value = enabled
    }

    fun setOnboardingComplete() {
        prefs.edit().putBoolean(PREF_ONBOARDING_COMPLETE, true).apply()
        _onboardingComplete.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean(PREF_ONBOARDING_COMPLETE, false).apply()
        _onboardingComplete.value = false
    }

    fun resetAllSettings() {
        prefs.edit().clear().apply()
        _onboardingComplete.value = false
        _securityEnabled.value = false
        _securityPin.value = ""
        _biometricsEnabled.value = false
        _protectionScope.value = "BOTH"
        _isSimpleMode.value = false
        _themeMode.value = "LIGHT"
        _isDarkTheme.value = false
        _isAmoledMode.value = false
        _dualSimEnabled.value = false
        _useDynamicColors.value = false
        _useMonospaceFont.value = true
        _coachMarkCompleted.value = false
        _quickActionCode.value = ""
        _quickActionLabel.value = ""
        _syncIntervalHours.value = 6
        _dataThresholdAlertEnabled.value = true
        _dataThresholdPercent.value = 80
        _expirationAlertDays.value = 5
        _secondExpirationAlertEnabled.value = false
        _secondExpirationAlertDays.value = 2
        _dailyLimitAlertsEnabled.value = true
        _smsAlertsEnabled.value = true
        _lowDataThresholdMb.value = 200L
        _disableBlurEffects.value = false
        _paletteStyle.value = "Tonal Spot"
    }

    fun setSimpleMode(enabled: Boolean) {
        prefs.edit().putBoolean("pref_is_simple_mode", enabled).apply()
        _isSimpleMode.value = enabled
    }

    fun setCoachMarkCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean("pref_coach_mark_completed", completed).apply()
        _coachMarkCompleted.value = completed
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
        if (enabled) setThemeMode("DARK") else setThemeMode("LIGHT")
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(PREF_THEME_MODE, mode).apply()
        _themeMode.value = mode
        if (mode == "DARK" || mode == "AMOLED") {
            prefs.edit().putBoolean(PREF_DARK_THEME, true).apply()
            _isDarkTheme.value = true
        } else if (mode == "LIGHT") {
            prefs.edit().putBoolean(PREF_DARK_THEME, false).apply()
            _isDarkTheme.value = false
        }
    }

    fun setAmoledMode(enabled: Boolean) {
        prefs.edit().putBoolean("pref_use_amoled", enabled).apply()
        _isAmoledMode.value = enabled
    }


    fun setSyncIntervalHours(hours: Int) {
        prefs.edit().putInt(PREF_SYNC_INTERVAL_HOURS, hours).apply()
        _syncIntervalHours.value = hours
        scheduleSyncWorker(hours)
    }

    fun setLowDataThresholdMb(thresholdMb: Long) {
        prefs.edit().putLong(PREF_LOW_DATA_THRESHOLD, thresholdMb).apply()
        _lowDataThresholdMb.value = thresholdMb
    }

    fun calculateInitialDelayMs(hours: Int): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetHours = when (hours) {
            12 -> listOf(8, 20)
            6 -> listOf(2, 8, 14, 20)
            24 -> listOf(8)
            3 -> listOf(2, 5, 8, 11, 14, 17, 20, 23)
            1 -> (0..23).toList()
            else -> {
                val list = mutableListOf<Int>()
                for (h in 0..23) {
                    if (Math.floorMod(h - 8, hours) == 0) {
                        list.add(h)
                    }
                }
                list.ifEmpty { listOf(8) }
            }
        }

        var minDiffMs = Long.MAX_VALUE
        for (dayOffset in 0..1) {
            for (targetHour in targetHours) {
                candidate.timeInMillis = now.timeInMillis
                candidate.set(Calendar.MINUTE, 0)
                candidate.set(Calendar.SECOND, 0)
                candidate.set(Calendar.MILLISECOND, 0)
                if (dayOffset > 0) {
                    candidate.add(Calendar.DAY_OF_YEAR, dayOffset)
                }
                candidate.set(Calendar.HOUR_OF_DAY, targetHour)
                val diff = candidate.timeInMillis - now.timeInMillis
                if (diff > 0 && diff < minDiffMs) {
                    minDiffMs = diff
                }
            }
        }
        return if (minDiffMs != Long.MAX_VALUE) minDiffMs.coerceAtLeast(60_000L) else (hours * 3600_000L).coerceAtLeast(60_000L)
    }

    fun scheduleSyncWorker(hours: Int) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val initialDelayMs = calculateInitialDelayMs(hours)

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(hours.toLong(), TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_SYNC,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUseDynamicColors(enabled: Boolean) {
        prefs.edit().putBoolean("pref_use_dynamic_colors", enabled).apply()
        _useDynamicColors.value = enabled
    }

    fun setDisableBlurEffects(disabled: Boolean) {
        prefs.edit().putBoolean("pref_disable_blur_effects", disabled).apply()
        _disableBlurEffects.value = disabled
    }

    fun setPaletteStyle(style: String) {
        prefs.edit().putString("pref_palette_style", style).apply()
        _paletteStyle.value = style
    }

    fun setDualSimEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_dual_sim_enabled", enabled).apply()
        _dualSimEnabled.value = enabled
    }

    fun setUseMonospaceFont(enabled: Boolean) {
        prefs.edit().putBoolean("pref_use_monospace", enabled).apply()
        _useMonospaceFont.value = enabled
    }

    suspend fun saveParsedData(parsed: ParsedPlanData, rawResponse: String, simSlot: Int = 1) {
        val currentSubId = SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
        var currentStatus = planDao.getPlanStatusDirect(simSlot)
        if (currentStatus != null) {
            if (currentSubId != null) {
                if (currentStatus.subscriptionId != currentSubId) {
                    currentStatus = PlanStatusEntity(id = simSlot, subscriptionId = currentSubId)
                }
            } else {
                if (currentStatus.subscriptionId != null) {
                    currentStatus = PlanStatusEntity(id = simSlot, subscriptionId = null)
                }
            }
        } else {
            currentStatus = PlanStatusEntity(id = simSlot, subscriptionId = currentSubId)
        }
        val now = System.currentTimeMillis()

        val newBalance = parsed.balanceCup ?: currentStatus.balanceCup
        val newDataMb = parsed.dataMb ?: currentStatus.dataMb
        val newDataLteMb = parsed.dataLteMb ?: currentStatus.dataLteMb
        val newBonusMb = parsed.bonusMb ?: currentStatus.bonusDataMb
        val newMinStr = parsed.minutesStr ?: currentStatus.minutesStr
        val newSms = parsed.sms ?: currentStatus.smsCount
        val newDataDays = parsed.dataDays ?: currentStatus.dataDays
        val newMinutesDays = parsed.minutesDays ?: currentStatus.minutesDays
        val newSmsDays = parsed.smsDays ?: currentStatus.smsDays
        val newNextRecharge = parsed.nextRechargeDateStr ?: currentStatus.nextRechargeDateStr
        val newNextRechargeDays = parsed.nextRechargeDays ?: currentStatus.nextRechargeDays

        val updatedEntity = currentStatus.copy(
            id = simSlot,
            subscriptionId = currentSubId,
            balanceCup = newBalance,
            dataMb = newDataMb,
            dataLteMb = newDataLteMb,
            bonusDataMb = newBonusMb,
            minutesStr = newMinStr,
            smsCount = newSms,
            dataDays = newDataDays,
            minutesDays = newMinutesDays,
            smsDays = newSmsDays,
            nextRechargeDateStr = newNextRecharge,
            nextRechargeDays = newNextRechargeDays,
            lastUpdatedTimestamp = now,
            rawLastResponse = rawResponse
        )
        planDao.insertOrUpdatePlanStatus(updatedEntity)

        if (currentStatus.balanceCup != newBalance || currentStatus.dataMb != newDataMb || currentStatus.dataLteMb != newDataLteMb) {
            val history = UsageHistoryEntity(
                 balanceCup = newBalance,
                 dataMb = newDataMb,
                 dataLteMb = newDataLteMb,
                 bonusDataMb = newBonusMb,
                 timestamp = now,
                 simSlot = simSlot,
                 subscriptionId = currentSubId
            )
            usageHistoryDao.insertUsageHistory(history)
        }

        try {
            com.ams.megascu.widget.MegasWidgetProvider.updateAllWidgets(context)
            com.ams.megascu.service.DailyLimitAlertManager.checkAndNotify(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun logInterceptedSms(
        sender: String,
        messageBody: String,
        extractedDateStr: String?,
        simSlot: Int = 1,
        subscriptionId: Int? = null
    ) {
        val currentSubId = subscriptionId ?: SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
        val log = SmsLogEntity(
            sender = sender,
            messageBody = messageBody,
            extractedDateStr = extractedDateStr,
            simSlot = simSlot,
            subscriptionId = currentSubId
        )
        smsLogDao.insertSmsLog(log)
        val currentStatus = planDao.getPlanStatusDirect(simSlot)
        if (currentStatus != null) {
            val updatedEntity = currentStatus.copy(
                rawLastResponse = messageBody
            )
            planDao.insertOrUpdatePlanStatus(updatedEntity)
        }
    }

    suspend fun clearSmsLogs(simSlot: Int? = null) {
        if (simSlot == null) {
            smsLogDao.clearLogs()
        } else {
            smsLogDao.clearLogsForSim(simSlot)
        }
    }

    suspend fun clearAllData() {
        planDao.clearPlanStatus()
        usageHistoryDao.clearHistory()
        smsLogDao.clearLogs()
    }

    companion object {
        private const val PREF_SMS_ALERTS_ENABLED = "sms_alerts_enabled"
        private const val PREF_ONBOARDING_COMPLETE = "onboarding_complete_rc"
        private const val PREF_DARK_THEME = "dark_theme"
        private const val PREF_THEME_MODE = "theme_mode"
        private const val PREF_SYNC_INTERVAL_HOURS = "sync_interval_hours"
        private const val PREF_LOW_DATA_THRESHOLD = "low_data_threshold_mb"
        private const val WORK_NAME_SYNC = "etecsa_background_sync"
    }
}
