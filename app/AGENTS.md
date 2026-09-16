# Instrucciones Persistentes (Reglas de Oro)

1. **Compilación Fresca, Subir APK y Compartir Enlaces (Dual Upload):**
   - Siempre que se genere un APK o se actualice versión, DEBES limpiar/eliminar APKs previos residuales (`rm -f MegasCU_*.apk app/build/outputs/apk/release/*.apk app/build/outputs/apk/debug/*.apk`).
   - Ejecuta `gradle assembleRelease` de forma fresca para compilar exactamente la versión actual con el código recién modificado.
   - Antes de subir el archivo, renómbralo siguiendo el formato `MegasCU_<versionName>.apk` (por ejemplo, `MegasCU_0.7.2-beta_(220).apk`).
   - Sube este archivo recién compilado en conjunto a los dos sitios:
     - **Litterbox (Catbox):** `curl -F "reqtype=fileupload" -F "time=72h" -F "fileToUpload=@MegasCU_<versionName>.apk" https://litterbox.catbox.moe/resources/internals/api.php`
     - **Tempfiles (tmpfiles.org):** `curl -s -F "file=@MegasCU_<versionName>.apk" -F "expire=28800" https://tmpfiles.org/api/v1/upload` (para descarga directa resolver el token temporal dinámico mediante `curl -s "<url_tmpfiles>" | grep -o 'https://tmpfiles.org/dl/[^"\' ]*'` ya que la API devuelve la URL de vista y requiere `/dl/<timestamp>.<hash>/<id>/<archivo>` para evitar error 404/redirección).
   - Devuelve en tu respuesta los enlaces de descarga de ambos sitios con el mismo nombre de APK base.
   - Ejecuta `compile_applet` para garantizar que la última versión de desarrollo quede compilada e instalada en la vista previa interactiva (emulador streaming) de Google AI Studio.
2. **Resumen de Cambios:** Al finalizar cada turno de modificaciones, debes entregar siempre un resumen claro, estructurado y profesional de los cambios realizados.
3. **Sistema de Versionado:** Al actualizar la versión, el `versionName` debe usar el formato `X.Y.Z-beta_(W)`, donde `W` debe coincidir exactamente con el valor del `versionCode`.
4. **Manejo Estricto Byte a Byte de Recursos Binarios (Fuentes e Imágenes):**
   - Todos los binarios (.ttf y .webp) provienen de los archivos comprimidos fuente de referencia (`res_font.zip` / `res_fonts.zip` para las fuentes personalizadas TrueType Space Mono y `res_images.zip` para las dos imágenes del menú de Bienvenida).
   - Estos archivos NUNCA deben modificarse con herramientas de texto, editores de código o convertidores que inserten secuencias de reemplazo (`EF BF BD`) o alteren sus bytes. Deben manejarse estrictamente como binarios puros (byte a byte).
   - En cada compilación y verificación de la app, o ante cualquier reporte de fallo, se desempaquetan byte a byte y se ejecuta la verificación de hashes MD5 y SHA-256 de los archivos en `src/main/res/` comparándolos con los contenidos en los ZIP originales para certificar su integridad absoluta.
