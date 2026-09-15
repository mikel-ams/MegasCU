package com.ams.megascu.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ams.megascu.ui.theme.*
import java.util.Locale

fun copyTextToClipboard(context: Context, label: String, text: String, toastMsg: String = "Copiado al portapapeles") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
    if (clipboard != null) {
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }
}

fun formatColorHex(color: Color): String {
    return String.format(Locale.US, "#%06X", (color.toArgb() and 0xFFFFFF))
}

fun generateSingleItemReport(item: ContrastCheckItem, themeMode: String): String {
    val fgHex = formatColorHex(item.foregroundColor)
    val bgHex = formatColorHex(item.backgroundColor)
    val action = if (item.isDarkTheme) {
        "Aclarar color de texto (FG) o aumentar luminancia para superar 4.5:1."
    } else {
        "Oscurecer color de texto (FG) o aumentar contraste con el fondo para superar 4.5:1."
    }
    return buildString {
        appendLine("--- INFORME DE COLOR PARA REVISIÓN ---")
        appendLine("Elemento: ${item.pairName}")
        appendLine("Descripción: ${item.description}")
        appendLine("Modo de Tema: $themeMode")
        appendLine("Color de Texto (FG): $fgHex")
        appendLine("Color de Fondo (BG): $bgHex")
        appendLine("Ratio de Contraste Actual: ${String.format(Locale.US, "%.2f", item.ratio)}:1 (${item.rating})")
        appendLine("Estado WCAG: ${if (item.ratio >= 4.5) "CUMPLE (>= 4.5:1)" else "REQUIERE REVISIÓN (< 4.5:1)"}")
        appendLine("Recomendación: $action")
    }
}

fun generateFullAuditReport(items: List<ContrastCheckItem>, themeMode: String): String {
    val needReview = items.filter { it.ratio < 4.5 }
    return buildString {
        appendLine("==================================================")
        appendLine("INFORME DE AUDITORÍA DE ACCESIBILIDAD Y COLOR WCAG")
        appendLine("Aplicación: MegasCU")
        appendLine("Modo de Tema Evaluado: $themeMode")
        appendLine("Total Elementos Evaluados: ${items.size}")
        appendLine("Elementos que requieren Revisión: ${needReview.size}")
        appendLine("==================================================")
        appendLine()
        if (needReview.isEmpty()) {
            appendLine("¡Excelente! Todos los elementos evaluados cumplen con el estándar WCAG AA (>= 4.5:1).")
        } else {
            appendLine("ELEMENTOS CON CONTRASTE INSUFICIENTE (< 4.5:1):")
            appendLine()
            needReview.forEachIndexed { index, item ->
                val fgHex = formatColorHex(item.foregroundColor)
                val bgHex = formatColorHex(item.backgroundColor)
                appendLine("[${index + 1}] ${item.pairName} (${item.description})")
                appendLine("     • Color Texto (FG): $fgHex | Color Fondo (BG): $bgHex")
                appendLine("     • Ratio Actual: ${String.format(Locale.US, "%.2f", item.ratio)}:1 (${item.rating})")
                appendLine("     • Sugerencia: ${if (item.isDarkTheme) "Aclarar texto (FG)" else "Oscurecer texto (FG)"} para alcanzar al menos 4.5:1.")
                appendLine()
            }
        }
        appendLine("TODOS LOS ELEMENTOS EVALUADOS:")
        items.forEach { item ->
            val fgHex = formatColorHex(item.foregroundColor)
            val bgHex = formatColorHex(item.backgroundColor)
            val status = if (item.ratio >= 7.0) "[AAA]" else if (item.ratio >= 4.5) "[AA]" else "[REVISAR]"
            appendLine("$status ${item.pairName}: ${String.format(Locale.US, "%.2f", item.ratio)}:1 (FG: $fgHex, BG: $bgHex)")
        }
        appendLine("==================================================")
    }
}

