package com.ams.megascu.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdStatusSync
import com.ams.megascu.widget.MegasWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = MegasDatabase.getDatabase(applicationContext)
            val planDao = db.planDao()
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

            var hasFailures = false
            for (simSlot in slotsToSync) {
                if (isStopped) return@withContext Result.retry()
                val report = UssdStatusSync(applicationContext, ussdExecutor).run(listOf("*222#", "*222*328#"), simSlot)
                if (!report.isComplete) hasFailures = true
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

            if (hasFailures) {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            } else Result.success()
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

}
