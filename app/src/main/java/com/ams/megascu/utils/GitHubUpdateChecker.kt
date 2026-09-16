package com.ams.megascu.utils

import android.content.Context
import com.ams.megascu.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

data class UpdateCheckResult(
    val isSuccess: Boolean,
    val isUpdateAvailable: Boolean = false,
    val isPrerelease: Boolean = false,
    val hasReleasesFound: Boolean = true,
    val latestVersionName: String = "",
    val latestVersionCode: Int = 0,
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val currentVersionCode: Int = BuildConfig.VERSION_CODE,
    val releaseTitle: String = "",
    val changelog: String = "",
    val apkDownloadUrl: String? = null,
    val releaseHtmlUrl: String = "",
    val publishedAt: String = "",
    val apkSizeMb: Float = 0f,
    val errorMessage: String? = null
)

object GitHubUpdateChecker {

    const val DEFAULT_REPO = "migue-ams/MegasCU"
    private const val PREF_NAME = "megas_prefs"
    const val PREF_GITHUB_REPO = "pref_github_repo"
    const val PREF_AUTO_UPDATE_CHECK = "pref_auto_update_check"
    const val PREF_LAST_UPDATE_CHECK_TIME = "pref_last_update_check_time"
    const val PREF_UPDATE_AVAILABLE = "pref_update_available"
    const val PREF_UPDATE_VERSION_NAME = "pref_update_version_name"
    const val PREF_UPDATE_VERSION_CODE = "pref_update_version_code"
    const val PREF_UPDATE_TITLE = "pref_update_title"
    const val PREF_UPDATE_CHANGELOG = "pref_update_changelog"
    const val PREF_UPDATE_APK_URL = "pref_update_apk_url"
    const val PREF_UPDATE_RELEASE_URL = "pref_update_release_url"
    const val PREF_UPDATE_DISMISSED_VERSION = "pref_update_dismissed_version"

    suspend fun checkForUpdates(context: Context, repoOwnerAndName: String? = null): UpdateCheckResult = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val targetRepo = repoOwnerAndName ?: prefs.getString(PREF_GITHUB_REPO, DEFAULT_REPO)?.ifBlank { DEFAULT_REPO } ?: DEFAULT_REPO
        val apiUrl = "https://api.github.com/repos/${targetRepo.trim()}/releases?per_page=5"

        try {
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "MegasCU-Android-App/${BuildConfig.VERSION_NAME}")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.use { it.readText() }
                connection.disconnect()

                val trimmedResponse = response.trim()
                val json: JSONObject? = if (trimmedResponse.startsWith("[")) {
                    val array = JSONArray(trimmedResponse)
                    if (array.length() > 0) array.getJSONObject(0) else null
                } else if (trimmedResponse.startsWith("{")) {
                    JSONObject(trimmedResponse)
                } else {
                    null
                }

                if (json == null) {
                    return@withContext UpdateCheckResult(
                        isSuccess = true,
                        isUpdateAvailable = false,
                        hasReleasesFound = false,
                        errorMessage = "No se encontraron releases ni pre-releases en el repositorio '$targetRepo'."
                    )
                }

                val tagName = json.optString("tag_name", "").trim()
                val releaseName = json.optString("name", "").trim()
                val body = json.optString("body", "").trim()
                val htmlUrl = json.optString("html_url", "").trim()
                val publishedAtRaw = json.optString("published_at", "")
                val isPrerelease = json.optBoolean("prerelease", false)

                var apkDownloadUrl: String? = null
                var apkSizeBytes: Long = 0L

