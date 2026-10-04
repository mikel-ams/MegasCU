package com.ams.megascu

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.*
import com.ams.megascu.data.ussd.ParsedPlanData
import com.ams.megascu.data.ussd.SimOperatorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StatusHistoryPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: MegasDatabase
    private lateinit var repository: MegasRepository
    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MegasDatabase::class.java).allowMainThreadQueries().build()
        repository = MegasRepository(context, database.planDao(), database.smsLogDao(), database.usageHistoryDao(), database)
    }
    @After fun close() { database.close() }
    @Test fun successfulDataQueriesRecordUnchangedBalancesForDailyComparison() = runBlocking {
        repeat(2) { repository.saveParsedData(ParsedPlanData(dataMb = 2048, dataLteMb = 1024), "Datos", recordDataSample = true) }
        assertEquals(2, database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).size)
        assertTrue(database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).all { it.isDataObservation })
        assertEquals(2048L, database.planDao().getPlanStatusDirect(1)!!.dataMb)
    }
    @Test fun concurrentCategoryUpdatesDoNotLoseOtherBalances() = runBlocking {
        listOf(
            async(Dispatchers.IO) { repository.saveParsedData(ParsedPlanData(dataMb = 2048), "Datos", recordDataSample = true) },
            async(Dispatchers.IO) { repository.saveParsedData(ParsedPlanData(balanceCup = 150.0), "Saldo") }
        ).awaitAll()
        val plan = database.planDao().getPlanStatusDirect(1)!!
        assertEquals(2048L, plan.dataMb)
        assertEquals(150.0, plan.balanceCup, 0.0)
    }
    @Test fun historyFailureRollsBackMergedPlan() = runBlocking {
        repository.saveParsedData(ParsedPlanData(dataMb = 2048), "Inicial", recordDataSample = true)
        val failingHistory = object : UsageHistoryDao by database.usageHistoryDao() {
            override suspend fun insertUsageHistory(history: UsageHistoryEntity) { error("Fallo de almacenamiento") }
        }
        val failingRepository = MegasRepository(context, database.planDao(), database.smsLogDao(), failingHistory, database)
        try {
            failingRepository.saveParsedData(ParsedPlanData(dataMb = 1024), "Datos", recordDataSample = true)
            fail("Debe propagarse el error de guardado")
        } catch (_: IllegalStateException) { }
        assertEquals(2048L, database.planDao().getPlanStatusDirect(1)!!.dataMb)
        assertEquals(1, database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).size)
    }
    @Test fun changedSubscriptionRejectsWriteBeforeCommit() = runBlocking {
        val current = SimOperatorUtils.getSubscriptionIdForSlot(context, 1)
        val different = if (current == 987) 988 else 987
        try {
            repository.saveParsedData(ParsedPlanData(dataMb = 1024), "Datos", recordDataSample = true,
                expectedSubscriptionId = different, enforceSubscriptionIdentity = true)
            fail("No debe guardarse para otra SIM")
        } catch (_: IllegalStateException) { }
        assertNull(database.planDao().getPlanStatusDirect(1))
        assertTrue(database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).isEmpty())
    }
    @Test fun clearingDataAlsoClearsWidgetCache() = runBlocking {
        repository.saveParsedData(ParsedPlanData(dataMb = 2048), "Datos", recordDataSample = true)
        val cache = context.getSharedPreferences("megas_widget_plan_cache", Context.MODE_PRIVATE)
        cache.edit().putLong("dataMb", 2048).commit()
        repository.clearAllData()
        assertTrue(cache.all.isEmpty())
        assertNull(database.planDao().getPlanStatusDirect(1))
        assertTrue(database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).isEmpty())
    }
    @Test fun daoDoesNotMixSubscriptionsIncludingUnknownIds() = runBlocking {
        database.usageHistoryDao().insertUsageHistory(UsageHistoryEntity(isDataObservation = true, subscriptionId = 101, timestamp = 1))
        database.usageHistoryDao().insertUsageHistory(UsageHistoryEntity(isDataObservation = true, subscriptionId = 102, timestamp = 2))
        database.usageHistoryDao().insertUsageHistory(UsageHistoryEntity(isDataObservation = true, subscriptionId = null, timestamp = 3))
        assertEquals(listOf(101), database.usageHistoryDao().getHistoryForSubscriptionSinceSync(1, 101, 0).map { it.subscriptionId })
        assertEquals(listOf<Int?>(null), database.usageHistoryDao().getHistoryForSubscriptionSinceSync(1, null, 0).map { it.subscriptionId })
    }
    @Test fun balanceOnlySnapshotsRemainInHistoryButNotInDataObservations() = runBlocking {
        repository.saveParsedData(ParsedPlanData(balanceCup = 150.0), "Saldo")
        val subscriptionId = SimOperatorUtils.getSubscriptionIdForSlot(context, 1)
        assertEquals(1, database.usageHistoryDao().getRecentHistoryForSimSync(1, 10).size)
        assertTrue(database.usageHistoryDao().getHistoryForSubscriptionSinceSync(1, subscriptionId, 0).isEmpty())
    }
}
