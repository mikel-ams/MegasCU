package com.ams.megascu.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ams.megascu.MainActivity
import com.ams.megascu.MegasApplication
import com.ams.megascu.R
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.ui.components.getMobileDataUsageToday
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailyLimitAlertManager {

    const val NOTIFICATION_ID_DAILY_LIMIT = 8802

    fun getDailyLimitNotificationId(simSlot: Int, subscriptionId: Int?): Int {
        return subscriptionId?.let { 880200 + (it % 1000) } ?: (NOTIFICATION_ID_DAILY_LIMIT + (simSlot - 1) * 10)
    }

    suspend fun checkAndNotify(context: Context) {
        try {
            val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
            val alertsEnabled = prefs.getBoolean("daily_limit_alerts_enabled", true)
            if (!alertsEnabled) return

            val db = MegasDatabase.getDatabase(context)
            val isEmulator = com.ams.megascu.data.ussd.UssdExecutor(context).isEmulator()
            val activeSimCount = com.ams.megascu.data.ussd.SimOperatorUtils.getActiveSimCount(context, isEmulator)
            val dualSimEnabled = prefs.getBoolean("pref_dual_sim_enabled", false)
            val slotsToProcess = if (dualSimEnabled || activeSimCount > 1) listOf(1, 2) else listOf(1)

            val defaultDataSubId = try {
                android.telephony.SubscriptionManager.getDefaultDataSubscriptionId()
            } catch (e: Exception) {
                android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID
            }

            for (simSlot in slotsToProcess) {
                if (!com.ams.megascu.data.ussd.SimOperatorUtils.isSimSlotAvailable(context, simSlot, isEmulator)) {
                    continue
                }
                val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
                val plan = db.planDao().getPlanStatusDirect(simSlot) ?: continue
                if (currentSubId != null && plan.subscriptionId != currentSubId) {
                    continue
                }

                // En configuración multi-SIM, evitar falsos positivos en SIMs secundarias atribuyendo consumo cruzado
                if (slotsToProcess.size > 1 && currentSubId != null &&
                    defaultDataSubId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID &&
                    currentSubId != defaultDataSubId) {
                    continue
                }

                val dataDays = plan.dataDays
                val remainingMb = plan.dataMb + plan.dataLteMb
                if (dataDays <= 0 || remainingMb <= 0) continue

                val recommendedDailyMb = remainingMb.toFloat() / dataDays
                val todayBytes = getMobileDataUsageToday(context, currentSubId)
                val todayMb = todayBytes / (1024f * 1024f)

                if (todayMb <= 0f) continue

                val prefix = plan.subscriptionId?.let { "sub_${it}_" } ?: if (plan.id > 1) "sim_${plan.id}_" else ""
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val lastExceededDay = prefs.getString("${prefix}last_daily_exceeded_alert_date", "") ?: ""
                val lastNearDay = prefs.getString("${prefix}last_daily_near_alert_date", "") ?: ""

                val notificationId = getDailyLimitNotificationId(simSlot, plan.subscriptionId)

                // Condition 1: Exceeded recommended limit (100%)
                if (todayMb >= recommendedDailyMb) {
                    if (lastExceededDay != todayStr) {
                        sendNotification(
                            context = context,
                            title = "🚨 ¡Límite diario superado!",
                            body = "Has consumido ${formatMb(todayMb)} hoy, superando el límite recomendado de ${formatMb(recommendedDailyMb)}/día.",
                            notificationId = notificationId
                        )
                        prefs.edit()
                            .putString("${prefix}last_daily_exceeded_alert_date", todayStr)
                            .apply()
                    }
                }
                // Condition 2: Approaching recommended limit (85%)
                else if (todayMb >= recommendedDailyMb * 0.85f) {
                    if (lastNearDay != todayStr && lastExceededDay != todayStr) {
                        sendNotification(
                            context = context,
                            title = "⚠️ Cerca del límite diario recomendado",
                            body = "Has consumido ${formatMb(todayMb)} hoy, acercándote a tu límite recomendado de ${formatMb(recommendedDailyMb)}/día.",
                            notificationId = notificationId
                        )
                        prefs.edit()
                            .putString("${prefix}last_daily_near_alert_date", todayStr)
                            .apply()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }
    }

    private fun formatMb(mb: Float): String {
        return if (mb >= 1024f) {
            String.format(Locale.US, "%.2f GB", mb / 1024f)
        } else {
            String.format(Locale.US, "%.0f MB", mb)
        }
    }

    fun sendNotification(context: Context, title: String, body: String, notificationId: Int = NOTIFICATION_ID_DAILY_LIMIT) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MegasApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_sms_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }
    }
}
