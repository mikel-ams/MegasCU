# MegasCU — Primera Release Pública (v0.8.0-beta / Build 243)

> **Short Description (GitHub About / Subtitle):**  
> Monitor nativo y gestor inteligente de saldo, paquetes LTE, bonos y consumo de datos para Cubacel / ETECSA en Android, construido con Jetpack Compose Expressive (M3), arquitectura Clean MVVM, telemetría celular en tiempo real, Room Database, actualizador OTA integrado y privacidad 100% Offline-First.

---

## 📌 Descripción de la Primera Release Pública (Pre-release)

**MegasCU** es una suite de herramientas de telecomunicaciones de código abierto desarrollada en Kotlin y Jetpack Compose para dispositivos Android. Diseñada a medida para optimizar y simplificar la gestión de saldo, planes LTE, paquetes combinados, bonos y llamadas en la red de **ETECSA / Cubacel** en Cuba.

Esta versión **v0.8.0-beta_(243)** constituye la primera compilación pública oficial distribuida mediante GitHub Releases con soporte para verificación y descarga automática de actualizaciones desde la propia aplicación.

---

### 🚀 Novedades y Características Principales

#### 1. Sistema Integrado de Actualizaciones OTA (GitHub Releases)
* **Comprobación Automática en Segundo Plano:** Tarea periódica programada cada 24 horas mediante `WorkManager` con restricciones inteligentes de red para no consumir recursos innecesarios.
* **Detección de Pre-releases y Releases:** Motor `GitHubUpdateChecker` conectado a la API de GitHub (`mikel-ams/MegasCU`) para encontrar al instante nuevas versiones, leer el changelog y calcular el tamaño del paquete.
* **Diálogo Modal Expresivo (`UpdateAvailableDialog`):** Ventana emergente con comparador visual de versiones, visor de novedades y descarga directa del archivo `.apk` mediante el navegador o gestor de descargas del sistema.
* **Ajustes de Actualización Personalizables:** Conmutador para activar/desactivar chequeos automáticos, botón para comprobación manual inmediata y visualización del historial de comprobaciones.

#### 2. Respuesta Háptica Táctil Expresiva (Haptic Feedback)
* **Microinteracciones Hápticas:** Retroalimentación táctil suave (`LocalHapticFeedback`) al accionar interruptores (`ExpressiveSwitch`), cambiar opciones y pulsar el botón de comprobación de actualizaciones.
* **Soporte Táctil Completo:** Respuesta táctil tanto al pulsar como al deslizar suavemente los componentes expresivos.

#### 3. Capa de Presentación (Material 3 Expressive UI)
* **Jetpack Compose Expressive:** Implementación integral de componentes M3 con física de resortes no lineales (`spring` physics con rigidez y amortiguación personalizadas).
* **Morphic Corner Dynamics:** Metamorfosis elástica de bordes en botones (`ExpressiveButton`, `ExpressiveIconButton`) y selectores táctiles.
* **Componentes Expresivos Nativos:**
  * Indicadores de progreso ondulatorios (`LinearWavyProgressIndicator`, `CircularWavyProgressIndicator`).
  * Indicador de carga morfológico (`ShapeMorphingLoadingIndicator`).
  * Deslizadores táctiles elásticos (`ExpressiveSlider`, `ExpressiveSwitch`).
  * Hojas de diálogo modales con controlador de arrastre (`ExpressiveDragHandle`) y desenfoque en tiempo real con `Haze`.
* **Inspector de Contraste y Accesibilidad WCAG AAA:** Módulo de diagnóstico en vivo que evalúa ratios de contraste y permite activar paletas de alta visibilidad para uso bajo luz solar intensa.

#### 4. Motor de Telemetría Celular y Procesamiento USSD
* **Telephony Stack Nativo:** Empleo de `TelephonyManager.sendUssdRequest()` en Android 8.0+ para consultas silenciosas en segundo plano con fallback por intenciones de llamada (`Intent.ACTION_CALL`).
* **Soporte Multi-SIM Granular:** Detección de líneas mediante `SubscriptionManager` para ejecutar consultas de saldo y consumo asignadas de forma independiente a la SIM 1 o SIM 2.
* **Parser Heurístico de Tramas Cubacel:** Decodificación instantánea de respuestas de saldo principal (`*222#`), paquetes de datos (`*222*328#`), bonos (`*222*835#`), minutos de voz (`*222*869#`), paquetes SMS (`*222*763#`) y adelanto de saldo (`*222*266#`).
* **Telemetría de Tráfico Celular:** Acceso a `NetworkStatsManager` para computar bytes transmitidos (subida/bajada) por interfaz celular diarios y mensuales.

#### 5. Persistencia y Arquitectura (Clean Architecture + Room)
* **Persistencia Reactiva Local:** Base de datos SQLite gestionada mediante **Android Room** con KSP. Entidades para estados de planes (`PlanStatusEntity`), historial de consultas (`UssdRecordEntity`) y accesos directos (`QuickActionEntity`).
* **Tasa de Consumo Recomendada:** Algoritmo dinámico que calcula los megabytes sugeridos por día según los días restantes del paquete activo.
* **Flujos Asíncronos:** Corrutinas de Kotlin con `Flow` y `StateFlow` sincronizados con el ciclo de vida de la UI (`collectAsStateWithLifecycle()`).

#### 6. Seguridad y Privacidad (100% Offline-First)
* **Biometría Local:** Protección opcional mediante `BiometricPrompt` para resguardar historiales y balances.
* **Sin Rastreos ni Publicidad:** Cero telemetría a servidores privados y ausencia total de librerías publicitarias.
* **Firma Criptográfica Segura:** Keystore oficial de producción firmada con llaves protegidas por variables de entorno.

---

### 📋 Ficha Técnica

| Parámetro | Especificación |
| :--- | :--- |
| **Versión** | `0.8.0-beta_(243)` (Pre-release) |
| **Código de Build** | `243` |
| **Plataforma** | Android (Kotlin) |
| **SDK Mínimo** | API 30 (Android 11.0) |
| **SDK Objetivo / Compilación** | API 36 |
| **Toolkit de UI** | Jetpack Compose con M3 Expressive |
| **Base de Datos** | Room Database (SQLite) |
| **Gráficos** | Vico Compose M3 / Canvas Nativo |
| **Repositorio Oficial** | [https://github.com/mikel-ams/MegasCU](https://github.com/mikel-ams/MegasCU) |
| **Licencia** | Código Abierto |
