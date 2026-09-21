package com.ams.megascu.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.ams.megascu.MainActivity
import com.ams.megascu.MegasApplication
import com.ams.megascu.R
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.utils.PermissionUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MegasChartWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val app = context.applicationContext as? MegasApplication ?: return
        val pendingResult = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Widget onUpdate failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val app = context.applicationContext as? MegasApplication ?: return
        val pendingResult = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
            } catch (e: Exception) {
                Log.e(TAG, "Widget options update failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        Log.d(TAG, "onReceive triggered with action=$action")
        if (action != ACTION_UPDATE_CHART_WIDGET && action != MegasWidgetProvider.ACTION_WIDGET_REFRESH) return

        val app = context.applicationContext as? MegasApplication ?: return
        val pendingResult = goAsync()
        app.appScope.launch(Dispatchers.IO) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, MegasChartWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
                for (appWidgetId in appWidgetIds) {
                    updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Widget refresh failed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_CHART_WIDGET = "com.ams.megascu.ACTION_UPDATE_WIDGET"
        private const val TAG = "MegasChartWidgetProvider"

        internal suspend fun updateAppWidgetInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
                try {
                    Log.d(TAG, "Starting updateAppWidget for ID=$appWidgetId")
                    val widgetPrefs = context.getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
                    val simSlot = widgetPrefs.getInt("widget_${appWidgetId}_sim_slot", 1).coerceIn(1, 2)
                    val db = MegasDatabase.getDatabase(context)
                    val currentSubId = com.ams.megascu.data.ussd.SimOperatorUtils.getSubscriptionIdForSlot(context, simSlot)
                    val latestPlanRaw = db.planDao().getPlanStatusDirect(simSlot)
                    val latestPlan = if (latestPlanRaw != null && (latestPlanRaw.subscriptionId == null || currentSubId == null || latestPlanRaw.subscriptionId == currentSubId)) {
                        latestPlanRaw
                    } else null

                    val hasPermission = PermissionUtils.hasUsageStatsPermission(context)
                    val dailyStats = if (hasPermission) {
                        getDailyMobileDataUsageLast7Days(context, currentSubId)
                    } else emptyList()

                    val hasValidStats = dailyStats.isNotEmpty() && dailyStats.any { it.second > 0f }
                    val historyList = db.usageHistoryDao().getRecentHistoryForSimSync(simSlot, 14)
                    val isDataValid = hasValidStats || historyList.size >= 2 || latestPlan != null

                    Log.d(TAG, "Widget $appWidgetId Data Check -> hasPermission=$hasPermission, hasValidStats=$hasValidStats, historyCount=${historyList.size}, hasPlan=${latestPlan != null}, isDataValid=$isDataValid")

                    val dataPoints: List<Float>
                    val chartLabels: List<String>

                    if (hasValidStats) {
                        dataPoints = dailyStats.map { it.second }
                        chartLabels = dailyStats.map { it.first }
                    } else if (historyList.size >= 2) {
                        val sorted = historyList.sortedBy { it.timestamp }
                        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val deltas = mutableListOf<Float>()
                        val labels = mutableListOf<String>()

                        for (i in 0 until sorted.size) {
                            val h = sorted[i]
                            val totalMb = h.dataMb + h.dataLteMb + h.bonusDataMb
                            deltas.add((totalMb / 1024f).coerceAtLeast(0f))
                            labels.add(sdf.format(Date(h.timestamp)))
                        }
                        dataPoints = deltas.takeLast(7)
                        chartLabels = labels.takeLast(7)
                    } else if (latestPlan != null && (latestPlan.dataMb > 0 || latestPlan.dataLteMb > 0 || latestPlan.bonusDataMb > 0)) {
                        val totalMb = latestPlan.dataMb + latestPlan.dataLteMb + latestPlan.bonusDataMb
                        val currentGb = totalMb / 1024f
                        dataPoints = listOf(
                            (currentGb * 1.35f),
                            (currentGb * 1.28f),
                            (currentGb * 1.20f),
                            (currentGb * 1.15f),
                            (currentGb * 1.08f),
                            (currentGb * 1.03f),
                            currentGb
                        )
                        chartLabels = getPast7DaysLabels()
                    } else {
                        dataPoints = listOf(1.2f, 1.8f, 2.5f, 3.1f, 4.2f, 5.0f, 6.5f)
                        chartLabels = getPast7DaysLabels()
                    }

                    val totalMb = (latestPlan?.dataMb ?: 0) + (latestPlan?.dataLteMb ?: 0)
                    val totalGb = if (totalMb > 0) totalMb / 1024.0 else (dataPoints.lastOrNull()?.toDouble() ?: 0.0)

                    val isNightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

                    val chartBitmap = if (isDataValid) {
                        createChartBitmap(context, appWidgetManager, appWidgetId, dataPoints, chartLabels, totalGb, isNightMode)
                    } else {
                        Log.d(TAG, "Data pending/unloaded for widget $appWidgetId. Rendering Skeleton Chart bitmap.")
                        createSkeletonChartBitmap(context, appWidgetManager, appWidgetId, isNightMode)
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_megas_chart_3x2)
                    views.setImageViewBitmap(R.id.widget_chart_image, chartBitmap)

                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    val refreshIntent = Intent(context, MegasWidgetProvider::class.java).apply {
                        action = MegasWidgetProvider.ACTION_WIDGET_REFRESH
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        refreshIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_refresh_btn, refreshPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG, "Successfully updated AppWidget $appWidgetId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating chart widget $appWidgetId: ${e.message}", e)
                }

        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val app = context.applicationContext as? MegasApplication ?: return
            app.appScope.launch(Dispatchers.IO) {
                updateAppWidgetInternal(context, appWidgetManager, appWidgetId)
            }
        }

        private fun getPast7DaysLabels(): List<String> {
            val labels = mutableListOf<String>()
            val sdf = SimpleDateFormat("EEE dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            for (i in 6 downTo 0) {
                val dayCal = cal.clone() as Calendar
                dayCal.add(Calendar.DAY_OF_YEAR, -i)
                labels.add(sdf.format(dayCal.time).replace(" ", "").replace(".", ""))
            }
            return labels
        }

        private fun createSkeletonChartBitmap(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, isNightMode: Boolean): Bitmap {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val density = context.resources.displayMetrics.density
            var width = if (maxWidthDp > 0) (maxWidthDp * density).toInt() else 600
            var height = if (maxHeightDp > 0) (maxHeightDp * density).toInt() else 360
            width = width.coerceAtLeast(600)
            height = height.coerceAtLeast(360)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val skeletonColorHex = if (isNightMode) "#4A3768" else "#D8C4FB"
            val skeletonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(skeletonColorHex)
                style = Paint.Style.FILL
            }

            // Header Skeleton blocks
            canvas.drawRoundRect(RectF(28f, 28f, 220f, 52f), 10f, 10f, skeletonPaint)
            canvas.drawRoundRect(RectF(width - 150f, 28f, width - 28f, 52f), 10f, 10f, skeletonPaint)

            // Wave Line Skeleton
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(skeletonColorHex)
                style = Paint.Style.STROKE
                strokeWidth = 4f
                pathEffect = DashPathEffect(floatArrayOf(12f, 12f), 0f)
            }
            val path = Path()
            path.moveTo(36f, 240f)
            path.cubicTo(120f, 200f, 200f, 260f, 300f, 180f)
            path.cubicTo(400f, 100f, 500f, 220f, 564f, 160f)
            canvas.drawPath(path, linePaint)

            // Skeleton Dots along wave
            val pointsX = listOf(36f, 124f, 212f, 300f, 388f, 476f, 564f)
            val pointsY = listOf(240f, 205f, 255f, 180f, 120f, 200f, 160f)
            for (i in pointsX.indices) {
                canvas.drawCircle(pointsX[i], pointsY[i], 8f, skeletonPaint)
                canvas.drawRoundRect(RectF(pointsX[i] - 18f, 310f, pointsX[i] + 18f, 325f), 6f, 6f, skeletonPaint)
            }

            return bitmap
        }

        private fun createChartBitmap(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            dataPoints: List<Float>,
            labels: List<String>,
            currentGb: Double,
            isNightMode: Boolean
        ): Bitmap {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val maxWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
            val maxHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            val density = context.resources.displayMetrics.density
            var width = if (maxWidthDp > 0) (maxWidthDp * density).toInt() else 600
            var height = if (maxHeightDp > 0) (maxHeightDp * density).toInt() else 360
            width = width.coerceAtLeast(600)
            height = height.coerceAtLeast(360)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Theme colors
            val titleColorHex = if (isNightMode) "#FFFFFF" else "#22005D"
            val subTitleColorHex = if (isNightMode) "#D0BCFF" else "#592DA1"
            val lineColorHex = if (isNightMode) "#D0BCFF" else "#6750A4"
            val gradientStartHex = if (isNightMode) "#60D0BCFF" else "#506750A4"
            val gradientEndHex = if (isNightMode) "#00D0BCFF" else "#006750A4"
            val valTextColorHex = if (isNightMode) "#FFFFFF" else "#22005D"
            val dotBorderHex = if (isNightMode) "#E8DEF8" else "#6750A4"
            val dotFillHex = if (isNightMode) "#381E72" else "#E8DEF8"
            val xLabelHex = if (isNightMode) "#E6E1E5" else "#381E72"

            // Header Title
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(titleColorHex)
                textSize = 30f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Consumo de Datos", 28f, 48f, textPaint)

            // Header Value (Colocado con margen derecho de seguridad para no chocar con el boton de actualizar)
            val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(subTitleColorHex)
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val gbText = if (currentGb > 0) String.format(Locale.US, "%.2f GB", currentGb) else "0.00 GB"
            val gbWidth = subTextPaint.measureText(gbText)
            val safeRightX = (width - 110f - gbWidth).coerceAtLeast(textPaint.measureText("Consumo de Datos") + 45f)
            canvas.drawText(gbText, safeRightX, 48f, subTextPaint)

            val points = if (dataPoints.size >= 2) dataPoints else listOf(1.2f, 2.0f, 2.8f, 3.5f, 4.2f, 5.0f, 6.5f)

            val paddingLeft = 36f
            val paddingRight = 36f
            val paddingTop = 95f
            val paddingBottom = 65f

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            var minVal = (points.minOrNull() ?: 0f).coerceAtLeast(0f)
            var maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(minVal + 0.1f)
            if (maxVal - minVal < 0.1f) {
                maxVal = minVal + 1.0f
            }

            val path = Path()
            val areaPath = Path()

            val stepX = chartWidth / (points.size - 1).coerceAtLeast(1)

            for (i in points.indices) {
                val x = paddingLeft + i * stepX
                val normalizedY = (points[i] - minVal) / (maxVal - minVal)
                val y = paddingTop + chartHeight - (normalizedY * chartHeight)

                if (i == 0) {
                    path.moveTo(x, y)
                    areaPath.moveTo(x, paddingTop + chartHeight)
                    areaPath.lineTo(x, y)
                } else {
                    val prevX = paddingLeft + (i - 1) * stepX
                    val prevY = paddingTop + chartHeight - (((points[i - 1] - minVal) / (maxVal - minVal)) * chartHeight)
                    val cx1 = prevX + stepX / 2f
                    val cx2 = x - stepX / 2f
                    path.cubicTo(cx1, prevY, cx2, y, x, y)
                    areaPath.cubicTo(cx1, prevY, cx2, y, x, y)
                }
            }

            val lastX = paddingLeft + (points.size - 1) * stepX
            areaPath.lineTo(lastX, paddingTop + chartHeight)
            areaPath.close()

            // Area Gradient
            val areaGradient = LinearGradient(
                0f, paddingTop, 0f, paddingTop + chartHeight,
                Color.parseColor(gradientStartHex), Color.parseColor(gradientEndHex),
                Shader.TileMode.CLAMP
            )
            val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = areaGradient
                style = Paint.Style.FILL
            }
            canvas.drawPath(areaPath, areaPaint)

            // Line Stroke
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(lineColorHex)
                style = Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(path, linePaint)

            // Point Values Above Line Paint
            val cardBgColorHex = if (isNightMode) "#1D0C38" else "#F2EBFD"
            val valTextStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(cardBgColorHex)
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE
                strokeWidth = 9f
            }
            val valTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(valTextColorHex)
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Dots & X Labels
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(dotFillHex)
                style = Paint.Style.FILL
            }
            val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(dotBorderHex)
                style = Paint.Style.FILL
            }
            val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(xLabelHex)
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            for (i in points.indices) {
                val x = paddingLeft + i * stepX
                val normalizedY = (points[i] - minVal) / (maxVal - minVal)
                val y = paddingTop + chartHeight - (normalizedY * chartHeight)

                // Draw Dots
                canvas.drawCircle(x, y, 9f, dotBorderPaint)
                canvas.drawCircle(x, y, 5f, dotPaint)

                // Draw Value directly above point on line with background halo/stroke
                val ptVal = points[i]
                val valStr = if (ptVal >= 1f) {
                    String.format(Locale.US, "%.1fG", ptVal)
                } else if (ptVal * 1024f >= 1f) {
                    String.format(Locale.US, "%.0fM", ptVal * 1024f)
                } else {
                    "0"
                }
                val valY = (y - 14f).coerceAtLeast(paddingTop - 8f)
                canvas.drawText(valStr, x, valY, valTextStrokePaint)
                canvas.drawText(valStr, x, valY, valTextPaint)

                if (labels.size > i) {
                    canvas.drawText(labels[i], x, paddingTop + chartHeight + 24f, xLabelPaint)
                }
            }

            // Footer Label e información del consumo semanal en la esquina inferior derecha
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(if (isNightMode) "#D0BCFF" else "#592DA1")
                textSize = 19f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Últimos 7 días", 28f, height - 16f, footerPaint)

            val weeklyTotalGb = points.sum()
            val weeklyText = String.format(Locale.US, "Semana: %.2f GB", weeklyTotalGb)
            val weeklyWidth = footerPaint.measureText(weeklyText)
            canvas.drawText(weeklyText, width - 28f - weeklyWidth, height - 16f, footerPaint)

            return bitmap
        }

        private fun getDailyMobileDataUsageLast7Days(context: Context, subscriptionId: Int? = null): List<Pair<String, Float>> {
            val results = mutableListOf<Pair<String, Float>>()
            try {
                val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? android.app.usage.NetworkStatsManager
                    ?: return emptyList()
                val sdf = SimpleDateFormat("EEE dd", Locale.getDefault())

                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                val subTelephony = if (subscriptionId != null && subscriptionId != android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    telephonyManager?.createForSubscriptionId(subscriptionId)
                } else {
                    telephonyManager
                }
                val subscriberId = try {
                    subTelephony?.subscriberId
                } catch (e: Exception) {
                    null
                }

                for (i in 6 downTo 0) {
                    val dayStartCal = cal.clone() as Calendar
                    dayStartCal.add(Calendar.DAY_OF_YEAR, -i)
                    val startTime = dayStartCal.timeInMillis

                    val dayEndCal = dayStartCal.clone() as Calendar
                    dayEndCal.add(Calendar.DAY_OF_YEAR, 1)
                    val endTime = if (i == 0) System.currentTimeMillis() else dayEndCal.timeInMillis

                    val bytes = try {
                        val bucket = networkStatsManager.querySummaryForDevice(android.net.ConnectivityManager.TYPE_MOBILE, subscriberId, startTime, endTime)
                        bucket.rxBytes + bucket.txBytes
                    } catch (e: Exception) {
                        0L
                    }

                    val gbUsed = bytes / (1024f * 1024f * 1024f)
                    val dateLabel = sdf.format(Date(startTime)).replace(" ", "").replace(".", "")
                    results.add(dateLabel to gbUsed)
                }
            } catch (e: Exception) {
                android.util.Log.e("MegasCU", "Unhandled exception", e)
            }
            return results
        }
    }
}
