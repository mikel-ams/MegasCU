package com.ams.megascu.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ams.megascu.widget.MegasWidget2x1Provider
import com.ams.megascu.widget.MegasWidgetProvider
import kotlinx.coroutines.delay
import kotlin.random.Random

object WidgetPromptManager {
    private const val PREFS_NAME = "megascu_widget_prompt_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch_timestamp"
    private const val KEY_LAST_PROMPT = "last_prompt_timestamp"

    fun isWidgetInstalled(context: Context): Boolean {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widget4x2 = ComponentName(context, MegasWidgetProvider::class.java)
            val widget2x1 = ComponentName(context, MegasWidget2x1Provider::class.java)
            val ids4x2 = appWidgetManager.getAppWidgetIds(widget4x2)
            val ids2x1 = appWidgetManager.getAppWidgetIds(widget2x1)
            ids4x2.isNotEmpty() || ids2x1.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun markPromptShown(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_PROMPT, System.currentTimeMillis()).apply()
    }

    fun shouldShowPrompt(context: Context): Boolean {
        if (isWidgetInstalled(context)) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        if (firstLaunch == 0L) {
            firstLaunch = now
            prefs.edit().putLong(KEY_FIRST_LAUNCH, firstLaunch).apply()
        }

        val lastPrompt = prefs.getLong(KEY_LAST_PROMPT, 0L)
        val timeSinceFirstLaunch = now - firstLaunch
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L

        if (lastPrompt == 0L) {
            // First time: trigger after first 1-2 minutes of usage
            return timeSinceFirstLaunch >= 60_000L
        }

        // Subsequent times: check if 3+ days passed
        val timeSinceLastPrompt = now - lastPrompt
        return timeSinceLastPrompt >= threeDaysMs
    }
}

@Composable
fun WidgetPromptController() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Wait 45-75 seconds after app open before checking
        val initialDelay = Random.nextLong(45_000L, 75_000L)
        delay(initialDelay)

        if (WidgetPromptManager.shouldShowPrompt(context)) {
            showDialog = true
            WidgetPromptManager.markPromptShown(context)
        }
    }

    if (showDialog) {
        WidgetPromptDialog(
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun WidgetPromptDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = spring(stiffness = 380f)) + scaleIn(initialScale = 0.78f, animationSpec = spring(dampingRatio = 0.62f, stiffness = 340f)),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }

                        ExpressiveIconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cerrar"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¡Añade el Widget a tu Pantalla de Inicio!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Consulta tu saldo, megas y bono nacional al instante sin abrir la app.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Stylized Preview Card of MegasCU Widget
                    WidgetCardPreview()

                    Spacer(modifier = Modifier.height(18.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
                        val myProvider = remember { ComponentName(context, MegasWidgetProvider::class.java) }

                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            ExpressiveButton(
                                onClick = {
                                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Añadir Widget Automáticamente",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    ExpressiveOutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Entendido, lo haré luego")
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetCardPreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.5f),
                        Color(0xFFC084FC).copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0066))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row inside preview widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saldo Principal",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFD8D2E3)
                    )
                    Text(
                        text = "816.44 CUP",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF5B1FE6)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Actualizar",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column: Datos & Llamadas
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Datos Disponibles",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD8D2E3)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "9.59 GB",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF5B1FE6)
                        ) {
                            Text(
                                text = "28d",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Llamadas (Voz)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD8D2E3)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "00:51:28",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF5B1FE6)
                        ) {
                            Text(
                                text = "28d",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Right column: Bono .CU & SMS
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bono Datos .CU",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD8D2E3)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "300 MB",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF5B1FE6)
                        ) {
                            Text(
                                text = "28d",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mensajes",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD8D2E3)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "96 SMS",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF5B1FE6)
                        ) {
                            Text(
                                text = "28d",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer pill tag inside preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MegasCU (Estándar 4x2)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}