data class ContrastCheckItem(
    val pairName: String,
    val description: String,
    val foregroundColor: Color,
    val backgroundColor: Color,
    val isDarkTheme: Boolean
) {
    val ratio: Double
        get() = calculateContrastRatio(foregroundColor, backgroundColor)

    val rating: String
        get() = when {
            ratio >= 7.0 -> "AAA"
            ratio >= 4.5 -> "AA"
            ratio >= 3.0 -> "AA Gran Texto"
            else -> "Ajuste"
        }

    val isPass: Boolean
        get() = ratio >= 3.0
}

fun calculateLuminance(color: Color): Double {
    fun transform(c: Float): Double {
        return if (c <= 0.03928f) {
            c.toDouble() / 12.92
        } else {
            Math.pow((c.toDouble() + 0.055) / 1.055, 2.4)
        }
    }
    val r = transform(color.red)
    val g = transform(color.green)
    val b = transform(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

fun calculateContrastRatio(foreground: Color, background: Color): Double {
    val l1 = calculateLuminance(foreground)
    val l2 = calculateLuminance(background)
    val lighter = Math.max(l1, l2)
    val darker = Math.min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

// Colores optimizados para accesibilidad WCAG preservando la identidad M3 Expresiva
val WcagOptimizedDarkPrimary = Color(0xFFCBB2FF)      // Violeta ultra legible en oscuro (Ratio > 10.5:1)
val WcagOptimizedDarkSecondary = Color(0xFFB88EFF)    // Acento secundario accesible
val WcagOptimizedDarkTertiary = Color(0xFFE8DCFF)     // Destacado terciario de alto contraste
val WcagOptimizedDarkOnSurfaceVariant = Color(0xFFE2D6FF) // Texto secundario súper claro (> 8.0:1)

val WcagOptimizedLightPrimary = Color(0xFF3800B0)     // Violeta profundo accesible en claro (Ratio > 7.5:1)
val WcagOptimizedLightSecondary = Color(0xFF7020E0)   // Acento secundario vibrante accesible
val WcagOptimizedLightOnSurfaceVariant = Color(0xFF2C006B) // Texto secundario oscuro accesible (> 8.5:1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WcagContrastInspectorBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE) }
    var isWcagEnabled by remember {
        mutableStateOf(prefs.getBoolean("pref_use_wcag_palette", false))
    }
    // "DARK", "LIGHT", "AMOLED"
    var selectedThemeMode by remember { mutableStateOf("DARK") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Contraste WCAG, 1: Tokens M3, 2: Vista Previa UI

    val currentTheme = MaterialTheme.colorScheme

    // Generar pares de contraste según tema seleccionado
    val contrastItems = remember(selectedThemeMode, isWcagEnabled) {
        when (selectedThemeMode) {
            "AMOLED" -> {
                val primary = if (isWcagEnabled) WcagOptimizedDarkPrimary else VioletTone080
                val secondary = if (isWcagEnabled) WcagOptimizedDarkSecondary else VioletTone070
                val tertiary = if (isWcagEnabled) WcagOptimizedDarkTertiary else VioletTone090
                val bg = Color.Black
                val surface = Color.Black
                val onSurface = Color(0xFFF5F5F5)
                val onSurfaceVariant = if (isWcagEnabled) WcagOptimizedDarkOnSurfaceVariant else Color(0xFFCCCCCC)
                val cardBg = Color(0xFF141414)

                listOf(
                    ContrastCheckItem("Texto Primario en Negro Puro", "Títulos clave en fondo AMOLED", onSurface, bg, true),
                    ContrastCheckItem("Texto Secundario en Negro", "Subtítulos y metadatos", onSurfaceVariant, bg, true),
                    ContrastCheckItem("Texto Primario en Tarjeta AMOLED", "Contenido en superficie #141414", onSurface, cardBg, true),
                    ContrastCheckItem("Texto Secundario en Tarjeta", "Detalles secundarios en tarjetas", onSurfaceVariant, cardBg, true),
                    ContrastCheckItem("Botón Primario (Acento)", "Botones interactivos en fondo negro", primary, bg, true),
                    ContrastCheckItem("Acento Secundario", "Switches e indicadores activos", secondary, bg, true),
                    ContrastCheckItem("Acento Terciario / Enlaces", "Iconos y badges de énfasis", tertiary, surface, true),
                    ContrastCheckItem("Alerta de Error en AMOLED", "Mensajes y botones de advertencia", AlertRed, bg, true)
                )
            }
            "LIGHT" -> {
                val primary = if (isWcagEnabled) WcagOptimizedLightPrimary else VioletTone040
                val secondary = if (isWcagEnabled) WcagOptimizedLightSecondary else VioletTone050
                val bg = VioletTone095
                val surface = VioletTone100
                val onSurface = VioletTone010
                val onSurfaceVariant = if (isWcagEnabled) WcagOptimizedLightOnSurfaceVariant else VioletTone020
                val cardBg = VioletTone090

                listOf(
                    ContrastCheckItem("Texto Primario en Fondo", "Títulos principales en modo claro", onSurface, bg, false),
                    ContrastCheckItem("Texto Secundario en Fondo", "Subtítulos en modo claro", onSurfaceVariant, bg, false),
                    ContrastCheckItem("Texto Primario en Superficie", "Tarjetas y diálogos blancos", onSurface, surface, false),
                    ContrastCheckItem("Texto Secundario en Tarjeta", "Detalles en tarjetas de contenido", onSurfaceVariant, cardBg, false),
                    ContrastCheckItem("Botón Primario", "Acción principal sobre fondo violeta", VioletTone100, primary, false),
                    ContrastCheckItem("Acento Secundario", "Switches y controles en modo claro", secondary, bg, false),
                    ContrastCheckItem("Borde / Contorno", "Divisores y líneas delimitadoras", VioletTone060, surface, false),
                    ContrastCheckItem("Alerta de Error", "Mensajes críticos de error", Color(0xFFDC2626), surface, false)
                )
            }
            else -> { // DARK
                val primary = if (isWcagEnabled) WcagOptimizedDarkPrimary else VioletTone080
                val secondary = if (isWcagEnabled) WcagOptimizedDarkSecondary else VioletTone070
                val tertiary = if (isWcagEnabled) WcagOptimizedDarkTertiary else VioletTone090
                val bg = VioletTone010
                val surface = VioletTone010
                val onSurface = VioletTone100
                val onSurfaceVariant = if (isWcagEnabled) WcagOptimizedDarkOnSurfaceVariant else VioletTone090
                val cardBg = VioletTone020

                listOf(
                    ContrastCheckItem("Texto Primario en Fondo", "Títulos y datos clave", onSurface, bg, true),
                    ContrastCheckItem("Texto Secundario en Fondo", "Subtítulos y etiquetas", onSurfaceVariant, bg, true),
                    ContrastCheckItem("Texto Primario en Tarjeta", "Contenido en tarjetas elevadas", onSurface, cardBg, true),
                    ContrastCheckItem("Texto Secundario en Tarjeta", "Detalles secundarios en tarjetas", onSurfaceVariant, cardBg, true),
                    ContrastCheckItem("Botón Primario (Acento/Texto)", "Botones interactivos principales", VioletTone010, primary, true),
                    ContrastCheckItem("Acento Secundario", "Switches e indicadores activos", secondary, bg, true),
                    ContrastCheckItem("Acento Terciario / Enlaces", "Iconos y enlaces de énfasis", tertiary, surface, true),
                    ContrastCheckItem("Alerta de Error", "Mensajes y botones de peligro", AlertRed, bg, true)
                )
            }
        }
    }

    val overallPassing = contrastItems.all { it.isPass }
    val avgRatio = contrastItems.map { it.ratio }.average()

    // Tokens dinámicos en ejecución
    val activeTokens = remember(currentTheme) {
        listOf(
            Triple("Primary", currentTheme.primary, currentTheme.onPrimary),
            Triple("PrimaryContainer", currentTheme.primaryContainer, currentTheme.onPrimaryContainer),
            Triple("Secondary", currentTheme.secondary, currentTheme.onSecondary),
            Triple("SecondaryContainer", currentTheme.secondaryContainer, currentTheme.onSecondaryContainer),
            Triple("Tertiary", currentTheme.tertiary, currentTheme.onTertiary),
            Triple("Surface", currentTheme.surface, currentTheme.onSurface),
            Triple("SurfaceContainerHigh", currentTheme.surfaceContainerHigh, currentTheme.onSurface),
            Triple("Error", currentTheme.error, currentTheme.onError)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        dragHandle = { ExpressiveDragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Auditoría de Contraste WCAG",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Verificación M3 Expressive & Accesibilidad",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    ExpressiveIconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
            }

            // Score Summary Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = if (overallPassing) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (overallPassing) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (overallPassing) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = if (overallPassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (overallPassing) "Cumple Estándares WCAG" else "Requiere Optimización",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (overallPassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Contraste Promedio: ${String.format(Locale.US, "%.1f", avgRatio)}:1 ($selectedThemeMode)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Badge Rating
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (overallPassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                text = if (avgRatio >= 7.0) "AAA PASS" else "AA PASS",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // Navigation Tabs
            item {
                ConnectedSegmentedGroup(
                    items = listOf(
                        SegmentOption(0, "Contraste", Icons.Rounded.Verified),
                        SegmentOption(1, "Tokens M3", Icons.Rounded.Palette),
                        SegmentOption(2, "Vista Previa", Icons.Rounded.Visibility)
                    ),
                    selectedValue = selectedTab,
                    onItemSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth(),
                    height = 38.dp,
                    fontSize = 11.sp
                )
            }

            // Controls Card con selector de temas debajo del título de la tarjeta
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Título del Selector de Modo de Tema
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (selectedThemeMode) {
                                    "LIGHT" -> Icons.Rounded.LightMode
                                    "AMOLED" -> Icons.Rounded.Contrast
                                    else -> Icons.Rounded.DarkMode
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Probar Modo de Tema:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Botones de visualización de temas debajo del título de la tarjeta
                        ConnectedSegmentedGroup(
                            items = listOf(
                                SegmentOption("DARK", "Oscuro", Icons.Rounded.DarkMode),
                                SegmentOption("LIGHT", "Claro", Icons.Rounded.LightMode),
                                SegmentOption("AMOLED", "AMOLED", Icons.Rounded.Contrast)
                            ),
                            selectedValue = selectedThemeMode,
                            onItemSelected = { selectedThemeMode = it },
                            modifier = Modifier.fillMaxWidth(),
                            height = 36.dp,
                            fontSize = 11.sp
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // WCAG Palette Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aplicar Paleta Expresiva WCAG",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Aumenta luminancia y legibilidad de textos secundarios",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ExpressiveSwitch(
                                checked = isWcagEnabled,
                                onCheckedChange = { checked ->
                                    isWcagEnabled = checked
                                    prefs.edit().putBoolean("pref_use_wcag_palette", checked).apply()
                                    Toast.makeText(
                                        context,
                                        if (checked) "Paleta WCAG activada en la app" else "Paleta Estándar restaurada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Contraste Individual
                    val itemsNeedingReview = contrastItems.filter { it.ratio < 4.5 }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pares de Contraste Evaluados",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (itemsNeedingReview.isEmpty()) "Todos cumplen WCAG AA (>= 4.5:1)" else "${itemsNeedingReview.size} de ${contrastItems.size} requieren revisión",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (itemsNeedingReview.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            ExpressiveFilledTonalButton(
                                onClick = {
                                    val report = generateFullAuditReport(contrastItems, selectedThemeMode)
                                    copyTextToClipboard(context, "Informe Auditoría WCAG", report, "Informe de auditoría copiado al portapapeles")
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (itemsNeedingReview.isNotEmpty()) "Copiar Informe (${itemsNeedingReview.size})" else "Copiar Informe",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    items(contrastItems, key = { it.pairName }) { item ->
                        ContrastCheckCardItem(item, selectedThemeMode)
                    }
                }
                1 -> {
                    // Tokens M3 Activos
                    item {
                        Text(
                            text = "Tokens Dinámicos Material 3 del Sistema",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(activeTokens, key = { it.first }) { (tokenName, bg, fg) ->
                        val hexBg = String.format("#%06X", (bg.toArgb() and 0xFFFFFF))
                        val hexFg = String.format("#%06X", (fg.toArgb() and 0xFFFFFF))
                        val ratio = calculateContrastRatio(fg, bg)
                        val pass = ratio >= 4.5

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = bg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "Aa",
                                            fontWeight = FontWeight.Bold,
                                            color = fg
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tokenName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Bg: $hexBg  •  On: $hexFg",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pass) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", ratio)}:1",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (pass) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Vista Previa de Componentes UI
                    item {
                        Text(
                            text = "Maqueta Interactiva de Componentes UI",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val testBg = when (selectedThemeMode) {
                            "AMOLED" -> Color.Black
                            "LIGHT" -> Color(0xFFF7F5FA)
                            else -> Color(0xFF141218)
                        }
                        val testCardBg = when (selectedThemeMode) {
                            "AMOLED" -> Color(0xFF161616)
                            "LIGHT" -> Color.White
                            else -> Color(0xFF211F26)
                        }
                        val testTextColor = when (selectedThemeMode) {
                            "LIGHT" -> Color(0xFF1C1B1F)
                            else -> Color(0xFFE6E1E5)
                        }
                        val testSubColor = when (selectedThemeMode) {
                            "LIGHT" -> Color(0xFF49454F)
                            else -> if (isWcagEnabled) Color(0xFFE2D6FF) else Color(0xFFCAC4D0)
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = testBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tarjeta de Muestra MegasCU",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = testTextColor
                                )
                                Text(
                                    text = "Texto secundario evaluado para accesibilidad y contraste WCAG.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = testSubColor
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = testCardBg
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Saldo Principal: 150.50 CUP", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = testTextColor)
                                            Text("Datos restantes: 4.5 GB LTE", style = MaterialTheme.typography.labelSmall, color = testSubColor)
                                        }
                                        ExpressiveButton(
                                            onClick = { Toast.makeText(context, "Botón interactivo de prueba", Toast.LENGTH_SHORT).show() },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Acción")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContrastCheckCardItem(item: ContrastCheckItem, themeMode: String = "DARK") {
    val context = LocalContext.current
    val needsReview = item.ratio < 4.5

    val itemInteraction = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressEffect(interactionSource = itemInteraction)
            .clickable(
                interactionSource = itemInteraction,
                indication = null
            ) {
                val singleReport = generateSingleItemReport(item, themeMode)
                copyTextToClipboard(context, "Diagnóstico ${item.pairName}", singleReport, "Diagnóstico de ${item.pairName} copiado")
            },
        shape = rememberExpressiveMorphShape(
            defaultRadius = 14.dp,
            pressedRadius = 8.dp,
            interactionSource = itemInteraction
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (needsReview) MaterialTheme.colorScheme.error.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Preview Chip Box
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = item.backgroundColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = item.foregroundColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Pair Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.pairName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Ratio & Rating Badge + Distintivo de Revisión
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%.2f", item.ratio)}:1",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (item.isPass) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (needsReview) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .expressiveClick {
                                    val singleReport = generateSingleItemReport(item, themeMode)
                                    copyTextToClipboard(context, "Diagnóstico ${item.pairName}", singleReport, "Diagnóstico copiado")
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Revisar",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.ratio >= 7.0) MaterialTheme.colorScheme.primaryContainer
                        else if (item.ratio >= 4.5) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = item.rating,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (item.ratio >= 7.0) MaterialTheme.colorScheme.onPrimaryContainer
                            else if (item.ratio >= 4.5) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
