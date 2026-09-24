package com.ams.megascu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ams.megascu.MainActivity
import com.ams.megascu.MegasApplication
import com.ams.megascu.R
import com.ams.megascu.utils.GitHubUpdateChecker

class GitHubUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
        val autoUpdateEnabled = prefs.getBoolean(GitHubUpdateChecker.PREF_AUTO_UPDATE_CHECK, true)

        if (!autoUpdateEnabled) {
            return Result.success()
        }

        val result = GitHubUpdateChecker.checkForUpdates(applicationContext)

        if (result.isSuccess && result.isUpdateAvailable) {
            val dismissedVersion = prefs.getString(GitHubUpdateChecker.PREF_UPDATE_DISMISSED_VERSION, "")
            // Solo enviar notificación si el usuario no ha descartado explícitamente esta versión exacta
            if (dismissedVersion != result.latestVersionName) {
                UpdateNotificationHelper.sendUpdateNotification(
                    context = applicationContext,
                    versionName = result.latestVersionName,
                    changelog = result.changelog,
                    apkUrl = result.apkDownloadUrl
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val NOTIFICATION_ID = 8842
    }
}
