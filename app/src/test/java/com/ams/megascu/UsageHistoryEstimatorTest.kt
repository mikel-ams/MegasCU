package com.ams.megascu

import com.ams.megascu.data.db.UsageHistoryEntity
import com.ams.megascu.utils.UsageHistoryEstimator
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class UsageHistoryEstimatorTest {
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 4)
    private fun sample(day: String, mb: Long, hour: Int = 12, slot: Int = 1, sub: Int? = 101, lte: Long = 0, bonus: Long = 0) =
        UsageHistoryEntity(isDataObservation = true, dataMb = mb, dataLteMb = lte, bonusDataMb = bonus, simSlot = slot, subscriptionId = sub,
            timestamp = LocalDate.parse(day).atTime(hour, 0).atZone(zone).toInstant().toEpochMilli())
    private fun estimate(vararg samples: UsageHistoryEntity) = UsageHistoryEstimator.dailyUsage(samples.toList(), 1, 101, today, zone)

    @Test fun emptyAndSingleSampleDoNotInventPoints() {
        assertTrue(estimate().isEmpty())
        assertTrue(estimate(sample("2026-10-04", 2048)).isEmpty())
    }
    @Test fun severalQueriesOnSameDateStillWaitForAnotherDay() {
        assertTrue(estimate(sample("2026-10-04", 2048, 8), sample("2026-10-04", 1024, 16)).isEmpty())
    }
    @Test fun differenceIsConsumedDataNotRemainingBalance() {
        val result = estimate(sample("2026-10-03", 2048, lte = 1024), sample("2026-10-04", 1536, lte = 768))
        assertEquals(1, result.size)
        assertEquals(768L, result.single().consumedMb)
        assertEquals(today, result.single().date)
    }
    @Test fun unchangedBalancesAreObservedZeroConsumption() {
        assertEquals(0L, estimate(sample("2026-10-03", 2048), sample("2026-10-04", 2048)).single().consumedMb)
    }
    @Test fun depletionToZeroIsRealConsumption() {
        assertEquals(2048L, estimate(sample("2026-10-03", 2048), sample("2026-10-04", 0)).single().consumedMb)
    }
    @Test fun refillBeginsNewBaselineAndIsNeverNegativeUsage() {
        val result = estimate(sample("2026-10-02", 400), sample("2026-10-03", 2400), sample("2026-10-04", 2000))
        assertEquals(listOf(400L), result.map { it.consumedMb })
        assertEquals(today, result.single().date)
    }
    @Test fun refillAndConsumptionOnSameDayKeepOnlyObservedDecrease() {
        val result = estimate(sample("2026-10-03", 600), sample("2026-10-04", 400, 8), sample("2026-10-04", 2400, 12), sample("2026-10-04", 2000, 18))
        assertEquals(600L, result.single().consumedMb)
    }
    @Test fun missingDaysAreNotFilledWithSyntheticPoints() {
        val result = estimate(sample("2026-09-29", 2048), sample("2026-10-04", 1024))
        assertEquals(1, result.size)
        assertEquals(today, result.single().date)
    }
    @Test fun ignoresOtherSlotsAndReplacedSubscriptions() {
        val result = estimate(sample("2026-10-03", 2048), sample("2026-10-04", 100, slot = 2), sample("2026-10-04", 50, sub = 102), sample("2026-10-04", 1536))
        assertEquals(512L, result.single().consumedMb)
    }
    @Test fun replacingSimDoesNotConnectOldAndNewBalances() {
        assertTrue(estimate(sample("2026-10-03", 4096, sub = 100), sample("2026-10-04", 100, sub = 101)).isEmpty())
    }
    @Test fun bonusIsExcludedFromGeneralAndLteConsumption() {
        assertEquals(0L, estimate(sample("2026-10-03", 2048, bonus = 1000), sample("2026-10-04", 2048, bonus = 0)).single().consumedMb)
    }
    @Test fun unsortedInputIsOrderedBeforeComputingDifferences() {
        assertEquals(512L, estimate(sample("2026-10-04", 1536), sample("2026-10-03", 2048)).single().consumedMb)
    }
    @Test fun duplicateTimestampsDoNotInventUsage() {
        val first = sample("2026-10-03", 2048)
        assertEquals(512L, estimate(first, first, sample("2026-10-04", 1536)).single().consumedMb)
    }
    @Test fun onlyLastSevenDatesAreDisplayed() {
        val result = estimate(sample("2026-09-26", 5000), sample("2026-09-27", 4000), sample("2026-09-28", 3000), sample("2026-10-04", 2000))
        assertEquals(listOf(LocalDate.of(2026, 9, 28), today), result.map { it.date })
    }
    @Test fun futureSampleDoesNotUnlockOneDayHistory() {
        assertTrue(estimate(sample("2026-10-04", 2048), sample("2026-10-05", 1536)).isEmpty())
    }
    @Test fun yearBoundaryUsesFullDatesRatherThanDayLabels() {
        val result = UsageHistoryEstimator.dailyUsage(listOf(sample("2025-12-31", 2048), sample("2026-01-01", 1536)), 1, 101, LocalDate.of(2026, 1, 1), zone)
        assertEquals(512L, result.single().consumedMb)
    }
    @Test fun calendarDateUsesRequestedTimeZone() {
        val result = UsageHistoryEstimator.dailyUsage(listOf(sample("2026-10-03", 2048, 3), sample("2026-10-03", 1536, 23)), 1, 101, today, ZoneId.of("America/Havana"))
        assertEquals(512L, result.single().consumedMb)
    }
    @Test fun nullSubscriptionIsSeparateFromKnownSubscription() {
        val result = UsageHistoryEstimator.dailyUsage(listOf(sample("2026-10-03", 2048, sub = null), sample("2026-10-04", 1536, sub = null), sample("2026-10-04", 1)), 1, null, today, zone)
        assertEquals(512L, result.single().consumedMb)
    }
    @Test fun cachedBalanceSnapshotsCannotUnlockOrContaminateGraph() {
        val genuine = sample("2026-10-03", 2048)
        val cached = sample("2026-10-04", 1).copy(isDataObservation = false)
        assertTrue(estimate(genuine, cached).isEmpty())
        assertEquals(512L, estimate(genuine, cached, sample("2026-10-04", 1536, 16)).single().consumedMb)
    }
}
