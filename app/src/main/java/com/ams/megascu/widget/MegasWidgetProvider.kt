package com.ams.megascu.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ams.megascu.MainActivity
import com.ams.megascu.R
import kotlinx.coroutines.withContext
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.PlanStatusEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class MegasWidgetProvider(
    private val layoutResId: Int = R.layout.widget_megas_4x2
) : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        android.util.Log.d("MegasWidgetProvider", "onReceive triggered with action=$action")
        if (action == ACTION_WIDGET_REFRESH) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    val widgetPrefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
                    val type = widgetPrefs.getString("widget_${appWidgetId}_type", "megas") ?: "megas"
                    val simSlot = widgetPrefs.getInt("widget_${appWidgetId}_sim_slot", 1).coerceIn(1, 2)
                    val ussdCode = when (type) {
                        "saldo" -> "*222#"
                        "megas" -> "*222*328#"
                        "llamadas" -> "*222*869#"
                        "mensajes" -> "*222*767#"
                        "bono" -> "*222*266#"
                        else -> "*222*328#"
                    }
                    val latch = java.util.concurrent.CountDownLatch(1)
                    var responseText = ""
                    val ussdExecutor = com.ams.megascu.data.ussd.UssdExecutor(context)
                    withContext(Dispatchers.Main) {
                        ussdExecutor.executeUssd(ussdCode, simSlot = simSlot, allowDialFallback = false, callback = object : com.ams.megascu.data.ussd.UssdCallback {
                            override fun onSuccess(response: String) {
                                responseText = response
                                latch.countDown()
                            }
                            override fun onError(errorMessage: String) {
                                latch.countDown()
                            }
                        })
                    }
                    try {
                        latch.await(12, java.util.concurrent.TimeUnit.SECONDS)
                    } catch (e: Exception) {}

                    if (responseText.isNotBlank()) {
                        val parsed = com.ams.megascu.data.ussd.EtecsaUssdParser.parseUssdResponse(responseText, ussdCode)
                        val db = MegasDatabase.getDatabase(context)
                        val planDao = db.planDao()
                        val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
                        var currentStatus = planDao.getPlanStatusDirect(simSlot)
                        if (currentStatus != null && currentSubId != null && currentStatus.subscriptionId != null && currentStatus.subscriptionId != currentSubId) {
                            currentStatus = PlanStatusEntity(id = simSlot, subscriptionId = currentSubId)
                        } else if (currentStatus == null) {
                            currentStatus = PlanStatusEntity(id = simSlot, subscriptionId = currentSubId)
                        }
                        val updatedEntity = currentStatus.copy(
                            id = simSlot,
                            subscriptionId = currentSubId ?: currentStatus.subscriptionId,
                            balanceCup = parsed.balanceCup ?: currentStatus.balanceCup,
                            dataMb = parsed.dataMb ?: currentStatus.dataMb,
                            dataLteMb = parsed.dataLteMb ?: currentStatus.dataLteMb,
                            bonusDataMb = parsed.bonusMb ?: currentStatus.bonusDataMb,
                            minutesStr = parsed.minutesStr ?: currentStatus.minutesStr,
                            smsCount = parsed.sms ?: currentStatus.smsCount,
                            dataDays = parsed.dataDays ?: currentStatus.dataDays,
                            minutesDays = parsed.minutesDays ?: currentStatus.minutesDays,
                            smsDays = parsed.smsDays ?: currentStatus.smsDays,
                            nextRechargeDateStr = parsed.nextRechargeDateStr ?: currentStatus.nextRechargeDateStr,
                            nextRechargeDays = parsed.nextRechargeDays ?: currentStatus.nextRechargeDays,
                            lastUpdatedTimestamp = System.currentTimeMillis()
                        )
                        planDao.insertOrUpdatePlanStatus(updatedEntity)
                    }
                    updateAllWidgets(context)
                    pendingResult.finish()
                }
            } else {
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        android.util.Log.d("MegasWidgetProvider", "onUpdate triggered for ${appWidgetIds.size} widgets (layoutResId=$layoutResId): ${appWidgetIds.joinToString()}")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, layoutResId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle?
    ) {
        android.util.Log.d("MegasWidgetProvider", "onAppWidgetOptionsChanged triggered for widget $appWidgetId")
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId, layoutResId)
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.ams.megascu.ACTION_WIDGET_REFRESH"

        data class WidgetParamData(
            val label: String,
            val value: String,
            val badgeText: String?,
            val showBadge: Boolean,
            val daysText: String? = null
        )

        fun getParamData(type: String, plan: PlanStatusEntity?, isWide: Boolean = false): WidgetParamData {
            val shortTitle = when (type) {
                "saldo" -> "Saldo"
                "llamadas" -> "Minutos"
                "mensajes" -> "SMS"
                else -> "Datos"
            }
            val longTitle = when (type) {
                "saldo" -> "Saldo Principal"
                "llamadas" -> "Llamadas (Voz)"
                "mensajes" -> "Mensajes (SMS)"
                else -> "Datos Disponibles"
            }
            val label = if (isWide) longTitle else shortTitle

            if (plan == null) {
                return WidgetParamData(
                    label = label,
                    value = when (type) {
                        "saldo" -> "0.00 CUP"
                        "llamadas" -> "0 Min"
                        "mensajes" -> "0 SMS"
                        else -> "0.00 GB"
                    },
                    badgeText = null,
                    showBadge = false,
                    daysText = null
                )
            }
            return when (type) {
                "saldo" -> {
                    val daysText = if (plan.nextRechargeDays > 0) {
                        if (isWide) "Recarga en ${plan.nextRechargeDays} días" else "${plan.nextRechargeDays}d"
                    } else if (!plan.nextRechargeDateStr.isNullOrBlank()) {
                        if (isWide) "Recarga el ${plan.nextRechargeDateStr}" else plan.nextRechargeDateStr
                    } else null

                    WidgetParamData(
                        label = label,
                        value = String.format(Locale.US, "%.2f CUP", plan.balanceCup),
                        badgeText = null,
                        showBadge = false,
                        daysText = daysText
                    )
                }
                "llamadas" -> {
                    val daysText = if (plan.minutesDays > 0) {
                        if (isWide) "Vence en ${plan.minutesDays} días" else "${plan.minutesDays}d"
                    } else null

                    WidgetParamData(
                        label = label,
                        value = plan.minutesStr.ifEmpty { "0 Min" },
                        badgeText = null,
                        showBadge = false,
                        daysText = daysText
                    )
                }
                "mensajes" -> {
                    val daysText = if (plan.smsDays > 0) {
                        if (isWide) "Vence en ${plan.smsDays} días" else "${plan.smsDays}d"
                    } else null

                    WidgetParamData(
                        label = label,
                        value = "${plan.smsCount} SMS",
                        badgeText = null,
                        showBadge = false,
                        daysText = daysText
                    )
                }
                else -> { // "megas"
                    val totalDataMb = plan.dataMb + plan.dataLteMb
                    val formattedData = if (totalDataMb >= 1024L) {
                        String.format(Locale.US, "%.2f GB", totalDataMb / 1024f)
                    } else {
                        String.format(Locale.US, "%d MB", totalDataMb)
                    }
                    val daysText = if (plan.dataDays > 0) {
                        if (isWide) "Vence en ${plan.dataDays} días" else "${plan.dataDays}d"
                    } else null

                    WidgetParamData(
                        label = label,
                        value = formattedData,
                        badgeText = null,
                        showBadge = false,
                        daysText = daysText
                    )
                }
            }
        }

        private fun saveCachedPlan(context: Context, plan: PlanStatusEntity, simSlot: Int = 1) {
            val prefs = context.getSharedPreferences("megas_widget_plan_cache", Context.MODE_PRIVATE)
            val prefix = if (simSlot > 1) "sim_${simSlot}_" else ""
            prefs.edit()
                .putLong("${prefix}dataMb", plan.dataMb)
                .putLong("${prefix}dataLteMb", plan.dataLteMb)
                .putLong("${prefix}bonusDataMb", plan.bonusDataMb)
                .putFloat("${prefix}balanceCup", plan.balanceCup.toFloat())
                .putInt("${prefix}smsCount", plan.smsCount)
                .putString("${prefix}minutesStr", plan.minutesStr)
                .putInt("${prefix}dataDays", plan.dataDays)
                .putInt("${prefix}smsDays", plan.smsDays)
                .putInt("${prefix}minutesDays", plan.minutesDays)
                .putString("${prefix}nextRechargeDateStr", plan.nextRechargeDateStr)
                .putInt("${prefix}nextRechargeDays", plan.nextRechargeDays)
                .putLong("${prefix}dataExpirationTimestamp", plan.dataExpirationTimestamp)
                .putLong("${prefix}lastUpdatedTimestamp", plan.lastUpdatedTimestamp)
                .apply()
        }

        private fun getCachedPlan(context: Context, simSlot: Int = 1): PlanStatusEntity? {
            val prefs = context.getSharedPreferences("megas_widget_plan_cache", Context.MODE_PRIVATE)
            val prefix = if (simSlot > 1) "sim_${simSlot}_" else ""
            if (!prefs.contains("${prefix}lastUpdatedTimestamp")) {
                if (simSlot == 1 && prefs.contains("lastUpdatedTimestamp")) {
                    return PlanStatusEntity(
                        id = 1,
                        balanceCup = prefs.getFloat("balanceCup", 0f).toDouble(),
                        dataMb = prefs.getLong("dataMb", 0L),
                        dataLteMb = prefs.getLong("dataLteMb", 0L),
                        bonusDataMb = prefs.getLong("bonusDataMb", 0L),
                        minutesStr = prefs.getString("minutesStr", "") ?: "",
                        smsCount = prefs.getInt("smsCount", 0),
                        dataDays = prefs.getInt("dataDays", 0),
                        minutesDays = prefs.getInt("minutesDays", 0),
                        smsDays = prefs.getInt("smsDays", 0),
                        dataExpirationTimestamp = prefs.getLong("dataExpirationTimestamp", 0L),
                        nextRechargeDateStr = prefs.getString("nextRechargeDateStr", "") ?: "",
                        nextRechargeDays = prefs.getInt("nextRechargeDays", 0),
                        lastUpdatedTimestamp = prefs.getLong("lastUpdatedTimestamp", 0L)
                    )
                }
                return null
            }
            return PlanStatusEntity(
                id = simSlot,
                balanceCup = prefs.getFloat("${prefix}balanceCup", 0f).toDouble(),
                dataMb = prefs.getLong("${prefix}dataMb", 0L),
                dataLteMb = prefs.getLong("${prefix}dataLteMb", 0L),
                bonusDataMb = prefs.getLong("${prefix}bonusDataMb", 0L),
                minutesStr = prefs.getString("${prefix}minutesStr", "") ?: "",
                smsCount = prefs.getInt("${prefix}smsCount", 0),
                dataDays = prefs.getInt("${prefix}dataDays", 0),
                minutesDays = prefs.getInt("${prefix}minutesDays", 0),
                smsDays = prefs.getInt("${prefix}smsDays", 0),
                dataExpirationTimestamp = prefs.getLong("${prefix}dataExpirationTimestamp", 0L),
                nextRechargeDateStr = prefs.getString("${prefix}nextRechargeDateStr", "") ?: "",
                nextRechargeDays = prefs.getInt("${prefix}nextRechargeDays", 0),
                lastUpdatedTimestamp = prefs.getLong("${prefix}lastUpdatedTimestamp", 0L)
            )
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            layoutResId: Int = R.layout.widget_megas_4x2
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val widgetPrefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
                    val simSlot = widgetPrefs.getInt("widget_${appWidgetId}_sim_slot", 1).coerceIn(1, 2)
                    val db = MegasDatabase.getDatabase(context)
                    val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
                    val dbPlanRaw = db.planDao().getPlanStatusDirect(simSlot)
                    val dbPlan = if (dbPlanRaw != null && (dbPlanRaw.subscriptionId == null || currentSubId == null || dbPlanRaw.subscriptionId == currentSubId)) {
                        dbPlanRaw
                    } else null

                    val plan = if (dbPlan != null) {
                        saveCachedPlan(context, dbPlan, simSlot)
                        dbPlan
                    } else {
                        getCachedPlan(context, simSlot)
                    }

                    val views = RemoteViews(context.packageName, layoutResId)

                    if (layoutResId == R.layout.widget_megas_2x1) {
                        val widgetPrefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
                        val type = widgetPrefs.getString("widget_${appWidgetId}_type", "megas") ?: "megas"

                        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                        val minWidthDp = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 110
                        val isWide = minWidthDp >= 180

                        val paramData = getParamData(type, plan, isWide)
                        views.setTextViewText(R.id.widget_label, paramData.label)
                        views.setTextViewText(R.id.widget_value, paramData.value)
                        
                        // Badge chip superior desactivado para dejar unicamente el chip inferior
                        views.setViewVisibility(R.id.widget_badge, android.view.View.GONE)

                        if (!paramData.daysText.isNullOrBlank()) {
                            views.setViewVisibility(R.id.widget_days, android.view.View.VISIBLE)
                            views.setTextViewText(R.id.widget_days, paramData.daysText)
                        } else {
                            views.setViewVisibility(R.id.widget_days, android.view.View.GONE)
                        }
                    } else {
                        // Standard 4x2 layout
                        if (plan != null) {
                        val totalDataMb = plan.dataMb + plan.dataLteMb
                        val formattedData = if (totalDataMb >= 1024L) {
                            String.format(Locale.US, "%.2f GB", totalDataMb / 1024f)
                        } else {
                            String.format(Locale.US, "%d MB", totalDataMb)
                        }

                        val bonusMb = plan.bonusDataMb
                        val formattedBonus = if (bonusMb >= 1024L) {
                            String.format(Locale.US, "%.2f GB", bonusMb / 1024f)
                        } else {
                            String.format(Locale.US, "%d MB", bonusMb)
                        }

                        views.setTextSafely(R.id.widget_data_value, formattedData)
                        views.setTextSafely(
                            R.id.widget_balance_cup,
                            String.format(Locale.US, "%.2f CUP", plan.balanceCup)
                        )
                        views.setTextSafely(R.id.widget_bonus_value, formattedBonus)
                        views.setTextSafely(R.id.widget_calls_value, plan.minutesStr.ifEmpty { "0 Min" })
                        views.setTextSafely(R.id.widget_sms_value, "${plan.smsCount} SMS")

                        // Update badges
                        views.setBadgeSafely(R.id.widget_data_days_badge, plan.dataDays)
                        views.setBadgeSafely(R.id.widget_bonus_days_badge, if (plan.bonusDataMb > 0) plan.dataDays else 0)
                        views.setBadgeSafely(R.id.widget_calls_days_badge, plan.minutesDays)
                        views.setBadgeSafely(R.id.widget_sms_days_badge, plan.smsDays)

                        val dateStr = plan.nextRechargeDateStr
                        if (!dateStr.isNullOrBlank()) {
                            views.setVisibilitySafely(R.id.widget_recharge_layout, android.view.View.VISIBLE)
                            views.setTextSafely(R.id.widget_recharge_text, "Puede recargar el $dateStr")
                            views.setBadgeSafely(R.id.widget_recharge_days_badge, plan.nextRechargeDays)
                        } else {
                            views.setVisibilitySafely(R.id.widget_recharge_layout, android.view.View.GONE)
                        }

                        // Backwards compatible fields
                        views.setTextSafely(R.id.widget_sub_details, "LTE: ${plan.dataLteMb} MB")

                        val expText = if (plan.dataDays > 0) {
                            "Vence en ${plan.dataDays} días"
                        } else if (plan.dataExpirationTimestamp > 0) {
                            val diffDays = ((plan.dataExpirationTimestamp - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                            "Vence en $diffDays días"
                        } else {
                            "Sin plan activo"
                        }
                        views.setTextSafely(R.id.widget_expiration, expText)

                        val progressPercent = (((totalDataMb + bonusMb) / 5120f) * 100).toInt().coerceIn(5, 100)
                        views.setProgressBarSafely(R.id.widget_progress_bar, 100, progressPercent, false)

                        val updatedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(plan.lastUpdatedTimestamp))
                        views.setTextSafely(R.id.widget_last_updated, "Act. $updatedTime")
                    } else {
                        views.setTextSafely(R.id.widget_data_value, "0.00 GB")
                        views.setTextSafely(R.id.widget_balance_cup, "0.00 CUP")
                        views.setTextSafely(R.id.widget_bonus_value, "0.00 GB")
                        views.setTextSafely(R.id.widget_calls_value, "0 Min")
                        views.setTextSafely(R.id.widget_sms_value, "0 SMS")
                        
                        views.setVisibilitySafely(R.id.widget_data_days_badge, android.view.View.GONE)
                        views.setVisibilitySafely(R.id.widget_bonus_days_badge, android.view.View.GONE)
                        views.setVisibilitySafely(R.id.widget_calls_days_badge, android.view.View.GONE)
                        views.setVisibilitySafely(R.id.widget_sms_days_badge, android.view.View.GONE)
                        views.setVisibilitySafely(R.id.widget_recharge_layout, android.view.View.GONE)

                        views.setTextSafely(R.id.widget_sub_details, "Presiona para actualizar")
                        views.setTextSafely(R.id.widget_expiration, "--")
                        views.setProgressBarSafely(R.id.widget_progress_bar, 100, 0, false)
                        views.setTextSafely(R.id.widget_last_updated, "Sin datos")
                    }
                    }

                    // Refresh button logic (only in layouts with the refresh button)
                    val refreshIntent = Intent(context, MegasWidgetProvider::class.java).apply {
                        action = ACTION_WIDGET_REFRESH
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)

                    // Click on card opens MainActivity
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            val standardComp = ComponentName(context, MegasWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(standardComp)) {
                updateAppWidget(context, appWidgetManager, id, R.layout.widget_megas_4x2)
            }

            val widget2x1Comp = ComponentName(context, MegasWidget2x1Provider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(widget2x1Comp)) {
                updateAppWidget(context, appWidgetManager, id, R.layout.widget_megas_2x1)
            }

            val chartComp = ComponentName(context, MegasChartWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(chartComp)) {
                MegasChartWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}

class MegasWidget2x1Provider : MegasWidgetProvider(R.layout.widget_megas_2x1)

// Funciones de extensión defensivas y seguras para RemoteViews
private fun RemoteViews.setTextSafely(viewId: Int, text: CharSequence?) {
    try {
        setTextViewText(viewId, text)
    } catch (_: Exception) {}
}

private fun RemoteViews.setVisibilitySafely(viewId: Int, visibility: Int) {
    try {
        setViewVisibility(viewId, visibility)
    } catch (_: Exception) {}
}

private fun RemoteViews.setProgressBarSafely(viewId: Int, max: Int, progress: Int, indeterminate: Boolean) {
    try {
        setProgressBar(viewId, max, progress, indeterminate)
    } catch (_: Exception) {}
}

private fun RemoteViews.setBadgeSafely(viewId: Int, days: Int) {
    try {
        if (days > 0) {
            setViewVisibility(viewId, android.view.View.VISIBLE)
            setTextViewText(viewId, "${days}d")
        } else {
            setViewVisibility(viewId, android.view.View.GONE)
        }
    } catch (_: Exception) {}
}
