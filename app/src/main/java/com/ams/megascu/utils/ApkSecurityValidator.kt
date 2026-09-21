package com.ams.megascu.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

data class ApkValidationResult(
    val isValid: Boolean,
    val isSignatureValid: Boolean = false,
    val isPackageNameValid: Boolean = false,
    val actualSha256: String = "",
    val expectedSha256: String? = null,
    val isSha256Matching: Boolean? = null,
    val packageVersionName: String? = null,
    val packageVersionCode: Long = 0L,
    val errorMessage: String? = null
)

object ApkSecurityValidator {

    private const val TAG = "ApkSecurityValidator"

    // Whitelist of approved domains for update checks and downloads
    private val ALLOWED_DOMAINS = setOf(
        "github.com",
        "api.github.com",
        "objects.githubusercontent.com",
        "raw.githubusercontent.com",
        "github-releases.githubusercontent.com",
        "codeload.github.com"
    )

    private val REPO_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$")
    private val SHA256_HEX_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$")

    /** Maximum allowed APK size: 150 MB to protect against DoS/storage exhaustion */
    const val MAX_APK_SIZE_BYTES = 150 * 1024 * 1024L // 150MB

    /** Minimum expected APK size: 100 KB to avoid corrupted empty files */
    const val MIN_APK_SIZE_BYTES = 100 * 1024L

    /**
     * Validates whether a given repository identifier string is strictly in format "owner/repo".
     */
    fun isValidRepoIdentifier(repo: String?): Boolean {
        if (repo.isNullOrBlank()) return false
        return REPO_PATTERN.matcher(repo.trim()).matches()
    }

    /**
     * Validates whether a given URL is a secure HTTPS GitHub release / asset URL.
     * Prevents open-redirects, SSRF, non-HTTPS protocols, or unexpected third-party hosts.
     */
    fun isValidReleaseUrl(urlString: String?): Boolean {
        if (urlString.isNullOrBlank()) return false
        return try {
            val uri = URI(urlString.trim())
            // 1. Strict HTTPS requirement
            if (!uri.scheme.equals("https", ignoreCase = true)) {
                Log.w(TAG, "Rejected insecure URL scheme: ${uri.scheme}")
                return false
            }

            val host = uri.host?.lowercase(Locale.ROOT) ?: return false

            // 2. Reject credentials in URL (e.g. user:pass@host)
            if (uri.userInfo != null) {
                Log.w(TAG, "Rejected URL with embedded credentials: $urlString")
                return false
            }

            // 3. Port check (must be standard or default)
            if (uri.port != -1 && uri.port != 443) {
                Log.w(TAG, "Rejected URL with non-standard port: ${uri.port}")
                return false
            }

            // 4. Host whitelisting
            val isAllowed = ALLOWED_DOMAINS.contains(host) ||
                host.endsWith(".github.com") ||
                host.endsWith(".githubusercontent.com")

            if (!isAllowed) {
                Log.w(TAG, "Rejected untrusted host: $host")
            }
            isAllowed
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing release URL: $urlString", e)
            false
        }
    }

