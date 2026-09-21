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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


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
    val sha256Checksum: String? = null,
    val errorMessage: String? = null
)

object GitHubUpdateChecker {

    const val DEFAULT_REPO = "mikel-ams/MegasCU"
    private const val PREF_NAME = "megas_prefs"
    const val PREF_GITHUB_REPO = "pref_github_repo" // retained for backward compatibility; ignored in release builds
    const val PREF_AUTO_UPDATE_CHECK = "pref_auto_update_check"
    const val PREF_LAST_UPDATE_CHECK_TIME = "pref_last_update_check_time"
    const val PREF_UPDATE_AVAILABLE = "pref_update_available"
    const val PREF_UPDATE_VERSION_NAME = "pref_update_version_name"
    const val PREF_UPDATE_VERSION_CODE = "pref_update_version_code"
    const val PREF_UPDATE_TITLE = "pref_update_title"
    const val PREF_UPDATE_CHANGELOG = "pref_update_changelog"
    const val PREF_UPDATE_APK_URL = "pref_update_apk_url"
    const val PREF_UPDATE_RELEASE_URL = "pref_update_release_url"
    const val PREF_UPDATE_SHA256 = "pref_update_sha256"
    const val PREF_UPDATE_DISMISSED_VERSION = "pref_update_dismissed_version"

    private const val MAX_RELEASES = 10
    private const val CONNECT_TIMEOUT_MS = 12_000
    private const val READ_TIMEOUT_MS = 12_000

    suspend fun checkForUpdates(
        context: Context,
        repoOwnerAndName: String? = null
    ): UpdateCheckResult = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val targetRepo = when {
            BuildConfig.DEBUG && !repoOwnerAndName.isNullOrBlank() && ApkSecurityValidator.isValidRepoIdentifier(repoOwnerAndName) -> {
                repoOwnerAndName.trim()
            }
            else -> DEFAULT_REPO
        }

        prefs.edit().remove(PREF_GITHUB_REPO).apply()

        val apiUrl = "https://api.github.com/repos/$targetRepo/releases?per_page=$MAX_RELEASES"
        if (!ApkSecurityValidator.isValidReleaseUrl(apiUrl)) {
            return@withContext UpdateCheckResult(
                isSuccess = false,
                errorMessage = "La URL de la API de GitHub no cumple con los requisitos de seguridad HTTPS."
            )
        }

