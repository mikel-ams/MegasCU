package com.ams.megascu.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingIndicatorsLabDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Simulated progresses (kept local)
    var linearWavyProgress by remember { mutableFloatStateOf(0.65f) }
    var linearDetProgress by remember { mutableFloatStateOf(0.70f) }

    // Bind parameters from LoadingIndicatorsConfig using local reactive states
    var linearWavyHeight by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearWavyHeight) }
    var linearWavyAmplitude by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearWavyAmplitude) }
    var linearWavyWaveLength by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearWavyWaveLength) }
    var linearWavyStrokeWidth by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearWavyStrokeWidth) }

    var circularWavySize by remember { mutableFloatStateOf(LoadingIndicatorsConfig.circularWavySize) }
    var circularWavyStroke by remember { mutableFloatStateOf(LoadingIndicatorsConfig.circularWavyStroke) }
    var circularWavyAmplitude by remember { mutableFloatStateOf(LoadingIndicatorsConfig.circularWavyAmplitude) }
    var circularWavyWavesCount by remember { mutableIntStateOf(LoadingIndicatorsConfig.circularWavyWavesCount) }

    var linearDetHeight by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearDetHeight) }
    var linearDetCornerRadius by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearDetCornerRadius) }

    var linearIndetHeight by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearIndetHeight) }
    var linearIndetCornerRadius by remember { mutableFloatStateOf(LoadingIndicatorsConfig.linearIndetCornerRadius) }

    var circularStdSize by remember { mutableFloatStateOf(LoadingIndicatorsConfig.circularStdSize) }
    var circularStdStroke by remember { mutableFloatStateOf(LoadingIndicatorsConfig.circularStdStroke) }

    var shapeMorphSize by remember { mutableFloatStateOf(LoadingIndicatorsConfig.shapeMorphSize) }

    // Sync local states back to global config automatically on every slider change
    LaunchedEffect(
        linearWavyHeight, linearWavyAmplitude, linearWavyWaveLength, linearWavyStrokeWidth,
        circularWavySize, circularWavyStroke, circularWavyAmplitude, circularWavyWavesCount,
        linearDetHeight, linearDetCornerRadius,
        linearIndetHeight, linearIndetCornerRadius,
        circularStdSize, circularStdStroke,
        shapeMorphSize
    ) {
        LoadingIndicatorsConfig.linearWavyHeight = linearWavyHeight
        LoadingIndicatorsConfig.linearWavyAmplitude = linearWavyAmplitude
        LoadingIndicatorsConfig.linearWavyWaveLength = linearWavyWaveLength
        LoadingIndicatorsConfig.linearWavyStrokeWidth = linearWavyStrokeWidth

        LoadingIndicatorsConfig.circularWavySize = circularWavySize
        LoadingIndicatorsConfig.circularWavyStroke = circularWavyStroke
        LoadingIndicatorsConfig.circularWavyAmplitude = circularWavyAmplitude
        LoadingIndicatorsConfig.circularWavyWavesCount = circularWavyWavesCount

        LoadingIndicatorsConfig.linearDetHeight = linearDetHeight
        LoadingIndicatorsConfig.linearDetCornerRadius = linearDetCornerRadius

        LoadingIndicatorsConfig.linearIndetHeight = linearIndetHeight
        LoadingIndicatorsConfig.linearIndetCornerRadius = linearIndetCornerRadius

        LoadingIndicatorsConfig.circularStdSize = circularStdSize
        LoadingIndicatorsConfig.circularStdStroke = circularStdStroke

        LoadingIndicatorsConfig.shapeMorphSize = shapeMorphSize
    }

    fun copyConfiguration() {
        val configJson = buildString {
            appendLine("Configuración de Indicadores y Barras de Carga (MegasCU):")
            appendLine("{")
            appendLine("  \"linear_wavy\": {")
            appendLine("    \"height_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearWavyHeight)},")
            appendLine("    \"amplitude_dp\": ${"%.2f".format(LoadingIndicatorsConfig.linearWavyAmplitude)},")
            appendLine("    \"wave_length_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearWavyWaveLength)},")
            appendLine("    \"stroke_width_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearWavyStrokeWidth)}")
            appendLine("  },")
            appendLine("  \"circular_wavy\": {")
            appendLine("    \"size_dp\": ${"%.1f".format(LoadingIndicatorsConfig.circularWavySize)},")
            appendLine("    \"stroke_width_dp\": ${"%.1f".format(LoadingIndicatorsConfig.circularWavyStroke)},")
            appendLine("    \"amplitude_dp\": ${"%.2f".format(LoadingIndicatorsConfig.circularWavyAmplitude)},")
            appendLine("    \"waves_count\": ${LoadingIndicatorsConfig.circularWavyWavesCount}")
            appendLine("  },")
            appendLine("  \"linear_determinate\": {")
            appendLine("    \"height_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearDetHeight)},")
            appendLine("    \"corner_radius_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearDetCornerRadius)}")
            appendLine("  },")
            appendLine("  \"linear_indeterminate\": {")
            appendLine("    \"height_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearIndetHeight)},")
            appendLine("    \"corner_radius_dp\": ${"%.1f".format(LoadingIndicatorsConfig.linearIndetCornerRadius)}")
            appendLine("  },")
            appendLine("  \"circular_standard\": {")
            appendLine("    \"size_dp\": ${"%.1f".format(LoadingIndicatorsConfig.circularStdSize)},")
            appendLine("    \"stroke_width_dp\": ${"%.1f".format(LoadingIndicatorsConfig.circularStdStroke)}")
            appendLine("  },")
            appendLine("  \"shape_morphing\": {")
            appendLine("    \"size_dp\": ${"%.1f".format(LoadingIndicatorsConfig.shapeMorphSize)}")
            appendLine("  }")
            appendLine("}")
        }
        clipboardManager.setText(AnnotatedString(configJson))
        Toast.makeText(context, "Configuración copiada. Pégala en el chat para aplicar estos valores.", Toast.LENGTH_LONG).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surface,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Laboratorio de Indicadores",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Ajuste visual y parámetros en vivo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Cerrar"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { copyConfiguration() }) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copiar Configuración",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .navigationBarsPadding()
                        ) {
                            Button(
                                onClick = { copyConfiguration() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copiar Configuración para el Agente", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. LinearWavyProgressIndicator
                    IndicatorSectionCard(
                        title = "Linear Wavy Progress Indicator",
                        locationDescription = "Dónde se ve: Diálogo de Descarga OTA (UpdateAvailableDialog) y Barra de Actualización de Datos en Pantalla Principal (cuando se activa el modo Wavy).",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LinearWavyProgressIndicator(
                                    progress = linearWavyProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(linearWavyHeight.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    strokeWidth = linearWavyStrokeWidth.dp,
                                    amplitude = linearWavyAmplitude.dp,
                                    waveLength = linearWavyWaveLength.dp
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Alto (height): ${"%.1f".format(linearWavyHeight)} dp",
                                value = linearWavyHeight,
                                valueRange = 4f..24f,
                                onValueChange = { linearWavyHeight = it }
                            )
                            ConfigSlider(
                                label = "Amplitud de Ondas (amplitude): ${"%.2f".format(linearWavyAmplitude)} dp",
                                value = linearWavyAmplitude,
                                valueRange = 0.2f..4.0f,
                                onValueChange = { linearWavyAmplitude = it }
                            )
                            ConfigSlider(
                                label = "Longitud de Onda (waveLength): ${"%.1f".format(linearWavyWaveLength)} dp",
                                value = linearWavyWaveLength,
                                valueRange = 10f..40f,
                                onValueChange = { linearWavyWaveLength = it }
                            )
                            ConfigSlider(
                                label = "Grosor de Trazo (strokeWidth): ${"%.1f".format(linearWavyStrokeWidth)} dp",
                                value = linearWavyStrokeWidth,
                                valueRange = 1f..8f,
                                onValueChange = { linearWavyStrokeWidth = it }
                            )
                            ConfigSlider(
                                label = "Progreso Simulado: ${(linearWavyProgress * 100).toInt()}%",
                                value = linearWavyProgress,
                                valueRange = 0f..1f,
                                onValueChange = { linearWavyProgress = it }
                            )
                        }
                    )

                    // 2. CircularWavyProgressIndicator
                    IndicatorSectionCard(
                        title = "Circular Wavy Progress Indicator",
                        locationDescription = "Dónde se ve: Botón Flotante de Refresco en Pantalla Principal (cuando se selecciona tipo 'Circular Wavy') y estado de carga de sincronización.",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(circularWavySize.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    trackThickness = circularWavyStroke.dp,
                                    cornerSize = circularWavyAmplitude.dp
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Tamaño (size): ${"%.1f".format(circularWavySize)} dp",
                                value = circularWavySize,
                                valueRange = 20f..64f,
                                onValueChange = { circularWavySize = it }
                            )
                            ConfigSlider(
                                label = "Grosor de Trazo (strokeWidth): ${"%.1f".format(circularWavyStroke)} dp",
                                value = circularWavyStroke,
                                valueRange = 1f..8f,
                                onValueChange = { circularWavyStroke = it }
                            )
                            ConfigSlider(
                                label = "Amplitud de Ondas (amplitude): ${"%.2f".format(circularWavyAmplitude)} dp",
                                value = circularWavyAmplitude,
                                valueRange = 0.5f..4.0f,
                                onValueChange = { circularWavyAmplitude = it }
                            )
                            ConfigSlider(
                                label = "Cantidad de Ondas (wavesCount): $circularWavyWavesCount",
                                value = circularWavyWavesCount.toFloat(),
                                valueRange = 4f..16f,
                                onValueChange = { circularWavyWavesCount = it.toInt() }
                            )
                        }
                    )

                    // 3. Linear Determinate (M3)
                    IndicatorSectionCard(
                        title = "Linear Progress Indicator (Determinado)",
                        locationDescription = "Dónde se ve: Barra de Progreso Límite Recomendado en la tarjeta de estadísticas de consumo (UsageHistoryChart.kt).",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LinearProgressIndicator(
                                    progress = linearDetProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(linearDetHeight.dp)
                                        .clip(RoundedCornerShape(linearDetCornerRadius.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Alto (height): ${"%.1f".format(linearDetHeight)} dp",
                                value = linearDetHeight,
                                valueRange = 2f..20f,
                                onValueChange = { linearDetHeight = it }
                            )
                            ConfigSlider(
                                label = "Radio de Esquinas: ${"%.1f".format(linearDetCornerRadius)} dp",
                                value = linearDetCornerRadius,
                                valueRange = 0f..10f,
                                onValueChange = { linearDetCornerRadius = it }
                            )
                            ConfigSlider(
                                label = "Progreso Simulado: ${(linearDetProgress * 100).toInt()}%",
                                value = linearDetProgress,
                                valueRange = 0f..1f,
                                onValueChange = { linearDetProgress = it }
                            )
                        }
                    )

                    // 4. Linear Indeterminate (M3)
                    IndicatorSectionCard(
                        title = "Linear Progress Indicator (Indeterminado)",
                        locationDescription = "Dónde se ve: Pantalla Principal al consultar USSD o refrescar saldos (debajo de la tarjeta principal en Modo Experto y Simple sin Wavy).",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(linearIndetHeight.dp)
                                        .clip(RoundedCornerShape(linearIndetCornerRadius.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Alto (height): ${"%.1f".format(linearIndetHeight)} dp",
                                value = linearIndetHeight,
                                valueRange = 2f..16f,
                                onValueChange = { linearIndetHeight = it }
                            )
                            ConfigSlider(
                                label = "Radio de Esquinas: ${"%.1f".format(linearIndetCornerRadius)} dp",
                                value = linearIndetCornerRadius,
                                valueRange = 0f..8f,
                                onValueChange = { linearIndetCornerRadius = it }
                            )
                        }
                    )

                    // 5. Circular Standard (M3)
                    IndicatorSectionCard(
                        title = "Circular Progress Indicator (Clásico M3)",
                        locationDescription = "Dónde se ve: Botón de refresco principal en pantalla cuando está activo el modo estándar y en diálogos de espera.",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(circularStdSize.dp),
                                    strokeWidth = circularStdStroke.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Tamaño (size): ${"%.1f".format(circularStdSize)} dp",
                                value = circularStdSize,
                                valueRange = 16f..56f,
                                onValueChange = { circularStdSize = it }
                            )
                            ConfigSlider(
                                label = "Grosor de Trazo (strokeWidth): ${"%.1f".format(circularStdStroke)} dp",
                                value = circularStdStroke,
                                valueRange = 1f..8f,
                                onValueChange = { circularStdStroke = it }
                            )
                        }
                    )

                    // 6. Shape Morphing (M3 Expressive)
                    IndicatorSectionCard(
                        title = "Shape Morphing Loading Indicator",
                        locationDescription = "Dónde se ve: Pantalla de carga animada y transiciones de verificación de seguridad.",
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ShapeMorphingLoadingIndicator(
                                    modifier = Modifier.size(shapeMorphSize.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        controls = {
                            ConfigSlider(
                                label = "Tamaño (size): ${"%.1f".format(shapeMorphSize)} dp",
                                value = shapeMorphSize,
                                valueRange = 24f..80f,
                                onValueChange = { shapeMorphSize = it }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun IndicatorSectionCard(
    title: String,
    locationDescription: String,
    previewContent: @Composable () -> Unit,
    controls: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = locationDescription,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Preview Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    previewContent()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sliders Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = controls
            )
        }
    }
}

@Composable
private fun ConfigSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
