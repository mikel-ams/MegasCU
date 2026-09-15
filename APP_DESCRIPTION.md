# MegasCU — Descripción Técnica para Repositorio de GitHub

> **Short Description (GitHub About / Subtitle):**  
> Monitor nativo y gestor inteligente de saldo, paquetes LTE, bonos y consumo de datos para Cubacel / ETECSA en Android, construido con Jetpack Compose Expressive (M3), arquitectura Clean MVVM, telemetría celular en tiempo real, Room Database y privacidad 100% Offline-First.

---

## 📌 Descripción Completa (Markdown para README / Wiki / Releases)

### Resumen Ejecutivo
**MegasCU** es una suite de herramientas y monitor de telecomunicaciones de código abierto para dispositivos Android, desarrollada específicamente para optimizar la gestión de recursos móviles en la red de **ETECSA / Cubacel** en Cuba. 

La aplicación combina un diseño visual moderno basado en las especificaciones **Material 3 Expressive** de Google con un motor heurístico de bajo nivel para el análisis de tramas USSD y telemetría de consumo de datos a través de los servicios del kernel y del framework de Android.

---

### 🧩 Arquitectura del Sistema y Componentes Clave

#### 1. Capa de Presentación (M3 Expressive UI)
* **Jetpack Compose Expressive:** Integración de los componentes de última generación de Material Design 3 con física de resortes no lineales (`spring` physics con amortiguación y rigidez personalizadas).
* **Morphic Corner Dynamics:** Metamorfosis elástica de bordes en botones (`ExpressiveButton`, `ExpressiveIconButton`) y selectores táctiles, sustituyendo las reducciones de escala rígidas tradicionales por deformaciones físicas asimétricas en tiempo real.
* **Componentes Expresivos Nativos:**
  * Indicadores de progreso ondulatorios (`LinearWavyProgressIndicator`, `CircularWavyProgressIndicator`).
  * Indicador de carga con metamorfosis morfológica (`ShapeMorphingLoadingIndicator`).
  * Deslizadores táctiles elásticos (`ExpressiveSlider`, `ExpressiveSwitch`).
  * Hojas de diálogo modales con controlador de arrastre expresivo (`ExpressiveDragHandle`) y renderizado de desenfoque en tiempo real con `Haze`.
* **Inspector de Contraste y Accesibilidad WCAG AAA:** Módulo de diagnóstico en vivo que evalúa los ratios de contraste de luminancia relativa y permite activar una paleta de alta accesibilidad para uso bajo luz solar intensa.

#### 2. Motor de Telemetría Celular y Procesamiento USSD
* **Telephony Stack Nativo:** Empleo de `TelephonyManager.sendUssdRequest()` en dispositivos con Android 8.0+ para consultas silenciosas en segundo plano con fallback por intenciones de llamada (`Intent.ACTION_CALL`).
* **Soporte Multi-SIM Avanzado:** Identificación granular de suscripciones mediante `SubscriptionManager` para enrutar consultas de saldo y consumo de forma independiente a la ranura SIM 1 o SIM 2.
* **Parser Heurístico de Tramas Cubacel:** Motor regex resiliente capaz de decodificar respuestas de saldo principal (`*222#`), paquetes de datos (`*222*328#`), bonos promocionales (`*222*835#`), minutos de voz (`*222*869#`), paquetes SMS (`*222*763#`) y adelanto de saldo (`*222*266#`).
* **Telemetría de Tráfico de Red:** Acceso a `NetworkStatsManager` para computar los bytes transmitidos por interfaz celular (subida y bajada) en el ciclo diario y mensual, correlacionándolos con la información de los paquetes activos.

#### 3. Capa de Dominio y Datos (Clean Architecture + Room)
* **Persistencia Reactiva:** Base de datos relacional SQLite gestionada mediante **Android Room** con compilación KSP. Estructura de entidades para estados de planes (`PlanStatusEntity`), historial de consultas (`UssdRecordEntity`) y accesos directos (`QuickActionEntity`).
* **Algoritmo de Tasa de Consumo Recomendada:** Calcula dinámicamente la cuota diaria ideal de megabytes en función del volumen de datos remanente y los días restantes antes de la fecha de expiración.
* **Procesamiento Asíncrono:** Corrutinas de Kotlin con `Flow` y `StateFlow` conectados al ciclo de vida del UI mediante `collectAsStateWithLifecycle()`.

#### 4. Seguridad, Widgets y Herramientas
* **Criptografía y Biometría Local:** Autenticación biométrica integrada (`androidx.biometric.BiometricPrompt`) para el resguardo de historiales e información de cuenta.
* **AppWidget de Inicio (Glance):** Widget interactivo para la pantalla de inicio con actualización periódica de recursos disponibles y accesos de ejecución rápida.
* **Entorno Sandbox / Modo Simulación:** Módulo de emulación que permite generar respuestas USSD virtuales para pruebas de desarrollo sin consumir saldo ni requerir conexión a la red de antenas base.
* **Política de Cero Telemetría (100% Offline-First):** La aplicación no realiza peticiones HTTP a servidores externos, no integra SDKs de publicidad ni almacena datos fuera del entorno local del dispositivo.

---

### 📋 Ficha Técnica

| Parámetro | Especificación |
| :--- | :--- |
| **Plataforma** | Android (Kotlin) |
| **SDK Mínimo** | API 30 (Android 11.0) |
| **SDK Objetivo / Compilación** | API 36 |
| **Toolkit de UI** | Jetpack Compose con M3 Expressive |
| **Motor de Base de Datos** | Room Database con SQLite nativo |
| **Motor Gráfico** | Vico Compose M3 / Canvas |
| **Licencia** | Open Source |
