package com.ams.megascu

import com.ams.megascu.data.ussd.UssdBatchReport
import com.ams.megascu.data.ussd.UssdStatusResponseValidator
import org.junit.Assert.*
import org.junit.Test

class UssdStatusResponseValidatorTest {
    @Test fun modemErrorTextIsNotStatusSuccess() {
        UssdBatchReport.STATUS_CODES.forEach { assertNull(UssdStatusResponseValidator.parse("Problema de conexión o código MMI incorrecto", it)) }
    }
    @Test fun emptyRepliesAreInvalid() { assertNull(UssdStatusResponseValidator.parse("  ", "*222#")) }
    @Test fun zeroBalanceIsValid() { assertEquals(0.0, UssdStatusResponseValidator.parse("Saldo: 0.00 CUP", "*222#")!!.balanceCup!!, 0.0) }
    @Test fun dataQueryCannotSucceedWithAnUnrelatedBalance() {
        assertNull(UssdStatusResponseValidator.parse("Saldo: 150 CUP", "*222*328#"))
    }
    @Test fun ordinaryDataAndLteBalancesRemainCompatible() {
        val parsed = UssdStatusResponseValidator.parse("Usted tiene 2048 MB de Datos principales y 4096 MB en Red LTE.", "*222*328#")!!
        assertEquals(2048L, parsed.dataMb)
        assertEquals(4096L, parsed.dataLteMb)
    }
    @Test fun explicitAbsenceResetsOnlyRequestedCategory() {
        val data = UssdStatusResponseValidator.parse("Usted no tiene paquetes de datos activos", "*222*328#")!!
        assertEquals(0L, data.dataMb)
        assertEquals(0L, data.dataLteMb)
        assertNull(data.balanceCup)
        assertEquals(0, UssdStatusResponseValidator.parse("No dispone de mensajes activos", "*222*767#")!!.sms)
        assertEquals(0L, UssdStatusResponseValidator.parse("No tiene bonos activos", "*222*266#")!!.bonusMb)
        assertEquals("0", UssdStatusResponseValidator.parse("No tiene minutos", "*222*869#")!!.minutesStr)
    }
    @Test fun interactiveMenusOutsideStatusQueriesRemainUsable() {
        assertNotNull(UssdStatusResponseValidator.parse("Menú ETECSA: 1. Datos 2. Voz", "*133#"))
    }
    @Test fun rechargeAvailabilityWithoutDateRemainsValid() {
        assertEquals(0, UssdStatusResponseValidator.parse("Ud puede recargar un monto de 360,00CUP", "*222*732#")!!.nextRechargeDays)
    }
}
