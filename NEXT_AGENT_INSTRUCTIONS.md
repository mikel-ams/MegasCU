# Próximas instrucciones y estado de MegasCU

## Estado actual: 0.9.4-beta_(262)

- Entrega solicitada: ZIP con archivos modificados y nuevos, conservando rutas; no APK ni publicación.
- SHA-256: comparación desactivada, lógica original intacta por decisión del usuario.
- Correcciones: resultados USSD completos/parciales/fallidos, respuesta validada, guardado transaccional compartido con historial, WorkManager para refrescar widgets y fuente Space Mono sin rediseño.
- Base de datos 9: migración 8→9 no destructiva; isDataObservation distingue nuevas consultas verificadas de snapshots en caché. El esquema 9.json lo genera KSP al compilar en AI Studio.
- Gráficas: consumo observado a partir de disminuciones de datos generales + LTE, al menos dos fechas; recargas excluidas, misma SIM, sin datos inventados. Android puede ofrecer estadísticas de tráfico con acceso de uso, sujeto a restricciones por SIM.
- Animación: AnimatedVisibility es el único propietario de la altura de las tarjetas; timeline dibujado sobre las dimensiones reales.
- CHANGELOG raíz, asset y fallback sincronizados; 261 es la única versión más reciente.
- La compilación Android completa y las pruebas Robolectric/Compose deben ejecutarse en AI Studio. Consultar VERIFICACION_CAMBIOS_261.md para las comprobaciones efectuadas y pendientes. No afirmar que existe APK 261 compilado o firmado.
- Recursos binarios y archivos de firma preservados byte a byte. No cambiar secretos ni keystore.

## Estado previo (referencia histórica, build 260)

# Próximas Instrucciones y Estado de MegasCU

Este archivo contiene el estado consolidado del proyecto y las tareas completadas al cierre de la sesión de chat anterior, permitiendo que el siguiente agente retome el desarrollo exactamente en el mismo punto sin pérdida de contexto.

---

## 📌 Estado de la Versión Actual
* **Versión:** `0.9.3-beta_(260)`
* **Código de Versión (versionCode):** `260`
* **Compilación de Producción:** Firmada exitosamente con la firma oficial (`keystore/release-key.jks`).
* **Optimización y Minificación:** Se aplicó R8 ProGuard de forma completa en el perfil de `release` (`isMinifyEnabled = true` y `isShrinkResources = true`).
* **Reducción de Peso de APK:** El tamaño de la aplicación se optimizó drásticamente, pasando de **24 MB** a tan solo **3.0 MB**.

---

## 🛠️ Cambios Consolidados en la Sesión Reciente

1. **Consolidación de Versión 0.9.3-beta_(260):**
   - Unificados todos los cambios de las builds 259 y 260 en la entrada única e integral de la versión `0.9.3-beta_(260)` en `CHANGELOG.md`, `app/src/main/assets/CHANGELOG.md` y `ChangelogRepository`.

2. **Configuración de Indicadores y Barras de Carga (LoadingIndicatorsConfig):**
   - Ajustados los valores por defecto en `LoadingIndicatorsConfig.kt` a la especificación exacta JSON:
     - `linear_wavy`: height 10.0dp, amplitude 0.60dp, wavelength 20.0dp, stroke 4.0dp.
     - `circular_wavy`: size 30.0dp, stroke 4.0dp, amplitude 2.00dp, waves_count 10.
     - `linear_determinate`: height 8.0dp, corner_radius 4.0dp.
     - `linear_indeterminate`: height 8.0dp, corner_radius 4.0dp.
     - `circular_standard`: size 30.0dp, stroke 4.0dp.
     - `shape_morphing`: size 40.0dp.

3. **Línea de Tiempo del Historial de Cambios (ChangelogBottomSheet):**
   - Conexión ajustada entre líneas y puntos del timeline con separación simétrica exacta de 4.0dp (`gap = 4.dp.toPx()`) y altura mínima intrínseca (`IntrinsicSize.Min`) en cada fila para asegurar trazado continuo sin desfasajes.
   - Animación de apertura/cierre en tarjetas colapsables optimizada con físicas de resorte (`Spring.StiffnessMediumLow`, `Spring.DampingRatioNoBouncy`) y `animateContentSize` + rotación suave del icono de flecha (`graphicsLayer { rotationZ }`), logrando transiciones fluidas y sin tirones.

4. **Botón Flotante en Modo Simple (MainActivity):**
   - Sustituido el botón flotante en la pantalla inicial del modo simple por un **Medium Animated Extended Floating Action Button** (`ExtendedFloatingActionButton`) con el texto "Actualizar", el icono `Icons.Rounded.Refresh` y la animación de colapso/extensión fluida de Material 3.

5. **Visualización de Fecha y Hora de Última Actualización (PriorityStatusCard):**
   - Añadido banner desplegable de última actualización (`LastUpdatedBanner`) oculto por defecto y visible al pulsar sobre la tarjeta principal tanto en Modo Experto como en Modo Simple.
   - Animación de salida expresiva combinando expansión vertical (`expandVertically`), deslizamiento (`slideInVertically`), desvanecimiento (`fadeIn`) y escalado suave (`scaleIn`) con física de rebote elástica `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)` y contenedor animado (`animateContentSize`).

---

## 📋 Tareas Pendientes o Siguientes Pasos sugeridos
* **Monitoreo de comportamiento:** Evaluar en emulador que todos los diálogos y animaciones de carga sigan mostrándose perfectamente con las nuevas reglas de ProGuard.
* **Nuevas Características:** Continuar con el mapa de ruta que proponga el usuario para la siguiente etapa de desarrollo.

---

## 📂 Archivos Clave para Referencia Rápida
* **Configuración del Build:** `app/build.gradle.kts`
* **Reglas de Optimización:** `app/proguard-rules.pro`
* **Pantalla Principal:** `MainActivity.kt`
* **Historial de Cambios:** `ChangelogBottomSheet.kt` y `CHANGELOG.md`
* **Widgets del Sistema:** `MegasWidgetProvider.kt` y layouts XML en `res/layout/`
