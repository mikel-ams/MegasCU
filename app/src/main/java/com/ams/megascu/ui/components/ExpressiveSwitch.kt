package com.ams.megascu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive switch. The official Switch owns semantics, touch handling,
 * motion and shape transitions; MegasCU supplies thicker thumb icons with rounded ends.
 */
@Composable
fun ExpressiveSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showThumbIcons: Boolean = true,
    checkedIcon: ImageVector = Icons.Rounded.Check,
    uncheckedIcon: ImageVector = Icons.Rounded.Close
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline
        ),
        thumbContent = if (showThumbIcons) {
            {
                val tint = if (checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                }

                if (checked && checkedIcon == Icons.Rounded.Check) {
                    // Indicador de Check con mayor grosor y extremos redondeados
                    Canvas(modifier = Modifier.size(SwitchDefaults.IconSize)) {
                        val strokeWidth = 2.4.dp.toPx()
                        val path = Path().apply {
                            moveTo(size.width * 0.22f, size.height * 0.52f)
                            lineTo(size.width * 0.42f, size.height * 0.72f)
                            lineTo(size.width * 0.78f, size.height * 0.32f)
                        }
                        drawPath(
                            path = path,
                            color = tint,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                } else if (!checked && uncheckedIcon == Icons.Rounded.Close) {
                    // Indicador de Close (X) con mayor grosor y extremos redondeados
                    Canvas(modifier = Modifier.size(SwitchDefaults.IconSize)) {
                        val strokeWidth = 2.4.dp.toPx()
                        drawLine(
                            color = tint,
                            start = Offset(size.width * 0.28f, size.height * 0.28f),
                            end = Offset(size.width * 0.72f, size.height * 0.72f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = tint,
                            start = Offset(size.width * 0.72f, size.height * 0.28f),
                            end = Offset(size.width * 0.28f, size.height * 0.72f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    val icon = if (checked) checkedIcon else uncheckedIcon
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            }
        } else null
    )
}
