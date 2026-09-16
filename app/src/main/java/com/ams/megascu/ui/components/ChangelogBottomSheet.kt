package com.ams.megascu.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val changelogList = listOf(
        ChangelogVersion(
            version = "0.8.1-beta_(244)",
            date = "2026-09-16",
            isLatest = true,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Gestor de Descarga Integrado (ApkDownloadManager) con descarga de APK en segundo plano y redirecciones HTTP seguras.",
                        "Barra de progreso Material M3 Expressive Wavy (LinearWavyProgressIndicator) con visualización de MB descargados y porcentaje dinámico.",
                        "Instalador automático de actualización: al completar la descarga se abre directamente el instalador del sistema.",
                        "Cálculo y muestra del tamaño de la aplicación y registro de cambios en el diálogo de actualización desde GitHub."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Reubicación de Opciones de Desarrollador en los ajustes de configuración, desplazada al pie de la ventana debajo de Gestión de Datos.",
                        "Sección de actualizaciones en Ajustes renombrada y enriquecida como Gestor de Descarga y Actualizaciones."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Activación directa de la ventana de PIN al pulsar sobre la tarjeta o píldora de Seguridad y Control en la pantalla de Bienvenida (Paso 7)."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.8.0-beta_(243)",
            date = "2026-09-15",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Primera versión pública Pre-release (Beta) para distribución mediante GitHub Releases.",
                        "Soporte nativo y detección automática de Pre-releases en el actualizador integrado de la app.",
                        "Respuesta háptica táctil expresiva al conmutar el interruptor de actualizaciones automáticas en Ajustes.",
                        "Respuesta háptica táctil en el botón de comprobación manual de actualizaciones en GitHub.",
                        "Respuesta háptica integrada nativamente en el componente ExpressiveSwitch tanto por toque como por arrastre.",
                        "Actualización de la dirección del repositorio a migue-ams/MegasCU."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Reordenamiento de interfaz en Ajustes: traslado de la sección Opciones de Desarrollador hacia el pie de la ventana, ubicándola debajo de la tarjeta de Gestión de Datos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Corrección en la Pantalla de Bienvenida (Paso 7: Gráficas y Seguridad): al pulsar sobre el elemento Seguridad y Control Parental se activa la verificación e ingreso de PIN."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.8-beta_(242)",
            date = "2026-09-15",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Verificación automática de actualizaciones mediante la API pública de GitHub Releases.",
                        "Programación periódica en segundo plano cada 24 horas con WorkManager restringida a conexión de red.",
                        "Diálogo modal interactivo de actualización disponible con registro de cambios y descarga directa de APK.",
                        "Tarjeta de control en Ajustes con comprobación manual, fecha/hora de último chequeo e interruptor automático.",
                        "Notificación del sistema para avisar sobre nuevas versiones de la aplicación con acción de descarga directa.",
                        "Creación del archivo CHANGELOG.md en la raíz del proyecto estructurado bajo Keep a Changelog 1.1.0 y SemVer 2.0.0."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Refactorización completa del historial de cambios categorizado bajo el estándar Keep a Changelog v1.1.0 y Semantic Versioning v2.0.0.",
                        "Fechas de versión estandarizadas al formato ISO 8601 (YYYY-MM-DD)."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.SECURITY,
                    items = listOf(
                        "Eliminación de contraseñas y credenciales en texto plano en la configuración de firma Gradle (build.gradle.kts), delegando la autenticación a variables de entorno."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.8-beta_(236)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Indicador de Desplazamiento Optimizado: Desenfoque de 2px en el fondo del indicador de bajar con estructura estática y animación de rebote aplicada exclusivamente al icono chevron interno.",
                        "Sincronía del Botón Siguiente: Dimensiones y tamaño de icono unificados (20dp) entre la pantalla 6 y 7 para una transición perfecta sin saltos visuales.",
                        "Botón Final en Píldora de Dos Líneas: Disposición del texto en dos líneas ('Comenzar Experiencia' / 'MegasCU'), ancho adaptado al contenido y extremos completamente redondeados."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Fijación del Chip Indicador de Pasos: Altura constante en el encabezado superior para mantener el chip de paso completamente estable al pasar a la pantalla final."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.7-beta_(235)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Margen Superior en Bienvenida: Margen incrementado a 6dp para una separación superior visualmente equilibrada y natural.",
                        "Centrado de Elementos en Pantalla Final: Todo el contenido de la pantalla '¡Todo Listo!' se encuadra en el centro vertical exacto de la ventana.",
                        "Márgenes y Área de Botón Expandidos: Separación superior e inferior del botón final incrementadas significativamente con extremos de píldora continua."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.6-beta_(234)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Difuminado Progresivo Inteligente: El desvanecimiento superior e inferior solo se hace visible si la pantalla requiere desplazamiento.",
                        "Indicador de Contenido Inferior Ampliado: Señalización de scroll expandida 2dp con fondo difuminado en pantalla 2 y pantalla 6."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Transformación Continua del Botón Final: El botón 'Siguiente' en la pantalla 7 se desliza y transforma fluidamente en 'Comenzar Experiencia MegasCU'.",
                        "Margen Superior en Bienvenida: Margen ajustado con precisión a 2dp en la pantalla inicial."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.5-beta_(233)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Señalización de Scroll Inferior: Indicador visual en cápsula con bordes redondeados y flecha animada en la segunda pantalla."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Botón de Bienvenida Debajo de Tarjetas: Botón reubicado directamente bajo las tarjetas con animación suave entre pantallas.",
                        "Márgenes Superiores Optimizados: Reducción de márgenes superiores del logo en Bienvenida para visibilidad total sin scroll.",
                        "Pull-to-Refresh Perfeccionado: El indicador emerge desde la barra de estado e incrementa su tamaño de forma progresiva.",
                        "Optimización de Tamaño de APK: Retorno al tamaño ligero de ~2.55 MB mediante compresión optimizada."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.4-beta_(232)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Tarjeta Segmentada de Permisos en Bienvenida: Agrupación visual en 3 bloques claros (Teléfono/USSD, Notificaciones y Estadísticas de Uso)."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Transición Animada Fluida de Navegación: El botón de 'Siguiente' se transforma suavemente con animación elástica al pasar al paso final.",
                        "Opciones de Desarrollo Optimizadas: Disposición en 2x2 para selección de tintas en Catálogo de Iconos y auditoría WCAG con scroll natural."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.REMOVED,
                    items = listOf(
                        "Alerta de Vencimiento Simplificada: Eliminación del interruptor redundante en Bienvenida, manteniendo la selección de días."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.SECURITY,
                    items = listOf(
                        "Seguridad en Bienvenida: Al configurar el PIN de acceso, la app muestra confirmación inmediata sin solicitar desbloqueo prematuro."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.3-beta_(231)",
            date = "2026-09-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Reubicación del Estimador Alternativo de Consumo: Integrado de forma armónica dentro de 'Gráficos e Indicadores' en Ajustes.",
                        "Estandarización de Modo Simple en Ajustes: Contenedor superior independiente para activación y configuración.",
                        "Normalización M3 Expressive: Estandarización de componentes con feedback háptico táctil y transiciones elásticas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.2-beta_(218)",
            date = "2026-08-30",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Indicador de Conexión Activa (Ping Dinámico): Monitor discreto 'Ping: 0,04 ms' en la barra inferior con punto de estado en tiempo real.",
                        "Tipografía Expresiva en Guía de Actualización: Rediseño visual del texto con estilo caligráfico y sombreado de alto contraste."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Trazo Discontinuo (Dashed) de Señalización: Flechas de indicación dibujadas con línea discontinua uniforme.",
                        "Tarjeta Unificada de Permisos en Bienvenida: Agrupación armónica con diseño segmentado e interactividad táctil."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.7.0-beta_(216)",
            date = "2026-08-29",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Terminal Retro Easter Egg: Crónica histórica de telecomunicaciones incluyendo las resoluciones y tarifas de 2025.",
                        "Escritura Retro Humanizada con Autocorrección: Cadencia de tecleo natural con micro-vacilaciones y sonido de retroceso DOS.",
                        "Conmutación Inteligente de Tema: Cambio dinámico a Negro AMOLED durante la sesión de terminal y restauración fiel al cerrarla."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Terminal Retro Desacoplada de Edge-to-Edge: Eliminación de insets conflictivos para compatibilidad con HyperOS, MIUI, OneUI y AOSP.",
                        "Elevación Firme de Elementos Inferiores (+48dp): Margen inferior rígido garantizando visibilidad total de la terminal sobre gestos del sistema."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.81-beta_(215)",
            date = "2026-08-29",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Indicador de Actualización Exclusivo para Gesto: El spinner de 'Hala para actualizar' solo se activa ante el gesto en pantalla.",
                        "Posicionamiento Superior sobre la Barra de Título: Indicador flotante superpuesto con Z-index y safe padding."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Sincronización Inteligente de Estados: Desacoplamiento de animaciones para evitar indicadores duplicados."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.80-beta_(214)",
            date = "2026-08-29",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Diseño Compacto y Resumido: Textos claros y concisos en la alerta de permiso de uso evitando scroll.",
                        "Efecto de Difuminado en Bordes (Fading Edges): Gradientes suaves superior e inferior.",
                        "Desenfoque de Fondo (Window Blur Behind): Fondo con desenfoque nativo y scrim para mayor enfoque.",
                        "Márgenes Simétricos Edge-to-Edge: Disposición armónica y uniforme en los cuatro bordes."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.79-beta_(213)",
            date = "2026-08-29",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Diálogo Explicativo de Permiso de Uso: Alerta interactiva M3 Expressive sobre el acceso a estadísticas de datos y privacidad.",
                        "Integración Unificada en UI: Activación del diálogo desde Ajustes, Onboarding y contador de consumo diario."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.SECURITY,
                    items = listOf(
                        "Keystore de Producción Oficial: Integración del almacén de claves release-key.jks con firma digital RSA 4096-bit."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.78-beta_(212)",
            date = "2026-08-29",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Optimización de Rendimiento y Tamaño: Reducción del APK y memoria RAM al suprimir fuentes de símbolos.",
                        "Catálogo de Iconos Material M3: Actualización del explorador con búsqueda en vivo."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.REMOVED,
                    items = listOf(
                        "Restauración de Iconografía Nativa Material M3: Reversión completa de fuentes personalizadas a vectores Compose nativos.",
                        "Limpieza de Arquitectura: Eliminación de selectores y CompositionLocals redundantes."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.77-beta_(211)",
            date = "2026-08-28",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Selector de Iconografía Conectado: Botones segmentados para alternar entre Nativo, M3 Filled y M3 Outlined en vivo.",
                        "Icono Dual SIM Dinámico: Profundidad geométrica y bisel perimetral opaco para los 3 estilos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Material Symbols 48pt Regular: Glifos actualizados a peso Regular para mayor nitidez.",
                        "Auditoría del Historial: Reestructuración integral con descripciones técnicas y redacción fluida."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.76-beta_(210)",
            date = "2026-08-28",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Verificación Estricta de Hashes: Comprobación automatizada SHA-256 en Gradle para binarios."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Optimización de Renderizado: Normalización de contenedores BoxWithConstraints y mapeo semántico TalkBack."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.75-beta_(209)",
            date = "2026-08-28",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Mapeador Centralizado MaterialIconsCatalog: Arquitectura de resolución de nombres de iconos Material y correspondencia vectorial.",
                        "Auditoría de Glifos: Validación de compatibilidad con Google Fonts e integridad binaria."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.74-beta_(208)",
            date = "2026-08-27",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Depuración de Permisos USSD: Estabilización de interceptores telefónicos ante rechazos transitorios.",
                        "Sincronización en Segundo Plano: Calibración de alarmas en WorkManager para mitigar consumo de batería."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.73-beta_(207)",
            date = "2026-08-27",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Limpieza de Telemetría: Supresión de logs redundantes durante la consulta y cálculo de saldos.",
                        "Microinteracciones Hápticas: Respuesta táctil calibrada en teclados y botones rápidos."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.72-beta_(206)",
            date = "2026-08-27",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Bisel Dinámico en Tarjetas: Antialiasing optimizado en componentes flotantes y hojas modales."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Gestión de Desenfoque: Fallback automático y suave en dispositivos con aceleración GPU limitada."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.71-beta_(205)",
            date = "2026-08-27",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Contraste en Modo Simple: Texto con contraste forzado en botones y eliminación de parpadeos.",
                        "Caché Tipográfica Space Mono: Carga optimizada en memoria de fuentes TrueType."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.70-beta_(204)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Persistencia de Preferencias: Consolidación de claves en SharedPreferences."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Alineación en Gráficas: Corrección de desfases en etiquetas de ejes de consumo semanal."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.69-beta_(203)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Alineación de Botones en Menú: Unificación visual de selectores y espaciados simétricos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Protección en Parsing: Mayor tolerancia ante respuestas USSD truncadas o con caracteres atípicos."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.68-beta_(202)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Aislamiento Transaccional: Sincronización atómica multi-SIM sin corrupción de estado en Room DB."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Servicios Foreground Seguros: Prevención de excepciones en Android 12+ mediante fallback a NotificationManager.",
                        "Concurrencia USSD Thread-Safe: Bloqueo seguro con radioMutex en corrutinas suspendibles con liberación inmediata."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.67-beta_(201)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Sincronización Dual-SIM: Caché y lógica independiente por ranura SIM en widgets 4x2, 2x1 y gráficas."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Programación Segura de Sync: Límite mínimo de demora inicial para prevenir ejecuciones redundantes."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.66-beta_(200)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Diálogo de Permisos Telefónicos: Advertencia M3 informativa para habilitar llamadas USSD de saldo.",
                        "Formateo Dinámico de Bonos: Conversión automática a GB para bolsas nacionales >= 1024 MB.",
                        "TestTags y Accesibilidad: Identificadores semánticos en componentes interactivos."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.65-beta_(199)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Validación de Paquetes: Desglose y validación precisa de bolsas LTE e Internacionales.",
                        "Optimización de Memoria: Reducción de recomposiciones innecesarias en el feed principal."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.64-beta_(198)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Elevaciones Tonales M3: Pulido de radios de curvatura y contrastes de superficie."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Gestión de Excepciones: Manejo defensivo en canales de notificación."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.63-beta_(197)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "ExpirationWorker Autónomo: Actualización de expiración y cálculo de días sin requerir datos móviles activos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "SyncWorker Robusto: Manejo seguro de cancelación cooperativa y control de excepciones en WorkManager.",
                        "Control de Concurrencia en ViewModel: Prevención de llamadas solapadas en actualización de estado."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.62-beta_(196)",
            date = "2026-08-26",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "WorkManager Bajo Demanda: Inicialización delegada a AndroidX eliminando overhead en Application.",
                        "Centralización de Cadenas: Migración de textos a strings.xml para internacionalización."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.REMOVED,
                    items = listOf(
                        "Limpieza de Permisos: Supresión de verificaciones SMS innecesarias tras migración completa a USSD."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.53-beta_(187)",
            date = "2026-08-23",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Icono Dual SIM con Contorno Sólido: Bisel perimetral opaco idéntico al color de tarjeta sin halos translúcidos.",
                        "Handles Modales Limpios: Eliminación del marco de foco al presionar tiradores de BottomSheets.",
                        "Alineación Izquierda: Títulos alineados con el margen del contenido en hojas modales."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.45-beta_(179)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Conteo Numérico Progresivo: Animación fluida de 0 al valor actual en encabezados de consumo y límites.",
                        "Chips Flotantes en Píldora: Geometría de píldora estilizada centrada a 5dp sobre crestas de gráficas y barras."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.44-beta_(178)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Desenfoque y Oscurecimiento Progresivo: Difuminado reactivo sincronizado con el arrastre de BottomSheets."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.43-beta_(177)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Expansión Proporcional M3 Expressive: Botones conectados con física spring que amplían el botón seleccionado mientras contraen los demás."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.42-beta_(176)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Transiciones Post-Splash: Tiempos extendidos para entrada coordinada de números, barras y gráficas."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Chips de Gráfica sin Cortes: Aparición directa con esquinas redondeadas sin parpadeo rectangular."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.41-beta_(175)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Sincronización de Límite Diario: Animación progresiva incremental para consumo de hoy y límite diario.",
                        "Tonalidad Violeta MegasCU: Colores violetas profundos (#4A00DF) en botones no seleccionados en modo oscuro y AMOLED."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.40-beta_(174)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Barra Inferior Flotante Limpia: Eliminación de sombras traseras sobre el botón de actualización.",
                        "Botones Conectados por Defecto: Estandarización de grupos segmentados en Ajustes y Bienvenida.",
                        "Cálculo de Recarga de Saldo: Ajuste de disponibilidad al día siguiente hábil con opción de alerta."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.39-beta_(173)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Cálculos de Recarga: Ajuste en fechas de vigencia y recarga de saldo.",
                        "Tipografía Space Mono: Aplicación en referencias numéricas de gráficas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.35-beta_(169)",
            date = "2026-08-22",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Predictive Back Gestures: Soporte oficial de gesto predictivo atrás en todas las ventanas modales y Acerca de."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Migración Nativa de BottomSheet: Extensión limpia edge-to-edge hasta la barra de navegación del sistema."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.34-beta_(168)",
            date = "2026-08-21",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Refinamiento M3 Expressive: Respuesta háptica optimizada y contraste dinámico según luminancia.",
                        "Elevación Tonal: Sombras y jerarquía en tarjetas secundarias agrupadas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.33-beta_(167)",
            date = "2026-08-20",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Curvas de Consumo: Renderizado optimizado de indicadores lineales y límites de datos.",
                        "Fluidez en Animaciones: Curvas de resorte sin micro-tirones."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.32-beta_(166)",
            date = "2026-08-18",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Haze Blur Reactivo: Reducción de consumo de GPU y memoria en fondos desenfocados.",
                        "Morphing Geométrico: Transiciones suaves en botones e indicadores de seguridad PIN."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.30-beta_(164)",
            date = "2026-08-17",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Seguridad Biométrica: Protección de compras de planes y acceso general mediante huella dactilar."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.SECURITY,
                    items = listOf(
                        "Auditoría Binaria Byte a Byte: Verificación estricta de integridad en recursos tipográficos y WebP."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.25-beta_(159)",
            date = "2026-08-15",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Widgets de Escritorio: Sincronización en segundo plano de saldos y paquetes de datos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Gestión de Batería: Optimización de ciclos de sondeo en reposo."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.20-beta_(154)",
            date = "2026-08-14",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Paletas de Color Dinámicas: Compatibilidad con Monet en Android 12+.",
                        "Tema AMOLED Puro: Modo negro ultra profundo para paneles OLED."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.15-beta_(149)",
            date = "2026-08-12",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Navegación por Gestos: Barra de navegación transparente edge-to-edge.",
                        "Microinteracciones: Respuesta háptica suave en switches y controles."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.10-beta_(144)",
            date = "2026-08-11",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Alertas de Vencimiento: Notificaciones configurables en rangos de 1 a 15 días.",
                        "Umbral de Datos: Avisos proactivos ante consumo acelerado de megas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.5-beta_(139)",
            date = "2026-08-10",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Tarjetas Apiladas (Stacked Cards): Radios de curvatura jerárquicos (24dp superior, 4dp intermedio, 24dp inferior)."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Estabilidad USSD: Manejo robusto de errores en consultas telefónicas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.3-beta_(137)",
            date = "2026-08-09",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Vistas Previas Skeleton: Gráficos vectoriales estilo skeleton para previsualización de widgets 4x2, 2x1 y 3x2.",
                        "Redimensión Adaptativa 5x2: Soporte para pantallas anchas y launchers con grilla expandida.",
                        "Fechas y Vencimientos en Widget 2x1: Inclusión directa de días restantes y fecha de recarga.",
                        "Widget Gráfico 3x2: Gráfica de consumo de alta resolución con etiquetas de días en eje X."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.2-beta_(136)",
            date = "2026-08-09",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Sincronización Reactiva de Colores: Aplicación instantánea de paleta de alto contraste sin reiniciar la app.",
                        "Control Switch WCAG: Activación directa de paleta accesible en opciones de desarrollo.",
                        "Motor Vector Kotlin (Icons.Rounded): Priorización de vectores nativos Compose con esquinas redondeadas."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Estandarización de Preferencias: Sincronización instantánea de UI mediante megas_prefs."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.1-beta_(135)",
            date = "2026-08-09",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Auditoría WCAG Automática: Herramienta de inspección de luminancia y ratios WCAG AAA/AA.",
                        "Mapeador de Colores M3: Previsualización de paleta M3 Expressive accesible.",
                        "Diagnóstico de Iconos Rounded: Validación de trazados y vista ampliada para inspección.",
                        "Ajustes de Desarrollo M3: Disposición con bordes suaves y botones mínimos de 48dp."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.6.0-beta_(134)",
            date = "2026-08-09",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Rediseño M3 de Ajustes de Desarrollo: Tarjetas surfaceContainerHigh con tipografía jerárquica.",
                        "Icono PowerSettingsNew: Reemplazo de botón de cierre por interruptor destacado de desactivación."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Corrección de Vectores Rounded: Corrección de pathData en 75 vectores XML eliminando uniones aglomeradas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.9-beta_(133)",
            date = "2026-08-09",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Catálogo de Iconos Material: Sección en desarrollador que compara ImageVector con vector XML redondeado."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.8-beta_(132)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Iconografía Rounded Global: Actualización de todos los iconos de la app a variantes redondeadas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.7-beta_(131)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Iconos de Ajustes Redondeados: Variante Rounded en toda la pantalla de configuración."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.6-beta_(130)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Legibilidad en Modo Oscuro: Tonos violetas claros (Tono 80 y 90) para optimizar contraste.",
                        "Switches Más Visibles: Thumb interno blanco en modo oscuro para mayor claridad."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.5-beta_(129)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Badge Chip de Registros: Contador de registros en vista principal con silueta de píldora."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Contraste de Superficies: Ajuste sutil de luminancia en tarjetas flotantes sobre fondos oscuros."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Animación de PIN Suavizada: Eliminación de sacudida y retraso de restablecimiento tras código erróneo."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.4-beta_(128)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Rediseño Widget 2x1 M3 Expressive: Tarjetas M3 con márgenes optimizados y badge pill para días restantes.",
                        "Badge en Gráficos: Distintivo con recuento de registros en widget de consumo."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.REMOVED,
                    items = listOf(
                        "Limpieza de Widgets: Eliminación de variantes redundantes manteniendo widgets estándar principales."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.3-beta_(127)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Widget de Gráfico de Consumo: Sincronización en tiempo real con historial Room DB y contenedor M3.",
                        "Widgets 2x1 y 4x2 Expressive: Tarjetas dinámicas con valores iniciales en cero."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Indicadores PIN Circulares: Reemplazo de formas complejas por círculos uniformes para evitar cierres."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.2-beta_(126)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Desbloqueo Biométrico M3: Corner morphing al presionar accesos biométricos.",
                        "Configuración de Widgets en Vivo: Vista previa interactiva con tarjetas morphing."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Respuesta Acelerada de PIN: Respuesta inmediata de 0.5s ante PIN erróneo y remoción de entradas duplicadas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.1-beta_(125)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "M3 Expressive Shape Diversity: Selección de 4 formas geométricas únicas por intento de PIN.",
                        "Pool Geométrico Extendido: Formas de Cuadrado, Arco, Diamante, Semicírculo, Gema y Flecha.",
                        "Animación de Error Horizontal: Oscilaciones suaves M3 Bouncy y destello rojo fluido."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.5.0-beta_(124)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Indicadores de PIN Dinámicos: Formas geométricas al ingresar dígitos de seguridad.",
                        "Guía Rápida en Tarjetas Apiladas: Tarjetas segmentadas con bordes de 22dp e intermedios de 4dp."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Barra Flotante Simétrica: Márgenes calibrados en botones Comprar Plan y Guía."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.4.1-beta_(117)",
            date = "2026-08-08",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Barra de Límite Diario en Vivo: Visualización en tiempo real al activar interruptor en ajustes."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Auditoría de Accesibilidad M3: Roles semánticos y soporte TalkBack en toda la app."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Splash Screen Optimizado: Ejecución exclusiva al inicio de la aplicación."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.4.0-beta_(116)",
            date = "2026-08-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Gráfica Lineal Unificada: Organización en Ajustes de Gráficos e Indicadores.",
                        "Guías Numéricas de Referencia: Marcadores (100%, 50%, 0 MB) dibujados en gráfica lineal.",
                        "Interpolación de Color M3: Transición suave de color al alternar entre modo claro y oscuro."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.10-beta_(115)",
            date = "2026-08-07",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Personalización Modular: Tarjetas independientes para Tema, AMOLED, Fuentes y Color Dinámico.",
                        "Itinerario de Tour Guiado: Textos y pasos actualizados en Coach Mark interactivo."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Rediseño de Ajustes M3 Expressive: Tarjetas segmentadas con espaciado vertical de 3dp."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.10-beta_(98)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Desenfoque Progresivo Restaurado: Blur suave en BottomSheets.",
                        "Switches con Física Spring: Transiciones de color y física de resorte con thumb ampliado."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(97)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Segunda Alerta de Expiración: Deslizador configurable y confirmación interactiva.",
                        "Acciones en Notificación: Botones directos 'Comprar Plan' y 'Abrir Menú'."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(96)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Seguimiento de Deslizamiento: Sincronización en tiempo real de desenfoque al arrastrar modales.",
                        "Microinteracciones Limpias: Pulsaciones directas sin torsión en botones de acción."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(94)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Contraste de Tarjetas en Modo Oscuro: Reducción de luminancia para mayor confort visual.",
                        "Desvanecimiento Progresivo: Efecto fade-out en desenfoques modales."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.8-beta_(93)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Contraste Mejorado: Ajuste visual en superficies oscuras."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Accesos Rápidos: Grosor y trazado corregido en accesos directos de inicio."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(92)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Animaciones BottomSheet: Resplandor radial y transiciones de entrada.",
                        "Icono de Modo de Pruebas: Indicador visual tipo bug en Acerca de."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(91)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Optimización de Rendimiento: Sincronización en background eficiente y reducción de memoria en gráficas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(90)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Alertas Inteligentes: Ajuste de límites de datos."
                    )
                ),
                ChangelogSection(
                    category = ChangeCategory.FIXED,
                    items = listOf(
                        "Estabilidad USSD en doble SIM: Manejo robusto de consultas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(89)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Accesibilidad y Lectores de Pantalla: Refinamiento de etiquetas y bordes adaptativos."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(88)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Legibilidad en Planes: Presentación de paquetes ETECSA y desglose de saldo."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(87)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Selector de Acciones Rápidas: Respuesta háptica y tiempos de ejecución calibrados."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(86)",
            date = "2026-08-05",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Panel de Consumo Diario: Rediseño visual con micro-interacciones táctiles y fondos translúcidos."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.6-beta_(85)",
            date = "2026-08-04",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Gráfica en Cápsulas M3: Animación de cápsulas de consumo y micro-interacciones Expressive."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.5-beta_(84)",
            date = "2026-08-04",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Switches M3 Expressive: Iconos de estado (✓ y ✕) integrados en el interruptor."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.4-beta_(83)",
            date = "2026-08-04",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Motor UI M3 Expressive: Formas orgánicas y elevaciones dinámicas en toda la aplicación."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.3-RC1_(82)",
            date = "2026-08-04",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.CHANGED,
                    items = listOf(
                        "Optimización de Tipografías: Reducción de peso de fuentes e incremento de rendimiento en gráficas."
                    )
                )
            )
        ),
        ChangelogVersion(
            version = "0.3.1-beta_(80)",
            date = "2026-08-03",
            isLatest = false,
            sections = listOf(
                ChangelogSection(
                    category = ChangeCategory.ADDED,
                    items = listOf(
                        "Lanzamiento Inicial: Estabilización de consultas USSD, widgets y control de saldo y megas."
                    )
                )
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogBottomSheet(
    onDismiss: () -> Unit,
    onProgress: (Float) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    AppIcon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Historial de Cambios",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                ChangelogRepository.changelogList.forEach { item ->
                    ChangelogCard(item = item)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }

            // Top transparent fade blur gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                surfaceColor,
                                surfaceColor.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun ChangelogCard(item: ChangelogVersion) {
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
                            text = change,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
