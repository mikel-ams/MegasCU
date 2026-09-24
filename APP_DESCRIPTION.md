# MegasCU — Descripción y Especificaciones de Release (v0.9.0-beta / Build 255)

> **Short Description (GitHub About / Subtitle):**  
> Monitor nativo y gestor inteligente de saldo, paquetes LTE, bonos y consumo de datos para Cubacel / ETECSA en Android, construido con Jetpack Compose Expressive (M3), arquitectura Clean MVVM, telemetría celular en tiempo real, Room Database, actualizador OTA integrado y privacidad 100% Offline-First.

---

## 📌 Descripción de la Release Oficial

**MegasCU** es una suite de herramientas de telecomunicaciones de código abierto desarrollada en Kotlin y Jetpack Compose para dispositivos Android. Diseñada a medida para optimizar y simplificar la gestión de saldo, planes LTE, paquetes combinados, bonos y llamadas en la red de **ETECSA / Cubacel** en Cuba.

Esta versión **v0.9.0-beta_(255)** incorpora optimizaciones integrales en la interfaz de usuario con Material 3 Expressive, animaciones morfológicas de cambio de forma en botones, soporte completo para Markdown en notas de versión, gestos predictivos de retroceso y un robusto sistema de seguridad con Android Keystore.

---

### 🚀 Novedades y Características Principales

#### 1. Sistema Integrado de Actualizaciones OTA (GitHub Releases)
* **Comprobación Automática en Segundo Plano:** Tarea periódica programada cada 24 horas mediante `WorkManager` con restricciones inteligentes de red para no consumir recursos innecesarios.
* **Detección de Pre-releases y Releases:** Motor `GitHubUpdateChecker` conectado a la API de GitHub (`mikel-ams/MegasCU`) para encontrar al instante nuevas versiones, leer el changelog y calcular el tamaño del paquete.
* **Diálogo Modal Expresivo (`UpdateAvailableDialog`):** Ventana emergente con comparador visual de versiones, renderizado de Markdown nativo para notas de versión y descarga directa del archivo `.apk`.
* **Seguridad Criptográfica en Actualizaciones:** Módulo `ApkSecurityValidator` para verificación de hashes SHA-256 y dominios autorizados antes de la instalación.
* **Ajustes de Actualización Personalizables:** Conmutador para activar/desactivar chequeos automáticos, botón para comprobación manual inmediata y visualización del historial de comprobaciones.

#### 2. Respuesta Háptica Táctil y Microinteracciones Expressive
* **Microinteracciones Hápticas:** Retroalimentación táctil suave (`LocalHapticFeedback`) al accionar interruptores (`ExpressiveSwitch`), cambiar opciones y pulsar botones.
* **Animaciones Morfológicas de Forma:** Transición dinámica y elástica de forma al presionar botones (`ButtonShapes` con `pressedShape`) en la navegación de bienvenida y componentes expresivos.
* **Soporte Táctil Completo:** Respuesta táctil sincronizada tanto al pulsar como al deslizar suavemente los componentes expresivos.

#### 3. Capa de Presentación (Material 3 Expressive UI)
* **Jetpack Compose Expressive:** Implementación integral de componentes M3 con física de resortes no lineales (`spring` physics con rigidez y amortiguación personalizadas) y MotionScheme expresivo.
* **Experiencia de Bienvenida y Selección de Modo:** Flujo interactivo de configuración inicial con selección visual entre Modo Normal (detallado) y Modo Simple (compacto).
* **Gestos Predictivos y Adaptabilidad:** Soporte nativo para gestos predictivos de retroceso (`Predictive Back`) y diseño responsivo adaptado a teléfonos, plegables y tablets (`widthIn(max = 680.dp)`).
* **Componentes Expresivos Nativos:**
  * Indicadores de progreso ondulatorios (`LinearWavyProgressIndicator`, `CircularWavyProgressIndicator`).
  * Indicador de carga morfológico (`ShapeMorphingLoadingIndicator`).
  * Deslizadores táctiles elásticos (`ExpressiveSlider`, `ExpressiveSwitch`).
  * Chips dinámicos de vigencia (`ExpressiveDaysChip`) y grupos segmentados conectados.
  * Hojas de diálogo modales con controlador de arrastre (`ExpressiveDragHandle`), difuminado progresivo y desenfoque en tiempo real con `Haze`.
* **Inspector de Contraste y Accesibilidad WCAG AAA:** Módulo de diagnóstico en vivo que evalúa ratios de contraste y permite activar paletas de alta visibilidad para uso bajo luz solar intensa.

#### 4. Motor de Telemetría Celular y Procesamiento USSD
* **Telephony Stack Nativo:** Empleo de `TelephonyManager.sendUssdRequest()` en Android 8.0+ para consultas silenciosas en segundo plano con fallback por intenciones de llamada (`Intent.ACTION_CALL`).
* **Soporte Multi-SIM Granular:** Detección de líneas mediante `SubscriptionManager` para ejecutar consultas de saldo y consumo asignadas de forma independiente a la SIM 1 o SIM 2.
* **Parser Heurístico de Tramas Cubacel Extendido:** Decodificación instantánea de respuestas de saldo principal (`*222#`), paquetes de datos (`*222*328#`), bonos (`*222*835#`), minutos de voz (`*222*869#`), paquetes SMS (`*222*763#`) y adelanto de saldo (`*222*266#`), con soporte para formatos de tiempo compuestos (`MM:SS`).
* **Telemetría de Tráfico Celular:** Acceso a `NetworkStatsManager` para computar bytes transmitidos (subida/bajada) por interfaz celular diarios y mensuales.

#### 5. Persistencia y Arquitectura (Clean Architecture + Room)
* **Persistencia Reactiva Local:** Base de datos SQLite gestionada mediante **Android Room** con KSP. Entidades para estados de planes (`PlanStatusEntity`), historial de consultas (`UssdRecordEntity`) y accesos directos (`QuickActionEntity`).
* **Tasa de Consumo Recomendada:** Algoritmo dinámico que calcula los megabytes sugeridos por día según los días restantes del paquete activo.
* **Flujos Asíncronos:** Corrutinas de Kotlin con `Flow` y `StateFlow` sincronizados con el ciclo de vida de la UI (`collectAsStateWithLifecycle()`).

#### 6. Seguridad y Privacidad (100% Offline-First)
* **Cifrado y Protección por Android Keystore:** Almacenamiento seguro respaldado por hardware para PIN de acceso y control contra intentos de fuerza bruta.
* **Biometría Local:** Protección opcional mediante `BiometricPrompt` para resguardar historiales y balances.
* **Sin Rastreos ni Publicidad:** Cero telemetría a servidores privados y ausencia total de librerías publicitarias.
* **Firma Criptográfica Segura:** Keystore oficial de producción firmada con llaves protegidas y verificación de firma dual v1/v2.

---

### 📋 Ficha Técnica

| Parámetro | Especificación |
| :--- | :--- |
| **Versión** | `0.9.0-beta_(255)` |
| **Código de Build** | `255` |
| **Plataforma** | Android (Kotlin) |
| **SDK Mínimo** | API 30 (Android 11.0) |
| **SDK Objetivo / Compilación** | API 36 |
| **Toolkit de UI** | Jetpack Compose con M3 Expressive |
| **Base de Datos** | Room Database (SQLite) |
| **Gráficos** | Vico Compose M3 / Canvas Nativo |
| **Repositorio Oficial** | [https://github.com/mikel-ams/MegasCU](https://github.com/mikel-ams/MegasCU) |
| **Licencia** | Código Abierto (MIT / Apache 2.0) |
