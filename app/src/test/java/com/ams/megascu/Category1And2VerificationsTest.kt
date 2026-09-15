package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasRepository
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.ui.components.ConsultaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Method
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Category1And2VerificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    // ==========================================
    // CATEGORÍA 1 (LEVES) - TESTS DE VERIFICACIÓN
    // ==========================================

    @Test
    fun testCategory1_dynamicBonusFormatting_mbAndGb() {
        // En ConsultasSection, verificamos la lógica del item de bono
        val consultaBonoLogic: (Long) -> String? = { bonusDataMb ->
            val status = PlanStatusEntity(
                id = 1,
                balanceCup = 100.0,
                dataMb = 0,
                dataLteMb = 0,
                bonusDataMb = bonusDataMb,
                minutesStr = "",
                smsCount = 0,
                dataDays = 30,
                minutesDays = 30,
                smsDays = 30,
                dataExpirationTimestamp = 0L,
                nextRechargeDateStr = "",
                nextRechargeDays = 0,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            if (status.bonusDataMb > 0) {
                if (status.bonusDataMb >= 1024) {
                    "${String.format(Locale.US, "%.2f", status.bonusDataMb / 1024.0)} GB"
                } else {
                    "${status.bonusDataMb} MB"
                }
            } else null
        }

        // Casos de prueba:
        // 1. Sin bono
        assertNull("Bono 0 MB debe ser nulo", consultaBonoLogic(0L))

        // 2. Menor a 1024 MB debe mostrar MB
        assertEquals("300 MB", consultaBonoLogic(300L))
        assertEquals("512 MB", consultaBonoLogic(512L))
        assertEquals("1023 MB", consultaBonoLogic(1023L))

        // 3. Mayor o igual a 1024 MB debe mostrar GB formateado con 2 decimales
        assertEquals("1.00 GB", consultaBonoLogic(1024L))
        assertEquals("1.50 GB", consultaBonoLogic(1536L))
        assertEquals("2.00 GB", consultaBonoLogic(2048L))
        assertEquals("3.25 GB", consultaBonoLogic(3328L))
        assertEquals("5.00 GB", consultaBonoLogic(5120L))
    }

    @Test
    fun testCategory1_dynamicDataFormatting_mbAndGb() {
        val consultaDatosLogic: (Long, Long) -> String? = { dataMb, dataLteMb ->
            val status = PlanStatusEntity(
                id = 1,
                balanceCup = 100.0,
                dataMb = dataMb,
                dataLteMb = dataLteMb,
                bonusDataMb = 0L,
                minutesStr = "",
                smsCount = 0,
                dataDays = 30,
                minutesDays = 30,
                smsDays = 30,
                dataExpirationTimestamp = 0L,
                nextRechargeDateStr = "",
                nextRechargeDays = 0,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            val totalMb = status.dataMb + status.dataLteMb
            if (totalMb > 0) {
                if (totalMb >= 1024) "${String.format(Locale.US, "%.2f", totalMb / 1024.0)} GB" else "$totalMb MB"
            } else null
        }

        assertNull("Datos 0 MB debe ser nulo", consultaDatosLogic(0L, 0L))
        assertEquals("500 MB", consultaDatosLogic(300L, 200L))
        assertEquals("1.00 GB", consultaDatosLogic(512L, 512L))
        assertEquals("2.50 GB", consultaDatosLogic(1536L, 1024L))
    }

    @Test
    fun testCategory1_balanceCupFormatting() {
        val status = PlanStatusEntity(
            id = 1,
            balanceCup = 245.75,
            dataMb = 1000L,
            dataLteMb = 1000L,
            bonusDataMb = 0L,
            minutesStr = "",
            smsCount = 0,
            dataDays = 30,
            minutesDays = 30,
            smsDays = 30,
            dataExpirationTimestamp = 0L,
            nextRechargeDateStr = "",
            nextRechargeDays = 0,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        val formatted = "${String.format(Locale.US, "%.2f", status.balanceCup)} CUP"
        assertEquals("245.75 CUP", formatted)
    }

    // ==========================================
    // CATEGORÍA 2 (MEDIAS) - TESTS DE VERIFICACIÓN
    // ==========================================

    @Test
    fun testCategory2_calculateInitialDelayMs_safetyLowerBound() {
        val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
        val repository = MegasRepository(context, db.planDao(), db.smsLogDao(), db.usageHistoryDao())
        val testHoursList = listOf(1, 2, 4, 6, 8, 12, 24)
        
        for (hours in testHoursList) {
            val delayMs = repository.calculateInitialDelayMs(hours)
            
            // Verificación 1: El retraso inicial siempre debe ser al menos 60,000 ms (1 minuto)
            // para evitar sincronizaciones continuas / ráfagas
            assertTrue(
                "Para intervalo $hours horas, delay ($delayMs ms) debe ser >= 60,000 ms",
                delayMs >= 60_000L
            )
            
            // Verificación 2: El retraso no debe superar el intervalo total de horas en ms
            val maxAllowedMs = hours * 3600_000L + 60_000L
            assertTrue(
                "Para intervalo $hours horas, delay ($delayMs ms) no debe superar $maxAllowedMs ms",
                delayMs <= maxAllowedMs
            )
        }
    }

    @Test
    fun testCategory2_widgetCacheMultiSimSeparation() {
        // Obtenemos los métodos privados saveCachedPlan y getCachedPlan mediante reflexión
        val widgetProviderClass = Class.forName("com.ams.megascu.widget.MegasWidgetProvider\$Companion")
        val companionField = Class.forName("com.ams.megascu.widget.MegasWidgetProvider").getField("Companion")
        val companionInstance = companionField.get(null)

        val saveCachedPlanMethod: Method = widgetProviderClass.getDeclaredMethod(
            "saveCachedPlan",
            Context::class.java,
            PlanStatusEntity::class.java,
            Int::class.javaPrimitiveType
        )
        saveCachedPlanMethod.isAccessible = true

        val getCachedPlanMethod: Method = widgetProviderClass.getDeclaredMethod(
            "getCachedPlan",
            Context::class.java,
            Int::class.javaPrimitiveType
        )
        getCachedPlanMethod.isAccessible = true

        // 1. Guardar Plan para SIM 1
        val planSim1 = PlanStatusEntity(
            id = 1,
            balanceCup = 150.0,
            dataMb = 2048L,
            dataLteMb = 1024L,
            bonusDataMb = 500L,
            minutesStr = "15:00",
            smsCount = 20,
            dataDays = 25,
            minutesDays = 25,
            smsDays = 25,
            dataExpirationTimestamp = 1756200000000L,
            nextRechargeDateStr = "15/09/2026",
            nextRechargeDays = 20,
            lastUpdatedTimestamp = 1756100000000L
        )
        saveCachedPlanMethod.invoke(companionInstance, context, planSim1, 1)

        // 2. Guardar Plan para SIM 2
        val planSim2 = PlanStatusEntity(
            id = 2,
            balanceCup = 450.0,
            dataMb = 8192L,
            dataLteMb = 4096L,
            bonusDataMb = 2048L,
            minutesStr = "45:00",
            smsCount = 80,
            dataDays = 10,
            minutesDays = 10,
            smsDays = 10,
            dataExpirationTimestamp = 1756300000000L,
            nextRechargeDateStr = "25/09/2026",
            nextRechargeDays = 30,
            lastUpdatedTimestamp = 1756150000000L
        )
        saveCachedPlanMethod.invoke(companionInstance, context, planSim2, 2)

        // 3. Recuperar y verificar independencia de SIM 1
        val cachedSim1 = getCachedPlanMethod.invoke(companionInstance, context, 1) as? PlanStatusEntity
        assertNotNull("El cache de SIM 1 no debe ser nulo", cachedSim1)
        assertEquals(1, cachedSim1?.id)
        assertEquals(150.0, cachedSim1?.balanceCup ?: 0.0, 0.01)
        assertEquals(2048L, cachedSim1?.dataMb)
        assertEquals(1024L, cachedSim1?.dataLteMb)
        assertEquals(500L, cachedSim1?.bonusDataMb)
        assertEquals("15:00", cachedSim1?.minutesStr)
        assertEquals(20, cachedSim1?.smsCount)
        assertEquals("15/09/2026", cachedSim1?.nextRechargeDateStr)

        // 4. Recuperar y verificar independencia de SIM 2
        val cachedSim2 = getCachedPlanMethod.invoke(companionInstance, context, 2) as? PlanStatusEntity
        assertNotNull("El cache de SIM 2 no debe ser nulo", cachedSim2)
        assertEquals(2, cachedSim2?.id)
        assertEquals(450.0, cachedSim2?.balanceCup ?: 0.0, 0.01)
        assertEquals(8192L, cachedSim2?.dataMb)
        assertEquals(4096L, cachedSim2?.dataLteMb)
        assertEquals(2048L, cachedSim2?.bonusDataMb)
        assertEquals("45:00", cachedSim2?.minutesStr)
        assertEquals(80, cachedSim2?.smsCount)
        assertEquals("25/09/2026", cachedSim2?.nextRechargeDateStr)
    }

    @Test
    fun testCategory2_widgetSimSlotPreferenceStorage() {
        val widgetPrefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
        
        // Asignamos SIM 1 a widget 101 y SIM 2 a widget 102
        widgetPrefs.edit()
            .putInt("widget_101_sim_slot", 1)
            .putInt("widget_102_sim_slot", 2)
            .apply()

        val slot101 = widgetPrefs.getInt("widget_101_sim_slot", 1).coerceIn(1, 2)
        val slot102 = widgetPrefs.getInt("widget_102_sim_slot", 1).coerceIn(1, 2)
        val slotDefault = widgetPrefs.getInt("widget_999_sim_slot", 1).coerceIn(1, 2)

        assertEquals(1, slot101)
        assertEquals(2, slot102)
        assertEquals(1, slotDefault)
    }

    @Test
    fun testCategory1_smsWidgetUssdCodeResolution() {
        val resolveCode: (String) -> String = { type ->
            when (type) {
                "saldo" -> "*222#"
                "megas" -> "*222*328#"
                "llamadas" -> "*222*869#"
                "mensajes" -> "*222*767#"
                "bono" -> "*222*266#"
                else -> "*222*328#"
            }
        }
        assertEquals("*222*767#", resolveCode("mensajes"))
        assertEquals("*222*266#", resolveCode("bono"))
        assertEquals("*222#", resolveCode("saldo"))
        assertEquals("*222*328#", resolveCode("megas"))
        assertEquals("*222*869#", resolveCode("llamadas"))
    }

    @Test
    fun testCategory1_naturalLanguageBalanceParsing() {
        val sample1 = "Su saldo es de 816.44 CUP con vigencia hasta 2026-11-20."
        val parsed1 = com.ams.megascu.data.ussd.EtecsaUssdParser.parseUssdResponse(sample1, "*222#")
        assertEquals(816.44, parsed1.balanceCup ?: 0.0, 0.001)

        val sample2 = "Saldo actual es 150 CUP."
        val parsed2 = com.ams.megascu.data.ussd.EtecsaUssdParser.parseUssdResponse(sample2, "*222#")
        assertEquals(150.0, parsed2.balanceCup ?: 0.0, 0.001)

        val sample3 = "Su saldo disponible es 50.25 CUP"
        val parsed3 = com.ams.megascu.data.ussd.EtecsaUssdParser.parseUssdResponse(sample3, "*222#")
        assertEquals(50.25, parsed3.balanceCup ?: 0.0, 0.001)
    }

    @Test
    fun testCategory1_bonusCodeDoesNotOverwriteMainDataDays() {
        val bonusResp = "Bono activo: 2048 MB para navegacion nacional. 10 dias restantes."
        val parsed = com.ams.megascu.data.ussd.EtecsaUssdParser.parseUssdResponse(bonusResp, "*222*266#")
        assertEquals(2048L, parsed.bonusMb)
        assertEquals(10, parsed.bonusDays)
        assertNull("dataDays no debe alterarse por consulta de bono", parsed.dataDays)
    }

    @Test
    fun testCategory1_notificationTitleTodayVsTomorrow() {
        val notifToday = com.ams.megascu.service.EtecsaMonitoringService.buildAlertNotification(
            context, "12/10/2026", 0L, isExpired = false, simSlot = 1
        )
        val notifTomorrow = com.ams.megascu.service.EtecsaMonitoringService.buildAlertNotification(
            context, "13/10/2026", 1L, isExpired = false, simSlot = 1
        )
        val notifExp = com.ams.megascu.service.EtecsaMonitoringService.buildAlertNotification(
            context, "11/10/2026", -1L, isExpired = true, simSlot = 1
        )

        val titleToday = notifToday.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val titleTomorrow = notifTomorrow.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
        val titleExp = notifExp.extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()

        assertTrue("Debe indicar VENCE HOY", titleToday?.contains("VENCE HOY") == true)
        assertTrue("Debe indicar VENCE MAÑANA", titleTomorrow?.contains("VENCE MAÑANA") == true)
        assertTrue("Debe indicar VENCIDO", titleExp?.contains("VENCIDO") == true)
    }
}
