package com.ams.megascu.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.ams.megascu.R

/** RemoteViews cannot transmit a custom Typeface to every launcher. Render only text,
 * keeping the original layout, dimensions, click targets and accessible text views.
 */
internal object WidgetTextRenderer {
    fun render(context: Context, manager: AppWidgetManager, widgetId: Int, source: RemoteViews, layoutId: Int): RemoteViews {
        val options = manager.getAppWidgetOptions(widgetId)
        val compact = layoutId == R.layout.widget_megas_2x1
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: if (compact) 110 else 260
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH).takeIf { it > 0 } ?: minWidth
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT).takeIf { it > 0 } ?: if (compact) 60 else 150
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: minHeight
        val portrait = renderSize(context, source, minWidth, maxHeight)
        val landscape = renderSize(context, source, maxWidth, minHeight)
        return RemoteViews(landscape, portrait)
    }

    internal fun renderSize(context: Context, source: RemoteViews, widthDp: Int, heightDp: Int): RemoteViews {
        val result = RemoteViews(source)
        val root = result.apply(context, FrameLayout(context))
        val typeface = ResourcesCompat.getFont(context, R.font.space_mono_bold) ?: return result
        val pairs = TEXT_IDS.mapNotNull { (textId, imageId, containerId) ->
            val text = root.findViewById<TextView>(textId) ?: return@mapNotNull null
            val container = root.findViewById<View>(containerId)
            container.visibility = text.visibility
            result.setViewVisibility(containerId, text.visibility)
            text.typeface = typeface
            Pair(text, imageId)
        }
        val density = context.resources.displayMetrics.density
        root.measure(
            View.MeasureSpec.makeMeasureSpec((widthDp * density).toInt().coerceAtLeast(1), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec((heightDp * density).toInt().coerceAtLeast(1), View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, root.measuredWidth, root.measuredHeight)
        for ((text, imageId) in pairs) {
            var visible = true
            var ancestor: View? = text
            while (ancestor != null) {
                if (ancestor.visibility != View.VISIBLE) visible = false
                ancestor = ancestor.parent as? View
            }
            if (!visible) {
                result.setViewVisibility(imageId, View.GONE)
                continue
            }
            val chip = text.id in CHIP_IDS
            if (chip) {
                // A badge must be measured at its natural width: the launcher may measure the
                // transparent TextView with another typeface and clip its separate background.
                text.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
                text.layout(0, 0, text.measuredWidth, text.measuredHeight)
            }
            if (text.width <= 0 || text.height <= 0) {
                result.setViewVisibility(imageId, View.GONE)
                continue
            }
            val bitmap = Bitmap.createBitmap(text.width, text.height, Bitmap.Config.ARGB_8888)
            bitmap.density = context.resources.displayMetrics.densityDpi
            // Chips include their rounded background in the bitmap, so it always reaches
            // the end of the Space Mono text. Other views keep their existing backgrounds.
            val background = text.background
            if (!chip) text.background = null
            text.draw(Canvas(bitmap))
            if (!chip) text.background = background
            result.setImageViewBitmap(imageId, bitmap)
            result.setViewVisibility(imageId, View.VISIBLE)
            result.setTextColor(text.id, Color.TRANSPARENT)
            if (chip) result.setInt(text.id, "setBackgroundResource", 0)
        }
        return result
    }

    // Direct IDs also keep these resources reachable during release shrinking.
    private val TEXT_IDS = listOf(
        Triple(R.id.widget_label, R.id.widget_label_font_image, R.id.widget_label_font_container),
        Triple(R.id.widget_badge, R.id.widget_badge_font_image, R.id.widget_badge_font_container),
        Triple(R.id.widget_value, R.id.widget_value_font_image, R.id.widget_value_font_container),
        Triple(R.id.widget_days, R.id.widget_days_font_image, R.id.widget_days_font_container),
        Triple(R.id.widget_megas_4x2_text_1, R.id.widget_megas_4x2_text_1_font_image, R.id.widget_megas_4x2_text_1_font_container),
        Triple(R.id.widget_balance_cup, R.id.widget_balance_cup_font_image, R.id.widget_balance_cup_font_container),
        Triple(R.id.widget_megas_4x2_text_3, R.id.widget_megas_4x2_text_3_font_image, R.id.widget_megas_4x2_text_3_font_container),
        Triple(R.id.widget_megas_4x2_text_4, R.id.widget_megas_4x2_text_4_font_image, R.id.widget_megas_4x2_text_4_font_container),
        Triple(R.id.widget_data_value, R.id.widget_data_value_font_image, R.id.widget_data_value_font_container),
        Triple(R.id.widget_data_days_badge, R.id.widget_data_days_badge_font_image, R.id.widget_data_days_badge_font_container),
        Triple(R.id.widget_megas_4x2_text_7, R.id.widget_megas_4x2_text_7_font_image, R.id.widget_megas_4x2_text_7_font_container),
        Triple(R.id.widget_bonus_value, R.id.widget_bonus_value_font_image, R.id.widget_bonus_value_font_container),
        Triple(R.id.widget_bonus_days_badge, R.id.widget_bonus_days_badge_font_image, R.id.widget_bonus_days_badge_font_container),
        Triple(R.id.widget_megas_4x2_text_10, R.id.widget_megas_4x2_text_10_font_image, R.id.widget_megas_4x2_text_10_font_container),
        Triple(R.id.widget_calls_value, R.id.widget_calls_value_font_image, R.id.widget_calls_value_font_container),
        Triple(R.id.widget_calls_days_badge, R.id.widget_calls_days_badge_font_image, R.id.widget_calls_days_badge_font_container),
        Triple(R.id.widget_megas_4x2_text_13, R.id.widget_megas_4x2_text_13_font_image, R.id.widget_megas_4x2_text_13_font_container),
        Triple(R.id.widget_sms_value, R.id.widget_sms_value_font_image, R.id.widget_sms_value_font_container),
        Triple(R.id.widget_sms_days_badge, R.id.widget_sms_days_badge_font_image, R.id.widget_sms_days_badge_font_container),
        Triple(R.id.widget_recharge_text, R.id.widget_recharge_text_font_image, R.id.widget_recharge_text_font_container),
        Triple(R.id.widget_recharge_days_badge, R.id.widget_recharge_days_badge_font_image, R.id.widget_recharge_days_badge_font_container)
    )

    private val CHIP_IDS = setOf(
        R.id.widget_badge, R.id.widget_days,
        R.id.widget_data_days_badge, R.id.widget_bonus_days_badge,
        R.id.widget_calls_days_badge, R.id.widget_sms_days_badge,
        R.id.widget_recharge_days_badge
    )
}
