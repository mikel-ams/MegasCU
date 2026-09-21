package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.ussd.EtecsaUssdParser
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Bloque3ConcurrencyAndUssdParserTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    // -------------------------------------------------------------
    // BLOQUE 3.1: Robustez de Parseo USSD y Casos Extremos
    // -------------------------------------------------------------

    @Test
    fun testParseUssdResponse_balanceVariationsAndCommaSeparators() {
        val resp1 = "Saldo principal: 125,50 CUP vence el 25/11/2026."
        val parsed1 = EtecsaUssdParser.parseUssdResponse(resp1, "*222#")
        assertEquals(125.50, parsed1.balanceCup ?: 0.0, 0.001)

        val resp2 = "Saldo actual: 0.75 CUP, vigencia 30 dias."
        val parsed2 = EtecsaUssdParser.parseUssdResponse(resp2, "*222#")
        assertEquals(0.75, parsed2.balanceCup ?: 0.0, 0.001)

        val resp3 = "CUP: 1500 CUP"
        val parsed3 = EtecsaUssdParser.parseUssdResponse(resp3, "*222#")
        assertEquals(1500.0, parsed3.balanceCup ?: 0.0, 0.001)

        val resp4 = "Su saldo es de 816.44 CUP."
        val parsed4 = EtecsaUssdParser.parseUssdResponse(resp4, "*222#")
        assertEquals(816.44, parsed4.balanceCup ?: 0.0, 0.001)

        val resp5 = "Saldo actual es 120.00 CUP"
        val parsed5 = EtecsaUssdParser.parseUssdResponse(resp5, "*222#")
        assertEquals(120.00, parsed5.balanceCup ?: 0.0, 0.001)

        val resp6 = "Su saldo principal es 250.50 CUP"
        val parsed6 = EtecsaUssdParser.parseUssdResponse(resp6, "*222#")
        assertEquals(250.50, parsed6.balanceCup ?: 0.0, 0.001)

        val respCorrupt = "Operacion rechazada sin saldo numerico."
        val parsedCorrupt = EtecsaUssdParser.parseUssdResponse(respCorrupt, "*222#")
        assertNull(parsedCorrupt.balanceCup)
    }

    @Test
    fun testParseUssdResponse_neverUsesPackagePriceAsBalance() {
        val parsed = EtecsaUssdParser.parseUssdResponse(
            "Paquete 300 CUP: 4 GB LTE. Saldo: 25.00 CUP",
            "*222#"
        )
        assertEquals(25.0, parsed.balanceCup ?: 0.0, 0.001)
    }

    @Test
    fun testParseUssdResponse_avoidsMinuteAndSmsFalsePositives() {
        val minutes = EtecsaUssdParser.parseUssdResponse(
            "Adelanto 100 minimo. Dispone de 45 minutos.",
            "*222*869#"
        )
        assertEquals("45", minutes.minutesStr)

        val sms = EtecsaUssdParser.parseUssdResponse(
            "Recibirá 2 mensajes de confirmación. Paquete vigente: 100 SMS.",
            "*222*767#"
        )
        assertEquals(100, sms.sms)
    }

    @Test
    fun testParseUssdResponse_associatesDaysWithTheCorrectSection() {
        val parsed = EtecsaUssdParser.parseUssdResponse(
            "Datos: 4 GB, vence en 30 días. Bono: 500 MB, 5 días",
            "*222*328#"
        )
        assertEquals(4096L, parsed.dataMb)
        assertEquals(500L, parsed.bonusMb)
        assertEquals(30, parsed.dataDays)
    }

    @Test
    fun testParseUssdResponse_roundsFractionalMegabytes() {
        val parsed = EtecsaUssdParser.parseUssdResponse(
            "Dispone de 0.5 MB.",
            "*222*328#"
        )
        assertEquals(1L, parsed.dataMb)
    }

    @Test
    fun testParseUssdResponse_dataMbAndGbConversions() {
        val respGb = "Usted dispone de 1.5 GB de datos y 2.0 GB LTE. Vence en 30 dias."
        val parsedGb = EtecsaUssdParser.parseUssdResponse(respGb, "*222*328#")
        assertEquals(1536L, parsedGb.dataMb)
        assertEquals(2048L, parsedGb.dataLteMb)
        assertEquals(30, parsedGb.dataDays)

        val respMb = "Dispone de 500 MB Nacionales + 1024 MB LTE. Vigencia 15 dias."
        val parsedMb = EtecsaUssdParser.parseUssdResponse(respMb, "*222*328#")
        assertEquals(500L, parsedMb.dataMb)
        assertEquals(1024L, parsedMb.dataLteMb)
        assertEquals(15, parsedMb.dataDays)
    }

    @Test
    fun testParseUssdResponse_bonusAndCombinedPackages() {
        val respBonus = "Bono activo: 1500 MB para navegacion nacional. 10 dias restantes."
        val parsedBonus = EtecsaUssdParser.parseUssdResponse(respBonus, "*222*266#")
        assertEquals(1500L, parsedBonus.bonusMb)
        assertNull("dataDays no debe ser sobrescrito por bono", parsedBonus.dataDays)
        assertEquals(10, parsedBonus.bonusDays)
    }

    @Test
    fun testParseUssdResponse_minutesAndSms() {
        val respMin = "Usted dispone de 45 MIN para llamadas nacionales vigentes por 20 dias."
        val parsedMin = EtecsaUssdParser.parseUssdResponse(respMin, "*222*869#")
        assertEquals("45", parsedMin.minutesStr)
        assertEquals(20, parsedMin.minutesDays)

        val respSms = "Dispone de 120 SMS vigentes por 25 dias."
        val parsedSms = EtecsaUssdParser.parseUssdResponse(respSms, "*222*767#")
        assertEquals(120, parsedSms.sms)
        assertEquals(25, parsedSms.smsDays)
    }

    @Test
    fun testParseUssdResponse_nextInternationalRechargeDateCalculation() {
        val today = LocalDate.now()
        val formattedDate = String.format(
            java.util.Locale.US,
            "%02d/%02d/%04d",
            today.dayOfMonth,
            today.monthValue,
            today.year
        )
        val respRecharge = "Su proxima recarga internacional debe realizarse despues del $formattedDate."
        val parsedRecharge = EtecsaUssdParser.parseUssdResponse(respRecharge, "*222*732#")

        assertNotNull(parsedRecharge.nextRechargeDateStr)
        // ETECSA recharge availability is originalDate + 1 day
        assertEquals(1, parsedRecharge.nextRechargeDays)
    }

    @Test
    fun testCalculateRechargeAvailability_corruptedOrMalformedDates() {
        val (res1, days1) = EtecsaUssdParser.calculateRechargeAvailability("")
        assertEquals("", res1)
        assertNull(days1)

        val (res2, days2) = EtecsaUssdParser.calculateRechargeAvailability("fecha-invalida")
        assertEquals("fecha-invalida", res2)
        assertNull(days2)
    }

    @Test
    fun testEvaluateExpiration_nearExpiryAndExpired() {
        val today = LocalDate.now()
        val todayStr = String.format(java.util.Locale.US, "%02d/%02d/%04d", today.dayOfMonth, today.monthValue, today.year)
        val (days0, isNear0, isExp0) = EtecsaUssdParser.evaluateExpiration(todayStr)
        assertEquals(0L, days0)
        assertTrue(isNear0)
        assertFalse(isExp0)

        val yesterday = today.minusDays(1)
        val yestStr = String.format(java.util.Locale.US, "%02d/%02d/%04d", yesterday.dayOfMonth, yesterday.monthValue, yesterday.year)
        val (daysNeg, isNearNeg, isExpNeg) = EtecsaUssdParser.evaluateExpiration(yestStr)
        assertEquals(-1L, daysNeg)
        assertFalse(isNearNeg)
        assertTrue(isExpNeg)

        val future = today.plusDays(10)
        val futStr = String.format(java.util.Locale.US, "%02d/%02d/%04d", future.dayOfMonth, future.monthValue, future.year)
        val (daysFut, isNearFut, isExpFut) = EtecsaUssdParser.evaluateExpiration(futStr)
        assertEquals(10L, daysFut)
        assertFalse(isNearFut)
        assertFalse(isExpFut)
    }

    // -------------------------------------------------------------
    // BLOQUE 3.2: Concurrencia, Mutex y Serialización de UssdExecutor
    // -------------------------------------------------------------

    @Test
    fun testUssdExecutor_invalidCodesRejectedImmediately() = runTest {
        val executor = UssdExecutor(context)
        val invalidCode1 = executor.executeUssdSuspend("222")
        assertTrue(invalidCode1 is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (invalidCode1 as UssdResult.Error).errorType)

        val invalidCode2 = executor.executeUssdSuspend("")
        assertTrue(invalidCode2 is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (invalidCode2 as UssdResult.Error).errorType)

        val invalidCode3 = executor.executeUssdSuspend("*222")
        assertTrue(invalidCode3 is UssdResult.Error)
        assertEquals(UssdErrorType.INVALID_CODE, (invalidCode3 as UssdResult.Error).errorType)
    }

    @Test
    fun testUssdExecutor_concurrentQueries_executeSafelyWithoutDeadlock() = runTest {
        val executor = UssdExecutor(context)
        val codes = listOf("*222#", "*222*328#", "*222*266#", "*222*869#", "*222*767#", "*222*732#")

        // Launch concurrent calls to simulate rapid user taps or background sync overlaps
        val deferredList = codes.map { code ->
            async {
                executor.executeUssdSuspend(code, simSlot = 1, allowDialFallback = false)
            }
        }

        val results = deferredList.awaitAll()
        assertEquals(codes.size, results.size)
        results.forEach { result ->
            assertTrue("Every concurrent execution must return a valid UssdResult", result is UssdResult.Success || result is UssdResult.Error)
        }
    }

    @Test
    fun testUssdExecutor_interactivePurchaseCode_handledGracefully() = runTest {
        val executor = UssdExecutor(context)
        val result = executor.executeUssdSuspend("*133#", simSlot = 1, allowDialFallback = false)
        assertTrue(result is UssdResult.Success)
        val successRes = result as UssdResult.Success
        assertTrue(
            successRes.response.contains("Compras") ||
            successRes.response.contains("*133") ||
            successRes.response.contains("solicitud USSD") ||
            successRes.response.contains("ETECSA") ||
            successRes.response.contains("Menú")
        )
    }
}
