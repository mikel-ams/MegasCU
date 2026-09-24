package com.ams.megascu.data.ussd

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.ams.megascu.utils.PermissionUtils
import com.ams.megascu.MegasApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

enum class UssdErrorType {
    SUCCESS,
    TIMEOUT,
    NETWORK_ERROR,
    INVALID_CODE,
    INVALID_RESPONSE,
    CARRIER_REJECTED,
    BUSY,
    UNKNOWN
}

sealed class UssdResult {
    data class Success(val response: String) : UssdResult()
    data class Error(val errorType: UssdErrorType, val message: String) : UssdResult()
}

interface UssdCallback {
    fun onSuccess(response: String)
    fun onError(errorMessage: String)
    fun onError(errorType: UssdErrorType, errorMessage: String) {
        onError(errorMessage)
    }
    fun onTimeout(code: String) {
        onError(UssdErrorType.TIMEOUT, "[ERR_CODE_101] Tiempo de espera de la red Cubacel agotado al consultar $code (10s).")
    }
}

class UssdExecutor(private val context: Context) {

    private val timeoutMs = 10000L // 10 seconds timeout
    private val appScope: CoroutineScope = (context.applicationContext as MegasApplication).appScope

    companion object {
        // Mutex to strictly serialize modem radio access across coroutines and threads
        private val radioMutex = Mutex()
    }

    fun isEmulator(): Boolean {
        try {
            val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
            // Only force simulation if explicitly enabled in developer settings
            if (prefs.getBoolean("pref_force_emulator_mode", false)) {
                return true
            }
        } catch (e: Exception) {
            // ignore
        }

        // If there is an active SIM subscription on slot 1 or 2, it is a real physical phone
        try {
            if (SimOperatorUtils.findActiveSubscriptionForSlot(context, 1) != null ||
                SimOperatorUtils.findActiveSubscriptionForSlot(context, 2) != null) {
                return false
            }
        } catch (e: Exception) {
            // ignore
        }

        val brand = Build.BRAND?.lowercase() ?: ""
        val device = Build.DEVICE?.lowercase() ?: ""
        val fingerprint = Build.FINGERPRINT?.lowercase() ?: ""
        val hardware = Build.HARDWARE?.lowercase() ?: ""
        val model = Build.MODEL?.lowercase() ?: ""
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val product = Build.PRODUCT?.lowercase() ?: ""

        return (brand.startsWith("generic") && device.startsWith("generic"))
                || fingerprint.startsWith("generic")
                || fingerprint.contains("robolectric")
                || model.contains("robolectric")
                || brand.contains("robolectric")
                || hardware.contains("robolectric")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk built for x86")
                || manufacturer.contains("genymotion")
                || product.contains("sdk_google")
                || product.contains("google_sdk")
                || product.contains("sdk_gphone")
                || product.contains("sdk_x86")
                || product.contains("vbox86p")
                || product.contains("emulator")
                || product.contains("simulator")
    }

    fun executePurchaseUssd(ussdCode: String, simSlot: Int = 1) {
        val cleanCode = ussdCode.trim()
        val isValidSyntax = cleanCode.isNotEmpty() &&
                (cleanCode.startsWith("*") || cleanCode.startsWith("#")) &&
                cleanCode.endsWith("#") &&
                cleanCode.all { it.isDigit() || it == '*' || it == '#' }
        if (!isValidSyntax) return

        val isRobolectricTest = android.os.Build.FINGERPRINT?.lowercase()?.contains("robolectric") == true
        if (isEmulator() && !isRobolectricTest) {
            val simResponse = simulateUssdResponse(cleanCode)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                android.widget.Toast.makeText(context, "Simulación Compra (SIM $simSlot):\n$simResponse", android.widget.Toast.LENGTH_LONG).show()
            }
            appScope.launch(Dispatchers.IO) {
                try {
                    val db = com.ams.megascu.data.db.MegasDatabase.getDatabase(context)
                    val subId = SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
                    val current = db.planDao().getPlanStatusDirect(simSlot) ?: com.ams.megascu.data.db.PlanStatusEntity(id = simSlot, subscriptionId = subId)
                    val updated = applySimulatedPurchaseToEntity(current, cleanCode)
                    db.planDao().insertOrUpdatePlanStatus(updated)
                } catch (e: Exception) {
                    android.util.Log.e("MegasCU", "Unhandled exception", e)
                }
            }
            return
        }

