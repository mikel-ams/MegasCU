package com.ams.megascu.service

import com.ams.megascu.data.db.PlanStatusEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class PlanExpiration(val label: String, val date: LocalDate, val amount: String)

internal fun PlanStatusEntity.upcomingExpirations(today: LocalDate): List<PlanExpiration> {
    val zone = ZoneId.systemDefault()
    fun date(timestamp: Long) = Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()
    fun event(label: String, timestamp: Long, days: Int, amount: String) =
        (when {
            timestamp > 0 -> date(timestamp)
            days > 0 -> date(lastUpdatedTimestamp).plusDays(days.toLong())
            else -> null
        })?.takeUnless { it.isBefore(today) }?.let { PlanExpiration(label, it, amount) }

    val data = dataMb + dataLteMb + bonusDataMb
    val minutes = minutesStr.trim()
    return listOfNotNull(
        if (data > 0) event("Datos", dataExpirationTimestamp, dataDays, "$data MB") else null,
        if (minutes.any { it in '1'..'9' }) {
            event("Llamadas", minutesExpirationTimestamp, minutesDays,
                if (minutes.contains("min", ignoreCase = true)) minutes else "$minutes min")
        } else null,
        if (smsCount > 0) event("SMS", smsExpirationTimestamp, smsDays, "$smsCount SMS") else null
    )
}
