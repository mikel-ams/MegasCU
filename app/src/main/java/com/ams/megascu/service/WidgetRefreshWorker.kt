package com.ams.megascu.service

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ams.megascu.data.ussd.UssdBatchReport
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdStatusSync
import com.ams.megascu.widget.MegasChartWidgetProvider
import com.ams.megascu.widget.MegasWidget2x1Provider
import com.ams.megascu.widget.MegasWidgetProvider
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

/** USSD may outlive a BroadcastReceiver. WorkManager owns the entire refresh. */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val slot = inputData.getInt(SIM_SLOT, 1).coerceIn(1, 2)
        return try {
            val report = UssdStatusSync(applicationContext).run(UssdBatchReport.STATUS_CODES, slot)
            // The modem can be in use by the app. Queue a bounded retry instead of losing the tap.
            if (report.successful == 0 && report.failures.values.any { it.errorType == UssdErrorType.BUSY } && runAttemptCount < 2) {
                return Result.retry()
            }
            MegasWidgetProvider.updateAllWidgets(applicationContext)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, report.message(slot), if (report.isComplete) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
            }
            if (report.isComplete) Result.success() else Result.failure()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            android.util.Log.e("WidgetRefreshWorker", "No se completó la actualización", error)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(applicationContext, "No se completó la actualización de SIM $slot. Vuelve a intentarlo.", Toast.LENGTH_LONG).show()
            }
            Result.failure()
        }
    }

    companion object {
        private const val SIM_SLOT = "sim_slot"

        fun enqueue(context: Context, widgetId: Int) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                if (manager.getAppWidgetInfo(widgetId)?.provider?.packageName != context.packageName) return
                listOf(widgetId)
            } else {
                listOf(MegasWidgetProvider::class.java, MegasWidget2x1Provider::class.java, MegasChartWidgetProvider::class.java)
                    .flatMap { manager.getAppWidgetIds(ComponentName(context, it)).toList() }
            }
            if (ids.isEmpty()) return
            Toast.makeText(context, "Actualización de widgets en cola…", Toast.LENGTH_SHORT).show()
            val prefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
            ids.map { prefs.getInt("widget_${it}_sim_slot", 1).coerceIn(1, 2) }.distinct().forEach { slot ->
                val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                    .setInputData(workDataOf(SIM_SLOT to slot))
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 10L, TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork("widget_status_refresh_$slot", ExistingWorkPolicy.KEEP, request)
            }
        }
    }
}
