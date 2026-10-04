package com.ams.megascu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.widget.WidgetTextRenderer
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WidgetTextRendererTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test fun textBitmapKeepsOriginalTextSizeAndContentForAccessibility() {
        val source = RemoteViews(context.packageName, R.layout.widget_megas_2x1)
        source.setTextViewText(R.id.widget_value, "2.00 GB")
        val result = WidgetTextRenderer.renderSize(context, source, 180, 100).apply(context, FrameLayout(context))
        val value = result.findViewById<TextView>(R.id.widget_value)
        val image = result.findViewById<ImageView>(R.id.widget_value_font_image)
        assertEquals("2.00 GB", value.text.toString())
        assertEquals(22f * context.resources.displayMetrics.scaledDensity, value.textSize, 0.1f)
        assertEquals(Color.TRANSPARENT, value.currentTextColor)
        assertEquals(View.VISIBLE, image.visibility)
        assertTrue(image.drawable is BitmapDrawable)
        assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_NO, image.importantForAccessibility)
    }

    @Test fun hiddenDaysChipDoesNotReserveSpaceThroughImageOverlay() {
        val source = RemoteViews(context.packageName, R.layout.widget_megas_2x1)
        source.setViewVisibility(R.id.widget_days, View.GONE)
        val result = WidgetTextRenderer.renderSize(context, source, 180, 100).apply(context, FrameLayout(context))
        assertEquals(View.GONE, result.findViewById<View>(R.id.widget_days_font_image).visibility)
        assertEquals(View.GONE, result.findViewById<View>(R.id.widget_days_font_container).visibility)
    }

    @Test fun visibleDaysChipCanReappearAfterAnEarlierEmptyUpdate() {
        val source = RemoteViews(context.packageName, R.layout.widget_megas_2x1)
        source.setTextViewText(R.id.widget_days, "15d")
        source.setViewVisibility(R.id.widget_days, View.VISIBLE)
        val result = WidgetTextRenderer.renderSize(context, source, 180, 100).apply(context, FrameLayout(context))
        assertEquals(View.VISIBLE, result.findViewById<View>(R.id.widget_days_font_image).visibility)
        assertEquals(View.VISIBLE, result.findViewById<View>(R.id.widget_days_font_container).visibility)
    }

    @Test fun longBadgeRendersItsRoundedBackgroundAcrossTheFullTextWidth() {
        val source = RemoteViews(context.packageName, R.layout.widget_megas_2x1)
        source.setTextViewText(R.id.widget_badge, "Recarga en 21 días")
        source.setViewVisibility(R.id.widget_badge, View.VISIBLE)
        val result = WidgetTextRenderer.renderSize(context, source, 240, 80)
            .apply(context, FrameLayout(context))
        val text = result.findViewById<TextView>(R.id.widget_badge)
        val image = result.findViewById<ImageView>(R.id.widget_badge_font_image)
        val bitmap = (image.drawable as BitmapDrawable).bitmap
        val textWidth = text.paint.measureText(text.text.toString())
        assertTrue(bitmap.width >= textWidth + text.paddingLeft + text.paddingRight - 2)
        assertNull(text.background)
        assertEquals("Recarga en 21 días", text.text.toString())
    }
}
