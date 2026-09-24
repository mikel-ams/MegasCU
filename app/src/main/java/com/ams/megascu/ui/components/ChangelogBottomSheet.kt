package com.ams.megascu.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            version = "0.9.2-beta_(258)",
            date = "2026-09-24",
            isLatest = true,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Enlace al Repositorio en Acerca de: Nuevo botón de acceso directo al repositorio de GitHub con icono distintivo.",
                        "Animaciones Expressive al Pulsar: Transición táctil con escala y rebote elástico en botones sociales, enlaces de soporte y versión.",
                        "Transición Fluida en Ventana Acerca de: Animación suave de apertura y cierre con escalado sutil, desvanecimiento y sincronización de desenfoque.",
                        "Indicador PullToRefresh M3 Expressive: Integración del indicador de carga nativo con transformación de formas durante la actualización."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Mayor Grosor en Indicadores de Interruptores: Incremento del trazo en los símbolos de verificación y cierre con terminaciones redondeadas.",
                        "Reorganización en Acerca de: Reubicación de la versión debajo de los créditos de desarrollo y eliminación del contorno de la tarjeta."
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
                        "Punto de notificación de Compra y Recarga (< 5 días): Incorporación de indicador de notificación cuando restan 5 días o menos para recargar saldo o renovar paquetes de datos.",
                        "Punto de notificación de Actualización: Indicador rojo en el botón de ajustes visible al detectar una nueva versión.",
                        "Notificación Enriquecida de Actualización Disponible: Mejora de la notificación del sistema de actualización.",
                        "Gestor de Actualizaciones en Ajustes: Rediseño de la tarjeta de actualizaciones con visualización directa de la versión instalada y transición dinámica del botón de búsqueda.",
                        "Rediseño Interactivo del Historial de Cambios: Nueva cabecera con versión destacada, insignias, métricas resumidas por categoría y tarjetas colapsables para versiones anteriores.",
                        "Tarjeta Material 3 Expressive de Alerta en Menú de Compras: Nueva tarjeta en el catálogo de Compras para alerta de compra de planes y saldo, conteo dinámico de días y botón de apertura directa de Transfermóvil.",
                        "Sistema de Alerta Unificada: Detección y notificación consolidada cuando coinciden la necesidad de recarga de saldo principal y el vencimiento inminente de paquetes de datos y planes.",
                        "Acceso Directo a Compras desde Historial USSD: Incorporación de botón \"Ir a Compras\" en el diálogo de resultado USSD tras consultar la acción rápida de Historial de Recargas e indica disponibilidad de recarga.",
                        "Opción de Acción Rápida \"Historial de Recargas\": Integración de la consulta *222*732# en el selector de acciones rápidas para acceso directo desde la pantalla principal.",
                        "Restauración de Indicadores en Switches: Reincorporación de los iconos indicadores de estado (Check y Close) en el thumb de los interruptores."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Rediseño y Estilización de Tarjeta de Recarga de Saldo: Aplicación de tono de color rojo en tarjeta y elementos interactivos, icono de pagos, descripción detallada de recarga y unificación del botón de acción a \"Abrir Transfermóvil\".",
                        "Disipación Continua de Desenfoque en Cierre de BottomSheets: Sincronización precisa del progreso de desenfoque con la posición física de la hoja durante el recorrido de cierre, alcanzando exactamente 0px de intensidad 2.5dp antes de cerrarse completamente.",
                        "Efecto de Desenfoque Progresivo Nativo Android: Sustitución integral de dependencias externas por modificadores nativos de Compose y RenderEffect.",
                        "Difuminado Progresivo con Scroll en Compras: Ajuste del efecto de desenfoque superior en la ventana de Compras para que aparezca gradualmente al desplazarse, evitando difuminados prematuros de elementos superiores.",
                        "Optimización Integral con R8 en Modo Completo: Activación de minificación R8 Full Mode y reducción agresiva de recursos en Gradle para minimizar el tamaño del APK y maximizar el rendimiento."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Fluidez y Persistencia Visual en Cierre de Modales: Corrección de la pérdida prematura de desenfoque y eliminación de saltos bruscos al soltar o deslizar las ventanas hacia abajo.",
                        "Desenfoque de Fondo en Ventana Acerca de: Corrección de la renderización del efecto desenfoque gaussiano limpio en la capa posterior de la ventana modal Acerca de.",
                        "Detección y Formato de Disponibilidad de Recarga: Actualización del analizador USSD para reconocer el mensaje \"Ud puede recargar un monto de 360,00CUP en un plazo de 30 dias\" y reflejar el estado \"Puede recargar saldo\" al vencer el plazo de espera o recibir confirmación de recarga disponible."
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
        ChangeCategory.CHANGED -> Pair(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary
        )
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                AppIcon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Historial de Cambios",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            val scrollState = rememberScrollState()
            val fadeAlpha by remember { derivedStateOf { (scrollState.value / 40f).coerceIn(0f, 1f) } }

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    if (changelogList.isNotEmpty()) {
                        // 1. Tarjeta de la última versión
                        LatestChangelogCard(item = changelogList.first())

                        // 2. Versiones anteriores
                        if (changelogList.size > 1) {
                            Text(
                                text = "Versiones Anteriores",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 0.dp)
                            )

                            changelogList.drop(1).forEach { previousItem ->
                                PreviousChangelogCard(item = previousItem)
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
                            border = BorderStroke(1.dp, catTextColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "($count ${getCategoryCountLabel(section.category, count)})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
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
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = section.category.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
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
