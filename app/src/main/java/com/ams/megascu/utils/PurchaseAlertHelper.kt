package com.ams.megascu.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import com.ams.megascu.data.db.PlanStatusEntity

data class PurchaseAlertState(
    val hasAlert: Boolean = false,
    val isDataAlert: Boolean = false,
    val isRechargeAlert: Boolean = false,
    val isUnifiedAlert: Boolean = false,
    val dataDaysRemaining: Int = 0,
    val rechargeDaysRemaining: Int = 0,
    val title: String = "",
    val description: String = ""
)

object PurchaseAlertHelper {
    const val KEY_DEV_SIMULATE_ALERT_ENABLED = "pref_dev_simulate_alerts_enabled"
    const val KEY_DEV_SIMULATE_TYPE = "pref_dev_sim_preset"
    const val KEY_DEV_SIMULATE_DATA_DAYS = "pref_dev_sim_data_days"
    const val KEY_DEV_SIMULATE_RECHARGE_DAYS = "pref_dev_sim_recharge_days"

    const val PREF_DEV_SIMULATE_ALERTS = KEY_DEV_SIMULATE_ALERT_ENABLED
    const val PREF_DEV_SIM_DATA_DAYS = KEY_DEV_SIMULATE_DATA_DAYS
    const val PREF_DEV_SIM_RECHARGE_DAYS = KEY_DEV_SIMULATE_RECHARGE_DAYS
    const val PREF_DEV_SIM_PRESET = KEY_DEV_SIMULATE_TYPE

    const val TRANSFERMOVIL_PACKAGE = "cu.etecsa.cubacel.tr.tm"

    fun calculateAlertState(
        planStatus: PlanStatusEntity?,
        prefs: SharedPreferences
    ): PurchaseAlertState {
        val isSimulated = prefs.getBoolean(KEY_DEV_SIMULATE_ALERT_ENABLED, false)

        val dataDays: Int
        val rechargeDays: Int

        if (isSimulated) {
            val type = prefs.getString(KEY_DEV_SIMULATE_TYPE, "unified") ?: "unified"
            val simData = prefs.getInt(KEY_DEV_SIMULATE_DATA_DAYS, 2)
            val simRecharge = prefs.getInt(KEY_DEV_SIMULATE_RECHARGE_DAYS, 3)
            when (type) {
                "recharge" -> {
                    dataDays = 15 // No alert for data
                    rechargeDays = simRecharge
                }
                "data" -> {
                    dataDays = simData
                    rechargeDays = 15 // No alert for recharge
                }
                else -> {
                    // Unified
                    dataDays = simData
                    rechargeDays = simRecharge
                }
            }
        } else {
            if (planStatus == null) {
                return PurchaseAlertState()
            }
            // Check active resource days (data, minutes, SMS)
            val activeDays = mutableListOf<Int>()
            val totalDataMb = planStatus.dataMb + planStatus.dataLteMb + planStatus.bonusDataMb
            if (planStatus.dataDays > 0 || totalDataMb > 0) {
                if (planStatus.dataDays > 0) activeDays.add(planStatus.dataDays)
            }
            val hasMinutes = planStatus.minutesStr.isNotBlank() &&
                    planStatus.minutesStr != "0" &&
                    planStatus.minutesStr != "0 min"
            if (planStatus.minutesDays > 0 || hasMinutes) {
                if (planStatus.minutesDays > 0) activeDays.add(planStatus.minutesDays)
            }
            if (planStatus.smsDays > 0 || planStatus.smsCount > 0) {
                if (planStatus.smsDays > 0) activeDays.add(planStatus.smsDays)
            }

            dataDays = activeDays.minOrNull() ?: 0
            rechargeDays = planStatus.nextRechargeDays
        }

        // Conditions: alert when less than or equal to 5 days remain
        val isDataAlert = dataDays in 1..5
        val isRechargeAlert = rechargeDays in 0..5
        val isUnified = isDataAlert && isRechargeAlert
        val hasAlert = isDataAlert || isRechargeAlert

        if (!hasAlert) {
            return PurchaseAlertState()
        }

        val (title, description) = when {
            isUnified -> {
                Pair(
                    "Alerta de Vencimiento y Recarga",
                    "Tus paquetes vencen en $dataDays ${if (dataDays == 1) "día" else "días"} y quedan $rechargeDays ${if (rechargeDays == 1) "día" else "días"} para recargar tu saldo. Renueva y recarga para no perder tus recursos acumulados."
                )
            }
            isDataAlert -> {
                Pair(
                    "Renovación de Paquetes Próxima",
                    "Quedan $dataDays ${if (dataDays == 1) "día" else "días"} para el vencimiento de tus paquetes. Adquiere un nuevo plan para acumular y no perder tus recursos actuales."
                )
            }
            isRechargeAlert -> {
                Pair(
                    "Recarga de Saldo Necesaria",
                    if (rechargeDays <= 0) {
                        "Ya puedes realizar la recarga de tu saldo. Recarga a través de Transfermóvil para mantener tu línea y servicios activos."
                    } else {
                        "Quedan $rechargeDays ${if (rechargeDays == 1) "día" else "días"} para la recarga de tu saldo. Recarga oportunamente para mantener tu línea activa."
                    }
                )
            }
            else -> Pair("", "")
        }

        return PurchaseAlertState(
            hasAlert = true,
            isDataAlert = isDataAlert,
            isRechargeAlert = isRechargeAlert,
            isUnifiedAlert = isUnified,
            dataDaysRemaining = dataDays,
            rechargeDaysRemaining = rechargeDays,
            title = title,
            description = description
        )
    }

    fun launchTransfermovil(context: Context) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(TRANSFERMOVIL_PACKAGE)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Transfermóvil no se encuentra instalada en el dispositivo",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No fue posible abrir Transfermóvil: ${e.localizedMessage ?: "Error desconocido"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
