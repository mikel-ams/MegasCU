package com.ams.megascu.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MaterialIconEntry(
    val name: String,
    val imageVector: ImageVector,
    val category: String = "General",
    val uiLocations: String = ""
)

object MaterialIconsCatalog {
    val iconsList = listOf(
        MaterialIconEntry("AccountBalanceWallet", Icons.Rounded.AccountBalanceWallet, "Finanzas", "Tarjeta Principal de Estado, Desglose de Saldo"),
        MaterialIconEntry("Add", Icons.Rounded.Add, "Acción", "Botón Flotante (FAB), Acciones Rápidas, Nuevos Registros"),
        MaterialIconEntry("AdminPanelSettings", Icons.Rounded.AdminPanelSettings, "Permisos", "Pantalla de Bienvenida (Botón Otorgar Permisos), Diagnóstico"),
        MaterialIconEntry("ArrowBack", Icons.AutoMirrored.Rounded.ArrowBack, "Navegación", "Barra Superior, Navegación de Bienvenida, Diálogos"),
        MaterialIconEntry("ArrowDownward", Icons.Rounded.ArrowDownward, "Navegación", "Indicadores de Consumo y Recarga"),
        MaterialIconEntry("ArrowForward", Icons.AutoMirrored.Rounded.ArrowForward, "Navegación", "Botón Siguiente de Bienvenida, Enlaces"),
        MaterialIconEntry("ArrowForwardIos", Icons.AutoMirrored.Rounded.ArrowForwardIos, "Navegación", "Filas de Ajustes, Indicador de Apertura"),
        MaterialIconEntry("AutoGraph", Icons.Rounded.AutoGraph, "Gráficos", "Pestaña de Gráficas de Consumo, Estadísticas"),
        MaterialIconEntry("Backspace", Icons.AutoMirrored.Rounded.Backspace, "Acción", "Teclado Numérico de PIN de Seguridad"),
        MaterialIconEntry("BarChart", Icons.Rounded.BarChart, "Gráficos", "Selector de Tipo de Gráfica, Resumen"),
        MaterialIconEntry("Block", Icons.Rounded.Block, "Seguridad", "Estado de Bloqueo de PIN, Expiración"),
        MaterialIconEntry("BlurOff", Icons.Rounded.BlurOff, "Ajustes", "Configuración de Efectos de Desenfoque Haze"),
        MaterialIconEntry("Bolt", Icons.Rounded.Bolt, "Acción", "Paso de Inicio Rápido (Bienvenida), Accesos Directos"),
        MaterialIconEntry("BrightnessAuto", Icons.Rounded.BrightnessAuto, "Ajustes", "Ajustes de Brillo y Modo de Tema del Sistema"),
        MaterialIconEntry("BugReport", Icons.Rounded.BugReport, "Desarrollo", "Opciones de Desarrollador, Logs de Auditoría"),
        MaterialIconEntry("Call", Icons.Rounded.Call, "Comunicación", "Consulta USSD, Códigos de Saldo y Voz"),
        MaterialIconEntry("CardGiftcard", Icons.Rounded.CardGiftcard, "Servicios", "Bono Promocional ETECSA, Desglose de Bonos"),
        MaterialIconEntry("Category", Icons.Rounded.Category, "Ajustes", "Iconografía de Ajustes y Sistema"),
        MaterialIconEntry("CellTower", Icons.Rounded.CellTower, "Red", "Diagnóstico de Red Móvil Cubacel, Estado de Cobertura"),
        MaterialIconEntry("Chat", Icons.AutoMirrored.Rounded.Chat, "Comunicación", "Comentarios y Retroalimentación"),
        MaterialIconEntry("Check", Icons.Rounded.Check, "Acción", "Interruptores ExpressiveSwitch, Diálogos de Confirmación"),
        MaterialIconEntry("CheckCircle", Icons.Rounded.CheckCircle, "Estado", "Página ¡Todo Listo!, Permisos Otorgados con Éxito"),
        MaterialIconEntry("Close", Icons.Rounded.Close, "Acción", "Cerrar Diálogos, Limpiar Búsqueda, Cerrar Hojas Modales"),
        MaterialIconEntry("Contrast", Icons.Rounded.Contrast, "Ajustes", "Selector de Tema AMOLED (Bienvenida y Ajustes)"),
        MaterialIconEntry("CropFree", Icons.Rounded.CropFree, "Ajustes", "Selector de Encuadre y Visualización"),
        MaterialIconEntry("DarkMode", Icons.Rounded.DarkMode, "Ajustes", "Selector de Tema Oscuro"),
        MaterialIconEntry("DashboardCustomize", Icons.Rounded.DashboardCustomize, "Personalización", "Opciones de Personalización y Diseño de Tarjeta"),
        MaterialIconEntry("DataUsage", Icons.Rounded.DataUsage, "Datos", "Métricas de Consumo Diario, Desglose de Megas"),
        MaterialIconEntry("DateRange", Icons.Rounded.DateRange, "General", "Historial por Fechas, Vigencia de Paquetes"),
        MaterialIconEntry("Email", Icons.Rounded.Email, "Comunicación", "Soporte al Desarrollador por Correo"),
        MaterialIconEntry("Error", Icons.Rounded.Error, "Estado", "Indicador de Fallo USSD, Alertas Críticas"),
        MaterialIconEntry("Event", Icons.Rounded.Event, "General", "Días Restantes de Planes, Calendario"),
        MaterialIconEntry("Explore", Icons.Rounded.Explore, "Navegación", "Pestaña de Servicios y Ofertas"),
        MaterialIconEntry("Fingerprint", Icons.Rounded.Fingerprint, "Seguridad", "Autenticación Biométrica (Huella Dactilar)"),
        MaterialIconEntry("FlashOn", Icons.Rounded.FlashOn, "Acción", "Acciones Instantáneas de Recarga y Compra"),
        MaterialIconEntry("FontDownload", Icons.Rounded.FontDownload, "Ajustes", "Ajuste de Fuente Monoespaciada Space Mono"),
        MaterialIconEntry("FormatPaint", Icons.Rounded.FormatPaint, "Ajustes", "Selector de Paleta de Color"),
        MaterialIconEntry("Group", Icons.Rounded.Group, "General", "Comunidad de Usuarios MegasCU"),
        MaterialIconEntry("HelpOutline", Icons.AutoMirrored.Rounded.HelpOutline, "Soporte", "Ventana de Consejos y Guía Útil, Tutorial"),
        MaterialIconEntry("History", Icons.Rounded.History, "General", "Historial de Consultas USSD y Notificaciones"),
        MaterialIconEntry("HorizontalRule", Icons.Rounded.HorizontalRule, "Gráficos", "Configuración de Barra de Límite Diario"),
        MaterialIconEntry("HourglassEmpty", Icons.Rounded.HourglassEmpty, "Estado", "Progreso de Espera de Respuesta USSD"),
        MaterialIconEntry("Info", Icons.Rounded.Info, "Información", "Tarjeta de Tutorial, Diálogos Informativos, Acerca de"),
        MaterialIconEntry("KeyboardArrowDown", Icons.Rounded.KeyboardArrowDown, "Navegación", "Desplegables y Colapsables"),
        MaterialIconEntry("KeyboardArrowRight", Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Navegación", "Listas de Ajustes y Guías"),
        MaterialIconEntry("KeyboardArrowUp", Icons.Rounded.KeyboardArrowUp, "Navegación", "Contraer Paneles"),
        MaterialIconEntry("Lightbulb", Icons.Rounded.Lightbulb, "General", "Consejos de Ahorro y Modo Offline"),
        MaterialIconEntry("LightMode", Icons.Rounded.LightMode, "Ajustes", "Selector de Tema Claro"),
        MaterialIconEntry("Lock", Icons.Rounded.Lock, "Seguridad", "Pantalla de Bloqueo PIN, Protección de Datos"),
        MaterialIconEntry("LockOpen", Icons.Rounded.LockOpen, "Seguridad", "Estado Desbloqueado de Seguridad"),
        MaterialIconEntry("MarkEmailRead", Icons.Rounded.MarkEmailRead, "Comunicación", "Marcado de Alertas Leídas"),
        MaterialIconEntry("MenuBook", Icons.AutoMirrored.Rounded.MenuBook, "General", "Documentación y Guía ETECSA"),
        MaterialIconEntry("Message", Icons.AutoMirrored.Rounded.Message, "Comunicación", "Consulta y Saldo de SMS"),
        MaterialIconEntry("MonetizationOn", Icons.Rounded.MonetizationOn, "Finanzas", "Tarjeta de Saldo Principal CUP"),
        MaterialIconEntry("NetworkCheck", Icons.Rounded.NetworkCheck, "Red", "Comprobación de Conectividad Móvil"),
        MaterialIconEntry("NotificationAdd", Icons.Rounded.NotificationAdd, "Alertas", "Segunda Alerta de Refuerzo de Vencimiento"),
        MaterialIconEntry("NotificationImportant", Icons.Rounded.NotificationImportant, "Alertas", "Avisos Críticos de Saldo Bajo"),
        MaterialIconEntry("Notifications", Icons.Rounded.Notifications, "Alertas", "Pestaña de Notificaciones, Permiso de Notificaciones"),
        MaterialIconEntry("NotificationsActive", Icons.Rounded.NotificationsActive, "Alertas", "Alerta de Vencimiento de Planes"),
        MaterialIconEntry("Palette", Icons.Rounded.Palette, "Ajustes", "Ajustes de Color Dinámico y Catálogo de Iconos"),
        MaterialIconEntry("Password", Icons.Rounded.Password, "Seguridad", "Cambio y Configuración de PIN de Seguridad"),
        MaterialIconEntry("PhoneInTalk", Icons.Rounded.PhoneInTalk, "Comunicación", "Minutos de Voz Disponibles"),
        MaterialIconEntry("PhonelinkRing", Icons.Rounded.PhonelinkRing, "Comunicación", "Prueba de Consulta y Llamada USSD"),
        MaterialIconEntry("PieChart", Icons.Rounded.PieChart, "Gráficos", "Alerta de Umbral de Consumo de Datos"),
        MaterialIconEntry("PowerSettingsNew", Icons.Rounded.PowerSettingsNew, "Sistema", "Reinicio de Ajustes Predeterminados"),
        MaterialIconEntry("Refresh", Icons.Rounded.Refresh, "Acción", "Botón de Actualización de Saldo, Sincronizar"),
        MaterialIconEntry("RocketLaunch", Icons.Rounded.RocketLaunch, "Acción", "Lanzamiento Rápido de Experiencia MegasCU"),
        MaterialIconEntry("Schedule", Icons.Rounded.Schedule, "Tiempo", "Hora de Última Sincronización"),
        MaterialIconEntry("Search", Icons.Rounded.Search, "Navegación", "Buscador de Iconos, Filtro de Registros"),
        MaterialIconEntry("Security", Icons.Rounded.Security, "Seguridad", "Ajustes de Seguridad y Control de Acceso"),
        MaterialIconEntry("Settings", Icons.Rounded.Settings, "Ajustes", "Botón de Ajustes en Barra Inferior"),
        MaterialIconEntry("SettingsPhone", Icons.Rounded.SettingsPhone, "Ajustes", "Configuración de SIM Dual y Red"),
        MaterialIconEntry("Shield", Icons.Rounded.Shield, "Seguridad", "Tarjeta de Permisos y Protección Local"),
        MaterialIconEntry("ShoppingBag", Icons.Rounded.ShoppingBag, "Tienda", "Catálogo de Paquetes de Datos ETECSA"),
        MaterialIconEntry("ShoppingCart", Icons.Rounded.ShoppingCart, "Tienda", "Compra de Combos y Planes"),
        MaterialIconEntry("ShowChart", Icons.AutoMirrored.Rounded.ShowChart, "Gráficos", "Gráfica de Consumo y Tendencias"),
        MaterialIconEntry("SignalCellularAlt", Icons.Rounded.SignalCellularAlt, "Red", "Indicador de Datos LTE e Internacionales"),
        MaterialIconEntry("SimCard", Icons.Rounded.SimCard, "Hardware", "Selector de Línea SIM 1 / SIM 2"),
        MaterialIconEntry("Smartphone", Icons.Rounded.Smartphone, "Hardware", "Selector de Tema del Sistema"),
        MaterialIconEntry("Sms", Icons.Rounded.Sms, "Comunicación", "Bolsa de Mensajes SMS Nacionales"),
        MaterialIconEntry("Speed", Icons.Rounded.Speed, "Medición", "Ritmo de Consumo Diario de Megas"),
        MaterialIconEntry("SwapVert", Icons.Rounded.SwapVert, "Navegación", "Alternar Orden de Saldo vs. Datos"),
        MaterialIconEntry("Sync", Icons.Rounded.Sync, "Acción", "Ajustes de Frecuencia de Sincronización"),
        MaterialIconEntry("TableChart", Icons.Rounded.TableChart, "Gráficos", "Tabla Detallada de Consumos"),
        MaterialIconEntry("TextFields", Icons.Rounded.TextFields, "Ajustes", "Selector de Tipografía Space Mono"),
        MaterialIconEntry("Timer", Icons.Rounded.Timer, "Tiempo", "Temporizador de Expiración de Planes"),
        MaterialIconEntry("TouchApp", Icons.Rounded.TouchApp, "Interacción", "Pestaña de Accesos Directos"),
        MaterialIconEntry("Verified", Icons.Rounded.Verified, "Estado", "Procesamiento 100% Privado en Dispositivo"),
        MaterialIconEntry("Visibility", Icons.Rounded.Visibility, "Ajustes", "Selector de Modo Simple / Modo Normal"),
        MaterialIconEntry("Warning", Icons.Rounded.Warning, "Estado", "Avisos de Alerta de Umbral y Advertencias"),
        MaterialIconEntry("Widgets", Icons.Rounded.Widgets, "General", "Personalización de Widgets y Tarjetas"),
        MaterialIconEntry("Wifi", Icons.Rounded.Wifi, "Red", "Bono de Datos Nacionales .CU")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialIconsBottomSheet(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedTintToken by remember { mutableStateOf("Primary") } // Primary, Secondary, Tertiary, Error
    var selectedIconForInfo by remember { mutableStateOf<MaterialIconEntry?>(null) }

    val categories = remember {
        listOf("Todos", "Acción", "Navegación", "Finanzas", "Datos", "Red", "Comunicación", "Alertas", "Seguridad", "Ajustes", "Gráficos", "Hardware", "Servicios", "General")
    }

    val activeTintColor = when (selectedTintToken) {
        "Secondary" -> MaterialTheme.colorScheme.secondary
        "Tertiary" -> MaterialTheme.colorScheme.tertiary
        "Error" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    val filteredIcons = remember(searchQuery, selectedCategory) {
        MaterialIconsCatalog.iconsList.filter { entry ->
            val matchesCategory = (selectedCategory == "Todos" || entry.category.equals(selectedCategory, ignoreCase = true))
            val matchesQuery = if (searchQuery.isBlank()) true else {
                entry.name.contains(searchQuery, ignoreCase = true) ||
                entry.category.contains(searchQuery, ignoreCase = true) ||
                entry.uiLocations.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIcon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Catálogo de Iconos M3",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${filteredIcons.size} de ${MaterialIconsCatalog.iconsList.size} iconos",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    ExpressiveIconButton(onClick = onDismissRequest) {
                        AppIcon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
            }

            // Buscador y Selector de Tinta en 2 Filas y 2 Columnas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                AppIcon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    ExpressiveIconButton(onClick = { searchQuery = "" }) {
                                        AppIcon(Icons.Rounded.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Selector de Tinta en 2 Filas x 2 Columnas (sin texto vertical)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Colores de Tinta:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val tintOptions = listOf(
                                Triple("Primary", MaterialTheme.colorScheme.primary, "Primario"),
                                Triple("Secondary", MaterialTheme.colorScheme.secondary, "Secundario"),
                                Triple("Tertiary", MaterialTheme.colorScheme.tertiary, "Terciario"),
                                Triple("Error", MaterialTheme.colorScheme.error, "Error")
                            )

                            // Fila 1: Primario & Secundario
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tintOptions.take(2).forEach { (key, col, label) ->
                                    val isSel = selectedTintToken == key
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) col else col.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .expressiveClick { selectedTintToken = key }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                color = if (isSel) Color.White else col
                                            )
                                        }
                                    }
                                }
                            }

                            // Fila 2: Terciario & Error
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tintOptions.drop(2).forEach { (key, col, label) ->
                                    val isSel = selectedTintToken == key
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) col else col.copy(alpha = 0.15f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .expressiveClick { selectedTintToken = key }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                color = if (isSel) Color.White else col
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories horizontal filter
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.5.sp) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.expressivePressEffect()
                        )
                    }
                }
            }

            // List of Icons
            items(filteredIcons, key = { it.name }) { iconEntry ->
                IconCatalogItemRow(
                    item = iconEntry,
                    tintColor = activeTintColor,
                    onShowInfo = { selectedIconForInfo = iconEntry },
                    onCopyCode = {
                        val clipManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Compose Icon", "Icons.Rounded.${iconEntry.name}")
                        clipManager.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Copiado: Icons.Rounded.${iconEntry.name}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal / Dialog Informativo de Dónde se usa el icono
    selectedIconForInfo?.let { iconEntry ->
        AlertDialog(
            onDismissRequest = { selectedIconForInfo = null },
            modifier = Modifier.expressiveModalEntrance(),
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AppIcon(
                            imageVector = iconEntry.imageVector,
                            contentDescription = iconEntry.name,
                            tint = activeTintColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = iconEntry.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Categoría: ${iconEntry.category}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Implementación en la Aplicación:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppIcon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (iconEntry.uiLocations.isNotEmpty()) iconEntry.uiLocations else "Usado globalmente en la interfaz",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Icons.Rounded.${iconEntry.name}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.primary
                            )
                            ExpressiveOutlinedButton(
                                onClick = {
                                    val clipManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Compose Icon", "Icons.Rounded.${iconEntry.name}")
                                    clipManager.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copiado al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Copiar", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                ExpressiveButton(
                    onClick = {
                        selectedIconForInfo = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun IconCatalogItemRow(
    item: MaterialIconEntry,
    tintColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onShowInfo: () -> Unit,
    onCopyCode: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    val itemInteraction = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .expressivePressEffect(interactionSource = itemInteraction)
            .clickable(
                interactionSource = itemInteraction,
                indication = null
            ) { isExpanded = !isExpanded },
        shape = rememberExpressiveMorphShape(
            defaultRadius = 16.dp,
            pressedRadius = 8.dp,
            interactionSource = itemInteraction
        ),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = tintColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = item.name,
                            tint = tintColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = tintColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Copy snippet button
                ExpressiveIconButton(
                    onClick = onCopyCode,
                    modifier = Modifier.size(34.dp)
                ) {
                    AppIcon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = "Copiar código",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Info Button
                ExpressiveIconButton(
                    onClick = onShowInfo,
                    modifier = Modifier.size(34.dp)
                ) {
                    AppIcon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Información de implementación en UI",
                        tint = tintColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Inline expansion preview of UI locations
            AnimatedVisibility(
                visible = isExpanded && item.uiLocations.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppIcon(
                            imageVector = Icons.Rounded.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = item.uiLocations,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
