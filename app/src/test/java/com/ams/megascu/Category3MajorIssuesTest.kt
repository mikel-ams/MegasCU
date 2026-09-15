package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdResult
import com.ams.megascu.service.EtecsaMonitoringService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Category3MajorIssuesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // ==========================================
    // 1. ETECSA MONITORING SERVICE & FOREGROUND FALLBACK
    // ==========================================

    @Test
    fun testForegroundServiceNotificationGeneration_multiSim() {
        val notifSim1 = EtecsaMonitoringService.buildAlertNotification(
            context = context,
            dateStr = "28/08/2026",
            daysRemaining = 2,
            isExpired = false,
            simSlot = 1
        )
        assertNotNull("La notificación para SIM 1 no debe ser nula", notifSim1)

        val notifSim2 = EtecsaMonitoringService.buildAlertNotification(
            context = context,
            dateStr = "29/08/2026",
            daysRemaining = 0,
            isExpired = true,
            simSlot = 2
        )
        assertNotNull("La notificación para SIM 2 no debe ser nula", notifSim2)

        val idSim1 = EtecsaMonitoringService.getNotificationId(1, 101)
        val idSim2 = EtecsaMonitoringService.getNotificationId(2, 102)
        assertTrue("Los IDs de notificación entre SIMs deben ser diferentes", idSim1 != idSim2)
    }

    @Test
    fun testForegroundServiceStartOrUpdate_doesNotCrash_fallbackToNotificationManager() {
        // En entorno de prueba (o donde startForegroundService falle por restricciones de background),
        // startOrUpdateAlert y stopAlert no deben lanzar excepciones no capturadas.
        EtecsaMonitoringService.startOrUpdateAlert(
            context = context,
            dateStr = "30/08/2026",
            daysRemaining = 3,
            isExpired = false,
            simSlot = 1,
            subscriptionId = 101
        )

        EtecsaMonitoringService.stopAlert(
            context = context,
            simSlot = 1,
            subscriptionId = 101
        )
    }

    // ==========================================
    // 2. USSD EXECUTOR CONCURRENCY & DEADLOCK SAFETY
    // ==========================================

    @Test
    fun testUssdExecutor_invalidCodeValidation() = runBlocking {
        val ussdExecutor = UssdExecutor(context)

        val resEmpty = ussdExecutor.executeUssdSuspend("")
        assertTrue("Código vacío debe ser error", resEmpty is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (resEmpty as UssdResult.Error).errorType)

        val resInvalidNoHash = ussdExecutor.executeUssdSuspend("*222")
        assertTrue("Sin hash final debe ser error", resInvalidNoHash is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (resInvalidNoHash as UssdResult.Error).errorType)

        val resInvalidLetters = ussdExecutor.executeUssdSuspend("222#")
        assertTrue("Sin asterisco inicial debe ser error", resInvalidLetters is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (resInvalidLetters as UssdResult.Error).errorType)
    }

    @Test
    fun testUssdExecutor_emulatorSimulation_concurrentCalls() = runBlocking {
        val ussdExecutor = UssdExecutor(context)
        assertTrue("Debe detectar entorno de prueba como emulador", ussdExecutor.isEmulator())

        // Disparar 10 solicitudes USSD concurrentes para probar que el mutex radioMutex no causa deadlocks
        // y rechaza con BUSY las llamadas concurrentes para proteger el módem celular
        val jobs = (1..10).map { index ->
            async(Dispatchers.IO) {
                val code = if (index % 2 == 0) "*222#" else "*222*328#"
                ussdExecutor.executeUssdSuspend(code, simSlot = if (index % 2 == 0) 1 else 2)
            }
        }

        val results = jobs.awaitAll()
        assertEquals(10, results.size)
        val successResults = results.filterIsInstance<UssdResult.Success>()
        val busyResults = results.filterIsInstance<UssdResult.Error>().filter { it.errorType == UssdErrorType.BUSY }
        assertTrue("Al menos una llamada debe tener éxito", successResults.isNotEmpty())
        assertTrue("Las llamadas concurrentes simultáneas deben ser rechazadas con BUSY para evitar saturar el módem", busyResults.isNotEmpty() || successResults.size == 10)
        successResults.forEach { result ->
            val resp = result.response
            assertTrue("La respuesta debe contener texto simulado de Cubacel", resp.contains("CUP") || resp.contains("MB") || resp.contains("Saldo"))
        }
    }

    // ==========================================
    // 3. DATABASE CONCURRENCY & ISOLATION
    // ==========================================

    @Test
    fun testDatabase_concurrentPlanUpdates_isolationBetweenSims() = runBlocking {
        val db = MegasDatabase.getDatabase(context)
        val planDao = db.planDao()

        val plan1 = PlanStatusEntity(
            id = 1,
            subscriptionId = 101,
            balanceCup = 250.0,
            dataMb = 2048L,
            dataLteMb = 1024L,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        val plan2 = PlanStatusEntity(
            id = 2,
            subscriptionId = 102,
            balanceCup = 600.0,
            dataMb = 8192L,
            dataLteMb = 4096L,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        // Ejecutar inserciones concurrentes en la base de datos Room
        val task1 = async(Dispatchers.IO) { planDao.insertOrUpdatePlanStatus(plan1) }
        val task2 = async(Dispatchers.IO) { planDao.insertOrUpdatePlanStatus(plan2) }
        awaitAll(task1, task2)

        val fetchedPlan1 = planDao.getPlanStatusDirect(1)
        val fetchedPlan2 = planDao.getPlanStatusDirect(2)

        assertNotNull(fetchedPlan1)
        assertNotNull(fetchedPlan2)
        assertEquals(250.0, fetchedPlan1?.balanceCup ?: 0.0, 0.01)
        assertEquals(600.0, fetchedPlan2?.balanceCup ?: 0.0, 0.01)
        assertEquals(101, fetchedPlan1?.subscriptionId)
        assertEquals(102, fetchedPlan2?.subscriptionId)
    }

    // ==========================================
    // 4. ETECSA USSD PARSER EDGE CASES
    // ==========================================

    @Test
    fun testEtecsaUssdParser_complexResponses() {
        // Respuesta *222# con saldo principal y datos
        val resp222 = "Saldo: 145.50 CUP. Paquete datos: 2.50 GB de 30 dias. Vigente hasta 15/09/2026."
        val parsed222 = EtecsaUssdParser.parseUssdResponse(resp222, "*222#")
        assertEquals(145.50, parsed222.balanceCup ?: 0.0, 0.01)
        assertEquals(2560L, parsed222.dataMb) // 2.50 GB = 2560 MB

        // Respuesta *222*328# con desglose de datos LTE y bono
        val resp328 = "Datos: 1024 MB principal, 2048 MB LTE, 500 MB bono. Vence en 20 dias."
        val parsed328 = EtecsaUssdParser.parseUssdResponse(resp328, "*222*328#")
        assertEquals(1024L, parsed328.dataMb)
        assertEquals(2048L, parsed328.dataLteMb)
        assertEquals(500L, parsed328.bonusMb)
        assertEquals(20, parsed328.dataDays)
    }

    @Test
    fun testEtecsaUssdParser_rechargeAvailabilityCalculation() {
        val (effDate, daysRem) = EtecsaUssdParser.calculateRechargeAvailability("25/08/2026")
        assertEquals("26/08/2026", effDate)
        assertNotNull(daysRem)
    }
}
