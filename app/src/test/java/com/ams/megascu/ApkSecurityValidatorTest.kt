package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.utils.ApkSecurityValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApkSecurityValidatorTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testIsValidRepoIdentifier_validAndInvalid() {
        assertTrue(ApkSecurityValidator.isValidRepoIdentifier("mikel-ams/MegasCU"))
        assertTrue(ApkSecurityValidator.isValidRepoIdentifier("owner-123/repo.name"))
        assertTrue(ApkSecurityValidator.isValidRepoIdentifier("org_name/project-v2"))

        assertFalse(ApkSecurityValidator.isValidRepoIdentifier(null))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier(""))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier("   "))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier("single_name_no_slash"))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier("bad/repo/extra_slash"))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier("bad$*#/repo"))
        assertFalse(ApkSecurityValidator.isValidRepoIdentifier("owner/repo;rm -rf /"))
    }

    @Test
    fun testIsValidReleaseUrl_strictHttpsAndWhitelisting() {
        // Valid GitHub URLs
        assertTrue(ApkSecurityValidator.isValidReleaseUrl("https://api.github.com/repos/mikel-ams/MegasCU/releases"))
        assertTrue(ApkSecurityValidator.isValidReleaseUrl("https://github.com/mikel-ams/MegasCU/releases/download/v0.8.7/MegasCU.apk"))
        assertTrue(ApkSecurityValidator.isValidReleaseUrl("https://objects.githubusercontent.com/github-production-release-asset-2e65be/12345/MegasCU.apk"))
        assertTrue(ApkSecurityValidator.isValidReleaseUrl("https://raw.githubusercontent.com/mikel-ams/MegasCU/main/CHANGELOG.md"))

        // Insecure HTTP (Rejected)
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("http://github.com/mikel-ams/MegasCU/releases"))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("http://api.github.com/repos/mikel-ams/MegasCU/releases"))

        // Untrusted Third-party hosts (Rejected)
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("https://malicious-site.com/MegasCU.apk"))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("https://github.com.evil.com/MegasCU.apk"))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("https://192.168.1.1/MegasCU.apk"))

        // Malformed or credential-embedded URLs (Rejected)
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("https://user:password@github.com/releases"))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl("https://github.com:8080/releases"))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl(null))
        assertFalse(ApkSecurityValidator.isValidReleaseUrl(""))
    }

    @Test
    fun testExtractExpectedSha256_findsValidHashes() {
        val hash64 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        val body1 = "Release v0.8.7\nSHA256: $hash64\nDisfruten la nueva versión."
        assertEquals(hash64, ApkSecurityValidator.extractExpectedSha256(body1))

        val body2 = "SHA-256: `$hash64`\nFixes and performance improvements."
        assertEquals(hash64, ApkSecurityValidator.extractExpectedSha256(body2))

        val body3 = "$hash64  MegasCU_0.8.7-beta_(250).apk"
        assertEquals(hash64, ApkSecurityValidator.extractExpectedSha256(body3))

        val bodyNoHash = "Just some release notes without any checksum."
        assertNull(ApkSecurityValidator.extractExpectedSha256(bodyNoHash))

        assertNull(ApkSecurityValidator.extractExpectedSha256(null))
    }

    @Test
    fun testCalculateSha256_computesCorrectDigest() {
        val payload = "MegasCU Security Verification Test Payload"
        val expected = java.security.MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

        val tempFile = File(context.cacheDir, "test_sha.txt").apply {
            writeText(payload, Charsets.UTF_8)
        }
        val sha256 = ApkSecurityValidator.calculateSha256(tempFile)
        assertNotNull(sha256)
        assertEquals(64, sha256.length)
        assertEquals(expected, sha256)

        tempFile.delete()
    }

    @Test
    fun testVerifyApk_rejectsNonExistentOrCorruptedFiles() {
        val nonExistent = File(context.cacheDir, "does_not_exist.apk")
        val result = ApkSecurityValidator.verifyApk(context, nonExistent)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("no existe") == true)

        // File too small (< 100KB)
        val smallFile = File(context.cacheDir, "too_small.apk").apply {
            writeBytes(ByteArray(1024))
        }
        val smallResult = ApkSecurityValidator.verifyApk(context, smallFile)
        assertFalse(smallResult.isValid)
        assertTrue(smallResult.errorMessage?.contains("menor a 100 KB") == true)
        smallFile.delete()
    }
}
