# Instrucciones Persistentes (Reglas de Oro)

1. **Compilación Fresca en Release, Subida de APK y Enlaces de Descarga (Dual Upload):**
   - **Limpieza de APKs Residuales:** Antes de compilar, elimina siempre cualquier binario previo:
     ```bash
     rm -f MegasCU_*.apk app/build/outputs/apk/release/*.apk app/build/outputs/apk/debug/*.apk
     ```
   - **Compilación Oficial:** Ejecuta siempre `gradle :app:assembleRelease` para compilar la versión con la keystore de producción oficial (`keystore/release-key.jks`).
   - **Copia y Renombrado:** Toma el binario generado en `app/build/outputs/apk/release/app-release.apk` y cópialo con el formato exacto de versión:
     ```bash
     cp app/build/outputs/apk/release/app-release.apk MegasCU_<versionName>.apk
     ```
     *(Ejemplo: `MegasCU_0.9.3-beta_(260).apk`, teniendo cuidado de escapar paréntesis en comandos bash `\(` y `\)`).*
   - **Subida Dual Obligatoria:**
     - **Litterbox (Catbox):**
       ```bash
       LITTERBOX_URL=$(curl -s -F "reqtype=fileupload" -F "time=72h" -F "fileToUpload=@MegasCU_<versionName>.apk" https://litterbox.catbox.moe/resources/internals/api.php)
       ```
     - **Tempfiles (tmpfiles.org):**
       ```bash
       TMP_RESP=$(curl -s -F "file=@MegasCU_<versionName>.apk" -F "expire=28800" https://tmpfiles.org/api/v1/upload)
       ```
       - La respuesta JSON contiene la URL en `.data.url` (ej: `https://tmpfiles.org/3973950/megascu_0.9.3-beta_(260).apk`).
       - **Página de Vista:** URL directa devuelta por la API (`https://tmpfiles.org/<id>/<archivo>`).
       - **Descarga Directa:** Transformar insertando `/dl/` tras el dominio (`https://tmpfiles.org/dl/<id>/<archivo>`) o extraer con `echo "$TMP_VIEW_URL" | sed 's|tmpfiles.org/|tmpfiles.org/dl/|'`.
   - **Presentación al Usuario:** Muestra siempre los 3 enlaces claros y estructurados:
     - 📦 **Litterbox (Catbox):** `<url_litterbox>` *(Válido por 72 horas)*
     - 📄 **Tempfiles (Página de Vista):** `<url_vista_tempfiles>`
     - 📥 **Tempfiles (Descarga Directa):** `<url_descarga_directa_tempfiles>`
   - **Sincronización con AI Studio:** Ejecuta `compile_applet` tras compilar para actualizar la vista previa interactiva en el emulador web.

2. **Resumen Estructurado de Cambios:** Al finalizar cada turno de modificaciones, entrega siempre un resumen claro, estructurado y profesional de los cambios realizados.

3. **Sistema Estricto de Versionado:**
   - Cada nueva versión incrementa `versionCode` (entero secuencial) y `versionName` en `app/build.gradle.kts`.
   - El formato estricto de `versionName` es `X.Y.Z-beta_(W)`, donde `W` DEBE coincidir exactamente con el valor numérico de `versionCode` (ejemplo: `versionCode = 260` -> `versionName = "0.9.3-beta_(260)"`).

4. **Manejo Estricto Byte a Byte de Recursos Binarios (Fuentes e Imágenes):**
   - Todos los binarios (.ttf y .webp) provienen de los archivos comprimidos fuente de referencia (`res_font.zip` / `res_fonts.zip` para las fuentes TrueType Space Mono y `res_images.zip` para las imágenes del menú).
   - NUNCA deben modificarse con herramientas de texto, editores de código o convertidores que inserten secuencias de reemplazo (`EF BF BD`) o alteren sus bytes.
   - En cada compilación y verificación de la app, se comprueban los hashes MD5 y SHA-256 de los archivos en `src/main/res/` para certificar su integridad absoluta.

5. **Gestión Detallada del Registro de Cambios (CHANGELOG y App):**
   - **En `CHANGELOG.md`:** Cada vez que se publique una nueva versión o se consoliden cambios, se documenta bajo los estándares *Keep a Changelog 1.1.0* y *Semantic Versioning 2.0.0*:
     ```markdown
     ## [X.Y.Z-beta_(W)] - YYYY-MM-DD

     ### Añadido
     - **Título en Negrita:** Descripción clara de la nueva funcionalidad o componente.

     ### Cambiado
     - **Título en Negrita:** Descripción de modificaciones, optimizaciones y refactorizaciones.

     ### Corregido
     - **Título en Negrita:** Descripción de correcciones de errores y bugs resueltos.
     ```
   - **Dentro de la App (`ChangelogBottomSheet.kt`):**
     - Actualizar la lista `defaultFallback` en `ChangelogRepository` agregando la nueva versión con `isLatest = true` y cambiando la versión previa a `isLatest = false`.
     - Clasificar cada cambio en su sección correspondiente (`ChangeCategory.ADDED`, `ChangeCategory.CHANGED`, `ChangeCategory.FIXED`, etc.) manteniendo el formato `"Título: Descripción"`.
   - **Bloque Copiable para GitHub Releases:** Proporcionar SIEMPRE al final de la respuesta un bloque de código markdown listo para copiar y pegar en las notas de la Release de GitHub.

6. **Exclusión Estricta de Ajustes de Desarrollo en CHANGELOG:**
   - Cualquier cambio, añadido, ajuste o eliminación correspondiente a las herramientas internas, Laboratorios de Pruebas u Opciones de Desarrollador NO debe reflejarse nunca en `CHANGELOG.md`, en las notas de versión de la app ni en las publicaciones de GitHub Releases.

7. **Protección e Inmunidad Estricta de Keystore (`keystore/release-key.jks` y `app/keystore.properties`):**
   - La carpeta `keystore/`, el almacén `keystore/release-key.jks` y `app/keystore.properties` son estrictamente requeridos para la firma oficial de producción.
   - Queda TERMINANTEMENTE PROHIBIDO eliminar, limpiar, reescribir o mover estos archivos en scripts o comandos `rm`.

8. **Actualización de Insignias y Recursos en README.md:**
   - Cada vez que se actualice la versión de la aplicación, se DEBE actualizar la insignia de versión en `README.md`:
     ```markdown
     [![Version: X.Y.Z-beta_(W)](https://img.shields.io/badge/Version-X.Y.Z--beta__(W)-007ACC?style=flat&logo=android&logoColor=white)](CHANGELOG.md)
     ```
     garantizando que refleje fielmente el estado actual del repositorio junto con la cabecera visual de la aplicación.


