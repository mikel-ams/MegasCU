package com.ams.megascu.utils

import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import com.ams.megascu.data.ussd.SimOperatorUtils
import java.time.LocalDate
import java.time.ZoneId

data class MobileUsageDay(val date: LocalDate, val bytes: Long)

/** Null means unavailable; an actual zero-byte bucket remains a valid measurement. */
object MobileDataUsageReader {
    @Suppress("DEPRECATION")
    fun lastDays(context: Context, subscriptionId: Int?, days: Int = 7): List<MobileUsageDay>? {
        require(days > 0)
        if (!PermissionUtils.hasUsageStatsPermission(context)) return null
        return try {
            val manager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager ?: return null
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val specific = if (subscriptionId != null) telephony?.createForSubscriptionId(subscriptionId) else telephony
            val subscriberId = runCatching { specific?.subscriberId }.getOrNull()
            // A null subscriber ID may describe all mobile traffic. Do not attribute it to one of two SIMs.
            if (subscriberId == null && SimOperatorUtils.getActiveSimCount(context) > 1) return null
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val now = System.currentTimeMillis()
            (days - 1 downTo 0).map { offset ->
                val date = today.minusDays(offset.toLong())
                val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = minOf(date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli(), now)
                val bucket = manager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, subscriberId, start, end)
                MobileUsageDay(date, (bucket.rxBytes + bucket.txBytes).coerceAtLeast(0L))
            }
        } catch (error: Exception) {
            android.util.Log.w("MobileDataUsageReader", "Estadísticas de tráfico no disponibles", error)
            null
        }
    }
}
