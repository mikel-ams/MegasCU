import java.io.File
import java.security.MessageDigest
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

abstract class VerifyResourceIntegrityTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val resourceFiles: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val expectedHashes = mapOf(
            "space_mono_regular.ttf" to ("508a2a382b46a55be24d9edb70ce7d59be695cd5808e641fda24c40864b0d5d2" to "FONT_BINARY_INTEGRITY_ERROR"),
            "space_mono_bold.ttf" to ("b87342616961e349e031bf1687371d14dc8ca7a751f8b1f39f0b800dbed48697" to "FONT_BINARY_INTEGRITY_ERROR"),
            "modo_normal.webp" to ("372d64ec2908174a28dac6ef1fed3a4a76bf2f1ec725458bba2db961ee026ca8" to "IMAGE_BINARY_INTEGRITY_ERROR"),
            "modo_simple.webp" to ("df41f561348918b771bbe4624494cee9232d1bff341fb191066b2a1812b341c1" to "IMAGE_BINARY_INTEGRITY_ERROR")
        )
        for (file in resourceFiles.files) {
            val name = file.name
            val info = expectedHashes[name] ?: continue
            val expectedSha256 = info.first
            val errorCode = info.second
            if (!file.exists()) {
                throw GradleException("[$errorCode] File not found: ${file.path}")
            }
            val bytes = file.readBytes()

            // Verify SHA-256
            val sha256Digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            val actualSha256 = sha256Digest.joinToString("") { "%02x".format(it) }
            if (!expectedSha256.equals(actualSha256, ignoreCase = true)) {
                throw GradleException("[$errorCode] SHA-256 mismatch for $name! Expected $expectedSha256, got $actualSha256")
            }

            // Level 2 Structural validation for TTF and WebP
            if (name.endsWith(".ttf", ignoreCase = true)) {
                validateTrueTypeStructure(name, bytes, errorCode)
            } else if (name.endsWith(".webp", ignoreCase = true)) {
                validateWebpStructure(name, bytes, errorCode)
            }
        }
        println("Binary resource integrity check (SHA-256 and structure) passed successfully.")
    }

    private fun validateWebpStructure(fileName: String, bytes: ByteArray, errorCode: String) {
        if (bytes.size < 12) {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: File too small (${bytes.size} bytes)")
        }
        val riff = String(bytes.sliceArray(0..3))
        if (riff != "RIFF") {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Missing RIFF signature")
        }
        val webp = String(bytes.sliceArray(8..11))
        if (webp != "WEBP") {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Missing WEBP signature")
        }
        val riffSize = (bytes[4].toInt() and 0xFF) or
                ((bytes[5].toInt() and 0xFF) shl 8) or
                ((bytes[6].toInt() and 0xFF) shl 16) or
                ((bytes[7].toInt() and 0xFF) shl 24)
        if (riffSize + 8 != bytes.size) {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Size in RIFF header ($riffSize + 8) does not match file size (${bytes.size})")
        }

        val efBfBd = byteArrayOf(0xEF.toByte(), 0xBF.toByte(), 0xBD.toByte())
        for (i in 0 until bytes.size - 2) {
            if (bytes[i] == efBfBd[0] && bytes[i + 1] == efBfBd[1] && bytes[i + 2] == efBfBd[2]) {
                throw GradleException("[$errorCode] $fileName failed WebP structural validation: Found corrupted Unicode replacement sequence EF BF BD at byte $i")
            }
        }
    }

    private fun validateTrueTypeStructure(fileName: String, bytes: ByteArray, errorCode: String) {
        if (bytes.size < 12) {
            throw GradleException("[$errorCode] $fileName failed TrueType structural validation: File too small (${bytes.size} bytes)")
        }
        val header = ((bytes[0].toInt() and 0xFF) shl 24) or
                     ((bytes[1].toInt() and 0xFF) shl 16) or
                     ((bytes[2].toInt() and 0xFF) shl 8) or
                     (bytes[3].toInt() and 0xFF)
        val validHeaders = listOf(0x00010000, 0x74727565, 0x4F54544F)
        if (header !in validHeaders) {
            throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Invalid SFNT header 0x${header.toString(16)}")
        }

        val efBfBd = byteArrayOf(0xEF.toByte(), 0xBF.toByte(), 0xBD.toByte())
        for (i in 0 until bytes.size - 2) {
            if (bytes[i] == efBfBd[0] && bytes[i + 1] == efBfBd[1] && bytes[i + 2] == efBfBd[2]) {
                throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Found corrupted Unicode replacement sequence EF BF BD at byte $i")
            }
        }

        val numTables = ((bytes[4].toInt() and 0xFF) shl 8) or (bytes[5].toInt() and 0xFF)
        if (numTables <= 0 || bytes.size < 12 + numTables * 16) {
            throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Invalid table count $numTables")
        }

        val tables = mutableSetOf<String>()
        for (i in 0 until numTables) {
            val offset = 12 + i * 16
            val tagChars = CharArray(4) { idx -> bytes[offset + idx].toInt().toChar() }
            val tag = String(tagChars)
            tables.add(tag)
        }

        val requiredTables = listOf("head", "maxp", "name")
        for (req in requiredTables) {
            if (req !in tables) {
                throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Missing required table $req")
            }
        }
    }
}

val verifyResourceIntegrity = tasks.register<VerifyResourceIntegrityTask>("verifyResourceIntegrity") {
    resourceFiles.from(
        layout.projectDirectory.file("src/main/res/font/space_mono_regular.ttf"),
        layout.projectDirectory.file("src/main/res/font/space_mono_bold.ttf"),
        layout.projectDirectory.file("src/main/res/drawable/modo_normal.webp"),
        layout.projectDirectory.file("src/main/res/drawable/modo_simple.webp")
    )
}

tasks.matching { it.name.startsWith("preBuild") }.configureEach {
    dependsOn(verifyResourceIntegrity)
}