    /**
     * Calculates the SHA-256 cryptographic digest of a local file in lowercase hex.
     */
    fun calculateSha256(file: File): String {
        if (!file.exists() || file.length() == 0L) return ""
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(64 * 1024)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Searches for a 64-character SHA-256 hexadecimal hash inside release notes / changelog text.
     */
    fun extractExpectedSha256(releaseBody: String?): String? {
        if (releaseBody.isNullOrBlank()) return null
        val normalized = releaseBody.trim()
        val labelled = Pattern.compile("(?i)(?:sha256|sha-256|sha256sum|checksum)\\s*[:=]?\\s*`?([a-fA-F0-9]{64})`?").matcher(normalized)
        if (labelled.find()) return labelled.group(1)?.lowercase(Locale.ROOT)
        val firstToken = Pattern.compile("(?m)^\\s*([a-fA-F0-9]{64})(?:\\s+.*)?$").matcher(normalized)
        return if (firstToken.find()) firstToken.group(1)?.lowercase(Locale.ROOT) else null
    }

    /**
     * Verifies the cryptographic signature, SHA-256 digest, package name, and structural integrity
     * of a downloaded APK before it is dispatched to the PackageInstaller.
     */
    fun verifyApk(
        context: Context,
        apkFile: File,
        expectedSha256: String? = null,
        expectedPackageName: String = context.packageName
    ): ApkValidationResult {
        // 1. File existence and size boundaries
        if (!apkFile.exists() || !apkFile.isFile) {
            return ApkValidationResult(
                isValid = false,
                errorMessage = "El archivo APK descargado no existe en el almacenamiento local."
            )
        }

        val fileLength = apkFile.length()
        if (fileLength < MIN_APK_SIZE_BYTES) {
            return ApkValidationResult(
                isValid = false,
                errorMessage = "El archivo APK está corrupto o incompleto (tamaño menor a 100 KB)."
            )
        }
        if (fileLength > MAX_APK_SIZE_BYTES) {
            return ApkValidationResult(
                isValid = false,
                errorMessage = "El archivo APK excede el tamaño máximo permitido de seguridad (150 MB)."
            )
        }

        // 2. SHA-256 calculation and verification
        val actualSha256 = calculateSha256(apkFile)
        if (actualSha256.isBlank()) {
            return ApkValidationResult(
                isValid = false,
                errorMessage = "No se pudo calcular el resumen criptográfico SHA-256 del archivo."
            )
        }

        val cleanExpectedSha256 = expectedSha256?.trim()?.lowercase(Locale.ROOT)
        if (cleanExpectedSha256.isNullOrBlank() || !SHA256_HEX_PATTERN.matcher(cleanExpectedSha256).matches()) {
            return ApkValidationResult(
                isValid = false,
                actualSha256 = actualSha256,
                expectedSha256 = cleanExpectedSha256,
                isSha256Matching = false,
                errorMessage = "El release no proporciona un SHA-256 válido para la APK; instalación cancelada."
            )
        }

        if (!MessageDigest.isEqual(
                actualSha256.lowercase(Locale.ROOT).toByteArray(Charsets.US_ASCII),
                cleanExpectedSha256.toByteArray(Charsets.US_ASCII)
            )
        ) {
            return ApkValidationResult(
                isValid = false,
                actualSha256 = actualSha256,
                expectedSha256 = cleanExpectedSha256,
                isSha256Matching = false,
                errorMessage = "El hash SHA-256 de la APK no coincide con el checksum publicado."
            )
        }
        val isSha256Matching: Boolean? = true

        // 3. Inspect package archive info via PackageManager
        val pm = context.packageManager
        val archiveInfo: PackageInfo? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading package archive info", e)
            null
        }

        if (archiveInfo == null) {
            return ApkValidationResult(
                isValid = false,
                actualSha256 = actualSha256,
                expectedSha256 = cleanExpectedSha256,
                isSha256Matching = isSha256Matching,
                errorMessage = "El instalador descargado no es un paquete Android (APK) válido o está dañado."
            )
        }

        // 4. Verify package name identity matches
        val apkPackageName = archiveInfo.packageName
        val isPackageNameValid = (apkPackageName == expectedPackageName)
        if (!isPackageNameValid) {
            return ApkValidationResult(
                isValid = false,
                isPackageNameValid = false,
                actualSha256 = actualSha256,
                expectedSha256 = cleanExpectedSha256,
                isSha256Matching = isSha256Matching,
                errorMessage = "El identificador del paquete ($apkPackageName) no coincide con MegasCU ($expectedPackageName)."
            )
        }

        val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            archiveInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            archiveInfo.versionCode.toLong()
        }
        val apkVersionName = archiveInfo.versionName ?: ""

        // 5. Compare signing certificate with current installed app
        val isSignatureValid = try {
            val currentPkgInfo: PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    pm.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
                    )
                } catch (_: Exception) { null }
            } else {
                try {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                } catch (_: Exception) { null }
            }

            val currentSignatures = extractSignatures(currentPkgInfo)
            val apkSignatures = extractSignatures(archiveInfo)

            if (currentSignatures.isNotEmpty() && apkSignatures.isNotEmpty()) {
                currentSignatures.any { cur ->
                    apkSignatures.any { apk -> cur.contentEquals(apk) }
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron comparar los certificados de firma", e)
            false
        }

        if (!isSignatureValid) {
            return ApkValidationResult(
                isValid = false,
                isSignatureValid = false,
                isPackageNameValid = true,
                actualSha256 = actualSha256,
                expectedSha256 = cleanExpectedSha256,
                isSha256Matching = isSha256Matching,
                packageVersionName = apkVersionName,
                packageVersionCode = apkVersionCode,
                errorMessage = "La firma criptográfica del paquete no coincide con la clave de MegasCU instalada."
            )
        }

        return ApkValidationResult(
            isValid = true,
            isSignatureValid = true,
            isPackageNameValid = true,
            actualSha256 = actualSha256,
            expectedSha256 = cleanExpectedSha256,
            isSha256Matching = isSha256Matching,
            packageVersionName = apkVersionName,
            packageVersionCode = apkVersionCode
        )
    }

    private fun extractSignatures(packageInfo: PackageInfo?): List<ByteArray> {
        if (packageInfo == null) return emptyList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo ?: return emptyList()
            val signatures = if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
            return signatures?.map { it.toByteArray() } ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            return packageInfo.signatures?.map { it.toByteArray() } ?: emptyList()
        }
    }
}
