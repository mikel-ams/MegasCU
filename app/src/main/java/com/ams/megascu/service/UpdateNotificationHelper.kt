package com.ams.megascu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ams.megascu.MainActivity
import com.ams.megascu.MegasApplication
import com.ams.megascu.R

object UpdateNotificationHelper {

    const val NOTIFICATION_ID = 8842

    fun extractFirstTwoPoints(changelog: String): List<String> {
        if (changelog.isBlank()) return emptyList()
        val lines = changelog.replace("\r\n", "\n").replace("\r", "\n").split("\n")
        val points = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                val clean = trimmed.substring(2).trim()
                    .replace(Regex("""\*\*([^*]+)\*\*"""), "$1")
                    .replace(Regex("""__([^_]+)__"""), "$1")
                    .replace(Regex("""`([^`]+)`"""), "$1")
                if (clean.isNotBlank()) {
                    points.add(clean)
                    if (points.size >= 2) break
                }
            }
        }

        if (points.isEmpty()) {
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isNotBlank() && !trimmed.startsWith("#") && !trimmed.startsWith("---") && !trimmed.startsWith("<")) {
                    val clean = trimmed
                        .replace(Regex("""\*\*([^*]+)\*\*"""), "$1")
                        .replace(Regex("""`([^`]+)`"""), "$1")
                    points.add(clean)
                    if (points.size >= 2) break
                }
            }
        }

        return points
    }

    fun sendUpdateNotification(
        context: Context,
        versionName: String,
        changelog: String,
        apkUrl: String? = null
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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

        val openSettingsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", "open_settings_update")
            putExtra("EXTRA_OPEN_SETTINGS_UPDATE", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9001,
            openSettingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val topPoints = extractFirstTwoPoints(changelog)
        val shortSummary = if (topPoints.isNotEmpty()) {
            "• " + topPoints.first()
        } else {
            "Nueva versión $versionName lista para actualizar."
        }

        val expandedText = if (topPoints.isNotEmpty()) {
            topPoints.joinToString("\n") { "• $it" }
        } else {
            "Hay mejoras y correcciones disponibles en la nueva versión $versionName."
        }

        val notificationBuilder = NotificationCompat.Builder(context, MegasApplication.CHANNEL_UPDATES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nueva versión disponible: $versionName")
            .setContentText(shortSummary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("Nueva versión disponible: $versionName")
                    .bigText(expandedText)
                    .setSummaryText("MegasCU")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Ir a Ajustes",
                pendingIntent
            )

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}
