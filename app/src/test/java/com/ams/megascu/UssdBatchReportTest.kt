package com.ams.megascu

import com.ams.megascu.data.ussd.*
import org.junit.Assert.*
import org.junit.Test

class UssdBatchReportTest {
    private val success = UssdResult.Success("Saldo: 0 CUP")
    private val timeout = UssdResult.Error(UssdErrorType.TIMEOUT, "Sin respuesta")
    @Test fun noQueriesIsNotSuccess() { assertFalse(UssdBatchReport().isComplete) }
    @Test fun oneFailureCannotBecomeCompleteSuccess() {
        val report = UssdBatchReport()
        UssdBatchReport.STATUS_CODES.forEach { report.record(it, success) }
        report.record("*222*767#", timeout)
        assertFalse(report.isComplete)
        assertEquals(5, report.successful)
        assertTrue(report.message(2).contains("parcial (5/6)"))
        assertTrue(report.message(2).contains("SMS"))
    }
    @Test fun allFailuresHaveNoCompletionMessage() {
        val report = UssdBatchReport()
        UssdBatchReport.STATUS_CODES.forEach { report.record(it, timeout) }
        assertFalse(report.isComplete)
        assertEquals(0, report.successful)
        assertTrue(report.message(1).contains("no se pudo actualizar"))
    }
    @Test fun retryRecoveryReplacesFailureRatherThanAddingQuery() {
        val report = UssdBatchReport()
        report.record("*222#", timeout)
        report.record("*222#", success)
        assertTrue(report.isComplete)
        assertEquals(1, report.total)
        assertTrue(report.failedCodes().isEmpty())
    }
    @Test fun failedRetryKeepsOriginalCodeAndPartialResult() {
        val report = UssdBatchReport()
        report.record("*222#", success)
        report.record("*222*328#", timeout)
        report.record("*222*328#", UssdResult.Error(UssdErrorType.NETWORK_ERROR, "Sin red"))
        assertEquals(listOf("*222*328#"), report.failedCodes())
        assertEquals(2, report.total)
        assertFalse(report.isComplete)
    }
    @Test fun validZeroBalanceIsSuccessAndDoesNotRequireRetry() {
        val report = UssdBatchReport()
        report.record("*222#", success)
        assertTrue(report.isComplete)
        assertTrue(report.message(1).contains("completa (1/1 consultas)"))
    }
    @Test fun persistenceErrorAlsoPreventsSuccessfulBatch() {
        val report = UssdBatchReport()
        report.record("*222#", UssdResult.Error(UssdErrorType.UNKNOWN, "No se pudo guardar"))
        assertFalse(report.isComplete)
    }
}
