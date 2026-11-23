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
import com.ams.megascu.data.db.PlanStatusEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PlanExpirationAlertManager {

    const val NOTIFICATION_ID_EXPIRATION = 8803
    const val NOTIFICATION_ID_RECHARGE = 8804

    fun getExpirationNotificationId(planId: Int, subscriptionId: Int?): Int {
        return subscriptionId?.let { 880300 + (it % 1000) } ?: (NOTIFICATION_ID_EXPIRATION + (planId - 1) * 10)
    }

    fun getRechargeNotificationId(planId: Int, subscriptionId: Int?): Int {
        return subscriptionId?.let { 880400 + (it % 1000) } ?: (NOTIFICATION_ID_RECHARGE + (planId - 1) * 10)
    }

    suspend fun checkAndUpdateMidnightDays(context: Context, plan: PlanStatusEntity): PlanStatusEntity {
        val now = System.currentTimeMillis()
        val calLast = Calendar.getInstance().apply { timeInMillis = plan.lastUpdatedTimestamp }
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        val daysPassed = calculateDaysPassed(calLast, calNow)
        if (daysPassed <= 0) return plan

        val db = MegasDatabase.getDatabase(context)

        val newDataDays = if (plan.dataDays > 0) maxOf(0, plan.dataDays - daysPassed) else plan.dataDays
        val newMinutesDays = if (plan.minutesDays > 0) maxOf(0, plan.minutesDays - daysPassed) else plan.minutesDays
        val newSmsDays = if (plan.smsDays > 0) maxOf(0, plan.smsDays - daysPassed) else plan.smsDays
        val newRechargeDays = if (plan.nextRechargeDays > 0) maxOf(0, plan.nextRechargeDays - daysPassed) else plan.nextRechargeDays

        val updatedPlan = plan.copy(
            dataDays = newDataDays,
            minutesDays = newMinutesDays,
            smsDays = newSmsDays,
            nextRechargeDays = newRechargeDays,
            lastUpdatedTimestamp = now
        )

        db.planDao().insertOrUpdatePlanStatus(updatedPlan)
        checkAndNotifyRechargeAvailability(context, updatedPlan)
        return updatedPlan
    }

    private fun calculateDaysPassed(cal1: Calendar, cal2: Calendar): Int {
        val c1 = cal1.clone() as Calendar
        val c2 = cal2.clone() as Calendar

        c1.set(Calendar.HOUR_OF_DAY, 0)
        c1.set(Calendar.MINUTE, 0)
        c1.set(Calendar.SECOND, 0)
        c1.set(Calendar.MILLISECOND, 0)

        c2.set(Calendar.HOUR_OF_DAY, 0)
        c2.set(Calendar.MINUTE, 0)
        c2.set(Calendar.SECOND, 0)
        c2.set(Calendar.MILLISECOND, 0)

        val diffMillis = c2.timeInMillis - c1.timeInMillis
        return (diffMillis / (24L * 3600 * 1000)).toInt()
    }

    fun checkAndNotifyExpiration(context: Context, plan: PlanStatusEntity) {
        val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)

        checkAndNotifyRechargeAvailability(context, plan)
        val today = java.time.LocalDate.now()
        val prefix = plan.subscriptionId?.let { "sub_${it}_" } ?: "sim_${plan.id}_"
        val thresholds = buildSet {
            add(prefs.getInt("pref_expiration_alert_days", 5))
            if (prefs.getBoolean("pref_second_expiration_alert_enabled", false)) {
                add(prefs.getInt("pref_second_expiration_alert_days", 2))
            }
            add(1)
            add(0)
        }
        val editor = prefs.edit()
        val due = plan.upcomingExpirations(today).mapNotNull { event ->
            val days = java.time.temporal.ChronoUnit.DAYS.between(today, event.date).toInt()
            if (days !in thresholds) return@mapNotNull null
            val key = "${prefix}expiration_${event.label}_$days"
            val count = if (prefs.getString("${key}_date", null) == today.toString()) {
                prefs.getInt("${key}_count", 0)
            } else 0
            if (count >= if (days == 1) 2 else 1) return@mapNotNull null
            editor.putString("${key}_date", today.toString()).putInt("${key}_count", count + 1)
            val whenText = when (days) {
                0 -> "vence hoy"
                1 -> "vence mañana"
                else -> "vence en $days días"
            }
            "${event.label} · ${event.amount} · $whenText"
        }
        if (due.isEmpty()) return
        sendNotification(context, due.first(), due.joinToString("\n"),
            getExpirationNotificationId(plan.id, plan.subscriptionId))
        editor.apply()
    }

    fun checkAndNotifyRechargeAvailability(context: Context, plan: PlanStatusEntity) {
        val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
        val rechargeAlertEnabled = prefs.getBoolean("pref_recharge_alert_enabled", true)
        if (!rechargeAlertEnabled) return

        val rechargeDays = plan.nextRechargeDays ?: return
        val rechargeDateStr = plan.nextRechargeDateStr.orEmpty()
        if (rechargeDateStr.isBlank() && rechargeDays > 0) return

        // When recharge is allowed (days <= 0)
        if (rechargeDays <= 0) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val prefix = plan.subscriptionId?.let { "sub_${it}_" } ?: if (plan.id > 1) "sim_${plan.id}_" else ""
            val lastRechargeAlertDate = prefs.getString("${prefix}last_recharge_alert_date", "") ?: ""
            if (lastRechargeAlertDate == todayStr) return

            prefs.edit().putString("${prefix}last_recharge_alert_date", todayStr).apply()

            val title = "💳 ¡Recarga de Saldo Disponible!"
            val body = if (rechargeDateStr.isNotBlank()) {
                "Ya puedes recargar tu saldo (fecha de recarga: $rechargeDateStr). ¡Recarga hoy para mantener tus servicios activos!"
            } else {
                "Ya puedes realizar la recarga de tu saldo. ¡Recarga hoy para mantener tus servicios activos!"
            }

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

            val notificationId = getRechargeNotificationId(plan.id, plan.subscriptionId)
            try {
                notificationManager.notify(notificationId, notification)
            } catch (e: Exception) {
                android.util.Log.e("MegasCU", "Unhandled exception", e)
            }
        }
    }

    private fun sendNotification(context: Context, title: String, body: String, notificationId: Int = NOTIFICATION_ID_EXPIRATION) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val comprarIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("action", "comprar")
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val menuIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("action", "menu")
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
            .addAction(R.drawable.ic_stat_sms_alert, "Comprar Plan", comprarIntent)
            .addAction(R.drawable.ic_stat_sms_alert, "Abrir Menú", menuIntent)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }
    }
}
