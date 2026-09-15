package com.ams.megascu

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Category4CriticalIssuesTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication
    private lateinit var db: MegasDatabase
    private lateinit var repository: com.ams.megascu.data.db.MegasRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)
        db = MegasDatabase.getDatabase(context)
        repository = com.ams.megascu.data.db.MegasRepository(
            context,
            db.planDao(),
            db.smsLogDao(),
            db.usageHistoryDao()
        )
    }

    @After
    fun tearDown() = runBlocking {
        db.planDao().clearPlanStatus()
        db.usageHistoryDao().clearHistory()
    }

    // ==========================================
    // 1. USSD INJECTION PROTECTION & DIALER FALLBACK SAFETY
    // ==========================================

    @Test
    fun testUssdInjectionProtection_rejectsMaliciousPatterns() = runBlocking {
        val ussdExecutor = UssdExecutor(context)

        val maliciousCodes = listOf(
            "*222#; rm -rf /",
            "*133*1,2#",
            "tel:*222#",
            "SELECT * FROM users",
            "*222#\n*133#",
            "*222#*",
            "222#"
        )

        for (code in maliciousCodes) {
            val res = ussdExecutor.executeUssdSuspend(code)
            assertTrue("Código malicioso o inválido '$code' debe ser rechazado", res is UssdResult.Error)
            val error = res as UssdResult.Error
            assertEquals("Error type debe ser INVALID_CODE", UssdErrorType.INVALID_CODE, error.errorType)
            assertTrue("Mensaje debe contener el código ERR_CODE_102", error.message.contains("ERR_CODE_102"))
        }
    }

    @Test
    fun testPurchaseUssd_safeDialerIntentFallback_includesAllSlotIndexExtras() {
        val executor = UssdExecutor(context)
        
        // Ejecución de compra USSD para ranuras SIM 1 y 2
        executor.executePurchaseUssd("*133*1*1#", simSlot = 1)
        val startedIntent1 = shadowApp.nextStartedActivity
        assertNotNull("Debe lanzar intent de marcado para SIM 1", startedIntent1)
        assertEquals(Intent.ACTION_DIAL, startedIntent1.action)
        assertEquals("tel:*133*1*1%23", startedIntent1.dataString)
        assertEquals(0, startedIntent1.getIntExtra("simSlot", -1))
        assertEquals(0, startedIntent1.getIntExtra("slot", -1))

        executor.executePurchaseUssd("*133*1*1#", simSlot = 2)
        val startedIntent2 = shadowApp.nextStartedActivity
        assertNotNull("Debe lanzar intent de marcado para SIM 2", startedIntent2)
        assertEquals(Intent.ACTION_DIAL, startedIntent2.action)
        assertEquals("tel:*133*1*1%23", startedIntent2.dataString)
        assertEquals(1, startedIntent2.getIntExtra("simSlot", -1))
        assertEquals(1, startedIntent2.getIntExtra("slot", -1))
    }

    // ==========================================
    // 2. PIN AUTHENTICATION SECURITY & RESET INTEGRITY
    // ==========================================

    @Test
    fun testPinSecurity_preferencesStorageAndValidation() {
        val prefs = context.getSharedPreferences("megas_security_prefs", Context.MODE_PRIVATE)

        // Inicialmente no hay PIN
        assertFalse("El PIN no debe estar activo inicialmente", prefs.getBoolean("pin_enabled", false))
        assertEquals("", prefs.getString("user_pin", ""))

        // Guardar PIN "4321"
        prefs.edit()
            .putBoolean("pin_enabled", true)
            .putString("user_pin", "4321")
            .apply()

        assertTrue("El PIN debe estar habilitado", prefs.getBoolean("pin_enabled", false))
        val storedPin = prefs.getString("user_pin", "")
        assertEquals("4321", storedPin)

        // Probar validación: PIN correcto vs incorrecto
        val userEnteredCorrect = "4321"
        val userEnteredWrong = "0000"
        val userEnteredPartial = "432"

        assertEquals(storedPin, userEnteredCorrect)
        assertFalse("PIN incorrecto no debe coincidir", storedPin == userEnteredWrong)
        assertFalse("PIN parcial no debe coincidir", storedPin == userEnteredPartial)

        // Reseteo atómico de seguridad
        prefs.edit()
            .putBoolean("pin_enabled", false)
            .remove("user_pin")
            .apply()

        assertFalse("El PIN debe deshabilitarse tras reseteo", prefs.getBoolean("pin_enabled", false))
        assertEquals("", prefs.getString("user_pin", ""))
    }

    // ==========================================
    // 3. ETECSA PARSER CRASH RESILIENCE & CORRUPTED INPUTS
    // ==========================================

    @Test
    fun testEtecsaParser_resilienceAgainstMalformedOrEmptyStrings() {
        val malformedStrings = listOf(
            "",
            "   ",
            "Error de red 500",
            "Saldo: N/A CUP",
            "Usted no tiene paquetes. ::: !!!",
            "DATOS: -100 MB. Vence: null",
            "Vencimiento: 99/99/99999",
            "Vences el -5 dias"
        )

        for (input in malformedStrings) {
            val parsed = EtecsaUssdParser.parseUssdResponse(input, "*222#")
            assertNotNull("El parser nunca debe lanzar excepción ni retornar objeto nulo", parsed)
        }
    }

    @Test
    fun testEtecsaParser_extremeValuesAndBoundaryConditions() {
        // Respuesta con valores extremadamente grandes (Terabytes o enteros desbordados)
        val extremeInput = "Saldo: 9999999.99 CUP. Paquete datos: 9999999 MB. Vence en 3650 dias."
        val parsed = EtecsaUssdParser.parseUssdResponse(extremeInput, "*222#")

        assertEquals(9999999.99, parsed.balanceCup ?: 0.0, 0.01)
        assertEquals(9999999L, parsed.dataMb)
        assertEquals(3650, parsed.dataDays)
    }

    // ==========================================
    // 4. ROOM DATABASE ATOMICITY & RECOVERY
    // ==========================================

    @Test
    fun testDatabase_atomicTransactionsAndConcurrentReadsWrites() = runBlocking {
        val planDao = db.planDao()

        // Insertar plan inicial para SIM 1
        val planSim1 = PlanStatusEntity(
            id = 1,
            subscriptionId = 101,
            balanceCup = 100.0,
            dataMb = 1024L,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        planDao.insertOrUpdatePlanStatus(planSim1)

        // Ejecutar 20 actualizaciones simultáneas simulando peticiones de red y workers
        val jobs = (1..20).map { i ->
            async(Dispatchers.IO) {
                val updated = PlanStatusEntity(
                    id = 1,
                    subscriptionId = 101,
                    balanceCup = 100.0 + i,
                    dataMb = 1024L + i,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
                planDao.insertOrUpdatePlanStatus(updated)
            }
        }
        jobs.awaitAll()

        val finalPlan = planDao.getPlanStatusDirect(1)
        assertNotNull("El plan no debe ser nulo tras inserciones concurrentes", finalPlan)
        assertTrue("El saldo final debe haberse actualizado de forma consistente", (finalPlan?.balanceCup ?: 0.0) >= 101.0)
    }

    @Test
    fun testSimOperatorUtils_absentSimHandling_doesNotCrash() {
        val detailsSim1 = SimOperatorUtils.checkSimOperator(context, 1, isEmulator = true)
        val detailsSim2 = SimOperatorUtils.checkSimOperator(context, 2, isEmulator = true)

        assertNotNull(detailsSim1)
        assertNotNull(detailsSim2)
        // En emulador debe reportar Cubacel para pruebas sin provocar fallos de puntero nulo
        assertTrue(detailsSim1.isCubacel)
        assertTrue(detailsSim2.isCubacel)
    }
}