        try {
            val response = readText(apiUrl, "application/vnd.github+json")
            val releaseObjects = parseReleaseObjects(response)
            if (releaseObjects.isEmpty()) {
                return@withContext UpdateCheckResult(
                    isSuccess = true,
                    isUpdateAvailable = false,
                    hasReleasesFound = false,
                    errorMessage = "No se encontraron releases públicos en '$targetRepo'."
                )
            }

            data class ReleaseCandidate(
                val tagName: String,
                val releaseName: String,
                val body: String,
                val htmlUrl: String,
                val publishedAtRaw: String,
                val isPrerelease: Boolean,
                val isDraft: Boolean,
                val apkDownloadUrl: String?,
                val apkSizeBytes: Long,
                val sha256Url: String?,
                val remoteVersionCode: Int,
                val remoteVersionName: String,
                val isNewer: Boolean
            )

            val candidates = releaseObjects.mapNotNull { json ->
                if (json.optBoolean("draft", false)) return@mapNotNull null

                val tagName = json.optString("tag_name", "").trim()
                val releaseName = json.optString("name", "").trim()
                val body = json.optString("body", "").trim()
                val htmlUrl = json.optString("html_url", "").trim()
                val publishedAtRaw = json.optString("published_at", "")
                val isPrerelease = json.optBoolean("prerelease", false)

                var apkDownloadUrl: String? = null
                var apkSizeBytes = 0L
                var sha256Url: String? = null

                json.optJSONArray("assets")?.let { assets ->
                    for (i in 0 until assets.length()) {
                        val asset = assets.optJSONObject(i) ?: continue
                        val assetName = asset.optString("name", "").trim()
                        val lower = assetName.lowercase(Locale.ROOT)
                        val downloadUrl = asset.optString("browser_download_url", "").trim()
                        if (!ApkSecurityValidator.isValidReleaseUrl(downloadUrl)) continue

                        when {
                            lower.endsWith(".apk") && apkDownloadUrl == null -> {
                                apkDownloadUrl = downloadUrl
                                apkSizeBytes = asset.optLong("size", 0L)
                            }
                            (lower.endsWith(".sha256") || lower.endsWith(".sha256sum")) && sha256Url == null -> {
                                sha256Url = downloadUrl
                            }
                        }
                    }
                }

                val remoteVersionCode = extractBuildCode(tagName, releaseName)
                val remoteVersionName = releaseName.ifBlank { tagName }
                val isNewer = isVersionNewer(
                    remoteTag = tagName,
                    remoteReleaseName = releaseName,
                    remoteCode = remoteVersionCode,
                    currentCode = BuildConfig.VERSION_CODE,
                    currentName = BuildConfig.VERSION_NAME
                )

                if (apkDownloadUrl == null || remoteVersionCode <= 0) return@mapNotNull null

                ReleaseCandidate(
                    tagName = tagName,
                    releaseName = releaseName,
                    body = body,
                    htmlUrl = htmlUrl,
                    publishedAtRaw = publishedAtRaw,
                    isPrerelease = isPrerelease,
                    isDraft = false,
                    apkDownloadUrl = apkDownloadUrl,
                    apkSizeBytes = apkSizeBytes,
                    sha256Url = sha256Url,
                    remoteVersionCode = remoteVersionCode,
                    remoteVersionName = remoteVersionName,
                    isNewer = isNewer
                )
            }

            if (candidates.isEmpty()) {
                return@withContext UpdateCheckResult(
                    isSuccess = true,
                    isUpdateAvailable = false,
                    hasReleasesFound = true,
                    errorMessage = "Se encontraron releases, pero ninguno contiene una APK válida con versionCode identificable."
                )
            }

            val newerCandidates = candidates.filter { it.isNewer }
            val chosen = (newerCandidates.ifEmpty { candidates }).maxByOrNull { it.remoteVersionCode }!!

            if (chosen.isNewer && chosen.sha256Url == null) {
                return@withContext UpdateCheckResult(
                    isSuccess = false,
                    errorMessage = "El release ${chosen.remoteVersionName} no publica un archivo SHA-256 para verificar la APK. Instalación bloqueada."
                )
            }

            val checksum = chosen.sha256Url?.let { checksumUrl ->
                val checksumText = readText(checksumUrl, "text/plain")
                ApkSecurityValidator.extractExpectedSha256(checksumText)
            }

            if (chosen.isNewer && checksum.isNullOrBlank()) {
                return@withContext UpdateCheckResult(
                    isSuccess = false,
                    errorMessage = "El archivo de checksum del release ${chosen.remoteVersionName} no contiene un SHA-256 válido. Instalación bloqueada."
                )
            }

            val formattedDate = formatPublishedDate(chosen.publishedAtRaw)
            val apkSizeMb = if (chosen.apkSizeBytes > 0L) chosen.apkSizeBytes / (1024f * 1024f) else 0f
            val now = System.currentTimeMillis()

            prefs.edit().apply {
                putLong(PREF_LAST_UPDATE_CHECK_TIME, now)
                putBoolean(PREF_UPDATE_AVAILABLE, chosen.isNewer)
                if (chosen.isNewer) {
                    putString(PREF_UPDATE_VERSION_NAME, chosen.remoteVersionName)
                    putInt(PREF_UPDATE_VERSION_CODE, chosen.remoteVersionCode)
                    putString(PREF_UPDATE_TITLE, chosen.releaseName.ifBlank { chosen.tagName })
                    putString(PREF_UPDATE_CHANGELOG, chosen.body)
                    putString(PREF_UPDATE_APK_URL, chosen.apkDownloadUrl ?: "")
                    putString(PREF_UPDATE_RELEASE_URL, chosen.htmlUrl)
                    putString(PREF_UPDATE_SHA256, checksum)
                }
            }.apply()

            UpdateCheckResult(
                isSuccess = true,
                isUpdateAvailable = chosen.isNewer,
                isPrerelease = chosen.isPrerelease,
                latestVersionName = chosen.remoteVersionName,
                latestVersionCode = chosen.remoteVersionCode,
                releaseTitle = chosen.releaseName.ifBlank { chosen.tagName },
                changelog = chosen.body,
                apkDownloadUrl = chosen.apkDownloadUrl,
                releaseHtmlUrl = chosen.htmlUrl,
                publishedAt = formattedDate,
                apkSizeMb = apkSizeMb,
                sha256Checksum = checksum
            )
        } catch (e: Exception) {
            android.util.Log.w("GitHubUpdateChecker", "No se pudo comprobar GitHub", e)
            UpdateCheckResult(
                isSuccess = false,
                errorMessage = "No se pudo conectar con GitHub. Verifica tu conexión a internet (${e.localizedMessage ?: "error de red"})."
            )
        }
    }

    private fun parseReleaseObjects(response: String): List<JSONObject> {
        val trimmed = response.trim()
        if (trimmed.startsWith("[")) {
            val array = JSONArray(trimmed)
            return buildList {
                for (i in 0 until array.length()) {
                    array.optJSONObject(i)?.let(::add)
                }
            }
        }
        return if (trimmed.startsWith("{")) listOf(JSONObject(trimmed)) else emptyList()
    }

    private fun readText(urlString: String, accept: String): String {
        var currentUrl = urlString
        repeat(4) { hop ->
            require(ApkSecurityValidator.isValidReleaseUrl(currentUrl)) { "URL no confiable" }
            val connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = false
                setRequestProperty("Accept", accept)
                setRequestProperty("User-Agent", "MegasCU-Android-App/${BuildConfig.VERSION_NAME}")
            }
            try {
                when (val responseCode = connection.responseCode) {
                    HttpURLConnection.HTTP_MOVED_PERM,
                    HttpURLConnection.HTTP_MOVED_TEMP,
                    HttpURLConnection.HTTP_SEE_OTHER,
                    307, 308 -> {
                        val location = connection.getHeaderField("Location")
                            ?: throw IllegalStateException("Redirección sin destino")
                        require(ApkSecurityValidator.isValidReleaseUrl(location)) {
                            "Redirección a un host no autorizado"
                        }
                        currentUrl = location
                        if (hop == 3) throw IllegalStateException("Demasiadas redirecciones")
                    }
                    in 200..299 -> {
                        val contentLength = connection.contentLengthLong
                        if (contentLength > 2 * 1024 * 1024L && accept == "text/plain") {
                            throw IllegalStateException("Checksum excesivamente grande")
                        }
                        return BufferedReader(
                            InputStreamReader(connection.inputStream, Charsets.UTF_8)
                        ).use { it.readText() }
                    }
                    else -> throw IllegalStateException("HTTP $responseCode")
                }
            } finally {
                connection.disconnect()
            }
        }
        error("No se pudo resolver la URL")
    }

    /** Supports the repository's existing beta_(W) form and the canonical beta.W form. */
    fun extractBuildCode(tagName: String, releaseName: String): Int {
        val canonical = Regex(
            """^v?(\d+)\.(\d+)\.(\d+)-(?:alpha|beta|rc)[._]\(?([0-9]{1,6})\)?$""",
            RegexOption.IGNORE_CASE
        )
        val legacy = Regex(
            """^v?(\d+)\.(\d+)\.(\d+)-beta_\(([0-9]{1,6})\)$""",
            RegexOption.IGNORE_CASE
        )

        for (value in listOf(tagName.trim(), releaseName.trim())) {
            canonical.matchEntire(value)?.groupValues?.getOrNull(4)?.toIntOrNull()?.let { return it }
            legacy.matchEntire(value)?.groupValues?.getOrNull(4)?.toIntOrNull()?.let { return it }
        }

        // Legacy releases occasionally kept the build only in the human-readable release title.
        Regex("""\b(?:build|version\s*code|vc)\s*[#:=-]?\s*([0-9]{2,6})\b""", RegexOption.IGNORE_CASE)
            .find(releaseName)?.groupValues?.getOrNull(1)?.toIntOrNull()?.let { return it }

        return 0
    }

    fun isVersionNewer(
        remoteTag: String,
        remoteReleaseName: String,
        remoteCode: Int,
        currentCode: Int,
        currentName: String
    ): Boolean {
        if (remoteCode > 0 && currentCode > 0) {
            return remoteCode > currentCode
        }

        val remote = parseSemver(remoteTag) ?: parseSemver(remoteReleaseName) ?: return false
        val current = parseSemver(currentName) ?: return false
        return compareVersion(remote, current) > 0
    }

    private data class ParsedVersion(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val channel: Int,
        val build: Int
    )

    private fun parseSemver(value: String): ParsedVersion? {
        val match = Regex(
            """^v?(\d+)\.(\d+)\.(\d+)(?:-(alpha|beta|rc)[._]?\(?([0-9]+)\)?)?$""",
            RegexOption.IGNORE_CASE
        ).matchEntire(value.trim()) ?: return null
        val channel = when (match.groupValues[4].lowercase(Locale.ROOT)) {
            "alpha" -> 1
            "beta" -> 2
            "rc" -> 3
            else -> 4
        }
        return ParsedVersion(
            major = match.groupValues[1].toInt(),
            minor = match.groupValues[2].toInt(),
            patch = match.groupValues[3].toInt(),
            channel = channel,
            build = match.groupValues.getOrNull(5)?.toIntOrNull() ?: Int.MAX_VALUE
        )
    }

    private fun compareVersion(a: ParsedVersion, b: ParsedVersion): Int =
        compareValuesBy(a, b, { it.major }, { it.minor }, { it.patch }, { it.channel }, { it.build })

    private fun formatPublishedDate(rawDate: String): String {
        if (rawDate.isBlank()) return ""
        return runCatching {
            val instant = Instant.parse(rawDate)
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(Locale("es", "ES"))
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrElse { rawDate.take(10) }
    }

    fun getFormattedLastCheck(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val timestamp = prefs.getLong(PREF_LAST_UPDATE_CHECK_TIME, 0L)
        if (timestamp == 0L) return "Nunca"
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000L -> "Hace un momento"
            diff < 3_600_000L -> "Hace ${diff / 60_000L} min"
            else -> formatPublishedDate(Instant.ofEpochMilli(timestamp).toString())
        }
    }
}
