package com.ams.megascu.data.ussd

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

data class SimOperatorDetails(
    val simSlot: Int,
    val isCubacel: Boolean,
    val operatorName: String,
    val isAbsent: Boolean = false,
    val isPendingInfo: Boolean = false
)

object SimOperatorUtils {

    /**
     * Resolución centralizada y estricta de SubscriptionInfo por slot físico (1 o 2).
     * Mapeo:
     * - simSlot 1 -> SubscriptionInfo.simSlotIndex == 0
     * - simSlot 2 -> SubscriptionInfo.simSlotIndex == 1
     * Nunca utiliza la posición de la lista ni selecciona otra suscripción como fallback.
     */
    @SuppressLint("MissingPermission")
    fun findActiveSubscriptionForSlot(context: Context, simSlot: Int): SubscriptionInfo? {
        if (simSlot !in 1..2) return null
        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                ?: return null
            val targetSlotIndex = simSlot - 1

            // 1. Intentar método oficial directo por slot index
            val directSub = try {
                subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(targetSlotIndex)
            } catch (e: Exception) {
                null
            }
            if (directSub != null) return directSub

            // 2. Búsqueda en la lista de suscripciones activas
            val activeSubscriptions = try {
                subscriptionManager.activeSubscriptionInfoList
            } catch (e: Exception) {
                null
            }
            val matchingSub = activeSubscriptions?.find { it.simSlotIndex == targetSlotIndex }
            if (matchingSub != null) return matchingSub

            // 3. Fallback: Si se consulta slot 1 y solo hay una suscripción activa con slotIndex == 0
            if (simSlot == 1 && activeSubscriptions != null && activeSubscriptions.size == 1 && activeSubscriptions[0].simSlotIndex == 0) {
                activeSubscriptions.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene el subscriptionId numérico de Android asociado a la ranura indicada (1 o 2).
     */
    @SuppressLint("MissingPermission")
    fun getSubscriptionIdForSlot(context: Context, simSlot: Int): Int? {
        return try {
            val subInfo = findActiveSubscriptionForSlot(context, simSlot)
            if (subInfo != null && subInfo.subscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                subInfo.subscriptionId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resuelve de forma segura el PhoneAccountHandle de TelecomManager para la SIM indicada.
     * Utilizado para enrutar llamadas y marcaciones hacia el slot correcto en Android 6.0+.
     */
    @SuppressLint("MissingPermission")
    fun getPhoneAccountHandleForSlot(context: Context, simSlot: Int): PhoneAccountHandle? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
                ?: return null
            val subInfo = findActiveSubscriptionForSlot(context, simSlot)
            val targetSlotIndex = simSlot - 1
            val targetSubId = subInfo?.subscriptionId

            val callCapableAccounts = try {
                telecomManager.callCapablePhoneAccounts
            } catch (e: Exception) {
                null
            }

            if (callCapableAccounts.isNullOrEmpty()) return null

            // 1. Coincidencia por ID de suscripción en el identificador del handle
            if (targetSubId != null && targetSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                val matchById = callCapableAccounts.find { handle ->
                    handle.id.contains(targetSubId.toString())
                }
                if (matchById != null) return matchById
            }

            // 2. Coincidencia por slot index en el identificador del handle
            val matchBySlot = callCapableAccounts.find { handle ->
                handle.id.contains("slot$targetSlotIndex", ignoreCase = true) ||
                        handle.id.contains("sim$targetSlotIndex", ignoreCase = true) ||
                        handle.id.endsWith(targetSlotIndex.toString())
            }
            if (matchBySlot != null) return matchBySlot

            // 3. Fallback posicional si está dentro del rango
            if (targetSlotIndex in callCapableAccounts.indices) {
                callCapableAccounts[targetSlotIndex]
            } else {
                callCapableAccounts.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene una instancia de TelephonyManager vinculada a la SIM solicitada.
     * En slot 1 se incluye fallback al TelephonyManager por defecto si aún no hay SubscriptionInfo.
     */
    @SuppressLint("MissingPermission")
    fun getTelephonyManagerForSlot(context: Context, simSlot: Int): TelephonyManager? {
        val baseTelephonyManager = try {
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        } catch (e: Exception) {
            null
        } ?: return null

        val subInfo = findActiveSubscriptionForSlot(context, simSlot)
        if (subInfo != null) {
            try {
                return baseTelephonyManager.createForSubscriptionId(subInfo.subscriptionId)
            } catch (e: Exception) {
                // Proceder a fallback
            }
        }
        if (simSlot == 1) {
            return baseTelephonyManager
        }
        return null
    }

    @SuppressLint("MissingPermission")
    fun checkSimOperator(context: Context, simSlot: Int = 1, isEmulator: Boolean = false): SimOperatorDetails {
        if (isEmulator) {
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = true,
                operatorName = "Cubacel (Simulador)",
                isAbsent = false,
                isPendingInfo = false
            )
        }

        if (simSlot !in 1..2) {
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = false,
                operatorName = "SIM no disponible (SIM $simSlot)",
                isAbsent = true,
                isPendingInfo = false
            )
        }

        val subscriptionManager = try {
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
        } catch (_: Exception) {
            null
        }
        val activeSubscriptions = try {
            subscriptionManager?.activeSubscriptionInfoList
        } catch (_: Exception) {
            null
        }

        val subInfo = findActiveSubscriptionForSlot(context, simSlot)
        val carrierNameStr = try { subInfo?.carrierName?.toString()?.trim() ?: "" } catch (_: Exception) { "" }
        val displayNameStr = try { subInfo?.displayName?.toString()?.trim() ?: "" } catch (_: Exception) { "" }

        val specificTelephony = getTelephonyManagerForSlot(context, simSlot)
        val defaultTelephony = try { context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager } catch (_: Exception) { null }

        var simOpName = try { (specificTelephony?.simOperatorName ?: specificTelephony?.networkOperatorName ?: "").trim() } catch (_: Exception) { "" }
        var simOpCode = try { (specificTelephony?.simOperator ?: specificTelephony?.networkOperator ?: "").trim() } catch (_: Exception) { "" }

        if (simSlot == 1 && simOpCode.isBlank() && simOpName.isBlank() && defaultTelephony != null) {
            simOpCode = try { (defaultTelephony.simOperator ?: defaultTelephony.networkOperator ?: "").trim() } catch (_: Exception) { "" }
            simOpName = try { (defaultTelephony.simOperatorName ?: defaultTelephony.networkOperatorName ?: "").trim() } catch (_: Exception) { "" }
        }

        val currentSimState = try {
            specificTelephony?.simState ?: defaultTelephony?.simState ?: TelephonyManager.SIM_STATE_UNKNOWN
        } catch (_: Exception) {
            TelephonyManager.SIM_STATE_UNKNOWN
        }

        // 1. Detección de SIM Ausente
        val isExplicitlyAbsent = (currentSimState == TelephonyManager.SIM_STATE_ABSENT) ||
                (simSlot == 2 && activeSubscriptions != null && subInfo == null) ||
                (activeSubscriptions != null && activeSubscriptions.isEmpty() && currentSimState == TelephonyManager.SIM_STATE_ABSENT)

        if (isExplicitlyAbsent) {
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = false,
                operatorName = "SIM ausente (SIM $simSlot)",
                isAbsent = true,
                isPendingInfo = false
            )
        }

        // 2. Detección de Operadora Explícita (Cubacel o Extranjera)
        var isExplicitCubacel = false
        var isExplicitForeign = false

        if (simOpCode.startsWith("368")) {
            isExplicitCubacel = true
        } else if (simOpCode.isNotBlank()) {
            isExplicitForeign = true
        }

        val allCandidateStrings = listOf(carrierNameStr, displayNameStr, simOpName)
        for (cand in allCandidateStrings) {
            val lower = cand.lowercase()
            if (lower.contains("cubacel") || lower.contains("etecsa") || lower.contains("cuba")) {
                isExplicitCubacel = true
                isExplicitForeign = false
                break
            }
        }

        if (!isExplicitCubacel && !isExplicitForeign) {
            val rawName = carrierNameStr.ifBlank { simOpName }.ifBlank { displayNameStr }
            if (rawName.isNotBlank() && isExplicitForeignName(rawName)) {
                isExplicitForeign = true
            }
        }

        if (isExplicitCubacel) {
            val operatorName = when {
                carrierNameStr.lowercase().let { it.contains("cubacel") || it.contains("etecsa") } -> carrierNameStr
                simOpName.lowercase().let { it.contains("cubacel") || it.contains("etecsa") } -> simOpName
                else -> "Cubacel (ETECSA)"
            }
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = true,
                operatorName = operatorName,
                isAbsent = false,
                isPendingInfo = false
            )
        }

        if (isExplicitForeign) {
            val rawName = carrierNameStr.ifBlank { simOpName }.ifBlank { displayNameStr }
            val operatorName = if (rawName.isNotBlank()) rawName else "Operadora Extranjera ($simOpCode)"
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = false,
                operatorName = operatorName,
                isAbsent = false,
                isPendingInfo = false
            )
        }

        // 3. Detección de Estado Transitorio (Información todavía no disponible)
        val hasNoTelephonyInfo = simOpCode.isBlank() && simOpName.isBlank() && carrierNameStr.isBlank() && displayNameStr.isBlank()
        val isTransientState = (subInfo == null && hasNoTelephonyInfo) ||
                (currentSimState == TelephonyManager.SIM_STATE_UNKNOWN || currentSimState == TelephonyManager.SIM_STATE_NOT_READY)

        if (isTransientState) {
            return SimOperatorDetails(
                simSlot = simSlot,
                isCubacel = false,
                operatorName = "Obteniendo información de operadora...",
                isAbsent = false,
                isPendingInfo = true
            )
        }

        // 4. SIM presente pero sin identificación concluyente (No se presume Cubacel automáticamente)
        val rawName = carrierNameStr.ifBlank { simOpName }.ifBlank { displayNameStr }
        val operatorName = if (rawName.isNotBlank() && !isGenericPlaceholder(rawName)) rawName else "Operadora Desconocida (SIM $simSlot)"
        return SimOperatorDetails(
            simSlot = simSlot,
            isCubacel = false,
            operatorName = operatorName,
            isAbsent = false,
            isPendingInfo = false
        )
    }

    private fun isGenericPlaceholder(name: String): Boolean {
        val lower = name.trim().lowercase()
        return lower.isEmpty() ||
                lower.startsWith("sim") ||
                lower.startsWith("slot") ||
                lower.startsWith("card") ||
                lower.contains("desconocida") ||
                lower.contains("no disponible") ||
                lower.contains("unknown") ||
                lower.startsWith("obteniendo")
    }

    private fun isExplicitForeignName(name: String): Boolean {
        if (isGenericPlaceholder(name)) return false
        val lower = name.trim().lowercase()
        if (lower.contains("cubacel") || lower.contains("etecsa") || lower.contains("cuba")) {
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    fun isSimSlotAvailable(context: Context, simSlot: Int, isEmulator: Boolean = false): Boolean {
        if (isEmulator) return true
        if (simSlot !in 1..2) return false

        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val targetSlotIndex = simSlot - 1
            val directSub = try {
                subscriptionManager?.getActiveSubscriptionInfoForSimSlotIndex(targetSlotIndex)
            } catch (e: Exception) {
                null
            }
            if (directSub != null) return true

            val activeList = try {
                subscriptionManager?.activeSubscriptionInfoList
            } catch (e: Exception) {
                null
            }
            if (activeList != null && activeList.isNotEmpty()) {
                if (activeList.any { it.simSlotIndex == targetSlotIndex }) return true
                if (simSlot == 1 && activeList.size == 1 && activeList[0].simSlotIndex == 0) return true
            }

            // Fallback conservador solo si la SIM 1 está confirmada como READY en TelephonyManager
            if (simSlot == 1) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                if (telephonyManager != null) {
                    val simState = try { telephonyManager.simState } catch (_: Exception) { TelephonyManager.SIM_STATE_UNKNOWN }
                    return simState == TelephonyManager.SIM_STATE_READY
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }

        return false
    }

    @SuppressLint("MissingPermission")
    fun getActiveSimCount(context: Context, isEmulator: Boolean = false): Int {
        if (isEmulator) return 2
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val activeList = subscriptionManager?.activeSubscriptionInfoList
            if (activeList != null) {
                return activeList.size
            }
        } catch (e: Exception) {
            android.util.Log.e("MegasCU", "Unhandled exception", e)
        }
        return 1
    }
}

