package com.ams.megascu.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ChangeCategory(
    val label: String
) {
    ADDED("Añadido"),
    CHANGED("Cambiado"),
    DEPRECATED("Obsoleto"),
    REMOVED("Eliminado"),
    FIXED("Corregido"),
    SECURITY("Seguridad")
}

data class ChangelogSection(
    val category: ChangeCategory,
    val items: List<String>
)

data class ChangelogVersion(
    val version: String,
    val date: String,
    val sections: List<ChangelogSection>,
    val isLatest: Boolean = false
)

object ChangelogRepository {
    @Volatile
    private var cachedList: List<ChangelogVersion>? = null

    private val defaultFallback = listOf(
        ChangelogVersion(
            version = "0.9.4-beta_(262)",
            date = "2026-10-04",
            isLatest = true,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Historial para consumo real: Las consultas de datos guardan observaciones verificadas incluso con saldo sin cambios. La migración de base de datos 8→9 conserva el historial previo y separa las nuevas mediciones de datos de las copias de saldo en caché. La gráfica estima disminuciones de datos generales y LTE de la misma SIM tras consultas en al menos dos fechas distintas, sin inventar puntos ni contar recargas como consumo.",
                        "Acceso a estadísticas de Android: Lectura compartida del tráfico móvil cuando está disponible, diferenciando cero consumo de estadísticas inaccesibles y ofreciendo acceso al permiso desde la app y el widget sin historial."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Configuración del widget 2x1: Ventana flotante con el launcher desenfocado y una vista previa que reproduce las proporciones y el estilo del widget compacto.",
                        "Consultas y persistencia unificadas: App, sincronización periódica y widgets usan el mismo guardado transaccional de estado e historial; se evita mezclar registros o caché de distintas SIM.",
                        "Gráficas transparentes: Se indican las estimaciones por historial, los últimos siete días y el estado de recopilación inicial. La comprobación SHA-256 continúa desactivada y su lógica se conserva."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Chips en widgets compactos: El fondo de los indicadores se ajusta al ancho real del texto Space Mono en 3x1 y 4x1; el control de actualización también se presenta en un chip sin cambiar su función.",
                        "Éxito aparente en consultas: El resultado final distingue actualización completa, parcial y fallida e identifica las consultas pendientes. Se valida la respuesta antes de guardar y el éxito se muestra después del guardado. Se reintentan fallos en la actualización de la app sin tratar saldos cero como error.",
                        "Actualización de widgets: Los botones ejecutan consultas mediante WorkManager, respetan la SIM configurada y notifican el resultado. Se evita depender de la duración limitada del receptor y se limpia la caché al borrar los datos.",
                        "Space Mono en widgets: Los textos de los widgets compactos y de resumen se renderizan con la fuente incluida, preservando tamaños, colores, distribución, controles y accesibilidad.",
                        "Tarjetas del historial de cambios: Una sola transición controla la altura al abrir y contraer las tarjetas, moviendo las siguientes durante toda la animación y evitando el salto por animaciones superpuestas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.9.3-beta_(260)",
            date = "2026-09-27",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Fondo Desenfocado y Fades en Modales: Efecto de desenfoque de fondo dinámico (Blur) en ventanas de actualizaciones y difuminados progresivos gaussianos (progressiveBlur) superior e inferior en la hoja modal de Acción Rápida.",
                        "Búsqueda Automatizada de Actualizaciones e Instalación OTA: Verificación silenciosa en segundo plano al iniciar la app, persistencia de indicadores rojos de alerta en ajustes e inicio automático del instalador tras la descarga.",
                        "Línea de Tiempo y Separación Ajustada en Historial: Timeline con puntos y líneas continuas para versiones anteriores, separación ajustada de 2dp y puntos rellenos para versiones estables.",
                        "Acceso Directo al Repositorio Oficial: Enlace oficial con icono representativo a GitHub Releases en la ventana Acerca de."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Tipografía Space Mono y Refinamiento de Widgets: Aplicación consistente de la fuente Space Mono en todos los widgets y pantallas de configuración, alineación visual a la tarjeta de estado y actualización en tiempo real desde widgets.",
                        "Pulsación Expressive y Microinteracciones: Animaciones táctiles de rebote elástico en botones, versión, enlaces de soporte y acción rápida sin desalineaciones ni hundimiento.",
                        "Rediseño de Iconos y Formas: Iconos de consultas rápidas sin contornos para un aspecto minimalista y forma de galleta lobulada de 9 lados en el éxito de bienvenida (Cookie9LadosShape).",
                        "Indicadores de Progreso y Descarga: Sustitución de la barra de límite diario por un indicador lineal estándar de Material 3 (LinearProgressIndicator Determinate), ondas suaves en la barra de descarga y ocultamiento fluido de notas durante la descarga activa.",
                        "Optimización y Limpieza en Actualizaciones: Módulo de actualización optimizado sin verificaciones redundantes para agilizar la instalación manual y eliminación de etiquetas de firma innecesarias."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Descarga OTA y Compatibilidad de Redirecciones: Enrutamiento y validación de URLs optimizado para garantizar compatibilidad con redirecciones dinámicas de GitHub Releases y AWS S3.",
                        "Transición Fluida en Tarjetas del Historial: Animación de apertura y cierre fluida en tarjetas colapsables del historial sin saltos ni tirones visuales."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.9.1-beta_(257)",
            date = "2026-09-24",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Punto de Notificación de Compra y Recarga (< 5 días): Indicador visual de alerta cuando restan 5 días o menos para recargar saldo o renovar paquetes de datos.",
                        "Tarjeta de Alerta M3 Expressive en Menú de Compras: Tarjeta de advertencia para compra de planes y saldo con conteo dinámico de días y botón directo a Transfermóvil.",
                        "Sistema de Alerta Unificada: Detección consolidada cuando coinciden la recarga de saldo principal y el vencimiento inminente de paquetes.",
                        "Acceso a Compras desde Historial USSD: Botón directo a Compras en el diálogo de resultado USSD tras consultar el Historial de Recargas (*222*732#).",
                        "Acción Rápida \"Historial de Recargas\": Integración de la consulta *222*732# en el selector de acciones rápidas de la pantalla principal."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Rediseño de Tarjeta de Recarga de Saldo: Tono rojo distintivo, icono de pagos, descripción detallada y botón unificado para abrir Transfermóvil.",
                        "Disipación Continua de Desenfoque en Cierre de Ventanas: Sincronización del efecto de desenfoque con la posición física del modal hasta disiparse por completo antes del cierre.",
                        "Difuminado Progresivo con Scroll en Compras: Aparición gradual del desenfoque superior al desplazarse en el catálogo de compras.",
                        "Optimización Integral con R8 en Modo Completo: Minificación R8 Full Mode y reducción agresiva de recursos para maximizar el rendimiento."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Fluidez y Persistencia Visual en Cierre de Modales: Eliminación de saltos bruscos y pérdida prematura del desenfoque al deslizar las ventanas hacia abajo.",
                        "Desenfoque de Fondo en Ventana Acerca de: Corrección de la renderización del efecto desenfoque gaussiano limpio en la capa posterior.",
                        "Detección y Formato de Disponibilidad de Recarga: Reconocimiento de los plazos de 30 días en el analizador USSD y actualización al estado \"Puede recargar saldo\"."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.9.0-beta_(255)",
            date = "2026-09-20",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Componentes y Sistema Visual Material 3 Expressive: Integración del tema expresivo nativo (MaterialExpressiveTheme y MotionScheme.expressive()), chips dinámicos de vigencia (ExpressiveDaysChip), indicadores de progreso lineales y recursos visuales WebP en alta definición para la selección de modo en la pantalla de bienvenida.",
                        "Soporte Markdown en Actualizaciones: Renderizado nativo de Markdown estructurado (MarkdownChangelog) en la ventana de actualización (UpdateAvailableDialog) para visualizar notas de versión completas.",
                        "Gestos Predictivos y Adaptabilidad Multidispositivo: Integración de gestos predictivos de retroceso (Predictive Back Gestures) con SheetProgressTracker y BackHandler, junto con límites de ancho responsivos (widthIn(max = 680.dp)) adaptados a teléfonos, plegables y tablets.",
                        "Analizador USSD Extendido: Soporte para formatos de tiempo con minutos y segundos (MM:SS), duraciones compuestas y valores numéricos decimales."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Estandarización de Interfaz y Zonas Táctiles: Normalización y simetría de márgenes en la barra de navegación flotante (MegasBottomBar), grupos de tarjetas segmentadas (ConnectedSegmentedGroup) y áreas de interacción táctil con mínimo de 48dp x 48dp conforme a WCAG 2.2.",
                        "Optimización de Compilación y Rendimiento: Activación de optimización R8 en Full Mode, compresión DEX y saneamiento integral de dependencias en Gradle Version Catalog, reduciendo el tamaño del APK a ~2.3 MB.",
                        "Almacenamiento y Esquema de Base de Datos: Migración de base de datos a versión 8, optimizando el esquema interno sin persistencia redundante de respuestas USSD en texto plano.",
                        "Insignias Visuales: Sustitución de etiquetas de texto en el diálogo de actualización por iconos vectoriales Material Symbols (PhoneAndroid y CloudDownload)."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Consistencia Visual en Tarjetas: Corrección de alineación y espaciado en los chips de días restantes y etiquetas métricas en las tarjetas de estado de la pantalla principal.",
                        "Resolución SIM y Análisis USSD: Corrección en la detección de ranuras SIM y procesamiento de respuestas USSD con formatos no estándar.",
                        "Estabilidad de Botones Interactivos: Corrección de la escala tipográfica en botones durante la pulsación, manteniendo la respuesta háptica táctil y la fluidez en transiciones."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.SECURITY,
                    items = listOf(
                        "Firma Oficial de Producción: Firma criptográfica con almacén oficial de claves (release-key.jks) con cifrado RSA de 4096 bits y esquemas duales v1 y v2.",
                        "Validación Criptográfica de Actualizaciones: Módulo ApkSecurityValidator para verificación previa de hashes SHA-256 de GitHub Releases, dominios HTTPS autorizados y correspondencia de certificados antes de proceder con la instalación.",
                        "Protección de PIN y Bloqueo por Keystore: Almacenamiento seguro de credenciales y control de intentos fallidos respaldado por hardware mediante Android Keystore.",
                        "Privacidad y Eliminación de Telemetría: Retiro total de librerías y componentes innecesarios, garantizando procesamiento local de datos 100% privado en el dispositivo."
                    )
                )
            )
        )
    )

    val changelogList: List<ChangelogVersion>
        get() = cachedList ?: defaultFallback

    fun getChangelogList(context: Context): List<ChangelogVersion> {
        cachedList?.let { return it }
        return synchronized(this) {
            cachedList?.let { return it }
            val loaded = loadFromAssets(context)
            if (loaded.isNotEmpty()) {
                cachedList = loaded
                loaded
            } else {
                defaultFallback
            }
        }
    }

    fun loadFromAssets(context: Context): List<ChangelogVersion> {
        return try {
            val content = context.assets.open("CHANGELOG.md").use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            }
            parseMarkdown(content)
        } catch (e: Exception) {
            android.util.Log.e("ChangelogRepository", "Error loading CHANGELOG.md from assets", e)
            emptyList()
        }
    }

    fun parseMarkdown(markdown: String): List<ChangelogVersion> {
        val lines = markdown.replace("\r\n", "\n").replace("\r", "\n").split("\n")
        val versions = mutableListOf<ChangelogVersion>()

        var currentVersionName: String? = null
        var currentDate: String = ""
        var currentCategory: ChangeCategory? = null
        val currentSections = mutableListOf<ChangelogSection>()
        val currentItems = mutableListOf<String>()
        val currentItemBuffer = StringBuilder()

        fun flushItem() {
            if (currentItemBuffer.isNotEmpty()) {
                val item = currentItemBuffer.toString().trim()
                if (item.isNotEmpty()) {
                    currentItems.add(item)
                }
                currentItemBuffer.clear()
            }
        }

        fun flushSection() {
            flushItem()
            if (currentCategory != null && currentItems.isNotEmpty()) {
                currentSections.add(ChangelogSection(currentCategory!!, currentItems.toList()))
            }
            currentCategory = null
            currentItems.clear()
        }

        fun flushVersion() {
            flushSection()
            if (currentVersionName != null) {
                val isLatest = versions.isEmpty()
                versions.add(
                    ChangelogVersion(
                        version = currentVersionName!!,
                        date = currentDate,
                        sections = currentSections.toList(),
                        isLatest = isLatest
                    )
                )
            }
            currentVersionName = null
            currentDate = ""
            currentSections.clear()
        }

        val versionRegex = Regex("""^##\s*\[?([^\]\s]+)\]?\s*-\s*(\d{4}-\d{2}-\d{2})?""")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("## ")) {
                flushVersion()
                val match = versionRegex.find(trimmed)
                if (match != null) {
                    currentVersionName = match.groupValues[1]
                    currentDate = match.groupValues.getOrNull(2) ?: ""
                } else {
                    currentVersionName = trimmed.removePrefix("##").trim().removePrefix("[").substringBefore("]")
                    currentDate = ""
                }
            } else if (trimmed.startsWith("### ")) {
                flushSection()
                val catText = trimmed.removePrefix("###").trim().lowercase()
                currentCategory = when {
                    catText.startsWith("añadido") || catText.startsWith("added") -> ChangeCategory.ADDED
                    catText.startsWith("cambiado") || catText.startsWith("changed") -> ChangeCategory.CHANGED
                    catText.startsWith("corregido") || catText.startsWith("fixed") -> ChangeCategory.FIXED
                    catText.startsWith("seguridad") || catText.startsWith("security") -> ChangeCategory.SECURITY
                    catText.startsWith("obsoleto") || catText.startsWith("deprecated") -> ChangeCategory.DEPRECATED
                    catText.startsWith("eliminado") || catText.startsWith("removed") -> ChangeCategory.REMOVED
                    else -> ChangeCategory.CHANGED
                }
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                flushItem()
                currentItemBuffer.append(trimmed.substring(2).trim())
            } else if (trimmed.startsWith("---")) {
                flushSection()
            } else if (trimmed.isNotEmpty() && currentCategory != null && currentItemBuffer.isNotEmpty()) {
                currentItemBuffer.append(" ").append(trimmed)
            }
        }
        flushVersion()
        return versions
    }
}

