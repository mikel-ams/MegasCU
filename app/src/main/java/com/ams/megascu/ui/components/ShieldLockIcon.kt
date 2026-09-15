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
 * Material Symbol / Icon 'shield_lock'
 */
val ShieldLockIcon: ImageVector
    get() {
        if (_shieldLockIcon != null) return _shieldLockIcon!!
        _shieldLockIcon = ImageVector.Builder(
            name = "ShieldLock",
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
                pathFillType = PathFillType.NonZero
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
                // Lock cutout body inside shield
                moveTo(12f, 7f)
                curveTo(13.1f, 7f, 14f, 7.9f, 14f, 9f)
                verticalLineTo(10.5f)
                horizontalLineTo(14.75f)
                curveTo(15.44f, 10.5f, 16f, 11.06f, 16f, 11.75f)
                verticalLineTo(16.25f)
                curveTo(16f, 16.94f, 15.44f, 17.5f, 14.75f, 17.5f)
                horizontalLineTo(9.25f)
                curveTo(8.56f, 17.5f, 8f, 16.94f, 8f, 16.25f)
                verticalLineTo(11.75f)
                curveTo(8f, 11.06f, 8.56f, 10.5f, 9.25f, 10.5f)
                horizontalLineTo(10f)
                verticalLineTo(9f)
                curveTo(10f, 7.9f, 10.9f, 7f, 12f, 7f)
                close()
                // Lock shackle hole
                moveTo(12f, 8.5f)
                curveTo(11.45f, 8.5f, 11f, 8.95f, 11f, 9.5f)
                verticalLineTo(10.5f)
                horizontalLineTo(13f)
                verticalLineTo(9.5f)
                curveTo(13f, 8.95f, 12.55f, 8.5f, 12f, 8.5f)
                close()
                // Keyhole in lock body
                moveTo(12f, 12.5f)
                curveTo(11.45f, 12.5f, 11f, 12.95f, 11f, 13.5f)
                curveTo(11f, 13.91f, 11.25f, 14.26f, 11.6f, 14.41f)
                lineTo(11.25f, 15.75f)
                curveTo(11.18f, 16.02f, 11.39f, 16.25f, 11.66f, 16.25f)
                horizontalLineTo(12.34f)
                curveTo(12.61f, 16.25f, 12.82f, 16.02f, 12.75f, 15.75f)
                lineTo(12.4f, 14.41f)
                curveTo(12.75f, 14.26f, 13f, 13.91f, 13f, 13.5f)
                curveTo(13f, 12.95f, 12.55f, 12.5f, 12f, 12.5f)
                close()
            }
        }.build()
        return _shieldLockIcon!!
    }

private var _shieldLockIcon: ImageVector? = null
