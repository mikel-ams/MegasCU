package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.utils.GitHubUpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GitHubUpdateCheckerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testDefaultRepo_isMikelAmsMegasCU() {
        assertEquals("mikel-ams/MegasCU", GitHubUpdateChecker.DEFAULT_REPO)
    }

    @Test
    fun testExtractBuildCode_extractsVariousTagFormats() {
        val testCases = listOf(
            Pair("v0.8.2-beta_(245)", "v0.8.2-beta_(245)") to 245,
            Pair("v0.8.1-beta_(244)", "v0.8.1-beta_(244)") to 244,
            Pair("v0.8.0-beta_(243)", "MegasCU v0.8.0-beta_(243) — Primera Beta Pública") to 243,
            Pair("0.7.9-beta_(242)", "0.7.9-beta_(242)") to 242,
            Pair("v0.8.2-245", "v0.8.2-245") to 245,
            Pair("v0.8.2_245", "v0.8.2_245") to 245,
            Pair("v0.8.2", "Release 0.8.2 Build 245") to 245
        )

        for ((input, expected) in testCases) {
            val (tag, name) = input
            val extracted = GitHubUpdateChecker.extractBuildCode(tag, name)
            assertEquals("Fallo al extraer código de build de tag '$tag' y nombre '$name'", expected, extracted)
        }
    }

    @Test
    fun testIsVersionNewer_comparesBuildCodesAndSemverCorrectly() {
        // 1. Código numérico remoto mayor
        assertTrue(
            GitHubUpdateChecker.isVersionNewer(
                remoteTag = "v0.8.2-beta_(245)",
                remoteReleaseName = "v0.8.2-beta_(245)",
                remoteCode = 245,
                currentCode = 244,
                currentName = "0.8.1-beta_(244)"
            )
        )

        // 2. Mismo código numérico
        assertFalse(
            GitHubUpdateChecker.isVersionNewer(
                remoteTag = "v0.8.2-beta_(245)",
                remoteReleaseName = "v0.8.2-beta_(245)",
                remoteCode = 245,
                currentCode = 245,
                currentName = "0.8.2-beta_(245)"
            )
        )

        // 3. Código remoto menor
        assertFalse(
            GitHubUpdateChecker.isVersionNewer(
                remoteTag = "v0.8.1-beta_(244)",
                remoteReleaseName = "v0.8.1-beta_(244)",
                remoteCode = 244,
                currentCode = 245,
                currentName = "0.8.2-beta_(245)"
            )
        )

        // 4. Semver fallback si no hay remoteCode
        assertTrue(
            GitHubUpdateChecker.isVersionNewer(
                remoteTag = "v0.8.2",
                remoteReleaseName = "v0.8.2",
                remoteCode = 0,
                currentCode = 244,
                currentName = "0.8.1-beta_(244)"
            )
        )
    }
}
