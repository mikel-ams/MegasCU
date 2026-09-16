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
     * Downloads an APK file from [downloadUrl] reporting byte-level progress.
     * Follows HTTP redirects (e.g. GitHub Releases -> AWS S3 assets) up to 5 hops.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        versionName: String,
        estimatedSizeBytes: Long = 0L,
        onProgress: (bytesDownloaded: Long, totalBytes: Long, progress: Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            var currentUrl = downloadUrl
            var redirects = 0
            var connected = false

            while (redirects < 5 && !connected) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
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
            val totalBytes = if (remoteLength > 0) remoteLength else estimatedSizeBytes

            val sanitizedVersion = versionName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(downloadDir, "MegasCU_$sanitizedVersion.apk")

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val input = BufferedInputStream(connection.inputStream, 32 * 1024)
            val output = FileOutputStream(destinationFile)

            val buffer = ByteArray(32 * 1024)
            var bytesRead: Int
            var totalDownloaded = 0L

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                totalDownloaded += bytesRead
                val progress = if (totalBytes > 0) {
                    (totalDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                } else {
                    -1f // Indeterminate
                }
                onProgress(totalDownloaded, totalBytes, progress)
            }

            output.flush()
            output.close()
            input.close()

            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading update APK", e)
            Result.failure(e)
        } finally {
            try {
                connection?.disconnect()
            } catch (_: Exception) {}
        }
    }

    /**
     * Launches the native Android Package Installer for the given [apkFile] using FileProvider.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Log.e(TAG, "APK file does not exist or is empty: ${apkFile.absolutePath}")
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
