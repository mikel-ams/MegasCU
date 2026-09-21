package com.ams.megascu

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResourceIntegrityTest {

    private val validHashes = mapOf(
        "space_mono_regular.ttf" to Pair(
            "508a2a382b46a55be24d9edb70ce7d59be695cd5808e641fda24c40864b0d5d2",
            "59c83e1fe244568db558bab69755a6dd"
        ),
        "space_mono_bold.ttf" to Pair(
            "b87342616961e349e031bf1687371d14dc8ca7a751f8b1f39f0b800dbed48697",
            "6a367d177f91835c02d6f27b466489ec"
        ),
        "modo_normal.webp" to Pair(
            "372d64ec2908174a28dac6ef1fed3a4a76bf2f1ec725458bba2db961ee026ca8",
            "ee3a2bb6ae980d7d3b361e2d2178aebb"
        ),
        "modo_simple.webp" to Pair(
            "df41f561348918b771bbe4624494cee9232d1bff341fb191066b2a1812b341c1",
            "81da0b8eb93c37329e3328e8104ed7e9"
        )
    )

    @Test
    fun verifySpaceMonoRegularFontIntegrityAndStructure() {
        verifyFontFile("src/main/res/font/space_mono_regular.ttf", "space_mono_regular.ttf")
    }

    @Test
    fun verifySpaceMonoBoldFontIntegrityAndStructure() {
        verifyFontFile("src/main/res/font/space_mono_bold.ttf", "space_mono_bold.ttf")
    }

    private fun verifyFontFile(filePath: String, fileName: String) {
        val file = File(filePath)
        assertTrue("$fileName must exist", file.exists())
        val actualSha256 = sha256(file)
        val actualMd5 = md5(file)
        assertEquals("$fileName SHA-256 mismatch", validHashes[fileName]?.first, actualSha256)
        assertEquals("$fileName MD5 mismatch", validHashes[fileName]?.second, actualMd5)

        val bytes = file.readBytes()
        // Header check
        val header = ((bytes[0].toInt() and 0xFF) shl 24) or
                     ((bytes[1].toInt() and 0xFF) shl 16) or
                     ((bytes[2].toInt() and 0xFF) shl 8) or
                     (bytes[3].toInt() and 0xFF)
        val validHeaders = listOf(0x00010000, 0x74727565, 0x4F54544F)
        assertTrue("Valid TrueType header for $fileName", header in validHeaders)

        // Ensure no EF BF BD
        val efBfBd = byteArrayOf(0xEF.toByte(), 0xBF.toByte(), 0xBD.toByte())
        for (i in 0 until bytes.size - 2) {
            val hasCorruptSequence = (bytes[i] == efBfBd[0] && bytes[i + 1] == efBfBd[1] && bytes[i + 2] == efBfBd[2])
            assertTrue("Must not contain EF BF BD sequence at byte $i in $fileName", !hasCorruptSequence)
        }

        // Table check
        val numTables = ((bytes[4].toInt() and 0xFF) shl 8) or (bytes[5].toInt() and 0xFF)
        val tables = mutableSetOf<String>()
        for (i in 0 until numTables) {
            val offset = 12 + i * 16
            val tagChars = CharArray(4) { idx -> bytes[offset + idx].toInt().toChar() }
            tables.add(String(tagChars))
        }
        for (req in listOf("head", "maxp", "name", "GDEF", "GPOS", "GSUB")) {
            assertTrue("Must contain table $req in $fileName", req in tables)
        }
    }

    @Test
    fun verifyFontLoadsInAndroidResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val regularTypeface = ResourcesCompat.getFont(context, R.font.space_mono_regular)
        assertNotNull("Typeface R.font.space_mono_regular must load successfully without errors", regularTypeface)
        val boldTypeface = ResourcesCompat.getFont(context, R.font.space_mono_bold)
        assertNotNull("Typeface R.font.space_mono_bold must load successfully without errors", boldTypeface)
    }

    @Test
    fun verifyModoNormalWebpIntegrity() {
        val file = File("src/main/res/drawable/modo_normal.webp")
        assertTrue("modo_normal.webp must exist", file.exists())
        val actualSha256 = sha256(file)
        val actualMd5 = md5(file)
        assertEquals("modo_normal.webp SHA-256 mismatch", validHashes["modo_normal.webp"]?.first, actualSha256)
        assertEquals("modo_normal.webp MD5 mismatch", validHashes["modo_normal.webp"]?.second, actualMd5)
    }

    @Test
    fun verifyModoSimpleWebpIntegrity() {
        val file = File("src/main/res/drawable/modo_simple.webp")
        assertTrue("modo_simple.webp must exist", file.exists())
        val actualSha256 = sha256(file)
        val actualMd5 = md5(file)
        assertEquals("modo_simple.webp SHA-256 mismatch", validHashes["modo_simple.webp"]?.first, actualSha256)
        assertEquals("modo_simple.webp MD5 mismatch", validHashes["modo_simple.webp"]?.second, actualMd5)
    }

    @Test
    fun verifyDrawablesLoadInAndroidResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val normalDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.modo_normal, null)
        assertNotNull("R.drawable.modo_normal must load as a valid drawable", normalDrawable)
        assertTrue("R.drawable.modo_normal must be BitmapDrawable", normalDrawable is android.graphics.drawable.BitmapDrawable)

        val simpleDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.modo_simple, null)
        assertNotNull("R.drawable.modo_simple must load as a valid drawable", simpleDrawable)
        assertTrue("R.drawable.modo_simple must be BitmapDrawable", simpleDrawable is android.graphics.drawable.BitmapDrawable)
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = file.readBytes()
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun md5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = file.readBytes()
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
