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
                    val latestPlan = if (latestPlanRaw != null && latestPlanRaw.subscriptionId == currentSubId) {
                        latestPlanRaw
                    } else null

                    val hasPermission = PermissionUtils.hasUsageStatsPermission(context)
                    val systemDays = com.ams.megascu.utils.MobileDataUsageReader.lastDays(context, currentSubId)
                    val since = java.time.LocalDate.now().minusDays(30).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val history = db.usageHistoryDao().getHistoryForSubscriptionSinceSync(simSlot, currentSubId, since)
                    val estimates = com.ams.megascu.utils.UsageHistoryEstimator.dailyUsage(history, simSlot, currentSubId)
                    val format = java.time.format.DateTimeFormatter.ofPattern("EEE dd", Locale.getDefault())
                    val isEstimated = systemDays == null
                    val dataPoints = systemDays?.map { it.bytes / (1024f * 1024f * 1024f) }
                        ?: estimates.map { it.consumedMb / 1024f }
                    val chartLabels = systemDays?.map { format.format(it.date) }
                        ?: estimates.map { format.format(it.date) }
                    val isDataValid = dataPoints.isNotEmpty()
                    val totalMb = (latestPlan?.dataMb ?: 0L) + (latestPlan?.dataLteMb ?: 0L)
                    val totalGb = totalMb / 1024.0

                    val isNightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES

                    val chartBitmap = if (isDataValid) {
                        createChartBitmap(context, appWidgetManager, appWidgetId, dataPoints, chartLabels, totalGb, isNightMode, isEstimated)
                    } else {
                        Log.d(TAG, "Data pending/unloaded for widget $appWidgetId. Rendering empty history state.")
                        createEmptyChartBitmap(context, appWidgetManager, appWidgetId, totalGb, isNightMode, hasPermission)
                    }

                    val views = RemoteViews(context.packageName, R.layout.widget_megas_chart_3x2)
                    views.setImageViewBitmap(R.id.widget_chart_image, chartBitmap)

                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("selected_sim_slot", simSlot)
                        putExtra("request_usage_access", !hasPermission && !isDataValid)
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
                        data = android.net.Uri.parse("megascu://widget/$appWidgetId/refresh")
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
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

        private fun createEmptyChartBitmap(context: Context, manager: AppWidgetManager, widgetId: Int, currentGb: Double, night: Boolean, hasPermission: Boolean): Bitmap {
            val options = manager.getAppWidgetOptions(widgetId)
            val density = context.resources.displayMetrics.density
            val width = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH) * density).toInt().coerceAtLeast(600)
            val height = (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) * density).toInt().coerceAtLeast(360)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val font = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.space_mono_bold)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = font
                color = Color.parseColor(if (night) "#FFFFFF" else "#22005D")
                textSize = 15f * density
            }
            canvas.drawText("Consumo de Datos", 20f * density, 32f * density, paint)
            paint.textSize = 14f * density
            val balance = String.format(Locale.US, "%.2f GB", currentGb)
            val right = (width - 100f * density - paint.measureText(balance)).coerceAtLeast(paint.measureText("Consumo de Datos") + 30f * density)
            canvas.drawText(balance, right, 32f * density, paint)
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 13f * density
            canvas.drawText("Reuniendo historial real", width / 2f, height / 2f, paint)
            paint.textSize = 9.5f * density
            canvas.drawText("Consulta los datos en al menos 2 días", width / 2f, height / 2f + 24f * density, paint)
            if (!hasPermission) {
                canvas.drawText("Abre la app para conceder acceso de uso", width / 2f, height - 20f * density, paint)
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
            isNightMode: Boolean,
            isEstimated: Boolean
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

            // Theme colors matching the app
            val titleColorHex = if (isNightMode) "#FFFFFF" else "#22005D"
            val subTitleColorHex = if (isNightMode) "#D0BCFF" else "#592DA1"
            val lineColorHex = if (isNightMode) "#D0BCFF" else "#6750A4"
            val gradientStartHex = if (isNightMode) "#60D0BCFF" else "#506750A4"
            val gradientEndHex = if (isNightMode) "#00D0BCFF" else "#006750A4"
            val valTextColorHex = if (isNightMode) "#FFFFFF" else "#22005D"
            val dotBorderHex = if (isNightMode) "#D0BCFF" else "#6750A4"
            val dotFillHex = if (isNightMode) "#160040" else "#FFFFFF" // match center inside dot
            val xLabelHex = if (isNightMode) "#DFD1FF" else "#22005D"
            val cardBgColorHex = if (isNightMode) "#23006B" else "#DFD1FF" // matches surfaceVariant

            // Font loading
            val customTypeface = try {
                androidx.core.content.res.ResourcesCompat.getFont(context, R.font.space_mono_bold)
            } catch (e: Exception) {
                Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }

            // Header Title
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(titleColorHex)
                textSize = 15f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Consumo de Datos", 20f * density, 32f * density, textPaint)

            // Header Value
            val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(subTitleColorHex)
                textSize = 14f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val gbText = if (currentGb > 0) String.format(Locale.US, "%.2f GB", currentGb) else "0.00 GB"
            val gbWidth = subTextPaint.measureText(gbText)
            val safeRightX = (width - 100f * density - gbWidth).coerceAtLeast(textPaint.measureText("Consumo de Datos") + 30f * density)
            canvas.drawText(gbText, safeRightX, 32f * density, subTextPaint)

            val points = dataPoints
            require(points.isNotEmpty())

            // Scaled padding
            val paddingLeft = 54f * density
            val paddingRight = 16f * density
            val paddingTop = 64f * density
            val paddingBottom = 48f * density

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            var minVal = (points.minOrNull() ?: 0f).coerceAtLeast(0f)
            var maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(minVal + 0.1f)
            if (maxVal - minVal < 0.1f) {
                maxVal = minVal + 1.0f
            }

            // Reference guidelines
            val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(lineColorHex)
                alpha = 45
                style = Paint.Style.STROKE
                strokeWidth = 1f * density
                pathEffect = DashPathEffect(floatArrayOf(8f * density, 8f * density), 0f)
            }

            val guideLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(subTitleColorHex)
                textSize = 9f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }

            // Reference levels
            val refLabels = listOf(
                String.format(Locale.US, "%.1fG", maxVal),
                String.format(Locale.US, "%.1fG", (maxVal + minVal) / 2f),
                String.format(Locale.US, "%.1fG", minVal)
            )

            val refYPositions = listOf(
                paddingTop,
                paddingTop + chartHeight / 2f,
                paddingTop + chartHeight
            )

            for (idx in refLabels.indices) {
                val yPos = refYPositions[idx]
                val label = refLabels[idx]
                canvas.drawText(label, 6f * density, yPos + 4f * density, guideLabelPaint)
                val guidePath = Path().apply {
                    moveTo(paddingLeft, yPos)
                    lineTo(width - paddingRight, yPos)
                }
                canvas.drawPath(guidePath, guidePaint)
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
                strokeWidth = 3f * density
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(path, linePaint)

            // Point Values Above Line Paint
            val valTextStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(cardBgColorHex)
                textSize = 9.5f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                style = Paint.Style.STROKE
                strokeWidth = 4f * density
            }
            val valTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(valTextColorHex)
                textSize = 9.5f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            // Dots & X Labels
            val outerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(lineColorHex)
                alpha = 64 // 25% alpha
                style = Paint.Style.FILL
            }
            val dotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(lineColorHex)
                style = Paint.Style.FILL
            }
            val dotFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(dotFillHex)
                style = Paint.Style.FILL
            }
            val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(xLabelHex)
                textSize = 9.5f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            for (i in points.indices) {
                val x = paddingLeft + i * stepX
                val normalizedY = (points[i] - minVal) / (maxVal - minVal)
                val y = paddingTop + chartHeight - (normalizedY * chartHeight)

                // Draw Dots (Concentric Circles)
                canvas.drawCircle(x, y, 9f * density, outerHaloPaint)
                canvas.drawCircle(x, y, 6f * density, dotBorderPaint)
                canvas.drawCircle(x, y, 3f * density, dotFillPaint)

                // Draw Value directly above point on line with background halo
                val ptVal = points[i]
                val valStr = if (ptVal >= 1f) {
                    String.format(Locale.US, "%.1fG", ptVal)
                } else if (ptVal * 1024f >= 1f) {
                    String.format(Locale.US, "%.0fM", ptVal * 1024f)
                } else {
                    "0"
                }
                val valY = (y - 10f * density).coerceAtLeast(paddingTop - 4f * density)
                canvas.drawText(valStr, x, valY, valTextStrokePaint)
                canvas.drawText(valStr, x, valY, valTextPaint)

                if (labels.size > i) {
                    canvas.drawText(labels[i], x, paddingTop + chartHeight + 16f * density, xLabelPaint)
                }
            }

            // Footer Label e información del consumo semanal en la esquina inferior derecha
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(subTitleColorHex)
                textSize = 10f * density
                typeface = customTypeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(if (isEstimated) "Estimado por historial" else "Últimos 7 días", 20f * density, height - 12f * density, footerPaint)

            val weeklyTotalGb = points.sum()
            val weeklyText = String.format(Locale.US, "Semana: %.2f GB", weeklyTotalGb)
            val weeklyWidth = footerPaint.measureText(weeklyText)
            canvas.drawText(weeklyText, width - 20f * density - weeklyWidth, height - 12f * density, footerPaint)

            return bitmap
        }

    }
}
