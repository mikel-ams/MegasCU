package com.ams.megascu.utils

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Verificación segura de permiso READ_PHONE_STATE.
     */
    fun hasPhoneStatePermission(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verificación segura de permiso CALL_PHONE.
     */
    fun hasCallPhonePermission(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verificación segura de permiso POST_NOTIFICATIONS (Android 13+ / API 33+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            true
        }
    }

    /**
     * Verificación segura de permiso PACKAGE_USAGE_STATS mediante AppOpsManager
     * diferenciando Android 10 (Build.VERSION_CODES.Q / API 29) de versiones anteriores.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Abre los Ajustes de Aplicación específicos en Android (ACTION_APPLICATION_DETAILS_SETTINGS).
     */
    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Apertura defensiva multinivel (fallback) de Ajustes de Acceso a Datos de Uso (PACKAGE_USAGE_STATS).
     * Evita crashes en capas de personalización (MIUI / HyperOS / EMUI / ColorOS / Funtouch / OxygenOS).
     *
     * 1. Intenta abrir directamente los detalles del paquete mediante Uri ("package:" + packageName) en Android 11+ (API 30+)
     * 2. Si falla por ActivityNotFoundException o SecurityException (frecuente en MIUI/EMUI), intenta la lista general de ACTION_USAGE_ACCESS_SETTINGS.
     * 3. Como último recurso defensivo, abre los Ajustes generales del sistema (ACTION_SETTINGS).
     */
    fun openUsageAccessSettings(context: Context) {
        val packageName = context.packageName

        // Intento 1: ACTION_USAGE_ACCESS_SETTINGS con Uri de paquete (disponible desde Android 11 / API 30)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intentDirect = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intentDirect)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Intento 2: Lista general de acceso a datos de uso ACTION_USAGE_ACCESS_SETTINGS (sin Uri)
        try {
            val intentGeneral = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intentGeneral)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Intento 3: Ajustes generales del sistema como último fallback absoluto
        try {
            val intentSettings = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intentSettings)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