        dialUssdCode(cleanCode, simSlot, forceDialer = true)
    }

    private fun applySimulatedPurchaseToEntity(current: com.ams.megascu.data.db.PlanStatusEntity, code: String): com.ams.megascu.data.db.PlanStatusEntity {
        val now = System.currentTimeMillis()
        val exp35Days = now + 35L * 24 * 3600 * 1000L
        return when (code) {
            "*133*1*4*1#" -> current.copy(
                dataMb = current.dataMb + 4608L,
                balanceCup = if (current.balanceCup >= 240.0) current.balanceCup - 240.0 else current.balanceCup,
                dataDays = 35,
                dataExpirationTimestamp = exp35Days,
                lastUpdatedTimestamp = now
            )
            "*133*1*4*2#" -> current.copy(
                dataMb = current.dataMb + 2048L,
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 15} Min",
                smsCount = current.smsCount + 20,
                balanceCup = if (current.balanceCup >= 120.0) current.balanceCup - 120.0 else current.balanceCup,
                dataDays = 35,
                minutesDays = 35,
                smsDays = 35,
                dataExpirationTimestamp = exp35Days,
                lastUpdatedTimestamp = now
            )
            "*133*1*4*3#" -> current.copy(
                dataMb = current.dataMb + 4096L,
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 35} Min",
                smsCount = current.smsCount + 40,
                balanceCup = if (current.balanceCup >= 240.0) current.balanceCup - 240.0 else current.balanceCup,
                dataDays = 35,
                minutesDays = 35,
                smsDays = 35,
                dataExpirationTimestamp = exp35Days,
                lastUpdatedTimestamp = now
            )
            "*133*1*4*4#" -> current.copy(
                dataMb = current.dataMb + 6144L,
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 60} Min",
                smsCount = current.smsCount + 70,
                balanceCup = if (current.balanceCup >= 360.0) current.balanceCup - 360.0 else current.balanceCup,
                dataDays = 35,
                minutesDays = 35,
                smsDays = 35,
                dataExpirationTimestamp = exp35Days,
                lastUpdatedTimestamp = now
            )
            "*133*1*2#" -> current.copy(
                bonusDataMb = current.bonusDataMb + 600L,
                balanceCup = if (current.balanceCup >= 25.0) current.balanceCup - 25.0 else current.balanceCup,
                lastUpdatedTimestamp = now
            )
            "*133*1*3#" -> current.copy(
                dataLteMb = current.dataLteMb + 200L,
                balanceCup = if (current.balanceCup >= 25.0) current.balanceCup - 25.0 else current.balanceCup,
                lastUpdatedTimestamp = now
            )
            "*133*3*1#" -> current.copy(
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 5} Min",
                balanceCup = if (current.balanceCup >= 37.5) current.balanceCup - 37.5 else current.balanceCup,
                minutesDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*3*2#" -> current.copy(
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 10} Min",
                balanceCup = if (current.balanceCup >= 72.5) current.balanceCup - 72.5 else current.balanceCup,
                minutesDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*3*3#" -> current.copy(
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 15} Min",
                balanceCup = if (current.balanceCup >= 105.0) current.balanceCup - 105.0 else current.balanceCup,
                minutesDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*3*4#" -> current.copy(
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 25} Min",
                balanceCup = if (current.balanceCup >= 162.5) current.balanceCup - 162.5 else current.balanceCup,
                minutesDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*3*5#" -> current.copy(
                minutesStr = "${(current.minutesStr.filter { it.isDigit() }.toIntOrNull() ?: 0) + 40} Min",
                balanceCup = if (current.balanceCup >= 250.0) current.balanceCup - 250.0 else current.balanceCup,
                minutesDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*2*1#" -> current.copy(
                smsCount = current.smsCount + 20,
                balanceCup = if (current.balanceCup >= 15.0) current.balanceCup - 15.0 else current.balanceCup,
                smsDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*2*2#" -> current.copy(
                smsCount = current.smsCount + 50,
                balanceCup = if (current.balanceCup >= 30.0) current.balanceCup - 30.0 else current.balanceCup,
                smsDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*2*3#" -> current.copy(
                smsCount = current.smsCount + 90,
                balanceCup = if (current.balanceCup >= 50.0) current.balanceCup - 50.0 else current.balanceCup,
                smsDays = 35,
                lastUpdatedTimestamp = now
            )
            "*133*2*4#" -> current.copy(
                smsCount = current.smsCount + 120,
                balanceCup = if (current.balanceCup >= 60.0) current.balanceCup - 60.0 else current.balanceCup,
                smsDays = 35,
                lastUpdatedTimestamp = now
            )
            else -> current.copy(lastUpdatedTimestamp = now)
        }
    }

    /**
     * Non-blocking, coroutine-safe USSD execution that rejects concurrent calls to prevent modem saturation.
     */
    @SuppressLint("MissingPermission")
    suspend fun executeUssdSuspend(
        ussdCode: String,
        simSlot: Int = 1,
        allowDialFallback: Boolean = false
    ): UssdResult {
        if (!radioMutex.tryLock()) {
            return UssdResult.Error(
                UssdErrorType.BUSY,
                "[ERR_CODE_105] Ya hay una consulta USSD en curso en el módem celular. Por favor, espera a que finalice."
            )
        }
        try {
            val cleanCode = ussdCode.trim()

            // 1. Validate Code Syntax
            val isValidSyntax = cleanCode.isNotEmpty() &&
                    (cleanCode.startsWith("*") || cleanCode.startsWith("#")) &&
                    cleanCode.endsWith("#") &&
                    cleanCode.all { it.isDigit() || it == '*' || it == '#' }

            if (!isValidSyntax) {
                return UssdResult.Error(
                    UssdErrorType.INVALID_CODE,
                    "[ERR_CODE_102] Código USSD no válido ($ussdCode). Debe comenzar por * o # y terminar en # (ej. *222#)."
                )
            }

            // 2. Seamless simulation on Emulators
            if (isEmulator()) {
                val simResponse = simulateUssdResponse(cleanCode)
                return UssdResult.Success(simResponse)
            }

        // 2b. Validate Call Phone permission
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (allowDialFallback) {
                dialUssdCode(cleanCode, simSlot)
            }
            return UssdResult.Error(
                UssdErrorType.NETWORK_ERROR,
                "[ERR_CODE_101] Permiso de Teléfono no concedido para ejecutar la consulta USSD."
            )
        }

        // 3. Resolve TelephonyManager with retry
        var telephonyManager: TelephonyManager? = null
        for (attempt in 1..3) {
            telephonyManager = SimOperatorUtils.getTelephonyManagerForSlot(context, simSlot)
            if (telephonyManager != null) break
            if (attempt < 3) {
                kotlinx.coroutines.delay(500L)
            }
        }

        if (telephonyManager == null) {
            return UssdResult.Error(
                UssdErrorType.NETWORK_ERROR,
                "[ERR_CODE_104] No se encontró una tarjeta SIM activa en la Ranura $simSlot."
            )
        }

        // 4. Validate Operator
        var simDetails = SimOperatorUtils.checkSimOperator(context, simSlot, isEmulator = false)
        if (simDetails.isPendingInfo) {
            kotlinx.coroutines.delay(500L)
            simDetails = SimOperatorUtils.checkSimOperator(context, simSlot, isEmulator = false)
        }

        if (simDetails.isAbsent || telephonyManager.simState == TelephonyManager.SIM_STATE_ABSENT) {
            if (allowDialFallback) {
                dialUssdCode(cleanCode, simSlot)
                return UssdResult.Error(
                    UssdErrorType.NETWORK_ERROR,
                    "[ERR_CODE_104] No hay tarjeta SIM insertada en la Ranura $simSlot. Se abrió el marcador con $cleanCode."
                )
            } else {
                return UssdResult.Error(
                    UssdErrorType.NETWORK_ERROR,
                    "[ERR_CODE_104] No hay tarjeta SIM insertada en la Ranura $simSlot."
                )
            }
        }

        if (!simDetails.isCubacel) {
            val subInfo = SimOperatorUtils.findActiveSubscriptionForSlot(context, simSlot)
            if (isConfirmedOtherOperator(subInfo, telephonyManager, simDetails)) {
                return UssdResult.Error(
                    UssdErrorType.CARRIER_REJECTED,
                    "Operación cancelada: La SIM seleccionada (Slot $simSlot) pertenece a la operadora '${simDetails.operatorName}'. MegasCU es una aplicación exclusiva para la red Cubacel (ETECSA)."
                )
            }
        }

        // 5. Interactive purchase code (*133#)
        if (cleanCode.startsWith("*133")) {
            dialUssdCode(cleanCode, simSlot)
            return UssdResult.Success(
                "Se ha iniciado la solicitud USSD para $cleanCode. Revisa la ventana de confirmación del sistema para completar tu compra."
            )
        }

        // 6. Execute USSD on Android Telephony
        return suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val handler = Handler(Looper.getMainLooper())
                val isHandled = AtomicBoolean(false)

                val timeoutRunnable = Runnable {
                    if (isHandled.compareAndSet(false, true)) {
                        if (allowDialFallback) {
                            dialUssdCode(cleanCode, simSlot)
                            if (continuation.isActive) {
                                continuation.resume(
                                    UssdResult.Error(
                                        UssdErrorType.TIMEOUT,
                                        "[ERR_CODE_101] Tiempo de espera agotado (10s) al consultar $cleanCode. Abriendo marcador..."
                                    )
                                )
                            }
                        } else {
                            if (continuation.isActive) {
                                continuation.resume(
                                    UssdResult.Error(
                                        UssdErrorType.TIMEOUT,
                                        "[ERR_CODE_101] Tiempo de espera de la red Cubacel agotado al consultar $cleanCode (10s)."
                                    )
                                )
                            }
                        }
                    }
                }

                handler.postDelayed(timeoutRunnable, timeoutMs)

                continuation.invokeOnCancellation {
                    if (isHandled.compareAndSet(false, true)) {
                        handler.removeCallbacks(timeoutRunnable)
                    }
                }

                try {
                    telephonyManager.sendUssdRequest(
                        cleanCode,
                        object : TelephonyManager.UssdResponseCallback() {
                            override fun onReceiveUssdResponse(
                                telephonyManager: TelephonyManager?,
                                request: String?,
                                response: CharSequence?
                            ) {
                                if (isHandled.compareAndSet(false, true)) {
                                    handler.removeCallbacks(timeoutRunnable)
                                    val respStr = response?.toString()?.trim()
                                    if (continuation.isActive) {
                                        if (respStr.isNullOrEmpty()) {
                                            continuation.resume(
                                                UssdResult.Error(
                                                    UssdErrorType.INVALID_RESPONSE,
                                                    "[ERR_CODE_105] Respuesta de la red Cubacel vacía para $request."
                                                )
                                            )
                                        } else {
                                            continuation.resume(UssdResult.Success(respStr))
                                        }
                                    }
                                }
                            }

                            override fun onReceiveUssdResponseFailed(
                                telephonyManager: TelephonyManager?,
                                request: String?,
                                failureCode: Int
                            ) {
                                if (isHandled.compareAndSet(false, true)) {
                                    handler.removeCallbacks(timeoutRunnable)
                                    val (errorType, errorReason) = when (failureCode) {
                                        TelephonyManager.USSD_RETURN_FAILURE ->
                                            UssdErrorType.CARRIER_REJECTED to "[ERR_CODE_106] Error de servidor en la red Cubacel al ejecutar $request."
                                        else ->
                                            UssdErrorType.NETWORK_ERROR to "[ERR_CODE_106] Respuesta de red fallida en Cubacel (Código de falla $failureCode)."
                                    }
                                    if (continuation.isActive) {
                                        if (allowDialFallback) {
                                            dialUssdCode(cleanCode, simSlot)
                                            continuation.resume(
                                                UssdResult.Error(errorType, "$errorReason\nSe abrió el marcador telefónico.")
                                            )
                                        } else {
                                            continuation.resume(UssdResult.Error(errorType, errorReason))
                                        }
                                    }
                                }
                            }
                        },
                        handler
                    )
                } catch (e: Exception) {
                    if (isHandled.compareAndSet(false, true)) {
                        handler.removeCallbacks(timeoutRunnable)
                        val errorMsg = "[ERR_CODE_100] Error al transmitir solicitud USSD: ${e.localizedMessage ?: e.message}"
                        if (continuation.isActive) {
                            if (allowDialFallback) {
                                dialUssdCode(cleanCode, simSlot)
                                continuation.resume(
                                    UssdResult.Error(UssdErrorType.NETWORK_ERROR, "$errorMsg. Abriendo marcador...")
                                )
                            } else {
                                continuation.resume(UssdResult.Error(UssdErrorType.NETWORK_ERROR, errorMsg))
                            }
                        }
                    }
                }
            } else {
                if (allowDialFallback) {
                    dialUssdCode(cleanCode, simSlot)
                    if (continuation.isActive) {
                        continuation.resume(
                            UssdResult.Success("Abriendo código $cleanCode en la aplicación de teléfono...")
                        )
                    }
                } else {
                    if (continuation.isActive) {
                        continuation.resume(
                            UssdResult.Error(
                                UssdErrorType.NETWORK_ERROR,
                                "[ERR_CODE_107] La ejecución automática USSD requiere Android 8.0 o superior."
                            )
                        )
                    }
                }
            }
        }
    } finally {
        radioMutex.unlock()
    }
}

    fun isBusy(): Boolean = radioMutex.isLocked

    /**
     * Callback-based adapter executing within a background CoroutineScope
     */
    fun executeUssd(
        ussdCode: String,
        simSlot: Int = 1,
        allowDialFallback: Boolean = false,
        callback: UssdCallback
    ) {
        appScope.launch(Dispatchers.Main) {
            when (val result = executeUssdSuspend(ussdCode, simSlot, allowDialFallback)) {
                is UssdResult.Success -> callback.onSuccess(result.response)
                is UssdResult.Error -> {
                    if (result.errorType == UssdErrorType.TIMEOUT) {
                        callback.onTimeout(ussdCode.trim())
                    } else {
                        callback.onError(result.errorType, result.message)
                    }
                }
            }
        }
    }

    private fun isConfirmedOtherOperator(
        subInfo: SubscriptionInfo?,
        telephonyManager: TelephonyManager,
        simDetails: SimOperatorDetails
    ): Boolean {
        val simOpCode = telephonyManager.simOperator.trim()
        if (simOpCode.isNotBlank() && !simOpCode.startsWith("368")) {
            return true
        }
        val carrierName = subInfo?.carrierName?.toString()?.trim() ?: ""
        val simOpName = (telephonyManager.simOperatorName ?: telephonyManager.networkOperatorName ?: "").trim()
        val name = (if (carrierName.isNotBlank()) carrierName else simOpName).lowercase()

        if (name.contains("cubacel") || name.contains("etecsa") || name.contains("cuba")) {
            return false
        }

        val isGenericPlaceholder = name.isEmpty() ||
                name.startsWith("sim") ||
                name.startsWith("slot") ||
                name.startsWith("card") ||
                name.contains("desconocida") ||
                name.contains("no disponible") ||
                name.contains("unknown")

        return !isGenericPlaceholder
    }

    private fun dialUssdCode(code: String, simSlot: Int = 1, forceDialer: Boolean = false) {
        val subInfo = SimOperatorUtils.findActiveSubscriptionForSlot(context, simSlot)
        val subscriptionId = subInfo?.subscriptionId
        val slotIndex = if (simSlot in 1..2) simSlot - 1 else null
        val phoneAccountHandle = SimOperatorUtils.getPhoneAccountHandleForSlot(context, simSlot)

        val encodedCode = Uri.encode(code)
        val telUri = Uri.parse("tel:$encodedCode")

        val populateExtras: (Intent) -> Unit = { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (phoneAccountHandle != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
            }
            if (subscriptionId != null && subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                intent.putExtra("subscription", subscriptionId)
                intent.putExtra("subId", subscriptionId)
                intent.putExtra("com.android.phone.extra.subscription", subscriptionId)
                intent.putExtra("android.telephony.extra.SUBSCRIPTION_INDEX", subscriptionId)
            }
            if (slotIndex != null) {
                intent.putExtra("simSlot", slotIndex)
                intent.putExtra("slot", slotIndex)
                intent.putExtra("phone_id", slotIndex)
                intent.putExtra("simId", slotIndex.toLong())
                intent.putExtra("com.android.phone.extra.slot", slotIndex)
                intent.putExtra("android.telephony.extra.SLOT_INDEX", slotIndex)
                intent.putExtra("extra_asus_dial_use_sub", slotIndex)
                intent.putExtra("com.android.phone.force.slot", slotIndex)
            }
        }

        // Intento 1: Llamada directa ACTION_CALL si se dispone del permiso CALL_PHONE
        val hasCallPermission = PermissionUtils.hasCallPhonePermission(context)
        if (!forceDialer && hasCallPermission) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, telUri).apply {
                    populateExtras(this)
                }
                context.startActivity(callIntent)
                return
            } catch (e: Exception) {
                // Fallback al marcador
            }
        }

        // Intento 2: Marcador telefónico ACTION_DIAL (no requiere permiso CALL_PHONE)
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL, telUri).apply {
                populateExtras(this)
            }
            context.startActivity(dialIntent)
            return
        } catch (e: Exception) {
            // Fallback a vista genérica
        }

        // Intento 3: ACTION_VIEW como fallback defensivo final
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW, telUri).apply {
                populateExtras(this)
            }
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }
    }

    /**
     * Helper for simulation/demo responses in emulator or offline test mode
     */
    fun simulateUssdResponse(ussdCode: String): String {
        return when (ussdCode.trim()) {
            "*222#" -> "Saldo: 150.50 CUP, vence el 25/08/2026. Saldo principal activo."
            "*222*328#" -> "Usted tiene 2048 MB de Datos principales y 4096 MB en Red LTE. Su plan de Datos vence el 25-08-26."
            "*222*266#" -> "Bono activo: 1000 MB Nacional + 500 MB Promocional LTE. Vencimiento 25-08-26."
            "*222*767#" -> "Dispone de 150 SMS vigentes hasta el 25-08-26."
            "*222*869#" -> "Dispone de 45 Minutos para llamadas nacionales hasta el 25-08-26."
            "*222*732#" -> "Ud puede recargar un monto de 360,00CUP en un plazo de 30 dias."
            "*222*736#" -> "Saldo disponible para renovación automática: 0.00 CUP. Servicio activo."
            "*222*468#" -> "Acceso a Internet: Su línea se encuentra habilitada para datos móviles y red 4G/LTE."
            "*222*264#" -> "Plan Amigos: Servicio activo con tarifas reducidas en sus 3 contactos elegidos."
            "*133#" -> "Menú ETECSA:\n1. Datos\n2. Voz\n3. SMS\n4. Plan Amigos\n5. Adelanta Saldo\nMarque el número de opción."
            "*133*1#" -> "Menú Datos ETECSA:\n1. Tarifa x Consumo\n2. Bolsa toDus\n3. Bolsa Diaria LTE\n4. Planes Combinados"
            "*133*1*1#" -> "Tarifa por Consumo:\nEstado actual: Desactivada.\n1. Activar\n2. Desactivar"
            "*133*1*2#" -> "Operación exitosa. Ha adquirido la Bolsa toDus (600 MB) por 25 CUP válida por 30 días."
            "*133*1*3#" -> "Operación exitosa. Ha adquirido la Bolsa Diaria LTE (200 MB) por 25 CUP válida 24h."
            "*133*1*4#" -> "Planes Combinados:\n1. 4.5 GB (240 CUP)\n2. 2 GB + 15 min + 20 SMS (120 CUP)\n3. 4 GB + 35 min + 40 SMS (240 CUP)\n4. 6 GB + 60 min + 70 SMS (360 CUP)"
            "*133*1*4*1#" -> "Operación exitosa. Ha adquirido el Plan 240 CUP (4.5 GB) válido por 35 días."
            "*133*1*4*2#" -> "Operación exitosa. Ha adquirido el Plan Combinado 120 CUP (2 GB + 15 min + 20 SMS) válido por 35 días."
            "*133*1*4*3#" -> "Operación exitosa. Ha adquirido el Plan Combinado 240 CUP (4 GB + 35 min + 40 SMS) válido por 35 días."
            "*133*1*4*4#" -> "Operación exitosa. Ha adquirido el Plan Combinado 360 CUP (6 GB + 60 min + 70 SMS) válido por 35 días."
            "*133*2#" -> "Planes de SMS:\n1. 20 SMS (15 CUP)\n2. 50 SMS (30 CUP)\n3. 90 SMS (50 CUP)\n4. 120 SMS (60 CUP)"
            "*133*2*1#" -> "Operación exitosa. Ha adquirido el Plan de 20 SMS por 15 CUP válido por 35 días."
            "*133*2*2#" -> "Operación exitosa. Ha adquirido el Plan de 50 SMS por 30 CUP válido por 35 días."
            "*133*2*3#" -> "Operación exitosa. Ha adquirido el Plan de 90 SMS por 50 CUP válido por 35 días."
            "*133*2*4#" -> "Operación exitosa. Ha adquirido el Plan de 120 SMS por 60 CUP válido por 35 días."
            "*133*3#" -> "Planes de Voz:\n1. 5 Min (37.50 CUP)\n2. 10 Min (72.50 CUP)\n3. 15 Min (105 CUP)\n4. 25 Min (162.50 CUP)\n5. 40 Min (250 CUP)"
            "*133*3*1#" -> "Operación exitosa. Ha adquirido el Plan de 5 Minutos (37.50 CUP) válido por 35 días."
            "*133*3*2#" -> "Operación exitosa. Ha adquirido el Plan de 10 Minutos (72.50 CUP) válido por 35 días."
            "*133*3*3#" -> "Operación exitosa. Ha adquirido el Plan de 15 Minutos (105 CUP) válido por 35 días."
            "*133*3*4#" -> "Operación exitosa. Ha adquirido el Plan de 25 Minutos (162.50 CUP) válido por 35 días."
            "*133*3*5#" -> "Operación exitosa. Ha adquirido el Plan de 40 Minutos (250 CUP) válido por 35 días."
            "*133*4#" -> "Plan Amigos:\n1. Activar servicio (gratis)\n2. Registrar amigos\n3. Eliminar amigos\n4. Estado"
            "*234#" -> "Servicios Cubacel:\n1. Transferir Saldo\n2. Cambiar Clave\n3. Adelanta Saldo"
            "*234*1#" -> "Transferencia de Saldo:\nMarque: *234*1*Número*Clave*Monto# para transferir saldo."
            "*234*2#" -> "Cambio de Clave:\nMarque: *234*2*ClaveActual*ClaveNueva#."
            "*234*3#" -> "Adelanta Saldo:\nUsted dispone de 25.00 CUP de adelanto. Se descontará en su próxima recarga."
            "*662#" -> "Recarga con Cupón:\nMarque: *662*CódigoRecarga# (16 dígitos de la tarjeta de recarga)."
            else -> "Respuesta de red Cubacel para $ussdCode: Operación simulada ejecutada con éxito."
        }
    }
}
