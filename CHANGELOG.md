# Registro de Cambios (Changelog)

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog v1.1.0](https://keepachangelog.com/es-ES/1.1.0/),
y este proyecto se adhiere a [Semantic Versioning v2.0.0](https://semver.org/lang/es/).

---

## [0.8.6-beta_(249)] - 2026-09-18

### Añadido
- Soporte nativo de Markdown (`MarkdownChangelog`) en la ventana de actualización (`UpdateAvailableDialog`), renderizando con formato estructurado (encabezados, viñetas, bloques de código, negritas, cursivas y citas) las notas de versión recibidas desde GitHub.

### Cambiado
- Sustitución de las etiquetas textuales "Instalada" y "Nueva" por iconos vectoriales Material Symbols (`PhoneAndroid` y `CloudDownload`) en las insignias de comparación de versiones de la ventana de actualización (`UpdateAvailableDialog`), logrando una interfaz más compacta, limpia y moderna.
- Desactivación predeterminada del "Estimador Alternativo de Consumo" en nuevas instalaciones, priorizando la medición directa de la red del sistema.
- Refactorización de la animación táctil en botones expresivos (`ExpressiveButton`, `ExpressiveTextButton`, `ExpressiveOutlinedButton`, `ExpressiveFilledTonalButton`, `ExpressiveIconButton`, `ExpressiveFloatingActionButton`) y modificadores de pulsación (`expressiveClick`, `expressivePressEffect`): se eliminó la distorsión y ensanchamiento de las etiquetas de texto (`scaleX`/`scaleY`), manteniendo intacta la metamorfosis dinámica de esquinas (shape morphing) y la respuesta háptica táctil.

### Corregido
- Corrección de la legibilidad y escala tipográfica en botones interactivos al ser presionados, evitando el efecto de deformación en el texto interior durante la pulsación.

---

## [0.8.5-beta_(248)] - 2026-09-16

### Añadido
- Componente de comprobación silenciosa en segundo plano (`AppLaunchUpdateChecker`): realiza una verificación rápida con el servidor de GitHub cada vez que se abre la aplicación y despliega automáticamente la ventana de actualización configurada si existe una versión más reciente.
- Sistema de alerta de novedades al primer inicio tras actualizar (`WhatsNewDialog`): presenta de forma clara y visual los cambios y mejoras incorporados en la nueva versión instalada con acceso directo al registro histórico completo.
- Sombra de elevación sutil y refinada con bordes redondeados orgánicos en la barra de navegación flotante inferior (`MegasBottomBar`), mejorando la jerarquía visual y profundidad bajo Material 3 Expressive.
- Transición animada suave con retardo coordinado al seleccionar una acción en `QuickActionSelectorBottomSheet`, asegurando un cierre fluido del modal antes del retorno a la pantalla de inicio.

### Cambiado
- Actualización de la etiqueta del botón de comprobación en la sección de Ajustes a `"Buscar Actualización"`.
- Optimización visual de las insignias de comparación de versiones en la ventana de actualización, garantizando presentación en una sola línea continua con protección de desbordamiento de texto.
- Refactorización de superficies y bordes en diálogos modales (Novedades de la Versión y Alerta de Actualización), asegurando contornos suaves y esquinas redondeadas continuas sin cortes de trazo.

### Corregido
- Corrección de saltos de línea indeseados en números e indicadores métricos (GB, MB, SMS, Minutos, CUP) en la pantalla de inicio: ahora mantienen posiciones fijas, espacios no separables (`\u00A0`), alineación centrada y textos subordinados multilínea para pantallas pequeñas o con baja densidad.

---

## [0.8.4-beta_(247)] - 2026-09-16

### Añadido
- Componente de comprobación silenciosa en background (`AppLaunchUpdateChecker`): realiza una verificación rápida con el servidor de GitHub cada vez que se abre la aplicación y despliega automáticamente la ventana de actualización configurada si existe una versión más reciente.
- Animación de cierre suave y retroalimentación háptica en la ventana de configuración de acción rápida (`QuickActionSelectorBottomSheet`), permitiendo una transición fluida hacia la pantalla principal al seleccionar una opción sin saltos bruscos.
- Sombra de elevación sutil y refinada con bordes redondeados orgánicos en la barra de navegación flotante inferior (`MegasBottomBar`), mejorando la jerarquía visual y profundidad bajo Material 3 Expressive.

### Cambiado
- Actualización de la etiqueta del botón de actualización en la sección de Ajustes (`SettingsBottomSheet`) a `"Buscar Actualización"`.

---

## [0.8.3-beta_(246)] - 2026-09-16

### Corregido
- Corrección de la dirección del repositorio oficial de GitHub a `mikel-ams/MegasCU` en `GitHubUpdateChecker` y `SettingsBottomSheet`, resolviendo el problema por el cual el actualizador no detectaba las nuevas versiones y releases publicados.
- Migración automática en tiempo de ejecución de las preferencias guardadas (`pref_github_repo`) hacia la dirección oficial `mikel-ams/MegasCU`.
- Búsqueda y evaluación multi-candidato de releases en la API de GitHub (`per_page=10`) para comparar y seleccionar con precisión el release con mayor código de compilación y versión semántica disponible.
- Soporte robusto de patrones de etiquetado en `extractBuildCode` para formatos `v0.8.2-beta_(245)`, `0.8.2-beta_(245)`, `v0.8.2_245`, `v0.8.2-245` y builds con espacios.

---

## [0.8.2-beta_(245)] - 2026-09-16

### Añadido
- Verificación segura y gestión guiada de permisos para instalación de paquetes desconocidos (`REQUEST_INSTALL_PACKAGES` y `canRequestPackageInstalls()`) en Android 8.0+ hasta Android 15.
- Tarjeta interactiva de advertencia y botón de concesión de permisos dentro del diálogo de descarga completada (`UpdateAvailableDialog`).
- Detección inteligente y mensaje contextual en Ajustes (`SettingsBottomSheet`) cuando el repositorio de GitHub aún no posee releases públicos publicados.

### Cambiado
- Redirección asistida a `Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` mediante `PermissionUtils.openInstallUnknownAppsSettings()` con fallback seguro a los ajustes de la aplicación ante capas personalizadas (MIUI/HyperOS, ColorOS, EMUI).
- Refactorización de `ApkDownloadManager.installApk()` y flujo de actualización para evitar bloqueos silenciosos o excepciones del sistema al instalar APKs actualizadas.

### Corregido
- Prevención de fallos silenciosos en la apertura del instalador de paquetes de Android cuando la aplicación carecía del permiso de origen desconocido.

---

## [0.8.1-beta_(244)] - 2026-09-16

### Añadido
- Gestor de Descarga Integrado (`ApkDownloadManager`): descarga en segundo plano con soporte para redirecciones HTTP seguras y cálculo dinámico de progreso.
- Barra de progreso Material M3 Expressive Wavy (`LinearWavyProgressIndicator`): visualización fluida del porcentaje y volumen de datos descargados (`MB / MB`).
- Instalador automático de actualización: al completar la descarga, se invoca automáticamente el instalador de paquetes de Android mediante `FileProvider` y permisos `REQUEST_INSTALL_PACKAGES`.
- Diálogo de actualización mejorado (`UpdateAvailableDialog`): cálculo y muestra del tamaño de la aplicación, visualización de notas de versión y registro de cambios directamente desde GitHub.

### Cambiado
- Reubicación de "Opciones de Desarrollador" en los ajustes de configuración (`SettingsBottomSheet`), desplazándola hacia el pie de la ventana justo debajo de la tarjeta de "Gestión de Datos".
- Actualización de la tarjeta de actualizaciones en Ajustes (`SettingsBottomSheet`) bajo el título "Gestor de Descarga y Actualizaciones".

### Corregido
- Activación directa y táctil de la ventana de ingreso y configuración de PIN al pulsar sobre la tarjeta o píldora de "Seguridad y Control" en el paso 7 de Gráficas y Seguridad de la pantalla de Bienvenida (`OnboardingScreen`).

---

## [0.8.0-beta_(243)] - 2026-09-15

### Añadido
- Primera versión pública en fase Pre-release (Beta) para distribución mediante GitHub Releases.
- Soporte nativo y detección automática de Pre-releases y Releases en `GitHubUpdateChecker` a través de la API REST de GitHub.
- Etiqueta distintiva `PRE-RELEASE BETA DISPONIBLE` en el diálogo de actualización modal `UpdateAvailableDialog`.
- Respuesta háptica táctil expresiva (`LocalHapticFeedback`) al activar o desactivar el interruptor de comprobación automática de actualizaciones en Ajustes.
- Respuesta háptica táctil en el botón "Comprobar actualización ahora" en la tarjeta de actualizaciones.
- Respuesta háptica nativa integrada en el componente `ExpressiveSwitch` tanto en toque directo como en gesto de arrastre horizontal.
- Actualización de dirección de repositorio a `mikel-ams/MegasCU`.

### Cambiado
- Reordenamiento de la interfaz en la ventana de Ajustes (`SettingsBottomSheet`): traslado de la sección "Opciones de Desarrollador" hacia el pie de la ventana, ubicándola por debajo de la tarjeta de "Gestión de Datos".

### Corregido
- Corrección en la Pantalla de Bienvenida (Paso 7: Gráficas y Seguridad): al pulsar sobre el elemento de "Seguridad y Control Parental" se activa correctamente la verificación e ingreso de PIN.

---

## [0.7.8-beta_(242)] - 2026-09-15

### Añadido
- Verificación automática de actualizaciones mediante la API pública de GitHub Releases (`GitHubUpdateChecker`).
- Programación de tarea periódica en segundo plano cada 24 horas con `WorkManager` (`GitHubUpdateWorker`), restringida a conectividad de red.
- Diálogo modal interactivo de actualización disponible (`UpdateAvailableDialog`) con visualizador del registro de cambios y descarga directa del archivo `.apk`.
- Tarjeta de control de actualizaciones en la hoja de Ajustes (`SettingsBottomSheet`) con comprobación manual, fecha/hora de último chequeo e interruptor de verificación automática.
- Canal y notificación del sistema para avisar sobre nuevas versiones de la aplicación con opción de descarga directa.
- Creación del archivo `CHANGELOG.md` en la raíz del proyecto estructurado bajo Keep a Changelog 1.1.0 y SemVer 2.0.0.

### Cambiado
- Refactorización completa del repositorio interno de cambios (`ChangelogRepository` y `ChangelogBottomSheet`) organizando cada versión en categorías estándar (*Añadido*, *Cambiado*, *Obsoleto*, *Eliminado*, *Corregido*, *Seguridad*).
- Fechas de versiones estandarizadas al formato ISO 8601 (`YYYY-MM-DD`).

### Seguridad
- Eliminación de contraseñas y credenciales en texto plano en la configuración de firma Gradle (`build.gradle.kts`), delegando la autenticación exclusivamente a variables de entorno del sistema.

---

## [0.7.8-beta_(236)] - 2026-09-07

### Cambiado
- Indicador de desplazamiento optimizado: desenfoque de 2px en el fondo del indicador de bajar con estructura estática y animación de rebote aplicada exclusivamente al icono chevron interno.
- Sincronía del botón siguiente: unificación de dimensiones y tamaño de icono (20dp) entre la pantalla 6 y 7 de Bienvenida para una transición sin saltos visuales.
- Botón final en píldora de dos líneas: texto dispuesto en dos líneas ("Comenzar Experiencia" / "MegasCU") con ancho adaptado al contenido y extremos completamente redondeados.

### Corregido
- Fijación del chip indicador de pasos: altura constante en el encabezado superior para mantener el chip de paso completamente estable al pasar a la pantalla final.

---

## [0.7.7-beta_(235)] - 2026-09-07

### Cambiado
- Margen superior en Bienvenida incrementado a 6dp para una separación superior visualmente equilibrada y natural.
- Centrado vertical exacto de todos los elementos en la pantalla final "¡Todo Listo!".
- Expansión de márgenes y área táctil del botón final, incorporando extremos totalmente redondeados en cápsula continua.

---

## [0.7.6-beta_(234)] - 2026-09-07

### Añadido
- Difuminado progresivo inteligente superior e inferior que solo se hace visible cuando la pantalla requiere scroll.
- Señalización de scroll inferior expandida 2dp con fondo difuminado de 3px en la pantalla 2 (Diseñado para Cubacel) y pantalla 6 (Alertas y Sincronización).

### Cambiado
- Transformación continua del botón "Siguiente" en la pantalla 7, deslizándose fluidamente hacia "Comenzar Experiencia MegasCU" debajo de las tarjetas en la pantalla final.
- Margen superior ajustado a 2dp en la pantalla inicial de Bienvenida para máximo aprovechamiento del espacio visual.

---

## [0.7.5-beta_(233)] - 2026-09-07

### Añadido
- Señalización visual de scroll en cápsula con flecha animada en la segunda pantalla de Bienvenida.

### Cambiado
- Botón de bienvenida reubicado directamente debajo de las tarjetas en la última pantalla, con animación fluida entre pantallas.
- Reducción de márgenes superiores del logo en la primera y última pantalla de Bienvenida para visibilidad completa sin scroll.
- Indicador de Pull-to-Refresh emergiendo desde la barra de estado con escalado progresivo al tirar de la pantalla.
- Optimización de compresión en el empaquetado del APK retornando al tamaño ligero de ~2.55 MB.

---

## [0.7.4-beta_(232)] - 2026-09-07

### Añadido
- Tarjeta segmentada de permisos en Bienvenida agrupando Teléfono/USSD, Notificaciones y Estadísticas de Uso para concesión individual o conjunta.

### Cambiado
- Transición animada suave del botón "Siguiente" hacia "Comenzar Experiencia MegasCU" con animación elástica al avanzar al paso final.
- Disposición en 2 filas y 2 columnas para la selección de tintas en el Catálogo de Iconos M3.
- Botones de prueba de temas reubicados debajo del título en la Auditoría WCAG con encabezados desfijados para scroll continuo.

### Eliminado
- Interruptor redundante de alerta de vencimiento en Bienvenida, manteniendo exclusivamente la selección rápida de días de anticipación.

### Seguridad
- Confirmación inmediata de configuración correcta del PIN de acceso en Bienvenida sin solicitar desbloqueo prematuro.

---

## [0.7.3-beta_(231)] - 2026-09-07

### Cambiado
- Reubicación del interruptor y descripción del estimador alternativo de consumo dentro de la sección segmentada de "Gráficos e Indicadores" en Ajustes.
- Estandarización del contenedor superior para activación y configuración del Modo Simple en Ajustes.
- Normalización M3 Expressive en botones y componentes interactivos con respuesta háptica y transiciones elásticas adaptativas.

---

## [0.7.2-beta_(218)] - 2026-08-30

### Añadido
- Monitor de conexión activa con ping dinámico ("Ping: 0,04 ms") y punto de estado sutil en tiempo real en la parte inferior de la pantalla.
- Tipografía expresiva de caligrafía y sombreado de alto contraste en el texto "Pulsa aquí para actualizar".

### Cambiado
- Trazo discontinuo (dashed) elegante y uniforme en las flechas de indicación tanto en Modo Normal como en Modo Simple.
- Tarjeta unificada y segmentada de permisos en Bienvenida con interactividad táctil.

---

## [0.7.0-beta_(216)] - 2026-08-29

### Añadido
- Terminal retro Easter Egg con crónica histórica de telecomunicaciones en Cuba (incluyendo resoluciones y tarifas 2025).
- Cadencia de tecleo humanizada con micro-vacilaciones y sonido de retroceso estilo DOS.
- Conmutación dinámica e instantánea a tema Negro AMOLED durante la sesión de terminal con restauración fiel del tema previo al cerrarla.

### Corregido
- Desacoplamiento de insets dinámicos de barras del sistema en la terminal retro para compatibilidad total con HyperOS, MIUI, OneUI y AOSP.
- Elevación rígida de seguridad (+48dp) garantizando que la caja de estado y el botón `[ CERRAR TERMINAL ]` queden completamente visibles sobre barras de gestos.

---

## [0.6.81-beta_(215)] - 2026-08-29

### Cambiado
- Indicador de Pull-to-refresh exclusivo para el gesto táctil ejecutado por el usuario en pantalla.
- Posicionamiento superior flotante del indicador de carga sobre la barra de título con elevación Z-index y safe padding.

### Corregido
- Desacoplamiento de animaciones de botones y barras lineales de consulta para evitar indicadores duplicados durante acciones secundarias.

---

## [0.6.80-beta_(214)] - 2026-08-29

### Cambiado
- Redacción compacta y concisa en la alerta de permiso de uso evitando desplazamientos innecesarios.
- Efecto de difuminado suave en bordes superior e inferior (fading edges).
- Desenfoque de fondo nativo (Window Blur Behind) y scrim para mayor enfoque visual.
- Márgenes simétricos y uniformes en los cuatro bordes de la pantalla Edge-to-Edge.

---

## [0.6.79-beta_(213)] - 2026-08-29

### Añadido
- Diálogo interactivo explicativo de permiso de acceso a estadísticas de uso con diseño Material 3 Expressive.
- Integración unificada del diálogo explicativo desde Ajustes, Onboarding y al tocar el contador de consumo diario.

### Seguridad
- Integración del keystore oficial de producción `release-key.jks` con firma digital RSA 4096-bit y validez hasta 2054.

---

## [0.6.78-beta_(212)] - 2026-08-29

### Cambiado
- Reducción significativa del tamaño del APK y del consumo de memoria al suprimir fuentes de glifos personalizadas.
- Actualización del Catálogo de Iconos Material M3 con búsqueda en vivo.

### Eliminado
- Reversión completa de fuentes Material Symbols personalizadas, adoptando exclusivamente iconografía vectorial nativa de Jetpack Compose.
- Eliminación de selectores y CompositionLocals redundantes.

---

## [0.6.77-beta_(211)] - 2026-08-28

### Añadido
- Selector segmentado para alternar en vivo entre estilos de iconos (Nativo Vector M3, M3 Filled y M3 Outlined).
- Icono dual SIM dinámico con bisel perimetral opaco y profundidad geométrica.

### Cambiado
- Actualización de la fuente de iconos a peso Regular eliminando grosores Thin y Light para mayor nitidez.
- Reestructuración integral de registros del historial con descripciones técnicas y redacción fluida.

---

## [0.6.76-beta_(210)] - 2026-08-28

### Añadido
- Verificación estricta de hashes SHA-256 en Gradle para recursos binarios.

### Cambiado
- Normalización de contenedores `BoxWithConstraints` y mapeo semántico para TalkBack.

---

## [0.6.75-beta_(209)] - 2026-08-28

### Añadido
- Mapeador centralizado `MaterialIconsCatalog` para resolución de nombres de iconos y correspondencia vectorial.
- Auditoría y validación de compatibilidad de glifos con Google Fonts.

---

## [0.6.74-beta_(208)] - 2026-08-27

### Corregido
- Estabilización de interceptores telefónicos USSD ante rechazos transitorios.
- Calibración de alarmas en WorkManager para mitigar consumo de batería en segundo plano.

---

## [0.6.73-beta_(207)] - 2026-08-27

### Cambiado
- Supresión de logs redundantes de telemetría durante la consulta y cálculo de saldos.
- Respuesta háptica táctil calibrada en teclados numéricos y botones de acción rápida.

---

## [0.6.72-beta_(206)] - 2026-08-27

### Cambiado
- Antialiasing optimizado en tarjetas flotantes y hojas modales Haze.

### Corregido
- Fallback automático y suave de desenfoque en dispositivos con aceleración GPU limitada.

---

## [0.6.71-beta_(205)] - 2026-08-27

### Cambiado
- Contraste forzado en textos de botones en Modo Simple para eliminar parpadeos de color.
- Carga optimizada en memoria de fuentes TrueType monoespaciadas Space Mono.

---

## [0.6.70-beta_(204)] - 2026-08-26

### Cambiado
- Consolidación de claves en SharedPreferences para evitar pérdidas de estado.

### Corregido
- Corrección de desfases en etiquetas de ejes de consumo semanal en gráficos.

---

## [0.6.69-beta_(203)] - 2026-08-26

### Cambiado
- Unificación visual de selectores y espaciados simétricos en botones de menú.

### Corregido
- Mayor tolerancia en análisis de respuestas USSD ante textos truncados o caracteres atípicos.

---

## [0.6.68-beta_(202)] - 2026-08-26

### Añadido
- Sincronización atómica multi-SIM con aislamiento transaccional en Room Database.

### Corregido
- Prevención de excepciones en servicios Foreground en Android 12+ mediante fallback a `NotificationManager`.
- Bloqueo concurrente thread-safe (`radioMutex`) en corrutinas de consulta USSD con liberación inmediata.

---

## [0.6.67-beta_(201)] - 2026-08-26

### Añadido
- Caché y lógica independiente por ranura SIM en widgets 4x2, 2x1 y gráficas.

### Corregido
- Límite mínimo de demora inicial en sincronización para prevenir ejecuciones redundantes.

---

## [0.6.66-beta_(200)] - 2026-08-26

### Añadido
- Diálogo de advertencia M3 informativo para habilitar llamadas USSD de consulta de saldo.
- Conversión automática a GB para bolsas nacionales iguales o mayores a 1024 MB.
- Identificadores semánticos `testTag` en componentes interactivos para pruebas.

---

## [0.6.65-beta_(199)] - 2026-08-26

### Cambiado
- Desglose y validación precisa de paquetes LTE e Internacionales.
- Reducción de recomposiciones innecesarias en la pantalla principal.

---

## [0.6.64-beta_(198)] - 2026-08-26

### Cambiado
- Pulido de elevaciones tonales M3, radios de curvatura y contrastes de superficie.

### Corregido
- Manejo defensivo de excepciones en canales de notificación.

---

## [0.6.63-beta_(197)] - 2026-08-26

### Añadido
- `ExpirationWorker` autónomo para cálculo de días de vigencia sin requerir datos móviles activos.

### Corregido
- Manejo seguro de cancelación cooperativa en `SyncWorker` y control de concurrencia en ViewModel.

---

## [0.6.62-beta_(196)] - 2026-08-26

### Cambiado
- Inicialización delegada a AndroidX de WorkManager bajo demanda para eliminar sobrecarga en Application.
- Centralización de textos en `strings.xml` para internacionalización.

### Eliminado
- Supresión de verificaciones de permisos SMS innecesarias tras migración completa a USSD.

---

## [0.6.53-beta_(187)] - 2026-08-23

### Cambiado
- Bisel perimetral opaco idéntico al color de tarjeta en el icono dual SIM sin halos translúcidos.
- Eliminación del marco de foco al presionar tiradores de BottomSheets.
- Títulos alineados con el margen del contenido en hojas modales.

---

## [0.6.45-beta_(179)] - 2026-08-22

### Añadido
- Conteo numérico progresivo animado de 0 al valor actual en encabezados de saldo y límites.
- Chips flotantes en píldora centrados a 5dp sobre crestas de barras y gráficas.

---

## [0.6.44-beta_(178)] - 2026-08-22

### Cambiado
- Desenfoque y oscurecimiento progresivo reactivo sincronizado con el arrastre de BottomSheets.

---

## [0.6.43-beta_(177)] - 2026-08-22

### Añadido
- Expansión proporcional M3 Expressive en botones conectados con física spring que amplían el botón activo mientras contraen los adyacentes.

---

## [0.6.42-beta_(176)] - 2026-08-22

### Cambiado
- Tiempos extendidos en transiciones post-splash para entrada coordinada de números, barras y gráficas.

### Corregido
- Aparición directa de chips de gráfica con esquinas redondeadas sin parpadeo rectangular.

---

## [0.6.41-beta_(175)] - 2026-08-22

### Añadido
- Animación progresiva incremental para consumo de hoy y límite diario.
- Tonalidad violeta profunda MegasCU (`#4A00DF`) en botones no seleccionados en modo oscuro y AMOLED.

---

## [0.6.40-beta_(174)] - 2026-08-22

### Cambiado
- Eliminación de sombras traseras sobre el botón de actualización en la barra inferior flotante.
- Estandarización de grupos de botones segmentados en Ajustes y Bienvenida.
- Ajuste de cálculo de disponibilidad de recarga al día siguiente hábil con opción de alerta.

---

## [0.6.39-beta_(173)] - 2026-08-22

### Cambiado
- Ajustes en algoritmos de cálculo de vigencia y recarga de saldo.
- Aplicación de tipografía Space Mono en referencias numéricas de gráficas.

---

## [0.6.35-beta_(169)] - 2026-08-22

### Añadido
- Soporte oficial del gesto predictivo hacia atrás (Predictive Back) en todas las ventanas modales y diálogo Acerca de.

### Cambiado
- Migración nativa de BottomSheets con extensión edge-to-edge hasta la barra de navegación del sistema.

---

## [0.6.34-beta_(168)] - 2026-08-21

### Cambiado
- Respuesta háptica refinada y contraste dinámico según luminancia.
- Jerarquía de elevación tonal y sombras en tarjetas secundarias agrupadas.

---

## [0.6.33-beta_(167)] - 2026-08-20

### Cambiado
- Renderizado optimizado de curvas de consumo e indicadores lineales de límite de datos.
- Curvas de resorte suavizadas en animaciones.

---

## [0.6.32-beta_(166)] - 2026-08-18

### Cambiado
- Desenfoque Haze reactivo con menor consumo de memoria y GPU.
- Transiciones suaves de morphing geométrico en botones e indicadores de PIN.

---

## [0.6.30-beta_(164)] - 2026-08-17

### Añadido
- Protección de compras de planes y acceso general mediante huella dactilar y biometría.

### Seguridad
- Verificación estricta de integridad byte a byte (MD5 y SHA-256) en recursos tipográficos y WebP.

---

## [0.6.25-beta_(159)] - 2026-08-15

### Añadido
- Sincronización en segundo plano de saldos y paquetes en widgets de escritorio.

### Cambiado
- Optimización de ciclos de sondeo en reposo para ahorro de batería.

---

## [0.6.20-beta_(154)] - 2026-08-14

### Añadido
- Paletas de color dinámicas Monet en Android 12+.
- Modo Negro AMOLED puro para paneles OLED.

---

## [0.6.15-beta_(149)] - 2026-08-12

### Cambiado
- Barra de navegación transparente edge-to-edge para navegación por gestos.
- Respuesta háptica suave en switches y controles interactivos.

---

## [0.6.10-beta_(144)] - 2026-08-11

### Añadido
- Notificaciones de vencimiento configurables en rangos de 1 a 15 días.
- Avisos proactivos ante umbral de consumo acelerado de datos.

---

## [0.6.5-beta_(139)] - 2026-08-10

### Cambiado
- Tarjetas apiladas (Stacked Cards) con radios de curvatura jerárquicos (24dp superior, 4dp intermedio, 24dp inferior).

### Corregido
- Manejo robusto de errores en consultas telefónicas USSD.

---

## [0.6.3-beta_(137)] - 2026-08-09

### Añadido
- Vistas previas estilo skeleton para widgets 4x2, 2x1 y 3x2.
- Soporte para pantallas anchas y launchers con grilla expandida 5x2.
- Inclusión directa de días restantes y fecha de recarga en widget 2x1.
- Widget gráfico 3x2 de alta resolución con etiquetas de días en el eje X.

---

## [0.6.2-beta_(136)] - 2026-08-09

### Añadido
- Sincronización reactiva de colores de alto contraste sin necesidad de reiniciar la app.
- Control switch WCAG para activar paleta accesible en opciones de desarrollo.
- Priorización de vectores nativos Compose con esquinas redondeadas (`Icons.Rounded`).

### Cambiado
- Estandarización de persistencia en `megas_prefs`.

---

## [0.6.1-beta_(135)] - 2026-08-09

### Añadido
- Herramienta de auditoría automática WCAG con inspección de luminancia y ratios AAA/AA.
- Mapeador de colores M3 con previsualización de paleta accesible.
- Diagnóstico de trazados y vista ampliada para inspección de iconos redondeados.
- Ajustes de desarrollo M3 con bordes suaves y botones con área mínima de 48dp.

---

## [0.6.0-beta_(134)] - 2026-08-09

### Cambiado
- Rediseño M3 de ajustes de desarrollo con tarjetas `surfaceContainerHigh`.
- Reemplazo de botón de cierre por interruptor destacado `PowerSettingsNew`.

### Corregido
- Corrección de `pathData` en 75 vectores XML eliminando uniones aglomeradas.

---

## [0.5.9-beta_(133)] - 2026-08-09

### Añadido
- Catálogo de iconos comparando `ImageVector` nativo con vector XML redondeado.

---

## [0.5.8-beta_(132)] - 2026-08-08

### Cambiado
- Actualización global de todos los iconos de la app a variantes redondeadas (`Rounded`).

---

## [0.5.7-beta_(131)] - 2026-08-08

### Cambiado
- Iconos de la pantalla de configuración actualizados a variantes redondeadas.

---

## [0.5.6-beta_(130)] - 2026-08-08

### Cambiado
- Tonos violetas claros (Tono 80 y 90) para optimizar contraste en Modo Oscuro.
- Thumb interno blanco en interruptores para mayor claridad en fondos oscuros.

---

## [0.5.5-beta_(129)] - 2026-08-08

### Añadido
- Contador de registros en vista principal con silueta de píldora (Badge Chip).

### Cambiado
- Ajuste sutil de luminancia en tarjetas flotantes sobre fondos oscuros.

### Corregido
- Eliminación de sacudida y retardo de restablecimiento tras ingresar PIN erróneo.

---

## [0.5.4-beta_(128)] - 2026-08-08

### Cambiado
- Rediseño de widget 2x1 M3 Expressive con tarjetas optimizadas y badge pill para días restantes.
- Distintivo con recuento de registros en widget de consumo.

### Eliminado
- Variantes redundantes de widgets de escritorio manteniendo los formatos estándar principales.

---

## [0.5.3-beta_(127)] - 2026-08-08

### Añadido
- Widget de gráfico de consumo sincronizado en tiempo real con historial de Room DB.
- Tarjetas dinámicas en widgets 2x1 y 4x2 con valores iniciales en cero.

### Corregido
- Reemplazo de formas complejas en indicadores de PIN por círculos uniformes para prevenir cierres.

---

## [0.5.2-beta_(126)] - 2026-08-08

### Añadido
- Animación corner morphing al presionar accesos biométricos.
- Vista previa interactiva con tarjetas morphing en configuración de widgets.

### Corregido
- Respuesta acelerada de 0.5s ante PIN erróneo y eliminación de entradas duplicadas.

---

## [0.5.1-beta_(125)] - 2026-08-08

### Añadido
- Selección de 4 formas geométricas únicas por intento de PIN (M3 Expressive Shape Diversity).
- Pool geométrico extendido: Cuadrado, Arco, Diamante, Semicírculo, Gema y Flecha.
- Animación de error horizontal con oscilaciones suaves M3 Bouncy y destello rojo fluido.

---

## [0.5.0-beta_(124)] - 2026-08-08

### Añadido
- Indicadores de PIN dinámicos con formas geométricas al ingresar dígitos de seguridad.
- Guía rápida estructurada en tarjetas apiladas con bordes jerárquicos (22dp e intermedios de 4dp).

### Cambiado
- Márgenes calibrados y simétricos en botones flotantes "Comprar Plan" y "Guía".

---

## [0.4.1-beta_(117)] - 2026-08-08

### Añadido
- Barra de límite diario en vivo sincronizada con interruptor en ajustes.

### Cambiado
- Auditoría de accesibilidad M3 con roles semánticos y soporte TalkBack en toda la app.

### Corregido
- Splash screen optimizado para ejecutarse exclusivamente en el inicio de la aplicación.

---

## [0.4.0-beta_(116)] - 2026-08-07

### Añadido
- Gráfica lineal unificada en sección de Gráficos e Indicadores de Ajustes.
- Guías numéricas de referencia (100%, 50%, 0 MB) dibujadas en la gráfica lineal.
- Transición suave de color con interpolación al alternar entre modo claro y oscuro.

---

## [0.3.10-beta_(115)] - 2026-08-07

### Añadido
- Personalización modular con tarjetas independientes para Tema, AMOLED, Fuentes y Color Dinámico.
- Itinerario de tour guiado interactivo (Coach Mark) con textos explicativos actualizados.

### Cambiado
- Rediseño de Ajustes M3 Expressive en tarjetas segmentadas con separación vertical de 3dp.

---

## [0.3.10-beta_(98)] - 2026-08-05

### Cambiado
- Restauración de desenfoque progresivo suave en BottomSheets.
- Switches con física spring, thumb ampliado y transiciones de color.

---

## [0.3.9-beta_(97)] - 2026-08-05

### Añadido
- Segunda alerta de expiración con deslizador configurable y confirmación interactiva.
- Botones de acción directa en notificación: "Comprar Plan" y "Abrir Menú".

---

## [0.3.9-beta_(96)] - 2026-08-05

### Cambiado
- Seguimiento en tiempo real de desenfoque al arrastrar modales.
- Pulsaciones directas sin deformación en botones de acción.

---

## [0.3.9-beta_(94)] - 2026-08-05

### Cambiado
- Reducción de luminancia en tarjetas para mayor confort visual en Modo Oscuro.
- Desvanecimiento progresivo (fade-out) en desenfoques modales.

---

## [0.3.8-beta_(93)] - 2026-08-05

### Cambiado
- Ajuste de contraste en superficies oscuras.

### Corregido
- Grosor y trazado corregido en iconos de accesos directos de inicio.

---

## [0.3.7-beta_(92)] - 2026-08-05

### Añadido
- Resplandor radial y transiciones de entrada suaves en BottomSheets.
- Icono tipo bug en el diálogo Acerca de para modo de pruebas.

---

## [0.3.7-beta_(91)] - 2026-08-05

### Cambiado
- Sincronización en segundo plano eficiente y reducción de consumo de memoria en gráficas.

---

## [0.3.7-beta_(90)] - 2026-08-05

### Cambiado
- Ajuste de límites de datos en alertas inteligentes.

### Corregido
- Estabilidad en consultas USSD en dispositivos con doble SIM.

---

## [0.3.7-beta_(89)] - 2026-08-05

### Cambiado
- Refinamiento de etiquetas y bordes adaptativos para accesibilidad y lectores de pantalla.

---

## [0.3.7-beta_(88)] - 2026-08-05

### Cambiado
- Presentación de paquetes ETECSA y desglose de saldo principal y bonos.

---

## [0.3.7-beta_(87)] - 2026-08-05

### Cambiado
- Respuesta háptica y tiempos de ejecución calibrados en el selector de acciones rápidas.

---

## [0.3.7-beta_(86)] - 2026-08-05

### Añadido
- Rediseño visual del panel de consumo diario con micro-interacciones táctiles y fondos translúcidos.

---

## [0.3.6-beta_(85)] - 2026-08-04

### Añadido
- Gráfica en cápsulas animadas M3 y micro-interacciones Expressive.

---

## [0.3.5-beta_(84)] - 2026-08-04

### Añadido
- Iconos de estado (✓ y ✕) integrados en el interior de los interruptores M3 Expressive.

---

## [0.3.4-beta_(83)] - 2026-08-04

### Añadido
- Motor UI M3 Expressive con formas orgánicas y elevaciones dinámicas en toda la aplicación.

---

## [0.3.3-RC1_(82)] - 2026-08-04

### Cambiado
- Reducción de peso de fuentes e incremento de rendimiento en renderizado de gráficas.

---

## [0.3.1-beta_(80)] - 2026-08-03

### Añadido
- Lanzamiento inicial de la aplicación MegasCU.
- Consultas automáticas de saldo y paquetes mediante llamadas USSD sin conexión a internet.
- Widgets de escritorio interactivos (formatos 4x2, 2x1 y 3x2).
- Monitor de consumo de datos móviles, alertas de vigencia y cálculo de vencimiento de recargas Cubacel.
- Gestión de seguridad con PIN de acceso y biometría.
