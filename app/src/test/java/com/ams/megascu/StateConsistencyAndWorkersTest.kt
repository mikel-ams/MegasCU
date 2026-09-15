package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.ParsedPlanData
import com.ams.megascu.service.PlanExpirationAlertManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StateConsistencyAndWorkersTest {

    private lateinit var context: Context
    private lateinit var db: MegasDatabase
    private lateinit var repository: com.ams.megascu.data.db.MegasRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = MegasDatabase.getDatabase(context)
        repository = com.ams.megascu.data.db.MegasRepository(
            context,
            db.planDao(),
            db.smsLogDao(),
            db.usageHistoryDao()
        )
    }

    @After
    fun tearDown() = runTest {
        db.planDao().clearPlanStatus()
        db.usageHistoryDao().clearHistory()
    }

    @Test
    fun testPlanExpirationAlertManager_checkAndUpdateMidnightDays_decrementsAccurately() = runTest {
        // Given a plan updated 2 days ago with 10 days remaining
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = cal.timeInMillis

        val initialPlan = PlanStatusEntity(
            id = 1,
            subscriptionId = 101,
            dataMb = 2048,
            dataLteMb = 1024,
            dataDays = 10,
            minutesDays = 8,
            smsDays = 5,
            nextRechargeDays = 12,
            lastUpdatedTimestamp = twoDaysAgo
        )
        db.planDao().insertOrUpdatePlanStatus(initialPlan)

        // When checking midnight days update
        val updatedResult = PlanExpirationAlertManager.checkAndUpdateMidnightDays(context, initialPlan)

        // Then days are decremented by 2
        assertNotNull(updatedResult)
        assertEquals(8, updatedResult.dataDays)
        assertEquals(6, updatedResult.minutesDays)
        assertEquals(3, updatedResult.smsDays)
        assertEquals(10, updatedResult.nextRechargeDays)

        val persistedPlan = db.planDao().getPlanStatusDirect(1)
        assertNotNull(persistedPlan)
        assertEquals(8, persistedPlan!!.dataDays)
        assertEquals(6, persistedPlan.minutesDays)
    }

    @Test
    fun testRepositorySaveParsedData_preservesStateConsistency() = runTest {
        // Given initial state
        val initialPlan = PlanStatusEntity(
            id = 1,
            balanceCup = 150.0,
            dataMb = 1024,
            dataLteMb = 512,
            dataDays = 15
        )
        db.planDao().insertOrUpdatePlanStatus(initialPlan)

        // When saving USSD response only updating balance
        val parsed = ParsedPlanData(
            balanceCup = 200.0
        )
        repository.saveParsedData(parsed, "Saldo: 200.00 CUP", simSlot = 1)

        // Then other fields like dataMb and dataDays remain preserved
        val currentPlan = repository.getPlanStatusDirect(1)
        assertNotNull(currentPlan)
        assertEquals(200.0, currentPlan!!.balanceCup, 0.001)
        assertEquals(1024L, currentPlan.dataMb)
        assertEquals(512L, currentPlan.dataLteMb)
        assertEquals(15, currentPlan.dataDays)
    }

    @Test
    fun testUsageHistory_recordedOnDataChange() = runTest {
        val parsed = ParsedPlanData(
            balanceCup = 50.0,
            dataMb = 3000L,
            dataLteMb = 1500L
        )
        repository.saveParsedData(parsed, "Datos: 3000 MB", simSlot = 1)

        val historyList = db.usageHistoryDao().getUsageHistory().first()
        assertTrue("Debe registrarse el cambio en el historial de uso", historyList.isNotEmpty())
        assertEquals(3000L, historyList.first().dataMb)
        assertEquals(1500L, historyList.first().dataLteMb)
    }

    @Test
    fun testUsageHistory_segregatedBySimSlot() = runTest {
        val parsedSim1 = ParsedPlanData(balanceCup = 100.0, dataMb = 4000L, dataLteMb = 2000L)
        val parsedSim2 = ParsedPlanData(balanceCup = 200.0, dataMb = 8000L, dataLteMb = 4000L)

        repository.saveParsedData(parsedSim1, "SIM1 USSD", simSlot = 1)
        repository.saveParsedData(parsedSim2, "SIM2 USSD", simSlot = 2)

        val sim1History = repository.getUsageHistory(simSlot = 1).first()
        val sim2History = repository.getUsageHistory(simSlot = 2).first()

        assertEquals("Historial de SIM 1 debe tener 1 elemento", 1, sim1History.size)
        assertEquals(4000L, sim1History.first().dataMb)
        assertEquals(1, sim1History.first().simSlot)

        assertEquals("Historial de SIM 2 debe tener 1 elemento", 1, sim2History.size)
        assertEquals(8000L, sim2History.first().dataMb)
        assertEquals(2, sim2History.first().simSlot)

        val recentSim1Sync = db.usageHistoryDao().getRecentHistoryForSimSync(1, 10)
        assertEquals(1, recentSim1Sync.size)
        assertEquals(4000L, recentSim1Sync.first().dataMb)
    }

    @Test
    fun testRepository_checkAndUpdateMidnightDays() = runTest {
        val twoDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -2)
        }.timeInMillis

        val oldPlanSim1 = PlanStatusEntity(
            id = 1,
            dataMb = 5000L,
            dataDays = 10,
            minutesStr = "30 Min",
            minutesDays = 10,
            smsCount = 50,
            smsDays = 10,
            lastUpdatedTimestamp = twoDaysAgo
        )
        db.planDao().insertOrUpdatePlanStatus(oldPlanSim1)

        repository.checkAndUpdateMidnightDays()

        val updatedPlan = db.planDao().getPlanStatusDirect(1)
        assertNotNull(updatedPlan)
        assertEquals("Los días de datos deben decrementarse en 2 días", 8, updatedPlan!!.dataDays)
        assertEquals(8, updatedPlan.minutesDays)
        assertEquals(8, updatedPlan.smsDays)
    }
}
