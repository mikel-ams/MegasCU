package com.ams.megascu.utils

import com.ams.megascu.data.db.UsageHistoryEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class EstimatedUsageDay(val date: LocalDate, val consumedMb: Long)

/** Observed decreases of general + LTE balances; refills begin a new baseline.
 * An interval belongs to its ending date, so this is an estimate, not Android traffic.
 * We never fabricate days with no observation or compare two different subscriptions.
 */
object UsageHistoryEstimator {
    fun dailyUsage(
        history: List<UsageHistoryEntity>, simSlot: Int, subscriptionId: Int?,
        today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(), days: Int = 7
    ): List<EstimatedUsageDay> {
        require(days > 0)
        val samples = history.filter { it.isDataObservation && it.simSlot == simSlot && it.subscriptionId == subscriptionId }
            .sortedWith(compareBy<UsageHistoryEntity> { it.timestamp }.thenBy { it.id })
        fun dateOf(sample: UsageHistoryEntity) = Instant.ofEpochMilli(sample.timestamp).atZone(zone).toLocalDate()
        val observed = samples.filter { dateOf(it) <= today }
        if (observed.map(::dateOf).distinct().size < 2) return emptyList()
        val daily = sortedMapOf<LocalDate, Long>()
        for ((before, after) in observed.zipWithNext()) {
            if (before.timestamp == after.timestamp) continue
            val date = dateOf(after)
            if (date > today || date < today.minusDays(days.toLong() - 1)) continue
            val previous = before.dataMb.coerceAtLeast(0L) + before.dataLteMb.coerceAtLeast(0L)
            val current = after.dataMb.coerceAtLeast(0L) + after.dataLteMb.coerceAtLeast(0L)
            if (current > previous) continue // A refill/reset is not negative consumption.
            daily[date] = (daily[date] ?: 0L) + (previous - current)
        }
        return daily.map { (date, consumed) -> EstimatedUsageDay(date, consumed) }
    }
}
