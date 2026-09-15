package com.ams.megascu.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ams.megascu.MainActivity
import com.ams.megascu.MegasApplication
import com.ams.megascu.R

class EtecsaMonitoringService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val simSlot = intent?.getIntExtra(EXTRA_SIM_SLOT, 1) ?: 1
        val subscriptionId = if (intent?.hasExtra(EXTRA_SUBSCRIPTION_ID) == true) {
            val id = intent.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1)
            if (id != -1) id else null
        } else null

        val notificationId = getNotificationId(simSlot, subscriptionId)

        if (action == ACTION_STOP) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(notificationId)
            if (simSlot == 1) {
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (_: Exception) {}
                stopSelf()
            }
            return START_NOT_STICKY
        }

        val dateStr = intent?.getStringExtra(EXTRA_DATE_STR) ?: "Próximamente"
        val daysRemaining = intent?.getLongExtra(EXTRA_DAYS_REMAINING, 0) ?: 0
        val isExpired = intent?.getBooleanExtra(EXTRA_IS_EXPIRED, false) ?: false

        val notification = buildAlertNotification(this, dateStr, daysRemaining, isExpired, simSlot)

        if (simSlot == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startForeground(
                            notificationId,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        )
                    } else {
                        startForeground(notificationId, notification)
                    }
                } catch (e: Exception) {
                    // Fallback for devices restricting FGS from background or missing type
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    notificationManager?.notify(notificationId, notification)
                }
            } else {
                try {
                    startForeground(notificationId, notification)
                } catch (e: Exception) {
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    notificationManager?.notify(notificationId, notification)
                }
            }
        } else {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notificationId, notification)
        }

        return START_STICKY
    }

    companion object {
        const val NOTIFICATION_ID = 8801
        const val ACTION_START_OR_UPDATE = "com.ams.megascu.service.START_OR_UPDATE"
        const val ACTION_STOP = "com.ams.megascu.service.STOP"
        const val EXTRA_DATE_STR = "extra_date_str"
        const val EXTRA_DAYS_REMAINING = "extra_days_remaining"
        const val EXTRA_IS_EXPIRED = "extra_is_expired"
        const val EXTRA_SIM_SLOT = "extra_sim_slot"
        const val EXTRA_SUBSCRIPTION_ID = "extra_subscription_id"

        fun getNotificationId(simSlot: Int, subscriptionId: Int?): Int {
            return subscriptionId?.let { 880100 + (it % 1000) } ?: (NOTIFICATION_ID + (simSlot - 1) * 10)
        }

        fun buildAlertNotification(
            context: Context,
            dateStr: String,
            daysRemaining: Long,
            isExpired: Boolean,
            simSlot: Int
        ): Notification {
            val contentIntent = PendingIntent.getActivity(
                context,
                simSlot,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("selected_sim_slot", simSlot)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Purchase *133# action intent
            val buyCode = Uri.encode("*133#")
            val buyIntent = PendingIntent.getActivity(
                context,
                simSlot + 10,
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$buyCode")),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val simTag = if (simSlot > 1) " (SIM $simSlot)" else ""
            val title = if (isExpired) {
                "⚠️ ¡ATENCIÓN CUBACEL$simTag! Plan de Datos VENCIDO"
            } else if (daysRemaining <= 0) {
                "🚨 ALERTA: Su Plan de Datos VENCE HOY ($dateStr)$simTag"
            } else if (daysRemaining == 1L) {
                "🚨 ALERTA: Su Plan de Datos VENCE MAÑANA ($dateStr)$simTag"
            } else {
                "⚠️ ALERTA: Su Plan de Datos Vence Pronto ($dateStr)$simTag"
            }

            val bodyText = if (isExpired) {
                "Su paquete expiró el $dateStr. Marque *133# para comprar un nuevo plan y renovar vigencia."
            } else {
                "ALERTA ETECSA. Su plan vence el $dateStr. Marque *133# para no perder sus datos acumulados."
            }

            return NotificationCompat.Builder(context, MegasApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sms_alert)
                .setContentTitle(title)
                .setContentText(bodyText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setContentIntent(contentIntent)
                .addAction(
                    android.R.drawable.ic_menu_call,
                    "Comprar Plan (*133#)",
                    buyIntent
                )
                .setOnlyAlertOnce(false)
                .build()
        }

        fun startOrUpdateAlert(
            context: Context,
            dateStr: String,
            daysRemaining: Long,
            isExpired: Boolean,
            simSlot: Int = 1,
            subscriptionId: Int? = null
        ) {
            val intent = Intent(context, EtecsaMonitoringService::class.java).apply {
                action = ACTION_START_OR_UPDATE
                putExtra(EXTRA_DATE_STR, dateStr)
                putExtra(EXTRA_DAYS_REMAINING, daysRemaining)
                putExtra(EXTRA_IS_EXPIRED, isExpired)
                putExtra(EXTRA_SIM_SLOT, simSlot)
                if (subscriptionId != null) {
                    putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
                }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Fallback direct notification via NotificationManager if background service launch is disallowed
                try {
                    val notification = buildAlertNotification(context, dateStr, daysRemaining, isExpired, simSlot)
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    val notificationId = getNotificationId(simSlot, subscriptionId)
                    notificationManager?.notify(notificationId, notification)
                } catch (ne: Exception) {
                    ne.printStackTrace()
                }
            }
        }

        fun stopAlert(context: Context, simSlot: Int = 1, subscriptionId: Int? = null) {
            val notificationId = getNotificationId(simSlot, subscriptionId)
            val intent = Intent(context, EtecsaMonitoringService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_SIM_SLOT, simSlot)
                if (subscriptionId != null) {
                    putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
                }
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                // Direct notification cancel fallback if service start is not allowed in background
                try {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    notificationManager?.cancel(notificationId)
                } catch (ne: Exception) {
                    ne.printStackTrace()
                }
            }
        }
    }
}
