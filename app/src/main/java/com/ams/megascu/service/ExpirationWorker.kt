package com.ams.megascu.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ams.megascu.data.db.MegasDatabase

class ExpirationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val db = MegasDatabase.getDatabase(applicationContext)
        val isEmulator = com.ams.megascu.data.ussd.UssdExecutor(applicationContext).isEmulator()
        val activeSimCount = com.ams.megascu.data.ussd.SimOperatorUtils.getActiveSimCount(applicationContext, isEmulator)
        val prefs = applicationContext.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
        val dualSimEnabled = prefs.getBoolean("pref_dual_sim_enabled", false)
        val slotsToProcess = if (dualSimEnabled || activeSimCount > 1) listOf(1, 2) else listOf(1)

        for (simSlot in slotsToProcess) {
            if (!com.ams.megascu.data.ussd.SimOperatorUtils.isSimSlotAvailable(applicationContext, simSlot, isEmulator)) {
                continue
            }
            val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(applicationContext, simSlot)
            val initialPlan = db.planDao().getPlanStatusDirect(simSlot) ?: continue

            if (currentSubId != null && initialPlan.subscriptionId != currentSubId) {
                continue
            }

            // 1. Decrement days automatically at midnight
            val plan = PlanExpirationAlertManager.checkAndUpdateMidnightDays(applicationContext, initialPlan)

            // 2. Check and send plan expiration alerts (5 days or less, 2 alerts on last day with lost resource info)
            PlanExpirationAlertManager.checkAndNotifyExpiration(applicationContext, plan)
        }

        // 3. Daily limit check
        DailyLimitAlertManager.checkAndNotify(applicationContext)

        return Result.success()
    }
}