                val assetsArray = json.optJSONArray("assets")
                if (assetsArray != null) {
                    for (i in 0 until assetsArray.length()) {
                        val asset = assetsArray.getJSONObject(i)
                        val name = asset.optString("name", "").lowercase(Locale.ROOT)
                        if (name.endsWith(".apk")) {
                            apkDownloadUrl = asset.optString("browser_download_url")
                            apkSizeBytes = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                val remoteVersionCode = extractBuildCode(tagName, releaseName)
                val remoteVersionName = if (releaseName.isNotBlank()) releaseName else tagName

                val isNewer = isVersionNewer(
                    remoteTag = tagName,
                    remoteReleaseName = releaseName,
                    remoteCode = remoteVersionCode,
                    currentCode = BuildConfig.VERSION_CODE,
                    currentName = BuildConfig.VERSION_NAME
                )

                val formattedDate = formatPublishedDate(publishedAtRaw)
                val apkSizeMb = if (apkSizeBytes > 0) apkSizeBytes / (1024f * 1024f) else 0f

                // Actualizar timestamp y preferencias
                val now = System.currentTimeMillis()
                prefs.edit().apply {
                    putLong(PREF_LAST_UPDATE_CHECK_TIME, now)
                    if (isNewer) {
                        putBoolean(PREF_UPDATE_AVAILABLE, true)
                        putString(PREF_UPDATE_VERSION_NAME, remoteVersionName)
                        putInt(PREF_UPDATE_VERSION_CODE, remoteVersionCode)
                        putString(PREF_UPDATE_TITLE, if (releaseName.isNotBlank()) releaseName else tagName)
                        putString(PREF_UPDATE_CHANGELOG, body)
                        putString(PREF_UPDATE_APK_URL, apkDownloadUrl ?: "")
                        putString(PREF_UPDATE_RELEASE_URL, htmlUrl)
                    } else {
                        putBoolean(PREF_UPDATE_AVAILABLE, false)
                    }
                    apply()
                }

                UpdateCheckResult(
                    isSuccess = true,
                    isUpdateAvailable = isNewer,
                    isPrerelease = isPrerelease,
                    latestVersionName = remoteVersionName,
                    latestVersionCode = remoteVersionCode,
                    releaseTitle = if (releaseName.isNotBlank()) releaseName else tagName,
                    changelog = body,
                    apkDownloadUrl = apkDownloadUrl,
                    releaseHtmlUrl = htmlUrl,
                    publishedAt = formattedDate,
                    apkSizeMb = apkSizeMb
                )
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                UpdateCheckResult(
                    isSuccess = false,
                    errorMessage = "No se encontraron versiones públicas en el repositorio '$targetRepo'. Asegúrate de que el repositorio sea público y tenga al menos un Release publicado."
                )
            } else {
                UpdateCheckResult(
                    isSuccess = false,
                    errorMessage = "Error del servidor de GitHub ($responseCode). Intenta nuevamente más tarde."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateCheckResult(
                isSuccess = false,
                errorMessage = "No se pudo conectar con GitHub. Verifica tu conexión a internet o el nombre del repositorio (${e.localizedMessage ?: "Error de red"})."
            )
        }
    }

    /**
     * Extrae el código de build si está presente (ej. 242 de "0.7.8-beta_(242)" o "-242")
     */
    fun extractBuildCode(tagName: String, releaseName: String): Int {
        val combined = "$tagName $releaseName"
        // Busca patrones como _(242), (242), _242, -242, Build 242, build 242
        val buildMatcher = Pattern.compile("(?:_|\\(|-|build\\s+|b)(\\d{2,5})", Pattern.CASE_INSENSITIVE).matcher(combined)
        if (buildMatcher.find()) {
            buildMatcher.group(1)?.toIntOrNull()?.let { return it }
        }
        return 0
    }

    /**
     * Compara si la versión remota es mayor a la instalada actualmente
     */
    fun isVersionNewer(
        remoteTag: String,
        remoteReleaseName: String,
        remoteCode: Int,
        currentCode: Int,
        currentName: String
    ): Boolean {
        // 1. Si tenemos un versionCode numérico en la versión remota mayor a 0 y distinto al actual
        if (remoteCode > 0 && currentCode > 0) {
            if (remoteCode > currentCode) return true
            if (remoteCode < currentCode) return false
        }

        // 2. Si los códigos numéricos son iguales o no se pudieron extraer, comparar semánticamente X.Y.Z
        val remoteSemver = extractSemver(remoteTag).ifEmpty { extractSemver(remoteReleaseName) }
        val currentSemver = extractSemver(currentName)

        if (remoteSemver.isNotEmpty() && currentSemver.isNotEmpty()) {
            val maxLen = maxOf(remoteSemver.size, currentSemver.size)
            for (i in 0 until maxLen) {
                val r = remoteSemver.getOrElse(i) { 0 }
                val c = currentSemver.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
        }

        // Si son idénticos o no se puede comprobar mayor
        return false
    }

    private fun extractSemver(versionStr: String): List<Int> {
        val matcher = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?").matcher(versionStr)
        if (matcher.find()) {
            val list = mutableListOf<Int>()
            for (i in 1..matcher.groupCount()) {
                matcher.group(i)?.toIntOrNull()?.let { list.add(it) }
            }
            return list
        }
        return emptyList()
    }

    private fun formatPublishedDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            val date = parser.parse(rawDate)
            val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "ES"))
            if (date != null) formatter.format(date) else rawDate
        } catch (e: Exception) {
            rawDate.take(10)
        }
    }

    fun getFormattedLastCheck(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(PREF_LAST_UPDATE_CHECK_TIME, 0L)
        if (timestamp == 0L) return "Nunca"
        val diff = System.currentTimeMillis() - timestamp
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return if (diff < 60_000) {
            "Hace un momento"
        } else if (diff < 3600_000) {
            "Hace ${diff / 60_000} min"
        } else {
            formatter.format(Date(timestamp))
        }
    }
}
