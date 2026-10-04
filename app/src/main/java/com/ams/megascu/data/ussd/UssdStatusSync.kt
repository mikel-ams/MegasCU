package com.ams.megascu.data.ussd

import android.content.Context
import com.ams.megascu.MegasApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** The UI, periodic sync and widgets all persist through the same repository. */
class UssdStatusSync(private val context: Context, private val executor: UssdExecutor = UssdExecutor(context)) {
    suspend fun query(code: String, simSlot: Int): UssdResult {
        val subscriptionBefore = SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
        return try {
            when (val result = executor.executeUssdSuspend(code, simSlot, allowDialFallback = false)) {
                is UssdResult.Error -> result
                is UssdResult.Success -> {
                    if (subscriptionBefore != SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)) {
                        UssdResult.Error(UssdErrorType.INVALID_RESPONSE, "La SIM cambió durante la consulta. Vuelve a actualizar.")
                    } else {
                        val parsed = UssdStatusResponseValidator.parse(result.response, code)
                            ?: return UssdResult.Error(UssdErrorType.INVALID_RESPONSE,
                                "No se reconoció la respuesta de ${UssdBatchReport.labelForCode(code)}. Vuelve a consultar.")
                        val repository = (context.applicationContext as MegasApplication).repository
                        repository.saveParsedData(
                            parsed, result.response,
                            simSlot = simSlot, recordDataSample = code == "*222*328#",
                            expectedSubscriptionId = subscriptionBefore, enforceSubscriptionIdentity = true
                        )
                        result
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            android.util.Log.w("UssdStatusSync", "No se pudo guardar la consulta $code", error)
            UssdResult.Error(UssdErrorType.UNKNOWN, "No se pudo completar o guardar la consulta de ${UssdBatchReport.labelForCode(code)}.")
        }
    }

    suspend fun run(codes: List<String>, simSlot: Int): UssdBatchReport {
        val report = UssdBatchReport()
        codes.distinct().forEachIndexed { index, code ->
            if (index > 0) delay(800L)
            report.record(code, query(code, simSlot))
        }
        return report
    }
}
