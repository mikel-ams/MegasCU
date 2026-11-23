package com.ams.megascu.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyUsageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        DailyLimitAlertManager.checkAndNotify(applicationContext)
        return Result.success()
    }
}
