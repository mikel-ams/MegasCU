package com.ams.megascu.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
            version = "0.9.0-beta_(254)",
            date = "2026-09-20",
            isLatest = true,
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

                    changelogList.forEach { item ->
                        ChangelogCard(item = item)
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

@Composable
fun ChangelogCard(item: ChangelogVersion) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val codeBgColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
    val codeTextColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isLatest) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "v${item.version}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.isLatest) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Actual",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = item.date,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            item.sections.forEachIndexed { sIndex, section ->
                if (sIndex > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Category Badge Pill
                val (catBgColor, catTextColor) = when (section.category) {
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
    }
}
