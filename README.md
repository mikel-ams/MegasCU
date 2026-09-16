# MegasCU — Monitor y Gestor Inteligente de Datos y Telecomunicaciones en Cuba

[![Platform: Android](https://img.shields.io/badge/Platform-Android_11.0%2B_(API_30%2B)-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Expressive_M3-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_MVVM_%2B_Flow-7952B3)](https://developer.android.com/topic/architecture)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.x-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Offline First](https://img.shields.io/badge/Privacy-100%25_Offline_First-success)](#privacidad-y-seguridad)

**MegasCU** es una aplicación nativa para Android de alto rendimiento, diseñada específicamente para el ecosistema de telecomunicaciones de Cuba (red móvil **ETECSA / Cubacel**). Combina telemetría celular en tiempo real, análisis heurístico de tramas USSD, persistencia local reactiva y una interfaz de usuario de vanguardia basada en **Material 3 Expressive**.

---

## ⚡ Aspectos Técnicos Destacados (Geek & Architecture Highlights)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│  Jetpack Compose Expressive M3 • Dynamic Physics Springs • Haze Glass  │
│    Wavy Progress • Morphic Corner Shapes • WCAG Contrast Inspector     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / UI States
┌───────────────────────────────────▼────────────────────────────────────┐
│                            DOMAIN / VIEWMODEL                          │
│     Clean MVVM • Heuristic USSD Parsers • Daily Rate Estimators        │
│          Biometric Protection • Expiration Alert Schedulers            │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │                                │
┌───────────────────▼──────────────┐ ┌───────────────▼───────────────────┐
│           DATA LAYER             │ │        HARDWARE & OS APIS         │
│  Room Database (KSP) + SQLite    │ │  TelephonyManager (sendUssdRequest│
│  Reactive DAOs • Flow Pipelines  │ │  SubscriptionManager (Multi-SIM)  │
│  Encrypted Local Preferences     │ │  NetworkStatsManager (Traffic API)│
└──────────────────────────────────┘ └───────────────────────────────────┘
```

* **UI 100% Declarativa con Material 3 Expressive:**
  * Implementación pionera de las especificaciones **M3 Expressive de Google**: componentes con física elástica no lineal (`dampingRatio`, `stiffness`), metamorfosis morfológica de bordes (*Corner Morphing* al contacto táctil), y feedback háptico fino sincronizado (`LocalHapticFeedback`).
  * Indicadores de actividad fluidos: `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator` y `ShapeMorphingLoadingIndicator`.
  * Efectos de desenfoque y translucidez reactiva en tiempo real (*Glassmorphism*) mediante `Haze` en hojas modales (`ModalBottomSheet`) y contenedores con amortiguación elástica.
* **Procesamiento y Telemetría Celular:**
  * Ejecución USSD nativa no intrusiva mediante `TelephonyManager.sendUssdRequest()` (API 26+) con enrutamiento dinámico por ranura SIM (`SubscriptionManager`).
  * Motor de expresiones regulares tolerante a fallos para la desestructuración de tramas de texto USSD de Cubacel (`*222#`, `*222*328#`, `*222*835#`, `*222*763#`, etc.).
  * Medición de tráfico de red a nivel de socket/interfaz de radio mediante `NetworkStatsManager` (`AppOpsManager.OPSTR_GET_USAGE_STATS`), calculando consumo real de subida/bajada diferenciado por tarjeta SIM.
* **Persistencia Reactiva y Almacenamiento Local (Room + KSP):**
  * Base de datos local SQLite con **Android Room** y procesamiento de anotaciones con **KSP** (Kotlin Symbol Processing).
  * Flujos asíncronos reactivos con `Kotlin Coroutines Flow` y `StateFlow` conectados a ciclos de vida de Compose con `collectAsStateWithLifecycle`.
* **Privacidad Absoluta (Zero-Telemetry / Offline-First):**
  * Sin servidores intermediarios, sin rastreadores (*trackers*), sin analíticas de terceros y sin telemetría remota. Toda la información de saldos, consumo y números se procesa y persiste estrictamente en la memoria del dispositivo.

---

## 🚀 Funcionalidades Principales

### 1. Panel de Control de Saldo y Paquetes de Datos
* **Lectura Desglosada de Recursos:**
  * Saldo principal en CUP y fecha de vencimiento del ciclo de recarga.
  * Datos para Todas las Redes (LTE + 3G) y datos exclusivos para red LTE.
  * Bonos especiales: Bono Nacional de navegación `.cu`, Bonos promocionales internacionales y Bono de Tráfico Ilimitado.
  * Paquetes de mensajería (SMS) y minutos de llamadas de voz nacionales e internacionales.
* **Soporte Multi-SIM Avanzado:**
  * Detección y cambio en caliente entre SIM 1 y SIM 2 con identificación por `subscriptionId`, operador, carrier y estado de red.
  * Consultas y registros de tráfico independientes para cada línea telefónica.

### 2. Monitor de Consumo y Tasa Diaria Inteligente
* **Estimación Heurística y Consumo Real:**
  * Monitor de bytes consumidos en el día a través de las APIs del sistema operativo.
  * Algoritmo de balance y proyección de consumo diario: calcula cuántos megabytes puedes consumir por día para que tu paquete dure exactamente hasta la fecha de expiración.
  * Gráficas interactivas de consumo histórico (diario, semanal y por períodos de recarga).
* **Alertas y Notificaciones de Expiración:**
  * Alertas programables cuando faltan `N` días para el vencimiento de los planes combinados o paquetes LTE (1 a 15 días).
  * Umbrales de consumo diario para prevenir el gasto involuntario de megas a alta velocidad.

### 3. Centro de Consultas Rápidas USSD y Compras de Planes
* **Catálogo Integrado de Operaciones Cubacel:**
  * Consultas directas de saldo, bonos, planes de datos, voz, SMS y adelanto de saldo.
  * Compra asistida de paquetes de datos combinados, bolsas de mensajería y planes internacionales sin necesidad de memorizar códigos USSD complejos.
  * Editor y selector de acciones rápidas personalizadas con catálogo de iconos vectoriales Material Symbols.

### 4. Seguridad, Biometría y Personalización
* **Bloqueo Biométrico Integrado:**
  * Autenticación con huella dactilar o reconocimiento facial (`androidx.biometric.BiometricPrompt`) para restringir el acceso a la app o proteger la visualización de saldos.
* **Accesibilidad y Motor de Temas:**
  * Inspector de contraste WCAG AAA en tiempo real con opción de forzar una paleta de alta luminancia optimizada para pantallas bajo luz solar directa o usuarios con baja visión.
  * Soporte completo para temas Claro, Oscuro y Material You Dynamic Colors (Android 12+).
  * Tipografías optimizadas de alta legibilidad (Space Mono TrueType integrada con verificación de integridad de hashes binarios).
* **Widget Interactivo para Pantalla de Inicio:**
  * Glance/AppWidget para consultar el estado de datos disponibles y realizar consultas rápidas sin abrir la aplicación.
* **Modo Simulación / Sandbox para Desarrolladores:**
  * Entorno de pruebas integrado para simular respuestas USSD de Cubacel y probar el comportamiento de la UI sin consumir saldo real ni requerir cobertura celular.

---

## 🛠️ Stack Tecnológico

| Componente | Tecnología / Librería |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.2+ |
| **Framework UI** | Jetpack Compose con Material 3 Expressive |
| **Inyección / Patrón** | Clean Architecture + MVVM + StateFlow |
| **Persistencia** | Room Database 2.7+ con KSP |
| **Gráficos & Visualización** | Vico Compose M3 + Canvas Custom Draw |
| **Efectos Visuales** | Haze (Blur & Real-time Glassmorphism) |
| **Seguridad** | AndroidX Biometric 1.2+ |
| **Widgets** | AndroidX Glance AppWidget |
| **Pruebas Automatizadas** | Robolectric, Roborazzi Screenshot Testing, JUnit 4 |
| **Compilación & Optimización** | Gradle Kotlin DSL, R8 Full Mode Shrinking, ProGuard Rules |

---

## 📦 Requisitos de Instalación

* **Versión mínima de Android:** Android 11.0 (API Nivel 30)
* **Versión recomendada:** Android 14.0+ (API Nivel 34/36)
* **Permisos requeridos:**
  * `CALL_PHONE` / `READ_PHONE_STATE`: Para ejecutar consultas USSD y detectar ranuras SIM.
  * `PACKAGE_USAGE_STATS` (Opcional): Para la telemetría precisa de bytes consumidos por día.
  * `USE_BIOMETRIC`: Para la autenticación biométrica de seguridad.
  * `POST_NOTIFICATIONS`: Para avisos de vencimiento de planes y umbrales de datos.

---

## 📄 Licencia

Este proyecto está distribuido bajo la licencia de código abierto correspondiente. Consulta el archivo `LICENSE` para más detalles.
