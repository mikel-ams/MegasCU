package com.ams.megascu

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.data.db.UsageHistoryEntity
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.SimOperatorUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSubscriptionManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DualSimUssdAndSyncTest {

    private lateinit var context: Context
    private lateinit var db: MegasDatabase
    private lateinit var repository: com.ams.megascu.data.db.MegasRepository

    private val subIdSim1 = 101
    private val subIdSim2 = 202

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, MegasDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = com.ams.megascu.data.db.MegasRepository(context, db.planDao(), db.smsLogDao(), db.usageHistoryDao())

        setupDualSimSubscriptions()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun setupDualSimSubscriptions() {
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)

        val subInfo1 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim1)
            .setSimSlotIndex(0)
            .setDisplayName("Cubacel Principal")
            .setCarrierName("Cubacel")
            .setCountryIso("cu")
            .setMcc("368")
            .setMnc("01")
            .buildSubscriptionInfo()

        val subInfo2 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim2)
            .setSimSlotIndex(1)
            .setDisplayName("Cubacel Trabajo")
            .setCarrierName("Cubacel")
            .setCountryIso("cu")
            .setMcc("368")
            .setMnc("01")
            .buildSubscriptionInfo()

        shadowSubManager.setActiveSubscriptionInfos(subInfo1, subInfo2)
    }

    @Test
    fun testSimSubscriptionResolution_forBothSlots() {
        val resolvedSub1 = SimOperatorUtils.getSubscriptionIdForSlot(context, 1)
        val resolvedSub2 = SimOperatorUtils.getSubscriptionIdForSlot(context, 2)
        val resolvedSub3 = SimOperatorUtils.getSubscriptionIdForSlot(context, 3)

        assertEquals("Slot 1 debe resolver a subId 101", subIdSim1, resolvedSub1)
        assertEquals("Slot 2 debe resolver a subId 202", subIdSim2, resolvedSub2)
        assertNull("Slot 3 inválido debe devolver null", resolvedSub3)

        val sim1Active = SimOperatorUtils.isSimSlotAvailable(context, 1, isEmulator = false)
        val sim2Active = SimOperatorUtils.isSimSlotAvailable(context, 2, isEmulator = false)

        assertTrue("SIM 1 debe estar activa", sim1Active)
        assertTrue("SIM 2 debe estar activa", sim2Active)
    }

    @Test
    fun testRealUssdResponseParsing_separatedPerSim() {
        // Respuestas USSD reales de ETECSA para SIM 1 (Línea Personal)
        val sim1SaldoUssd = "Saldo: 250.50 CUP. Linea activa hasta 24/09/2026."
        val sim1DatosUssd = "Datos: 3.5 GB todas las redes, 1.2 GB LTE, 500 MB Bono. Vence 15/09/2026."
        val sim1VozSmsUssd = "Voz: 45:00 MIN, SMS: 120 SMS. Vence 20/09/2026."

        val parsedSim1Saldo = EtecsaUssdParser.parseUssdResponse(sim1SaldoUssd, "*222#")
        val parsedSim1Datos = EtecsaUssdParser.parseUssdResponse(sim1DatosUssd, "*222*328#")
        val parsedSim1VozSms = EtecsaUssdParser.parseUssdResponse(sim1VozSmsUssd, "*222*869#")

        assertEquals(250.50, parsedSim1Saldo.balanceCup ?: 0.0, 0.01)
        assertEquals((3.5 * 1024).toLong(), parsedSim1Datos.dataMb)
        assertEquals((1.2 * 1024).toLong(), parsedSim1Datos.dataLteMb)
        assertEquals(500L, parsedSim1Datos.bonusMb)
        assertEquals("45:00", parsedSim1VozSms.minutesStr)
        assertEquals(120, parsedSim1VozSms.sms)

        // Respuestas USSD reales de ETECSA para SIM 2 (Línea de Trabajo con diferentes planes)
        val sim2SaldoUssd = "Saldo: 1450.00 CUP. Linea activa hasta 10/12/2026."
        val sim2DatosUssd = "Datos: 8.0 GB todas las redes, 4.5 GB LTE, 2.0 GB Bono. Vence 28/09/2026."
        val sim2VozSmsUssd = "Voz: 180:00 MIN, SMS: 300 SMS. Vence 30/09/2026."

        val parsedSim2Saldo = EtecsaUssdParser.parseUssdResponse(sim2SaldoUssd, "*222#")
        val parsedSim2Datos = EtecsaUssdParser.parseUssdResponse(sim2DatosUssd, "*222*328#")
        val parsedSim2VozSms = EtecsaUssdParser.parseUssdResponse(sim2VozSmsUssd, "*222*869#")

        assertEquals(1450.00, parsedSim2Saldo.balanceCup ?: 0.0, 0.01)
        assertEquals((8.0 * 1024).toLong(), parsedSim2Datos.dataMb)
        assertEquals((4.5 * 1024).toLong(), parsedSim2Datos.dataLteMb)
        assertEquals((2.0 * 1024).toLong(), parsedSim2Datos.bonusMb)
        assertEquals("180:00", parsedSim2VozSms.minutesStr)
        assertEquals(300, parsedSim2VozSms.sms)
    }

    @Test
    fun testDatabasePersistenceAndIsolation_betweenSim1AndSim2() = runTest {
        val planDao = db.planDao()
        val usageHistoryDao = db.usageHistoryDao()

        val timestamp = System.currentTimeMillis()

        // Entidad SIM 1
        val entitySim1 = PlanStatusEntity(
            id = 1,
            balanceCup = 250.50,
            dataMb = 3584,
            dataLteMb = 1228,
            bonusDataMb = 500,
            minutesStr = "45:00",
            smsCount = 120,
            dataDays = 22,
            minutesDays = 27,
            smsDays = 27,
            nextRechargeDateStr = "24/09/2026",
            lastUpdatedTimestamp = timestamp,
            rawLastResponse = "Sim1 Data Response"
        )

        // Entidad SIM 2
        val entitySim2 = PlanStatusEntity(
            id = 2,
            balanceCup = 1450.00,
            dataMb = 8192,
            dataLteMb = 4608,
            bonusDataMb = 2048,
            minutesStr = "180:00",
            smsCount = 300,
            dataDays = 35,
            minutesDays = 37,
            smsDays = 37,
            nextRechargeDateStr = "10/12/2026",
            lastUpdatedTimestamp = timestamp + 1000,
            rawLastResponse = "Sim2 Data Response"
        )

        planDao.insertOrUpdatePlanStatus(entitySim1)
        planDao.insertOrUpdatePlanStatus(entitySim2)

        // Verificación directa de aislamiento
        val retrievedSim1 = planDao.getPlanStatusDirect(1)
        val retrievedSim2 = planDao.getPlanStatusDirect(2)

        assertNotNull("SIM 1 debe existir en BD", retrievedSim1)
        assertNotNull("SIM 2 debe existir en BD", retrievedSim2)

        assertEquals("SIM 1 balance no coincide", 250.50, retrievedSim1!!.balanceCup, 0.01)
        assertEquals("SIM 1 datos no coinciden", 3584L, retrievedSim1.dataMb)
        assertEquals("SIM 1 LTE no coincide", 1228L, retrievedSim1.dataLteMb)
        assertEquals("SIM 1 Bono no coincide", 500L, retrievedSim1.bonusDataMb)
        assertEquals("SIM 1 minutos", "45:00", retrievedSim1.minutesStr)

        assertEquals("SIM 2 balance no coincide", 1450.00, retrievedSim2!!.balanceCup, 0.01)
        assertEquals("SIM 2 datos no coinciden", 8192L, retrievedSim2.dataMb)
        assertEquals("SIM 2 LTE no coincide", 4608L, retrievedSim2.dataLteMb)
        assertEquals("SIM 2 Bono no coincide", 2048L, retrievedSim2.bonusDataMb)
        assertEquals("SIM 2 minutos", "180:00", retrievedSim2.minutesStr)

        // Verificar reactividad con Flows
        val flowSim1 = planDao.getPlanStatus(1).first()
        val flowSim2 = planDao.getPlanStatus(2).first()

        assertEquals(250.50, flowSim1?.balanceCup ?: 0.0, 0.01)
        assertEquals(1450.00, flowSim2?.balanceCup ?: 0.0, 0.01)
    }

    @Test
    fun testDialIntentExtrasIntegrity_forBothSimSlots() {
        val ussdCode = "*222#"
        val encodedCode = Uri.encode(ussdCode)

        // SIM 1 (Slot 0, SubId 101)
        val slot1 = 1
        val subInfo1 = SimOperatorUtils.findActiveSubscriptionForSlot(context, slot1)
        val intentSim1 = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedCode")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val subscriptionId = subInfo1?.subscriptionId
            val slotIndex = slot1 - 1
            if (subscriptionId != null && subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                putExtra("subscription", subscriptionId)
                putExtra("subId", subscriptionId)
                putExtra("com.android.phone.extra.subscription", subscriptionId)
                putExtra("com.android.phone.extra.slot", slotIndex)
            }
            putExtra("simSlot", slotIndex)
            putExtra("slot", slotIndex)
            putExtra("phone_id", slotIndex)
            putExtra("simId", slotIndex.toLong())
        }

        assertEquals(subIdSim1, intentSim1.getIntExtra("subscription", -1))
        assertEquals(subIdSim1, intentSim1.getIntExtra("subId", -1))
        assertEquals(subIdSim1, intentSim1.getIntExtra("com.android.phone.extra.subscription", -1))
        assertEquals(0, intentSim1.getIntExtra("simSlot", -1))
        assertEquals(0, intentSim1.getIntExtra("slot", -1))
        assertEquals(0, intentSim1.getIntExtra("phone_id", -1))

        // SIM 2 (Slot 1, SubId 202)
        val slot2 = 2
        val subInfo2 = SimOperatorUtils.findActiveSubscriptionForSlot(context, slot2)
        val intentSim2 = Intent(Intent.ACTION_CALL, Uri.parse("tel:$encodedCode")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            val subscriptionId = subInfo2?.subscriptionId
            val slotIndex = slot2 - 1
            if (subscriptionId != null && subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                putExtra("subscription", subscriptionId)
                putExtra("subId", subscriptionId)
                putExtra("com.android.phone.extra.subscription", subscriptionId)
                putExtra("com.android.phone.extra.slot", slotIndex)
            }
            putExtra("simSlot", slotIndex)
            putExtra("slot", slotIndex)
            putExtra("phone_id", slotIndex)
            putExtra("simId", slotIndex.toLong())
        }

        assertEquals(subIdSim2, intentSim2.getIntExtra("subscription", -1))
        assertEquals(subIdSim2, intentSim2.getIntExtra("subId", -1))
        assertEquals(subIdSim2, intentSim2.getIntExtra("com.android.phone.extra.subscription", -1))
        assertEquals(1, intentSim2.getIntExtra("simSlot", -1))
        assertEquals(1, intentSim2.getIntExtra("slot", -1))
        assertEquals(1, intentSim2.getIntExtra("phone_id", -1))
    }

    @Test
    fun testSubscriptionIdentityShielding_simSwapDetectionAndIsolation() = runTest {
        val repository = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )

        // 1. Guardar datos para SIM 1 (subId = 101)
        val sim1Ussd = "Saldo: 250.50 CUP. Linea activa hasta 24/09/2026."
        val parsedSim1 = EtecsaUssdParser.parseUssdResponse(sim1Ussd, "*222#")
        repository.saveParsedData(parsedSim1, sim1Ussd, simSlot = 1)

        val planSim1 = repository.getPlanStatusDirect(1)
        assertNotNull("Plan para SIM 1 debe existir", planSim1)
        assertEquals(subIdSim1, planSim1?.subscriptionId)
        assertEquals(250.50, planSim1?.balanceCup ?: 0.0, 0.01)

        // 2. Simular cambio físico de tarjeta SIM en la Ranura 1 (reemplazo por nueva SIM con subId = 303)
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)

        val newSubIdSim1 = 303
        val newSubInfo1 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(newSubIdSim1)
            .setSimSlotIndex(0)
            .setDisplayName("Nueva SIM Cubacel")
            .setCarrierName("Cubacel")
            .setCountryIso("cu")
            .setMcc("368")
            .setMnc("01")
            .buildSubscriptionInfo()

        val subInfo2 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim2)
            .setSimSlotIndex(1)
            .setDisplayName("Cubacel Trabajo")
            .setCarrierName("Cubacel")
            .setCountryIso("cu")
            .setMcc("368")
            .setMnc("01")
            .buildSubscriptionInfo()

        shadowSubManager.setActiveSubscriptionInfos(newSubInfo1, subInfo2)

        // 3. Comprobar que la lectura directa y por Flow NO muestran los datos obsoletos de la SIM anterior
        val stalePlanDirect = repository.getPlanStatusDirect(1)
        assertNull("getPlanStatusDirect no debe entregar datos obsoletos de una SIM sustituida", stalePlanDirect)

        val stalePlanFlow = repository.getPlanStatus(1).first()
        assertNull("getPlanStatus (Flow) no debe entregar datos obsoletos de una SIM sustituida", stalePlanFlow)

        // 4. Al realizar una consulta con la nueva SIM en Slot 1, los datos se inicializan limpios para el nuevo subId
        val newSim1Ussd = "Saldo: 50.00 CUP. Linea activa hasta 01/01/2027."
        val parsedNewSim1 = EtecsaUssdParser.parseUssdResponse(newSim1Ussd, "*222#")
        repository.saveParsedData(parsedNewSim1, newSim1Ussd, simSlot = 1)

        val newPlanSim1 = repository.getPlanStatusDirect(1)
        assertNotNull("Plan para nueva SIM debe existir", newPlanSim1)
        assertEquals(newSubIdSim1, newPlanSim1?.subscriptionId)
        assertEquals(50.00, newPlanSim1?.balanceCup ?: 0.0, 0.01)
        // Verificar que los datos anteriores no se mezclaron
        assertEquals(0L, newPlanSim1?.dataMb ?: 0L)

        // 5. Verificar que la SIM 2 (subId = 202) se mantiene completamente intacta e independiente
        val sim2Ussd = "Saldo: 1450.00 CUP. Linea activa hasta 10/12/2026."
        val parsedSim2 = EtecsaUssdParser.parseUssdResponse(sim2Ussd, "*222#")
        repository.saveParsedData(parsedSim2, sim2Ussd, simSlot = 2)

        val planSim2 = repository.getPlanStatusDirect(2)
        assertNotNull(planSim2)
        assertEquals(subIdSim2, planSim2?.subscriptionId)
        assertEquals(1450.00, planSim2?.balanceCup ?: 0.0, 0.01)
    }

    @Test
    fun test1_singleSim_operations() = runTest {
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)
        val subInfo1 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim1)
            .setSimSlotIndex(0)
            .setDisplayName("Cubacel Single")
            .setCarrierName("Cubacel")
            .buildSubscriptionInfo()
        shadowSubManager.setActiveSubscriptionInfos(subInfo1)

        val repository = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )

        val ussd = "Saldo: 500.00 CUP. Linea activa hasta 01/01/2027."
        val parsed = EtecsaUssdParser.parseUssdResponse(ussd, "*222#")
        repository.saveParsedData(parsed, ussd, simSlot = 1)

        val plan1 = repository.getPlanStatusDirect(1)
        val plan2 = repository.getPlanStatusDirect(2)

        assertNotNull("Slot 1 debe devolver datos", plan1)
        assertEquals(500.00, plan1?.balanceCup ?: 0.0, 0.01)
        assertEquals(subIdSim1, plan1?.subscriptionId)
        assertNull("Slot 2 inexistente debe devolver null", plan2)
    }

    @Test
    fun test5_slotReorder_identityPreserved() = runTest {
        val repository = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )

        // Estado inicial: SIM A (101) en Slot 1, SIM B (202) en Slot 2
        val ussdA = "Saldo: 100.00 CUP. Linea activa hasta 01/01/2027."
        val ussdB = "Saldo: 200.00 CUP. Linea activa hasta 01/01/2027."
        repository.saveParsedData(EtecsaUssdParser.parseUssdResponse(ussdA, "*222#"), ussdA, simSlot = 1)
        repository.saveParsedData(EtecsaUssdParser.parseUssdResponse(ussdB, "*222#"), ussdB, simSlot = 2)

        assertEquals(100.00, repository.getPlanStatusDirect(1)?.balanceCup ?: 0.0, 0.01)
        assertEquals(200.00, repository.getPlanStatusDirect(2)?.balanceCup ?: 0.0, 0.01)

        // Reordenamiento de Slots: SIM A (101) pasa a Slot 2, SIM B (202) pasa a Slot 1
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)

        val swappedSubInfo1 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim2) // 202 ahora en Slot 0 (Slot 1 lógico)
            .setSimSlotIndex(0)
            .setDisplayName("Cubacel Trabajo")
            .setCarrierName("Cubacel")
            .buildSubscriptionInfo()

        val swappedSubInfo2 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(subIdSim1) // 101 ahora en Slot 1 (Slot 2 lógico)
            .setSimSlotIndex(1)
            .setDisplayName("Cubacel Principal")
            .setCarrierName("Cubacel")
            .buildSubscriptionInfo()

        shadowSubManager.setActiveSubscriptionInfos(swappedSubInfo1, swappedSubInfo2)

        // Al cambiar de slot, el repositorio no debe entregar los datos antiguos invertidos
        assertNull("Slot 1 con nueva SIM 202 no debe mostrar datos viejos de SIM 101", repository.getPlanStatusDirect(1))
        assertNull("Slot 2 con nueva SIM 101 no debe mostrar datos viejos de SIM 202", repository.getPlanStatusDirect(2))

        // Al refrescar SIM B (202) en Slot 1 y SIM A (101) en Slot 2:
        repository.saveParsedData(EtecsaUssdParser.parseUssdResponse(ussdB, "*222#"), ussdB, simSlot = 1)
        repository.saveParsedData(EtecsaUssdParser.parseUssdResponse(ussdA, "*222#"), ussdA, simSlot = 2)

        val newPlanSlot1 = repository.getPlanStatusDirect(1)
        val newPlanSlot2 = repository.getPlanStatusDirect(2)

        assertEquals(202, newPlanSlot1?.subscriptionId)
        assertEquals(200.00, newPlanSlot1?.balanceCup ?: 0.0, 0.01)

        assertEquals(101, newPlanSlot2?.subscriptionId)
        assertEquals(100.00, newPlanSlot2?.balanceCup ?: 0.0, 0.01)
    }

    @Test
    fun test6_coldStart_afterSimSwap() = runTest {
        // 1. Guardar datos iniciales para subId 101
        val repositoryOld = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )
        val ussd = "Saldo: 300.00 CUP. Linea activa hasta 01/01/2027."
        repositoryOld.saveParsedData(EtecsaUssdParser.parseUssdResponse(ussd, "*222#"), ussd, simSlot = 1)

        // 2. SIM swap en Slot 1 -> subId 303
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)
        val newSubInfo = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(303)
            .setSimSlotIndex(0)
            .setDisplayName("SIM 303")
            .setCarrierName("Cubacel")
            .buildSubscriptionInfo()
        shadowSubManager.setActiveSubscriptionInfos(newSubInfo)

        // 3. Simular COLD START creando un nuevo repositorio desde cero
        val repositoryNew = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )

        val planColdStart = repositoryNew.getPlanStatusDirect(1)
        assertNull("Tras Cold Start con nueva SIM, no se deben devolver datos de la SIM anterior", planColdStart)
    }

    @Test
    fun test8_alerts_and_widget_subscriptionShielding() = runTest {
        // Guardar datos con subId 101 en Slot 1
        val ussd = "Saldo: 10.00 CUP. Linea activa hasta 01/01/2027."
        val entity = PlanStatusEntity(
            id = 1,
            subscriptionId = 101,
            balanceCup = 10.00,
            dataMb = 50,
            dataLteMb = 0,
            dataDays = 2,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.planDao().insertOrUpdatePlanStatus(entity)

        // SIM Swap a subId 303
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)
        val newSubInfo = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(303)
            .setSimSlotIndex(0)
            .setDisplayName("SIM 303")
            .setCarrierName("Cubacel")
            .buildSubscriptionInfo()
        shadowSubManager.setActiveSubscriptionInfos(newSubInfo)

        // Comprobar que getPlanStatusDirect para Slot 1 filtra la entidad obsoleta
        val repository = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )
        assertNull("Repositorio no debe retornar la entidad con subId 101 cuando la SIM actual es 303", repository.getPlanStatusDirect(1))
    }

    @Test
    fun test9_smsLoggingAndIsolation_forDualSim() = runTest {
        val repository = com.ams.megascu.data.db.MegasRepository(
            context = context,
            planDao = db.planDao(),
            smsLogDao = db.smsLogDao(),
            usageHistoryDao = db.usageHistoryDao()
        )

        // Registrar SMS en SIM 1 (subId = 101)
        val smsTextSim1 = "Estimado cliente, su plan de datos vence el 24/09/2026. Recargue para no perder megas."
        val extractedDate1 = EtecsaUssdParser.parseEtecsaSms(smsTextSim1)
        repository.logInterceptedSms("Cubacel-ETECSA", smsTextSim1, extractedDate1, simSlot = 1, subscriptionId = subIdSim1)

        // Registrar SMS en SIM 2 (subId = 202)
        val smsTextSim2 = "Estimado cliente, su paquete LTE de 4.5GB vence el 28/09/2026. Marque *133#."
        val extractedDate2 = EtecsaUssdParser.parseEtecsaSms(smsTextSim2)
        repository.logInterceptedSms("Cubacel-ETECSA", smsTextSim2, extractedDate2, simSlot = 2, subscriptionId = subIdSim2)

        // Verificar listado histórico completo
        val allLogs = repository.smsLogs.first()
        assertEquals("Debe haber 2 SMS registrados en total", 2, allLogs.size)

        // Verificar filtrado por SIM
        val sim1Logs = repository.getSmsLogs(1).first()
        val sim2Logs = repository.getSmsLogs(2).first()

        assertEquals(1, sim1Logs.size)
        assertEquals(1, sim2Logs.size)

        assertEquals("simSlot debe ser 1", 1, sim1Logs[0].simSlot)
        assertEquals("subscriptionId debe ser 101", subIdSim1, sim1Logs[0].subscriptionId)
        assertEquals("Fecha extraída SIM 1", "24/09/2026", sim1Logs[0].extractedDateStr)

        assertEquals("simSlot debe ser 2", 2, sim2Logs[0].simSlot)
        assertEquals("subscriptionId debe ser 202", subIdSim2, sim2Logs[0].subscriptionId)
        assertEquals("Fecha extraída SIM 2", "28/09/2026", sim2Logs[0].extractedDateStr)
    }

    @Test
    fun test10_smsMigration_v5_to_v6() {
        val sqliteDb = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name("test_migration.db")
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(5) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            // Crear esquema v5 de sms_logs (sin simSlot ni subscriptionId)
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS `sms_logs` (
                                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                    `sender` TEXT NOT NULL,
                                    `messageBody` TEXT NOT NULL,
                                    `extractedDateStr` TEXT,
                                    `timestamp` INTEGER NOT NULL
                                )
                                """.trimIndent()
                            )
                            // Insertar SMS previo existente
                            db.execSQL("INSERT INTO sms_logs (sender, messageBody, extractedDateStr, timestamp) VALUES ('ETECSA', 'Mensaje existente v5', '20/09/2026', 1700000000000)")
                        }

                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            ).writableDatabase

        // Ejecutar migración 5 -> 6
        MegasDatabase.MIGRATION_5_6.migrate(sqliteDb)

        // Consultar los datos tras migración
        val cursor = sqliteDb.query("SELECT id, sender, messageBody, extractedDateStr, simSlot, subscriptionId FROM sms_logs")
        assertTrue(cursor.moveToFirst())
        assertEquals("ETECSA", cursor.getString(1))
        assertEquals("Mensaje existente v5", cursor.getString(2))
        assertEquals("20/09/2026", cursor.getString(3))
        assertEquals("Default simSlot debe ser 1", 1, cursor.getInt(4))
        assertTrue("subscriptionId previo debe ser null", cursor.isNull(5))
        cursor.close()
        sqliteDb.close()
    }

    @Test
    fun testExpirationAndRechargeAlertsDualSimIsolation() = runTest {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancelAll()

        val planSim1 = PlanStatusEntity(
            id = 1,
            subscriptionId = subIdSim1,
            dataDays = 1, // 1 day remaining (last day)
            dataMb = 500L,
            dataLteMb = 500L,
            nextRechargeDays = 0,
            nextRechargeDateStr = "24/08/2026"
        )
        val planSim2 = PlanStatusEntity(
            id = 2,
            subscriptionId = subIdSim2,
            dataDays = 5, // 5 days remaining
            dataMb = 3000L,
            dataLteMb = 1000L,
            nextRechargeDays = 10,
            nextRechargeDateStr = "03/09/2026"
        )

        // Trigger expiration alert for SIM 1
        com.ams.megascu.service.PlanExpirationAlertManager.checkAndNotifyExpiration(context, planSim1)

        // Trigger expiration alert for SIM 2
        com.ams.megascu.service.PlanExpirationAlertManager.checkAndNotifyExpiration(context, planSim2)

        val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)

        // Verify that anti-spam keys are separated by subscription
        val sim1AlertDate = prefs.getString("sub_${subIdSim1}_last_exp_alert_date_1day", "")
        val sim2AlertDate = prefs.getString("sub_${subIdSim2}_last_exp_alert_date_5days", "")
        assertTrue("SIM 1 last day alert anti-spam registered", !sim1AlertDate.isNullOrBlank())
        assertTrue("SIM 2 5-days alert anti-spam registered", !sim2AlertDate.isNullOrBlank())

        // Verify recharge alert anti-spam isolation
        val sim1RechargeDate = prefs.getString("sub_${subIdSim1}_last_recharge_alert_date", "")
        val sim2RechargeDate = prefs.getString("sub_${subIdSim2}_last_recharge_alert_date", "")
        assertTrue("SIM 1 recharge alert registered", !sim1RechargeDate.isNullOrBlank())
        assertTrue("SIM 2 recharge alert should NOT be registered as nextRechargeDays > 0", sim2RechargeDate.isNullOrBlank())
    }

    @Test
    fun testAlertManagersSubscriptionDerivedNotificationIds() {
        // Expiration Alert Manager IDs
        val expIdSim1 = com.ams.megascu.service.PlanExpirationAlertManager.getExpirationNotificationId(1, 101)
        val expIdSim2 = com.ams.megascu.service.PlanExpirationAlertManager.getExpirationNotificationId(2, 202)
        val expIdFallback1 = com.ams.megascu.service.PlanExpirationAlertManager.getExpirationNotificationId(1, null)
        val expIdFallback2 = com.ams.megascu.service.PlanExpirationAlertManager.getExpirationNotificationId(2, null)

        assertNotEquals("IDs de expiración con subscriptionId deben ser distintos", expIdSim1, expIdSim2)
        assertEquals(880300 + 101, expIdSim1)
        assertEquals(880300 + 202, expIdSim2)
        assertEquals(8803, expIdFallback1)
        assertEquals(8813, expIdFallback2)

        // Recharge Alert Manager IDs
        val recIdSim1 = com.ams.megascu.service.PlanExpirationAlertManager.getRechargeNotificationId(1, 101)
        val recIdSim2 = com.ams.megascu.service.PlanExpirationAlertManager.getRechargeNotificationId(2, 202)
        assertNotEquals("IDs de recarga deben ser distintos", recIdSim1, recIdSim2)
        assertEquals(880400 + 101, recIdSim1)
        assertEquals(880400 + 202, recIdSim2)

        // Daily Limit Alert Manager IDs
        val dailyIdSim1 = com.ams.megascu.service.DailyLimitAlertManager.getDailyLimitNotificationId(1, 101)
        val dailyIdSim2 = com.ams.megascu.service.DailyLimitAlertManager.getDailyLimitNotificationId(2, 202)
        assertNotEquals("IDs de límite diario deben ser distintos", dailyIdSim1, dailyIdSim2)
        assertEquals(880200 + 101, dailyIdSim1)
        assertEquals(880200 + 202, dailyIdSim2)
    }

    @Test
    fun testEtecsaMonitoringServiceNotificationIdsAndIntentIsolation() {
        val notifIdSim1 = com.ams.megascu.service.EtecsaMonitoringService.getNotificationId(1, 101)
        val notifIdSim2 = com.ams.megascu.service.EtecsaMonitoringService.getNotificationId(2, 202)
        val notifIdFallback1 = com.ams.megascu.service.EtecsaMonitoringService.getNotificationId(1, null)
        val notifIdFallback2 = com.ams.megascu.service.EtecsaMonitoringService.getNotificationId(2, null)

        assertNotEquals("IDs de EtecsaMonitoringService deben ser distintos entre suscripciones", notifIdSim1, notifIdSim2)
        assertEquals(880100 + 101, notifIdSim1)
        assertEquals(880100 + 202, notifIdSim2)
        assertEquals(8801, notifIdFallback1)
        assertEquals(8811, notifIdFallback2)
    }

    @Test
    fun testLegacyNullSubscriptionIdShieldingInRepository() = runTest {
        // Insert a legacy record in DB with subscriptionId = null and old data
        val legacyEntity = PlanStatusEntity(
            id = 1,
            subscriptionId = null,
            balanceCup = 999.0,
            dataMb = 5000L,
            dataLteMb = 5000L
        )
        db.planDao().insertOrUpdatePlanStatus(legacyEntity)

        // When a new SIM with known subscriptionId (subIdSim1 = 101) is active:
        // getPlanStatusDirect(1) should return NULL because legacy entity without subscriptionId must not leak into sub 101!
        val retrieved = repository.getPlanStatusDirect(1)
        assertNull("Registro heredado con subscriptionId null no debe asignarse automáticamente a la nueva SIM con subId", retrieved)

        // Now simulate new USSD parsed data arriving for subIdSim1
        val parsed = com.ams.megascu.data.ussd.ParsedPlanData(
            balanceCup = 50.0,
            dataMb = 200L,
            dataLteMb = 200L,
            dataDays = 30
        )
        repository.saveParsedData(parsed, "Response sub 101", 1)

        val updated = repository.getPlanStatusDirect(1)
        assertNotNull(updated)
        assertEquals(subIdSim1, updated?.subscriptionId)
        assertEquals(50.0, updated?.balanceCup ?: 0.0, 0.001)
        assertEquals(200L, updated?.dataMb)
    }

    @Test
    fun testNoCubacelPresumptionOnEmptyOperatorCode() {
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager: ShadowSubscriptionManager = shadowOf(subManager)

        val unknownSub = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(999)
            .setSimSlotIndex(0)
            .setDisplayName("SIM 1")
            .setCarrierName("")
            .setCountryIso("")
            .setMcc("")
            .setMnc("")
            .buildSubscriptionInfo()
        shadowSubManager.setActiveSubscriptionInfos(unknownSub)

        val shadowTelephony = org.robolectric.Shadows.shadowOf(
            context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        )
        shadowTelephony.setNetworkOperatorName("")
        shadowTelephony.setNetworkOperator("")
        shadowTelephony.setSimOperatorName("")
        shadowTelephony.setSimOperator("")
        shadowTelephony.setSimState(android.telephony.TelephonyManager.SIM_STATE_READY)

        val details = com.ams.megascu.data.ussd.SimOperatorUtils.checkSimOperator(context, 1, isEmulator = false)
        assertFalse("Operador vacío en SIM 1 no debe presumirse automáticamente como Cubacel", details.isCubacel)
        assertEquals("Operadora Desconocida (SIM 1)", details.operatorName)
    }

    @Test
    fun testDailyLimitAlertsDualSimIndependentLimits() = runTest {
        val planSim1 = PlanStatusEntity(
            id = 1,
            subscriptionId = subIdSim1,
            dataDays = 10,
            dataMb = 500L,
            dataLteMb = 500L // Total 1000 MB => 100 MB/day recommended
        )
        val planSim2 = PlanStatusEntity(
            id = 2,
            subscriptionId = subIdSim2,
            dataDays = 2,
            dataMb = 2000L,
            dataLteMb = 2000L // Total 4000 MB => 2000 MB/day recommended
        )
        db.planDao().insertOrUpdatePlanStatus(planSim1)
        db.planDao().insertOrUpdatePlanStatus(planSim2)

        // Trigger daily limit check
        com.ams.megascu.service.DailyLimitAlertManager.checkAndNotify(context)

        // Verify both SIMs are saved and accessible independently
        val storedSim1 = db.planDao().getPlanStatusDirect(1)
        val storedSim2 = db.planDao().getPlanStatusDirect(2)
        assertEquals(1000L, (storedSim1?.dataMb ?: 0) + (storedSim1?.dataLteMb ?: 0))
        assertEquals(4000L, (storedSim2?.dataMb ?: 0) + (storedSim2?.dataLteMb ?: 0))
    }
}
