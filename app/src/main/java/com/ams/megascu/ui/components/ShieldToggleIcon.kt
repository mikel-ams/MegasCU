package com.ams.megascu.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Material Symbol / Icon 'shield_toggle'
 */
val ShieldToggleIcon: ImageVector
    get() {
        if (_shieldToggleIcon != null) return _shieldToggleIcon!!
        _shieldToggleIcon = ImageVector.Builder(
            name = "ShieldToggle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                // Outer shield shape
                moveTo(12f, 2f)
                lineTo(4f, 5.5f)
                verticalLineTo(11.5f)
                curveTo(4f, 16.55f, 7.41f, 21.26f, 12f, 22.41f)
                curveTo(16.59f, 21.26f, 20f, 16.55f, 20f, 11.5f)
                verticalLineTo(5.5f)
                lineTo(12f, 2f)
                close()

                // Toggle track cutout
                moveTo(10f, 9.5f)
                horizontalLineTo(14f)
                curveTo(15.38f, 9.5f, 16.5f, 10.62f, 16.5f, 12f)
                curveTo(16.5f, 13.38f, 15.38f, 14.5f, 14f, 14.5f)
                horizontalLineTo(10f)
                curveTo(8.62f, 9.5f, 7.5f, 13.38f, 7.5f, 12f)
                curveTo(7.5f, 10.62f, 8.62f, 9.5f, 10f, 9.5f)
                close()

                // Toggle thumb circle
                moveTo(14f, 10.5f)
                curveTo(14.83f, 10.5f, 15.5f, 11.17f, 15.5f, 12f)
                curveTo(15.5f, 12.83f, 14.83f, 13.5f, 14f, 13.5f)
                curveTo(13.17f, 13.5f, 12.5f, 12.83f, 12.5f, 12f)
                curveTo(12.5f, 11.17f, 13.17f, 10.5f, 14f, 10.5f)
                close()
            }
        }.build()
        return _shieldToggleIcon!!
    }

private var _shieldToggleIcon: ImageVector? = null