private fun isBetaVersion(version: String): Boolean {
    val lower = version.lowercase()
    return lower.contains("beta") || lower.contains("alpha") || lower.contains("rc") || lower.contains("dev")
}

private fun getCategoryCountLabel(category: ChangeCategory, count: Int): String {
    return when (category) {
        ChangeCategory.ADDED -> if (count == 1) "Añadido" else "Añadidos"
        ChangeCategory.CHANGED -> if (count == 1) "Cambio" else "Cambios"
        ChangeCategory.FIXED -> if (count == 1) "Corrección" else "Correcciones"
        ChangeCategory.SECURITY -> "Seguridad"
        ChangeCategory.DEPRECATED -> if (count == 1) "Obsoleto" else "Obsoletos"
        ChangeCategory.REMOVED -> if (count == 1) "Eliminado" else "Eliminados"
    }
}

@Composable
private fun getCategoryColors(category: ChangeCategory): Pair<Color, Color> {
    return when (category) {
        ChangeCategory.ADDED -> Pair(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary
        )
        ChangeCategory.CHANGED -> {
            val isDark = isSystemInDarkTheme()
            val textColor = if (isDark) Color(0xFFE8DDFF) else Color(0xFF140033)
            val bgColor = if (isDark) {
                Color(0xFF381E72).copy(alpha = 0.55f)
            } else {
                Color(0xFFE8DEF8).copy(alpha = 0.85f)
            }
            Pair(bgColor, textColor)
        }
        ChangeCategory.DEPRECATED -> Pair(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary
        )
        ChangeCategory.REMOVED -> Pair(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )
        ChangeCategory.FIXED -> Pair(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.onSurface
        )
        ChangeCategory.SECURITY -> Pair(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet(
    onDismiss: () -> Unit,
    onProgress: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val changelogList = remember(context) { ChangelogRepository.getChangelogList(context) }

    SheetProgressTracker(sheetState = sheetState, onProgress = onProgress)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = { ExpressiveDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.35f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .align(Alignment.CenterHorizontally)
                .navigationBarsPadding()
        ) {
            val scrollState = rememberScrollState()
            val fadeAlpha by remember { derivedStateOf { (scrollState.value / 40f).coerceIn(0f, 1f) } }

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Cabecera principal restablecida arriba fuera de las tarjetas
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Historial de Cambios",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (changelogList.isNotEmpty()) {
                        // 1. Tarjeta de la versión activa / actual
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            LatestChangelogCard(item = changelogList.first())
                        }

                        // 2. Sección Versiones Anteriores con timeline
                        if (changelogList.size > 1) {
                            Text(
                                text = "Versiones Anteriores",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            changelogList.drop(1).forEachIndexed { index, previousItem ->
                                val isStable = !isBetaVersion(previousItem.version)
                                val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                val dotColor = MaterialTheme.colorScheme.primary
                                Row(
                                    modifier = Modifier.fillMaxWidth().drawBehind {
                                        val centerX = 8.dp.toPx()
                                        val dotCenterY = 24.dp.toPx()
                                        val dotRadius = 4.dp.toPx()
                                        val gap = 4.dp.toPx()

                                        // 1. Draw top line if index > 0
                                        if (index > 0) {
                                            val topYEnd = dotCenterY - dotRadius - gap
                                            if (topYEnd > 0f) {
                                                drawLine(
                                                    color = lineColor,
                                                    start = androidx.compose.ui.geometry.Offset(centerX, 0f),
                                                    end = androidx.compose.ui.geometry.Offset(centerX, topYEnd),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }
                                        }

                                        // 2. Draw bottom line if not the last item
                                        if (index < changelogList.size - 2) {
                                            val bottomYStart = dotCenterY + dotRadius + gap
                                            if (bottomYStart < size.height) {
                                                drawLine(
                                                    color = lineColor,
                                                    start = androidx.compose.ui.geometry.Offset(centerX, bottomYStart),
                                                    end = androidx.compose.ui.geometry.Offset(centerX, size.height),
                                                    strokeWidth = 2.dp.toPx()
                                                )
                                            }
                                        }

                                        // 3. Draw the dot
                                        if (isStable) {
                                            drawCircle(
                                                color = dotColor,
                                                radius = dotRadius,
                                                center = androidx.compose.ui.geometry.Offset(centerX, dotCenterY)
                                            )
                                        } else {
                                            drawCircle(
                                                color = dotColor,
                                                radius = dotRadius,
                                                center = androidx.compose.ui.geometry.Offset(centerX, dotCenterY),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                    },
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Box(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
                                        PreviousChangelogCard(item = previousItem)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Top fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    surfaceColor.copy(alpha = fadeAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                // Bottom fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    surfaceColor
                                )
                            )
                        )
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LatestChangelogCard(item: ChangelogVersion) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
    val codeTextColor = MaterialTheme.colorScheme.primary
    val isBeta = isBetaVersion(item.version)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Cabecera: Versión en texto grande y a la derecha los dos chips apilados (Última / Beta o Estable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "v${item.version}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Chips apilados verticalmente uno encima del otro
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Chip 1: Última
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Última",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                        )
                    }

                    // Chip 2: Beta / Estable
                    Surface(
                        shape = CircleShape,
                        color = if (isBeta) 
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f) 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = if (isBeta) "Beta" else "Estable",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                            color = if (isBeta) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chips en línea con la cantidad de novedades, cambios, correcciones y seguridad
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.sections.forEach { section ->
                    val count = section.items.size
                    if (count > 0) {
                        val (catBgColor, catTextColor) = getCategoryColors(section.category)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = catBgColor,
                            border = BorderStroke(1.dp, catTextColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "$count ${getCategoryCountLabel(section.category, count)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                ),
                                color = catTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Detalle de secciones
            RenderChangelogSections(
                sections = item.sections,
                primaryColor = primaryColor,
                codeBgColor = codeBgColor,
                codeTextColor = codeTextColor
            )
        }
    }
}

@Composable
fun PreviousChangelogCard(item: ChangelogVersion) {
    var isExpanded by rememberSaveable(item.version) { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
    val codeTextColor = MaterialTheme.colorScheme.primary
    val isBeta = isBetaVersion(item.version)

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "arrowRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Versión con letra normal
                Text(
                    text = "v${item.version}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Chips a la derecha: arriba Beta/Estable, abajo fecha
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isBeta) 
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = if (isBeta) "Beta" else "Estable",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isBeta) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (item.date.isNotBlank()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = item.date,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 9.5.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer { rotationZ = arrowRotation }
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ) + fadeIn(animationSpec = tween(220)),
                exit = shrinkVertically(
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(animationSpec = tween(160))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    RenderChangelogSections(
                        sections = item.sections,
                        primaryColor = primaryColor,
                        codeBgColor = codeBgColor,
                        codeTextColor = codeTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderChangelogSections(
    sections: List<ChangelogSection>,
    primaryColor: Color,
    codeBgColor: Color,
    codeTextColor: Color
) {
    sections.forEachIndexed { sIndex, section ->
        if (sIndex > 0) {
            Spacer(modifier = Modifier.height(10.dp))
        }

        val (catBgColor, catTextColor) = getCategoryColors(section.category)

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = catBgColor,
            border = BorderStroke(1.dp, catTextColor.copy(alpha = 0.35f)),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = section.category.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                color = catTextColor,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        }

        section.items.forEach { change ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "• ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = catTextColor
                )
                Text(
                    text = parseMarkdownInline(
                        text = change,
                        primaryColor = primaryColor,
                        codeBgColor = codeBgColor,
                        codeTextColor = codeTextColor
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
