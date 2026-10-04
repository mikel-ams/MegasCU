package com.ams.megascu.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ams.megascu.ui.theme.MegasTheme
import com.ams.megascu.ui.theme.SpaceMono
import com.ams.megascu.ui.components.ExpressiveButton
import com.ams.megascu.ui.components.ExpressiveOutlinedButton
import com.ams.megascu.R

abstract class BaseWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.30f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                blurBehindRadius = (24 * resources.displayMetrics.density).toInt()
            }
        }

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_CANCELED, resultValue)

        setContent {
            MegasTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    WidgetConfigScreen(
                        onSave = { param1 ->
                            savePreferences(param1)
                            val appWidgetManager = AppWidgetManager.getInstance(this@BaseWidgetConfigActivity)
                            MegasWidgetProvider.updateAppWidget(
                                this@BaseWidgetConfigActivity,
                                appWidgetManager,
                                appWidgetId,
                                com.ams.megascu.R.layout.widget_megas_2x1
                            )
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun savePreferences(param1: String) {
        val prefs = getSharedPreferences("megas_widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("widget_${appWidgetId}_param1", param1)
            putString("widget_${appWidgetId}_type", param1)
            apply()
        }
    }
}

class WidgetConfigActivity : BaseWidgetConfigActivity()

class Widget2x1ConfigActivity : BaseWidgetConfigActivity()

@Composable
fun WidgetConfigScreen(
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedParam by remember { mutableStateOf("megas") }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxHeight - 32.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Widgets, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                        }
                    }
                    Text("Configurar Widget 2x1", fontWeight = FontWeight.ExtraBold,
                        fontFamily = SpaceMono, style = MaterialTheme.typography.titleMedium)
                }

                Text(
                    "Selecciona el parámetro que se mostrará en el widget compacto 2x1 en tu pantalla de inicio.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = SpaceMono,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Vista previa", style = MaterialTheme.typography.labelLarge,
                    fontFamily = SpaceMono, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary)
                WidgetLivePreviewCard(selectedParam = selectedParam)

                Text("Selecciona la métrica principal", style = MaterialTheme.typography.labelLarge,
                    fontFamily = SpaceMono, fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary)
                ExpressiveOptionGrid(selectedId = selectedParam, onSelected = { selectedParam = it })

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpressiveOutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) {
                        Text("Cancelar", fontWeight = FontWeight.Bold, fontFamily = SpaceMono)
                    }
                    ExpressiveButton(
                        onClick = { onSave(selectedParam) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold, fontFamily = SpaceMono)
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetLivePreviewCard(selectedParam: String) {
    val (label, value, badge) = when (selectedParam) {
        "saldo" -> Triple("Saldo", "1030.64", "21d")
        "llamadas" -> Triple("Minutos", "17:39", "26d")
        "mensajes" -> Triple("SMS", "89 SMS", "26d")
        else -> Triple("Datos", "14.53 GB", "25d")
    }

    // The preview follows the compact home-screen widget's proportions and colors.
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colorResource(R.color.widget_card_bg),
        border = BorderStroke(1.5.dp, colorResource(R.color.widget_card_border)),
        modifier = Modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .aspectRatio(1.8f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f), fontFamily = SpaceMono,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.widget_card_text_secondary))
                Surface(shape = RoundedCornerShape(16.dp), color = colorResource(R.color.widget_card_badge_bg)) {
                    Text(badge, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontFamily = SpaceMono, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.widget_card_text_primary))
                }
                Surface(
                    modifier = Modifier.padding(start = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.widget_card_badge_bg)
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Actualizar",
                        modifier = Modifier.size(30.dp).padding(5.dp),
                        tint = colorResource(R.color.widget_card_text_secondary))
                }
            }
            Text(value, fontFamily = SpaceMono, fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorResource(R.color.widget_card_text_primary))
        }
    }
}

@Composable
fun ExpressiveOptionGrid(
    selectedId: String,
    onSelected: (String) -> Unit
) {
    val options = listOf(
        WidgetOption("megas", "Datos", Icons.Rounded.DataUsage, "Megas"),
        WidgetOption("saldo", "Saldo", Icons.Rounded.MonetizationOn, "Saldo"),
        WidgetOption("llamadas", "Minutos", Icons.Rounded.PhoneInTalk, "Minutos"),
        WidgetOption("mensajes", "SMS", Icons.AutoMirrored.Rounded.Chat, "SMS")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.take(2).forEach { option ->
                ExpressiveOptionCard(
                    option = option,
                    isSelected = selectedId == option.id,
                    onSelect = { onSelected(option.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.drop(2).forEach { option ->
                ExpressiveOptionCard(
                    option = option,
                    isSelected = selectedId == option.id,
                    onSelect = { onSelected(option.id) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ExpressiveOptionCard(
    option: WidgetOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 8.dp else if (isSelected) 22.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "cardCorner"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        label = "cardBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        label = "cardFg"
    )

    Surface(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(interactionSource = interactionSource, indication = null) { onSelect() },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = option.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMono,
                color = contentColor
            )
        }
    }
}

data class WidgetOption(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String
)
