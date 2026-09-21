package com.ams.megascu.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ApkDownloadManager {

    private const val TAG = "ApkDownloadManager"

    /**
     * Cleans up any cached APK updates from previous downloads to free storage and avoid stale artifacts.
     */
    fun clearDownloadedUpdates(context: Context) {
        try {
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir, "updates")
            if (downloadDir.exists() && downloadDir.isDirectory) {
                downloadDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk", ignoreCase = true)) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning up update files: ${e.message}")
        }
    }

    /**
     * Downloads an APK file from [downloadUrl] reporting byte-level progress and strictly validates
     * URL authenticity, maximum payload boundaries, SHA-256 hash integrity, and cryptographic signatures.
     * Follows HTTP redirects (e.g. GitHub Releases -> AWS S3 assets) up to 5 hops only if secure.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        versionName: String,
        estimatedSizeBytes: Long = 0L,
        expectedSha256: String? = null,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, progress: Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var destinationFile: File? = null

        // 1. Verificación previa de seguridad de la URL inicial
        if (!ApkSecurityValidator.isValidReleaseUrl(downloadUrl)) {
            return@withContext Result.failure(
                SecurityException("La URL de descarga no cumple con las directivas de seguridad HTTPS y dominios autorizados de GitHub.")
            )
        }

        try {
            var currentUrl = downloadUrl
            var redirects = 0
            var connected = false

            while (redirects < 5 && !connected) {
                if (!ApkSecurityValidator.isValidReleaseUrl(currentUrl)) {
                    return@withContext Result.failure(
                        SecurityException("Redirección rechazada: la URL de destino no es un host seguro de GitHub.")
                    )
                }

                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 20000
                    readTimeout = 25000
                    setRequestProperty("User-Agent", "MegasCU-Updater/${com.ams.megascu.BuildConfig.VERSION_NAME}")
                    setRequestProperty("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308
                ) {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (!location.isNullOrBlank()) {
                        currentUrl = location
                        redirects++
                    } else {
                        break
                    }
                } else if (status in 200..299) {
                    connected = true
                } else {
                    return@withContext Result.failure(
                        Exception("Error del servidor al descargar la APK (HTTP $status)")
                    )
                }
            }

            if (connection == null || !connected) {
                return@withContext Result.failure(Exception("No se pudo conectar con el servidor de descargas."))
            }

            val remoteLength = connection.contentLengthLong.let { if (it > 0) it else connection.contentLength.toLong() }
            if (remoteLength > ApkSecurityValidator.MAX_APK_SIZE_BYTES) {
                return@withContext Result.failure(
                    SecurityException("El tamaño del paquete excede el límite máximo permitido de seguridad (150 MB).")
                )
            }

            val totalBytes = if (remoteLength > 0) remoteLength else estimatedSizeBytes

            val sanitizedVersion = versionName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            destinationFile = File(downloadDir, "MegasCU_$sanitizedVersion.apk")

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val input = BufferedInputStream(connection.inputStream, 32 * 1024)
            val output = FileOutputStream(destinationFile)

            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var totalDownloaded = 0L

            while (input.read(buffer).also { bytesRead = it } != -1) {
                totalDownloaded += bytesRead
                if (totalDownloaded > ApkSecurityValidator.MAX_APK_SIZE_BYTES) {
                    output.close()
                    input.close()
                    destinationFile.delete()
                    return@withContext Result.failure(
                        SecurityException("La descarga excedió el límite máximo de seguridad de almacenamiento (150 MB).")
                    )
                }
                output.write(buffer, 0, bytesRead)
                val progress = if (totalBytes > 0) {
                    (totalDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                } else {
                    -1f // Indeterminado
                }
                onProgress(totalDownloaded, totalBytes, progress)
            }

            output.flush()
            output.close()
            input.close()

            // 2. Validación criptográfica rigurosa post-descarga (SHA-256 + Firma Digital + PackageName)
            val validation = ApkSecurityValidator.verifyApk(
                context = context,
                apkFile = destinationFile,
                expectedSha256 = expectedSha256
            )

            if (!validation.isValid) {
                destinationFile.delete()
                return@withContext Result.failure(
                    SecurityException(validation.errorMessage ?: "Fallo de validación de integridad y firma del APK descargado.")
                )
            }

            Log.i(TAG, "APK verificado exitosamente. SHA-256: ${validation.actualSha256}, Firma válida: ${validation.isSignatureValid}")
            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update APK", e)
            destinationFile?.delete()
            Result.failure(e)
        } finally {
            try {
                connection?.disconnect()
            } catch (e: Exception) { Log.w(TAG, "No se pudo cerrar la conexión HTTP", e) }
        }
    }

    /**
     * Launches the native Android Package Installer for the given [apkFile] using FileProvider.
     */
    fun installApk(context: Context, apkFile: File, expectedSha256: String): Boolean {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Log.e(TAG, "APK file does not exist or is empty: ${apkFile.absolutePath}")
                return false
            }

            val validation = ApkSecurityValidator.verifyApk(
                context = context,
                apkFile = apkFile,
                expectedSha256 = expectedSha256
            )
            if (!validation.isValid) {
                Log.e(TAG, "Instalación bloqueada: ${validation.errorMessage}")
                return false
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching package installer", e)
            false
        }
    }
}

