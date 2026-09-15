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
                sendUpdateNotification(result.latestVersionName, result.releaseTitle, result.changelog, result.apkDownloadUrl)
            }
        }

        return Result.success()
    }

    private fun sendUpdateNotification(
        versionName: String,
        title: String,
        changelog: String,
        apkUrl: String?
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para actualizaciones si es Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MegasApplication.CHANNEL_UPDATES_ID,
                "Actualizaciones de la App",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos de nuevas versiones y mejoras disponibles en GitHub"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir MainActivity mostrando el diálogo de actualización
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHOW_UPDATE", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            9001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cleanChangelog = if (changelog.isNotBlank()) {
            changelog.take(250) + if (changelog.length > 250) "..." else ""
        } else {
            "Hay mejoras y correcciones disponibles."
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, MegasApplication.CHANNEL_UPDATES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nueva versión de MegasCU: $versionName")
            .setContentText(cleanChangelog)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Nueva versión disponible: $versionName")
                    .bigText("Registro de cambios:\n$cleanChangelog")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // Botón directo para descargar APK si existe enlace
        if (!apkUrl.isNullOrBlank()) {
            val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val downloadPendingIntent = PendingIntent.getActivity(
                applicationContext,
                9002,
                downloadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            notificationBuilder.addAction(
                R.drawable.ic_launcher_foreground,
                "Descargar APK",
                downloadPendingIntent
            )
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        const val NOTIFICATION_ID = 8842
    }
}
