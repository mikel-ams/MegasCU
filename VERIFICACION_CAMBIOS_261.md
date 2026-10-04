# Verificación de MegasCU 0.9.4-beta_(261)

## Aplicación del ZIP

Descomprimir sobre la raíz del proyecto de la versión 260 proporcionada, conservando las rutas y reemplazando los archivos indicados. El ZIP es un paquete de diferencias: no contiene la aplicación completa, APK, fuentes/imágenes sin cambios ni archivos de firma. Incluye los archivos nuevos necesarios y pruebas de regresión.

## Comprobaciones ejecutadas

- **34 pruebas JUnit aprobadas**, compiladas con Kotlin 2.2.10: diferencias de datos generales/LTE, requisito de dos fechas, saldo cero, recargas, días sin observación, muestras duplicadas o futuras, cambio de año/zona horaria, separación por SIM, exclusión de snapshots en caché, respuestas USSD inválidas, resultados parciales y recuperación con reintentos.
- Para ejecutar estas pruebas de lógica fuera de Android se usaron las clases de producción originales, el modelo UssdResult extraído sin cambios y anotaciones Room mínimas como soporte de compilación. No sustituyen los tests de integración Android.
- **Compilación de comprobación de APIs Android** del renderizador de fuente y lector de estadísticas, con Kotlin 2.2.10, referencia Android API 36 y AndroidX Core 1.18.0. Se usaron referencias mínimas para R, PermissionUtils y SimOperatorUtils. Esta comprobación valida tipos y llamadas Android de esos dos archivos, no la compilación de toda la app con SDK 37.
- **SQL de migración 8→9 y consulta DAO ejecutados en SQLite** sobre la tabla del esquema 8 original: los valores anteriores se conservan, se inicializa isDataObservation=0 y la consulta separa muestras verificadas y subscriptionId, incluido NULL.
- Análisis sintáctico de todos los archivos Kotlin cambiados y nuevos, XML de recursos bien formado y referencias de recursos revisadas.
- Los 21 textos de los widgets conservan los atributos originales de tamaño, color, fuente, contenido, fondos y márgenes/pesos/distribución. Los contenedores y overlays permiten transmitir Space Mono al launcher sin rediseñar las tarjetas; TalkBack conserva los textos.
- Fuentes, imágenes y archivos de firma existentes permanecen idénticos byte a byte. SHA-256 de los cuatro recursos protegidos coincide con gradle/integrity.gradle.kts; MD5 y SHA-256 coinciden también con el ZIP original.
- ApkSecurityValidator y ApkDownloadManager se conservan íntegros: la comparación SHA-256 sigue desactivada.
- Versión 261 sincronizada con CHANGELOG raíz, asset y fallback; la versión 260 deja de ser la más reciente. Se verifica que el ZIP solo contenga diferencias respecto al original y que al superponerlo reconstruya el proyecto corregido.

## Comprobaciones pendientes en Google AI Studio / Android

Este entorno no dispone de Gradle, SDK Android 37, Java 21 ni emulador/ADB configurados. No se ha generado ni firmado un APK, no se ejecutó compile_applet y no se ejecutaron las pruebas Robolectric/Compose de integración. La revisión estática de atributos no demuestra el resultado visual ni la fluidez en un dispositivo.

Ejecutar en el entorno del proyecto, con los secretos de firma ya configurados:

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleRelease
```

KSP generará el esquema Room 9.json al compilar. La migración es aditiva y no borra historial. Los registros antiguos permanecen visibles, pero no alimentan la nueva estimación porque no identificaban si los datos se habían consultado o copiado de caché. Para la gráfica por historial se necesitan nuevas consultas correctas de datos en al menos dos fechas distintas.

Pruebas de integración añadidas: StatusHistoryPersistenceTest (guardado concurrente, rollback, caché y observaciones), UsageObservationMigrationTest (migración), WidgetTextRendererTest (contenido, dimensiones y chips) y ChangelogCollapseAnimationTest (posiciones intermedias de la siguiente tarjeta al cerrar). DualSimUssdAndSyncTest recibe explícitamente su base en memoria para que las transacciones usen la misma base que sus DAOs.

Comprobar en dispositivo:

1. Actualización con todas las consultas correctas, un fallo, todos los fallos y pérdida de red durante una consulta. Debe mostrarse completa, parcial o fallida; un saldo cero válido debe ser éxito. Verificar que al finalizar ya se hayan guardado los datos.
2. Pulsar actualizar en los widgets 4×2, 2×1 y gráfico, también con la app cerrada. Deben encolar consultas, respetar la SIM configurada, guardar historial y notificar el resultado; pulsaciones repetidas no deben duplicar trabajos pendientes para la misma SIM.
3. Widgets con distintos tamaños, orientación, tema y escala de fuente, incluidas etiquetas de días visibles/ocultas. Comparar distribución con la versión 260 y comprobar Space Mono y TalkBack.
4. SIM 1 y SIM 2, intercambio de SIM y borrado de datos. No debe recuperarse saldo desde la caché de otra línea ni después de borrar los datos.
5. Un día de consultas: estado de recopilación; dos fechas: diferencias reales; recarga: sin consumo negativo; cero tráfico Android: cero legítimo. El tráfico Android mide bytes del dispositivo y puede diferir de la facturación de Cubacel. Con dos SIM y sin identificador de abonado accesible, se usa historial para evitar atribuir tráfico agregado a una sola línea.
6. Expandir, contraer e invertir rápidamente la animación de tarjetas en Historial de Cambios; las siguientes deben desplazarse durante toda la transición.

## Alcance

SHA-256 permanece desactivado, como se solicitó. No se cambiaron dependencias, diseño de widgets, códigos de compras USSD, recursos binarios, secretos ni archivos de firma. La entrega es un ZIP con cambios para revisión e integración; la compilación Android completa y validación visual siguen pendientes.
