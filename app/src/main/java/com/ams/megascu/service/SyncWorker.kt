package com.ams.megascu.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.db.UsageHistoryEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdResult
import com.ams.megascu.widget.MegasWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = MegasDatabase.getDatabase(applicationContext)
            val planDao = db.planDao()
            val usageHistoryDao = db.usageHistoryDao()
            val ussdExecutor = UssdExecutor(applicationContext)
            val prefs = applicationContext.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)

            val dualSimEnabled = prefs.getBoolean("pref_dual_sim_enabled", false)
            val isEmulator = ussdExecutor.isEmulator()

            // Determinar slots a sincronizar según tarjetas SIM activas y preferencia Dual SIM
            val slotsToSync = mutableListOf<Int>()
            if (SimOperatorUtils.isSimSlotAvailable(applicationContext, 1, isEmulator)) {
                val sim1Details = SimOperatorUtils.checkSimOperator(applicationContext, 1, isEmulator)
                if (sim1Details.isCubacel) {
                    slotsToSync.add(1)
                }
            }

            val activeSimCount = SimOperatorUtils.getActiveSimCount(applicationContext, isEmulator)
            if ((dualSimEnabled || activeSimCount > 1) && SimOperatorUtils.isSimSlotAvailable(applicationContext, 2, isEmulator)) {
                val sim2Details = SimOperatorUtils.checkSimOperator(applicationContext, 2, isEmulator)
                if (sim2Details.isCubacel) {
                    slotsToSync.add(2)
                }
            }

            // Fallback únicamente si es el emulador de pruebas
            if (slotsToSync.isEmpty() && isEmulator) {
                slotsToSync.add(1)
            }

            for (simSlot in slotsToSync) {
                if (isStopped) return@withContext Result.retry()
                syncSimSlot(simSlot, ussdExecutor, planDao, usageHistoryDao)
            }

            if (isStopped) return@withContext Result.retry()

            // Actualizar Widgets y alertas
            try {
                MegasWidgetProvider.updateAllWidgets(applicationContext)
            } catch (e: Exception) {
                android.util.Log.e("MegasCU", "Unhandled exception", e)
            }

            // Verificar alertas de expiración y límites para cada SIM sincronizada
            val lowDataThresholdMb = prefs.getLong("low_data_threshold_mb", 200L)
            for (simSlot in slotsToSync) {
                if (isStopped) break
                val currentSubId = SimOperatorUtils.getSubscriptionIdForSlot(applicationContext, simSlot)
                val plan = planDao.getPlanStatusDirect(simSlot)
                if (plan != null) {
                    if (currentSubId != null && plan.subscriptionId != currentSubId) {
                        continue
                    }
                    val totalData = plan.dataMb + plan.dataLteMb
                    if (totalData in 1..lowDataThresholdMb || (plan.dataDays in 1..3)) {
                        EtecsaMonitoringService.startOrUpdateAlert(
                            applicationContext,
                            dateStr = plan.nextRechargeDateStr.ifBlank { "${plan.dataDays} días" },
                            daysRemaining = plan.dataDays.toLong(),
                            isExpired = plan.dataDays <= 0,
                            simSlot = simSlot,
                            subscriptionId = currentSubId ?: plan.subscriptionId
                        )
                    }
                }
            }

            DailyLimitAlertManager.checkAndNotify(applicationContext)

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun syncSimSlot(
        simSlot: Int,
        ussdExecutor: UssdExecutor,
        planDao: com.ams.megascu.data.db.PlanDao,
        usageHistoryDao: com.ams.megascu.data.db.UsageHistoryDao
    ) {
        val codes = listOf("*222#", "*222*328#")
        for (code in codes) {
            if (isStopped) return

            val ussdResult = ussdExecutor.executeUssdSuspend(
                ussdCode = code,
                simSlot = simSlot,
                allowDialFallback = false
            )

            val responseText = when (ussdResult) {
                is UssdResult.Success -> ussdResult.response
                is UssdResult.Error -> ""
            }

            if (responseText.isNotBlank()) {
                val parsed = EtecsaUssdParser.parseUssdResponse(responseText, code)
                val currentSubId = SimOperatorUtils.getSubscriptionIdForSlot(applicationContext, simSlot)
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
                    rawLastResponse = ""
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
            }

            // Pequeña pausa entre USSD queries
            delay(800)
        }
    }
}

