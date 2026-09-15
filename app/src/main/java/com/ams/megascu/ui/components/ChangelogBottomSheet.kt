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

data class ChangelogVersion(
    val version: String,
    val date: String,
    val items: List<String>,
    val isLatest: Boolean = false
)

object ChangelogRepository {
    val changelogList = listOf(
        ChangelogVersion(
            version = "0.7.8-beta_(236)",
            date = "07 Sep 2026",
            isLatest = true,
            items = listOf(
                "Indicador de Desplazamiento Optimizado: Desenfoque de 2px en el fondo del indicador de bajar con estructura estática y animación de rebote aplicada exclusivamente al icono chevron interno.",
                "Sincronía del Botón Siguiente: Dimensiones y tamaño de icono unificados (20dp) entre la pantalla 6 y 7 para una transición perfecta sin saltos visuales.",
                "Botón Final en Píldora de Dos Líneas: Eliminación del icono de cohete, disposición del texto en dos líneas ('Comenzar Experiencia' / 'MegasCU'), ancho adaptado al contenido y extremos completamente redondeados.",
                "Fijación del Chip Indicador de Pasos: Altura constante en el encabezado superior para mantener el chip de paso completamente estable y sin saltos al pasar a la pantalla final."
            )
        ),
        ChangelogVersion(
            version = "0.7.7-beta_(235)",
            date = "07 Sep 2026",
            isLatest = false,
            items = listOf(
                "Margen Superior en Bienvenida: Margen incrementado en 4dp más (total 6dp) para una separación superior visualmente equilibrada y natural.",
                "Centrado de Elementos en Pantalla Final: Todo el contenido de la pantalla '¡Todo Listo!' ahora se encuadra en el centro vertical exacto de la ventana.",
                "Márgenes y Área de Botón Expandidos: Separación superior e inferior del botón final incrementadas significativamente, brindando un área táctil más amplia y destacada.",
                "Esquinas Completamente Redondeadas (Píldora): Extremos del botón totalmente redondeados en cápsula continua, preservando la animación elástica y transformación interactiva entre pantallas."
            )
        ),
        ChangelogVersion(
            version = "0.7.6-beta_(234)",
            date = "07 Sep 2026",
            isLatest = false,
            items = listOf(
                "Margen Superior en Bienvenida: Margen ajustado con precisión a 2dp en la pantalla inicial para máximo aprovechamiento del espacio visual.",
                "Difuminado Progresivo Inteligente: El desvanecimiento superior e inferior ahora solo se hace visible si la pantalla requiere desplazamiento, apareciendo de manera suave y progresiva con el scroll.",
                "Transformación Continua del Botón Final: El botón 'Siguiente' en la pantalla 7 se desliza y transforma fluidamente en 'Comenzar Experiencia MegasCU' debajo de las tarjetas en la pantalla final, interpolando su tamaño medio/grande y posición de forma interactiva entre pantallas.",
                "Indicador de Contenido Inferior Ampliado: Señalización de scroll expandida 2dp con fondo difuminado de 3px y activa tanto en la pantalla 2 (Diseñado para Cubacel) como en la pantalla 6 (Alertas y Sincronización)."
            )
        ),
        ChangelogVersion(
            version = "0.7.5-beta_(233)",
            date = "07 Sep 2026",
            isLatest = false,
            items = listOf(
                "Botón de Bienvenida Debajo de Tarjetas: Botón de tamaño medio ubicado directamente bajo las tarjetas en la última pantalla, con animación suave y fluida que se desplaza y transforma desde el botón 'Siguiente'.",
                "Márgenes Superiores Optimizados: Reducción de márgenes superiores del logo en la primera y última pantalla de Bienvenida para garantizar que todo el contenido sea visible sin necesidad de scroll.",
                "Señalización de Scroll Inferior: Indicador visual en cápsula con bordes redondeados y flecha animada sutil en la segunda pantalla que señala la presencia de más contenido abajo.",
                "Pull-to-Refresh Perfeccionado: El indicador emerge desde más arriba en la barra de estado e incrementa su tamaño de forma progresiva hasta su tamaño real al tirar de la pantalla.",
                "Optimización de Tamaño de APK: Retorno al tamaño ligero de ~2.55 MB mediante compresión optimizada en el empaquetado del archivo."
            )
        ),
        ChangelogVersion(
            version = "0.7.4-beta_(232)",
            date = "07 Sep 2026",
            isLatest = false,
            items = listOf(
                "Seguridad en Bienvenida: Al configurar el PIN de acceso durante la bienvenida, la app muestra confirmación inmediata de configuración correcta sin solicitar desbloqueo prematuro.",
                "Tarjeta Segmentada de Permisos en Bienvenida: Agrupación visual en 3 bloques claros (Teléfono/USSD, Notificaciones y Estadísticas de Uso) para otorgar permisos individuales o conjuntos.",
                "Alerta de Vencimiento Simplificada: Eliminación del interruptor redundante en Bienvenida, manteniendo exclusivamente la selección rápida de frecuencia de días de anticipación.",
                "Transición Animada Fluida de Navegación: El botón de 'Siguiente' se transforma suavemente en 'Comenzar Experiencia MegasCU' con animación elástica al pasar al paso final.",
                "Opciones de Desarrollo Optimizadas: Disposición en 2 filas y 2 columnas para la selección de tintas en el Catálogo de Iconos M3, botones de prueba de temas reubicados debajo del título en la Auditoría WCAG, y encabezados completamente desfijados para un scroll natural."
            )
        ),
        ChangelogVersion(
            version = "0.7.3-beta_(231)",
            date = "07 Sep 2026",
            isLatest = false,
            items = listOf(
                "Reubicación del Estimador Alternativo de Consumo: El interruptor y descripción del estimador alternativo se integraron de forma armónica como tarjeta intermedia dentro del bloque segmentado de 'Gráficos e Indicadores' en la ventana de Ajustes.",
                "Estandarización de Modo Simple en Ajustes: Contenedor superior independiente y limpio para la activación y configuración del Modo Simple.",
                "Normalización M3 Expressive: Estandarización de botones y componentes interactivos con feedback háptico táctil y transiciones elásticas adaptativas en toda la aplicación."
            )
        ),
        ChangelogVersion(
            version = "0.7.2-beta_(218)",
            date = "30 Ago 2026",
            isLatest = false,
            items = listOf(
                "Tipografía Expresiva en Guía de Actualización: Rediseño visual del texto 'Pulsa aquí para actualizar' con estilo de caligrafía expresiva y sombreado de alto contraste.",
                "Trazo Discontinuo (Dashed) de Señalización: Flechas de indicación dibujadas con línea discontinua elegante y uniforme tanto en Modo Normal como en Modo Simple.",
                "Indicador de Conexión Activa (Ping Dinámico): Monitor discreto 'Ping: 0,04 ms' en la parte inferior de la pantalla con actualización periódica y punto de estado sutil en tiempo real.",
                "Tarjeta Unificada de Permisos en Bienvenida: Agrupación armónica de permisos con diseño segmentado e interactividad táctil."
            )
        ),
        ChangelogVersion(
            version = "0.7.0-beta_(216)",
            date = "29 Ago 2026",
            isLatest = false,
            items = listOf(
                "Terminal Retro Easter Egg Desacoplada de Edge-to-Edge: Eliminación de insets dinámicos conflictivos de barras del sistema en el overlay de la terminal, garantizando compatibilidad total en todas las capas de personalización (HyperOS, MIUI, OneUI, AOSP).",
                "Elevación Firme de Elementos Inferiores (+48dp): Margen inferior rígido de seguridad que garantiza que la caja de estado y el botón [ CERRAR TERMINAL ] queden 100% visibles, elevados y cómodamente accesibles por encima de cualquier barra de gestos o botones de navegación.",
                "Escritura Retro Humanizada con Autocorrección: Cadencia de tecleo natural con micro-vacilaciones, correcciones en tiempo real (Backspace con sonido DOS) y crónica histórica de telecomunicaciones incluyendo las resoluciones y tarifas de 2025.",
                "Conmutación Inteligente de Tema: Cambio dinámico e instantáneo a Negro AMOLED durante la sesión de terminal y restauración fiel del tema previo al cerrarla."
            )
        ),
        ChangelogVersion(
            version = "0.6.81-beta_(215)",
            date = "29 Ago 2026",
            isLatest = false,
            items = listOf(
                "Indicador de Actualización Exclusivo para Gesto: El spinner de 'Hala para actualizar' (Pull-to-refresh) solo se activa y se muestra cuando el usuario realiza el gesto en pantalla.",
                "Posicionamiento Superior sobre la Barra de Título: El indicador de carga ahora se superpone flotante por encima de la barra de título con elevación Z-index y safe padding, evitando que quede oculto tras el texto del encabezado.",
                "Sincronización Inteligente de Estados: Desacoplamiento de las animaciones de botones y barras lineales de consulta para evitar indicadores duplicados durante acciones secundarias."
            )
        ),
        ChangelogVersion(
            version = "0.6.80-beta_(214)",
            date = "29 Ago 2026",
            isLatest = false,
            items = listOf(
                "Diseño Compacto y Resumido: Textos claros y concisos en la alerta de permiso de uso, evitando scrolls innecesarios.",
                "Efecto de Difuminado en Bordes (Fading Edges): Gradientes suaves superior e inferior para evitar cortes abruptos de contenido.",
                "Desenfoque de Fondo (Window Blur Behind): Fondo con desenfoque nativo y scrim para mayor enfoque y elegancia visual.",
                "Márgenes Simétricos Edge-to-Edge: Disposición armónica con márgenes uniformes en los cuatro lados de la pantalla."
            )
        ),
        ChangelogVersion(
            version = "0.6.79-beta_(213)",
            date = "29 Ago 2026",
            isLatest = false,
            items = listOf(
                "Diálogo Explicativo de Permiso de Uso: Alerta interactiva con diseño Material 3 Expressive que clarifica en lenguaje claro y transparente por qué se necesita el acceso a estadísticas de datos, cómo protege la privacidad del usuario y los pasos sencillos para activarlo en Ajustes.",
                "Prevención de Fricción en Permisos: Flujo guiado previo a la apertura de la pantalla de ajustes de Android para evitar abandonos y mejorar la experiencia de usuario.",
                "Integración Unificada en UI: Activación del diálogo explicativo desde la configuración, onboarding y al tocar el contador de consumo diario.",
                "Keystore de Producción Oficial: Integración del almacén de claves release-key.jks con firma digital RSA 4096-bit y validez hasta 2054."
            )
        ),
        ChangelogVersion(
            version = "0.6.78-beta_(212)",
            date = "29 Ago 2026",
            isLatest = false,
            items = listOf(
                "Restauración de Iconografía Nativa Material M3: Reversión completa de la fuente Material Symbols y eliminación de código relacionado, adoptando exclusivamente la iconografía vectorial nativa de Jetpack Compose.",
                "Optimización de Rendimiento y Tamaño: Reducción significativa del tamaño del APK y del consumo de memoria al suprimir la carga de fuentes de glifos de símbolos.",
                "Catálogo de Iconos Material M3: Actualización del explorador de iconografía con búsqueda y visor de componentes de interfaz.",
                "Limpieza de Arquitectura: Eliminación de selectores y CompositionLocals redundantes, manteniendo una arquitectura limpia y reactiva."
            )
        ),
        ChangelogVersion(
            version = "0.6.77-beta_(211)",
            date = "28 Ago 2026",
            isLatest = false,
            items = listOf(
                "Material Symbols 48pt Regular: Actualización de la fuente de iconos a peso Regular eliminando grosores Thin y Light, logrando trazos más definidos y legibles en toda la app.",
                "Selector de Iconografía Conectado: Grupo de botones segmentados en pantalla de fuentes y opciones de desarrollo para alternar entre Nativo (Vector M3), M3 Filled y M3 Outlined en vivo.",
                "Icono Dual SIM Dinámico: Adaptación del icono compuesto de doble SIM con profundidad geométrica y bisel perimetral opaco para los tres estilos de iconos.",
                "Auditoría del Historial: Reestructuración integral de registros con descripciones técnicas y redacción fluida."
            )
        ),
        ChangelogVersion(
            version = "0.6.76-beta_(210)",
            date = "28 Ago 2026",
            isLatest = false,
            items = listOf(
                "Motor Tipográfico de Iconos: Integración preliminar de Material Symbols Rounded y verificación estricta de hashes SHA-256 en Gradle.",
                "Optimización de Renderizado: Normalización de contenedores BoxWithConstraints y mapeo semántico para TalkBack."
            )
        ),
        ChangelogVersion(
            version = "0.6.75-beta_(209)",
            date = "28 Ago 2026",
            isLatest = false,
            items = listOf(
                "Mapeador Centralizado MaterialIconsCatalog: Arquitectura de resolución de nombres de iconos Material y correspondencia vectorial.",
                "Auditoría de Glifos: Validación de compatibilidad con Google Fonts y verificación de integridad binaria."
            )
        ),
        ChangelogVersion(
            version = "0.6.74-beta_(208)",
            date = "27 Ago 2026",
            isLatest = false,
            items = listOf(
                "Depuración de Permisos USSD: Estabilización de interceptores telefónicos y diálogos ante rechazos transitorios.",
                "Sincronización en Segundo Plano: Calibración de alarmas en WorkManager para mitigar consumo de batería."
            )
        ),
        ChangelogVersion(
            version = "0.6.73-beta_(207)",
            date = "27 Ago 2026",
            isLatest = false,
            items = listOf(
                "Limpieza de Telemetría: Supresión de logs redundantes durante la consulta y cálculo de saldos.",
                "Microinteracciones Hápticas: Respuesta táctil calibrada en teclados numéricos y botones de acción rápida."
            )
        ),
        ChangelogVersion(
            version = "0.6.72-beta_(206)",
            date = "27 Ago 2026",
            isLatest = false,
            items = listOf(
                "Bisel Dinámico en Tarjetas: Antialiasing optimizado en componentes flotantes y hojas modales Haze.",
                "Gestión de Desenfoque: Fallback automático y suave en dispositivos con aceleración GPU limitada."
            )
        ),
        ChangelogVersion(
            version = "0.6.71-beta_(205)",
            date = "27 Ago 2026",
            isLatest = false,
            items = listOf(
                "Contraste en Modo Simple: Texto con contraste forzado en botones y eliminación de parpadeos de color.",
                "Caché Tipográfica Space Mono: Carga optimizada en memoria de fuentes TrueType monoespaciadas."
            )
        ),
        ChangelogVersion(
            version = "0.6.70-beta_(204)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Persistencia de Preferencias: Consolidación de claves en SharedPreferences para evitar pérdidas de estado.",
                "Alineación en Gráficas: Corrección de desfases en etiquetas de ejes de consumo semanal."
            )
        ),
        ChangelogVersion(
            version = "0.6.69-beta_(203)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Alineación de Botones en Menú: Unificación visual de selectores y espaciados simétricos.",
                "Protección en Parsing: Mayor tolerancia ante respuestas USSD truncadas o con caracteres atípicos."
            )
        ),
        ChangelogVersion(
            version = "0.6.68-beta_(202)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Servicios Foreground Seguros: Prevención de excepciones en Android 12+ mediante fallback a NotificationManager.",
                "Concurrencia USSD Thread-Safe: Bloqueo seguro con radioMutex en corrutinas suspendibles con liberación inmediata.",
                "Aislamiento Transaccional: Sincronización atómica multi-SIM sin corrupción de estado en Room DB."
            )
        ),
        ChangelogVersion(
            version = "0.6.67-beta_(201)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Sincronización Dual-SIM: Caché y lógica independiente por ranura SIM en widgets 4x2, 2x1 y gráficas.",
                "Programación Segura de Sync: Límite mínimo de demora inicial para prevenir ejecuciones redundantes."
            )
        ),
        ChangelogVersion(
            version = "0.6.66-beta_(200)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Diálogo de Permisos Telefónicos: Advertencia M3 informativa para habilitar llamadas USSD de saldo.",
                "Formateo Dinámico de Bonos: Conversión automática a GB para bolsas nacionales iguales o mayores a 1024 MB.",
                "TestTags y Accesibilidad: Identificadores semánticos en componentes interactivos."
            )
        ),
        ChangelogVersion(
            version = "0.6.65-beta_(199)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Validación de Paquetes: Desglose y validación precisa de bolsas LTE e Internacionales.",
                "Optimización de Memoria: Reducción de recomposiciones innecesarias en el feed principal."
            )
        ),
        ChangelogVersion(
            version = "0.6.64-beta_(198)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Elevaciones Tonales M3: Pulido de radios de curvatura y contrastes de superficie.",
                "Gestión de Excepciones: Manejo defensivo en canales de notificación."
            )
        ),
        ChangelogVersion(
            version = "0.6.63-beta_(197)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "ExpirationWorker Autónomo: Actualización de expiración y cálculo de días sin requerir datos móviles activos.",
                "SyncWorker Robusto: Manejo seguro de cancelación cooperativa y control de excepciones en WorkManager.",
                "Control de Concurrencia en ViewModel: Prevención de llamadas solapadas en actualización de estado."
            )
        ),
        ChangelogVersion(
            version = "0.6.62-beta_(196)",
            date = "26 Ago 2026",
            isLatest = false,
            items = listOf(
                "Limpieza de Permisos: Supresión de verificaciones SMS innecesarias tras migración completa a USSD.",
                "WorkManager Bajo Demanda: Inicialización delegada a AndroidX eliminando overhead en Application.",
                "Centralización de Cadenas: Migración de textos a strings.xml para internacionalización."
            )
        ),
        ChangelogVersion(
            version = "0.6.53-beta_(187)",
            date = "23 Ago 2026",
            isLatest = false,
            items = listOf(
                "Icono Dual SIM con Contorno Sólido: Bisel perimetral opaco idéntico al color de tarjeta sin halos translúcidos.",
                "Handles Modales Limpios: Eliminación del marco de foco al presionar tiradores de BottomSheets.",
                "Alineación Izquierda: Títulos alineados con el margen del contenido en hojas modales."
            )
        ),
        ChangelogVersion(
            version = "0.6.45-beta_(179)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Conteo Numérico Progresivo: Animación fluida de 0 al valor actual en encabezados de consumo y límites.",
                "Chips Flotantes en Píldora: Geometría de píldora estilizada centrada a 5dp sobre crestas de gráficas y barras."
            )
        ),
        ChangelogVersion(
            version = "0.6.44-beta_(178)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Desenfoque y Oscurecimiento Progresivo: Difuminado reactivo sincronizado con el arrastre de BottomSheets."
            )
        ),
        ChangelogVersion(
            version = "0.6.43-beta_(177)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Expansión Proporcional M3 Expressive: Botones conectados con física spring que amplían el botón seleccionado mientras contraen los demás."
            )
        ),
        ChangelogVersion(
            version = "0.6.42-beta_(176)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Transiciones Post-Splash: Tiempos extendidos para entrada coordinada de números, barras y gráficas.",
                "Chips de Gráfica sin Cortes: Aparición directa con esquinas redondeadas sin parpadeo rectangular."
            )
        ),
        ChangelogVersion(
            version = "0.6.41-beta_(175)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Sincronización de Límite Diario: Animación progresiva incremental para consumo de hoy y límite diario.",
                "Tonalidad Violeta MegasCU: Colores violetas profundos (#4A00DF) en botones no seleccionados en modo oscuro y AMOLED."
            )
        ),
        ChangelogVersion(
            version = "0.6.40-beta_(174)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Barra Inferior Flotante Limpia: Eliminación de sombras traseras sobre el botón de actualización.",
                "Botones Conectados por Defecto: Estandarización de grupos segmentados en Ajustes y Bienvenida.",
                "Cálculo de Recarga de Saldo: Ajuste de disponibilidad al día siguiente hábil con opción de alerta."
            )
        ),
        ChangelogVersion(
            version = "0.6.39-beta_(173)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Cálculos de Recarga: Ajuste en fechas de vigencia y recarga de saldo.",
                "Tipografía Space Mono: Aplicación en referencias numéricas de gráficas."
            )
        ),
        ChangelogVersion(
            version = "0.6.35-beta_(169)",
            date = "22 Ago 2026",
            isLatest = false,
            items = listOf(
                "Migración Nativa de BottomSheet: Extensión limpia edge-to-edge hasta la barra de navegación del sistema.",
                "Predictive Back Gestures: Soporte oficial de gesto predictivo atrás en todas las ventanas modales y Acerca de."
            )
        ),
        ChangelogVersion(
            version = "0.6.34-beta_(168)",
            date = "21 Ago 2026",
            isLatest = false,
            items = listOf(
                "Refinamiento M3 Expressive: Respuesta háptica optimizada y contraste dinámico según luminancia.",
                "Elevación Tonal: Sombras y jerarquía en tarjetas secundarias agrupadas."
            )
        ),
        ChangelogVersion(
            version = "0.6.33-beta_(167)",
            date = "20 Ago 2026",
            isLatest = false,
            items = listOf(
                "Curvas de Consumo: Renderizado optimizado de indicadores lineales y límites de datos.",
                "Fluidez en Animaciones: Curvas de resorte sin micro-tirones."
            )
        ),
        ChangelogVersion(
            version = "0.6.32-beta_(166)",
            date = "18 Ago 2026",
            isLatest = false,
            items = listOf(
                "Haze Blur Reactivo: Reducción de consumo de GPU y memoria en fondos desenfocados.",
                "Morphing Geométrico: Transiciones suaves en botones e indicadores de seguridad PIN."
            )
        ),
        ChangelogVersion(
            version = "0.6.30-beta_(164)",
            date = "17 Ago 2026",
            isLatest = false,
            items = listOf(
                "Auditoría Binaria Byte a Byte: Verificación estricta de integridad en recursos tipográficos y WebP.",
                "Seguridad Biométrica: Protección de compras de planes y acceso general mediante huella dactilar."
            )
        ),
        ChangelogVersion(
            version = "0.6.25-beta_(159)",
            date = "15 Ago 2026",
            isLatest = false,
            items = listOf(
                "Widgets de Escritorio: Sincronización en segundo plano de saldos y paquetes de datos.",
                "Gestión de Batería: Optimización de ciclos de sondeo en reposo."
            )
        ),
        ChangelogVersion(
            version = "0.6.20-beta_(154)",
            date = "14 Ago 2026",
            isLatest = false,
            items = listOf(
                "Paletas de Color Dinámicas: Compatibilidad con Monet en Android 12+.",
                "Tema AMOLED Puro: Modo negro ultra profundo para paneles OLED."
            )
        ),
        ChangelogVersion(
            version = "0.6.15-beta_(149)",
            date = "12 Ago 2026",
            isLatest = false,
            items = listOf(
                "Navegación por Gestos: Barra de navegación transparente edge-to-edge.",
                "Microinteracciones: Respuesta háptica suave en switches y controles."
            )
        ),
        ChangelogVersion(
            version = "0.6.10-beta_(144)",
            date = "11 Ago 2026",
            isLatest = false,
            items = listOf(
                "Alertas de Vencimiento: Notificaciones configurables en rangos de 1 a 15 días.",
                "Umbral de Datos: Avisos proactivos ante consumo acelerado de megas."
            )
        ),
        ChangelogVersion(
            version = "0.6.5-beta_(139)",
            date = "10 Ago 2026",
            isLatest = false,
            items = listOf(
                "Tarjetas Apiladas (Stacked Cards): Radios de curvatura jerárquicos (24dp superior, 4dp intermedio, 24dp inferior).",
                "Estabilidad USSD: Manejo robusto de errores en consultas telefónicas."
            )
        ),
        ChangelogVersion(
            version = "0.6.3-beta_(137)",
            date = "09 Ago 2026",
            isLatest = false,
            items = listOf(
                "Vistas Previas Skeleton: Gráficos vectoriales estilo skeleton para previsualización de widgets 4x2, 2x1 y 3x2.",
                "Redimensión Adaptativa 5x2: Soporte para pantallas anchas y launchers con grilla expandida.",
                "Fechas y Vencimientos en Widget 2x1: Inclusión directa de días restantes y fecha de recarga.",
                "Widget Gráfico 3x2: Gráfica de consumo de alta resolución con etiquetas de días en eje X."
            )
        ),
        ChangelogVersion(
            version = "0.6.2-beta_(136)",
            date = "09 Ago 2026",
            isLatest = false,
            items = listOf(
                "Sincronización Reactiva de Colores: Aplicación instantánea de paleta de alto contraste sin reiniciar la app.",
                "Control Switch WCAG: Activación directa de paleta accesible en opciones de desarrollo.",
                "Motor Vector Kotlin (Icons.Rounded): Priorización de vectores nativos Compose con esquinas redondeadas.",
                "Estandarización de Preferencias: Sincronización instantánea de UI mediante megas_prefs."
            )
        ),
        ChangelogVersion(
            version = "0.6.1-beta_(135)",
            date = "09 Ago 2026",
            isLatest = false,
            items = listOf(
                "Auditoría WCAG Automática: Herramienta de inspección de luminancia y ratios WCAG AAA/AA.",
                "Mapeador de Colores M3: Previsualización de paleta M3 Expressive accesible.",
                "Diagnóstico de Iconos Rounded: Validación de trazados y vista ampliada para inspección.",
                "Ajustes de Desarrollo M3: Disposición con bordes suaves y botones mínimos de 48dp."
            )
        ),
        ChangelogVersion(
            version = "0.6.0-beta_(134)",
            date = "09 Ago 2026",
            isLatest = false,
            items = listOf(
                "Corrección de Vectores Rounded: Corrección de pathData en 75 vectores XML eliminando uniones aglomeradas.",
                "Rediseño M3 de Ajustes de Desarrollo: Tarjetas surfaceContainerHigh con tipografía jerárquica.",
                "Icono PowerSettingsNew: Reemplazo de botón de cierre por interruptor destacado de desactivación."
            )
        ),
        ChangelogVersion(
            version = "0.5.9-beta_(133)",
            date = "09 Ago 2026",
            isLatest = false,
            items = listOf(
                "Catálogo de Iconos Material: Sección en desarrollador que compara ImageVector con vector XML redondeado."
            )
        ),
        ChangelogVersion(
            version = "0.5.8-beta_(132)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Iconografía Rounded Global: Actualización de todos los iconos de la app a variantes redondeadas."
            )
        ),
        ChangelogVersion(
            version = "0.5.7-beta_(131)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Iconos de Ajustes Redondeados: Variante Rounded en toda la pantalla de configuración."
            )
        ),
        ChangelogVersion(
            version = "0.5.6-beta_(130)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Legibilidad en Modo Oscuro: Tonos violetas claros (Tono 80 y 90) para optimizar contraste.",
                "Switches Más Visibles: Thumb interno blanco en modo oscuro para mayor claridad."
            )
        ),
        ChangelogVersion(
            version = "0.5.5-beta_(129)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Animación de PIN Suavizada: Eliminación de sacudida y retraso de restablecimiento tras código erróneo.",
                "Badge Chip de Registros: Contador de registros en vista principal con silueta de píldora.",
                "Contraste de Superficies: Ajuste sutil de luminancia en tarjetas flotantes sobre fondos oscuros."
            )
        ),
        ChangelogVersion(
            version = "0.5.4-beta_(128)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Limpieza de Widgets: Eliminación de variantes redundantes manteniendo widgets estándar principales.",
                "Rediseño Widget 2x1 M3 Expressive: Tarjetas M3 con márgenes optimizados y badge pill para días restantes.",
                "Badge en Gráficos: Distintivo con recuento de registros en widget de consumo."
            )
        ),
        ChangelogVersion(
            version = "0.5.3-beta_(127)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Indicadores PIN Circulares: Reemplazo de formas complejas por círculos uniformes para evitar cierres.",
                "Widget de Gráfico de Consumo: Sincronización en tiempo real con historial Room DB y contenedor M3.",
                "Widgets 2x1 y 4x2 Expressive: Tarjetas dinámicas con valores iniciales en cero."
            )
        ),
        ChangelogVersion(
            version = "0.5.2-beta_(126)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Desbloqueo Biométrico M3: Corner morphing al presionar accesos biométricos.",
                "Respuesta Acelerada de PIN: Respuesta inmediata de 0.5s ante PIN erróneo y remoción de entradas duplicadas.",
                "Configuración de Widgets en Vivo: Vista previa interactiva con tarjetas morphing."
            )
        ),
        ChangelogVersion(
            version = "0.5.1-beta_(125)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "M3 Expressive Shape Diversity: Selección de 4 formas geométricas únicas por intento de PIN.",
                "Pool Geométrico Extendido: Formas de Cuadrado, Arco, Diamante, Semicírculo, Gema y Flecha.",
                "Animación de Error Horizontal: Oscilaciones suaves M3 Bouncy y destello rojo fluido."
            )
        ),
        ChangelogVersion(
            version = "0.5.0-beta_(124)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Indicadores de PIN Dinámicos: Formas geométricas al ingresar dígitos de seguridad.",
                "Barra Flotante Simétrica: Márgenes calibrados en botones Comprar Plan y Guía.",
                "Guía Rápida en Tarjetas Apiladas: Tarjetas segmentadas con bordes de 22dp e intermedios de 4dp."
            )
        ),
        ChangelogVersion(
            version = "0.4.1-beta_(117)",
            date = "08 Ago 2026",
            isLatest = false,
            items = listOf(
                "Auditoría de Accesibilidad M3: Roles semánticos y soporte TalkBack en toda la app.",
                "Barra de Límite Diario en Vivo: Visualización en tiempo real al activar interruptor en ajustes.",
                "Splash Screen Optimizado: Ejecución exclusiva al inicio de la aplicación."
            )
        ),
        ChangelogVersion(
            version = "0.4.0-beta_(116)",
            date = "07 Ago 2026",
            isLatest = false,
            items = listOf(
                "Gráfica Lineal Unificada: Organización en Ajustes de Gráficos e Indicadores.",
                "Guías Numéricas de Referencia: Marcadores (100%, 50%, 0 MB) dibujados en gráfica lineal.",
                "Interpolación de Color M3: Transición suave de color al alternar entre modo claro y oscuro."
            )
        ),
        ChangelogVersion(
            version = "0.3.10-beta_(115)",
            date = "07 Ago 2026",
            isLatest = false,
            items = listOf(
                "Rediseño de Ajustes M3 Expressive: Tarjetas segmentadas con espaciado vertical de 3dp.",
                "Personalización Modular: Tarjetas independientes para Tema, AMOLED, Fuentes y Color Dinámico.",
                "Itinerario de Tour Guiado: Textos y pasos actualizados en Coach Mark interactivo."
            )
        ),
        ChangelogVersion(
            version = "0.3.10-beta_(98)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Desenfoque Progresivo Restaurado: Blur suave en BottomSheets.",
                "Switches con Física Spring: Transiciones de color y física de resorte con thumb ampliado."
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(97)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Segunda Alerta de Expiración: Deslizador configurable y confirmación interactiva.",
                "Acciones en Notificación: Botones directos 'Comprar Plan' y 'Abrir Menú'."
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(96)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Seguimiento de Deslizamiento: Sincronización en tiempo real de desenfoque al arrastrar modales.",
                "Microinteracciones Limpias: Pulsaciones directas sin torsión en botones de acción."
            )
        ),
        ChangelogVersion(
            version = "0.3.9-beta_(94)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Contraste de Tarjetas en Modo Oscuro: Reducción de luminancia para mayor confort visual.",
                "Desvanecimiento Progresivo: Efecto fade-out en desenfoques modales."
            )
        ),
        ChangelogVersion(
            version = "0.3.8-beta_(93)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Contraste Mejorado: Ajuste visual en superficies oscuras.",
                "Accesos Rápidos: Grosor y trazado corregido en accesos directos de inicio."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(92)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Animaciones BottomSheet: Resplandor radial y transiciones de entrada.",
                "Icono de Modo de Pruebas: Indicador visual tipo bug en Acerca de."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(91)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Optimización de Rendimiento: Sincronización en background eficiente y reducción de memoria en gráficas."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(90)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Alertas Inteligentes: Ajuste de límites de datos y estabilidad USSD en doble SIM."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(89)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Accesibilidad y Lectores de Pantalla: Refinamiento de etiquetas y bordes adaptativos."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(88)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Legibilidad en Planes: Presentación de paquetes ETECSA y desglose de saldo."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(87)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Selector de Acciones Rápidas: Respuesta háptica y tiempos de ejecución calibrados."
            )
        ),
        ChangelogVersion(
            version = "0.3.7-beta_(86)",
            date = "05 Ago 2026",
            isLatest = false,
            items = listOf(
                "Panel de Consumo Diario: Rediseño visual con micro-interacciones táctiles y fondos translúcidos."
            )
        ),
        ChangelogVersion(
            version = "0.3.6-beta_(85)",
            date = "04 Ago 2026",
            isLatest = false,
            items = listOf(
                "Gráfica en Cápsulas M3: Animación de cápsulas de consumo y micro-interacciones Expressive."
            )
        ),
        ChangelogVersion(
            version = "0.3.5-beta_(84)",
            date = "04 Ago 2026",
            isLatest = false,
            items = listOf(
                "Switches M3 Expressive: Iconos de estado (✓ y ✕) integrados en el interruptor."
            )
        ),
        ChangelogVersion(
            version = "0.3.4-beta_(83)",
            date = "04 Ago 2026",
            isLatest = false,
            items = listOf(
                "Motor UI M3 Expressive: Formas orgánicas y elevaciones dinámicas en toda la aplicación."
            )
        ),
        ChangelogVersion(
            version = "0.3.3-RC1_(82)",
            date = "04 Ago 2026",
            isLatest = false,
            items = listOf(
                "Optimización de Tipografías: Reducción de peso de fuentes e incremento de rendimiento en gráficas."
            )
        ),
        ChangelogVersion(
            version = "0.3.1-beta_(80)",
            date = "03 Ago 2026",
            isLatest = false,
            items = listOf(
                "Lanzamiento Inicial: Estabilización de consultas USSD, widgets y control de saldo y megas."
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
                    Text(
                        text = "Historial de Cambios",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

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
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            item.items.forEach { change ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
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
