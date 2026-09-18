import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.ams.megascu"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.ams.megascu"
    minSdk = 30
    targetSdk = 36
    versionCode = 248
    versionName = "0.8.5-beta_(248)"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val keystorePropsFile = listOf(
      file("${rootDir}/keystore/keystore.properties"),
      file("${rootDir}/keystore.properties"),
      file("${projectDir}/keystore.properties"),
      file("${rootDir}/signing.properties"),
      file("${rootDir}/secrets.properties")
    ).firstOrNull { it.exists() && it.isFile }
    val keystoreProps = Properties()
    if (keystorePropsFile != null) {
      FileInputStream(keystorePropsFile).use { keystoreProps.load(it) }
    }

    val envFile = listOf(
      file("${rootDir}/.env"),
      file("${projectDir}/.env")
    ).firstOrNull { it.exists() && it.isFile }
    val envProps = Properties()
    if (envFile != null) {
      FileInputStream(envFile).use { envProps.load(it) }
    }

    fun getSecureProperty(key: String, vararg fallbackKeys: String): String? {
      return System.getenv(key)
        ?: (project.findProperty(key) as? String)
        ?: keystoreProps.getProperty(key)
        ?: envProps.getProperty(key)
        ?: fallbackKeys.firstNotNullOfOrNull { fallback ->
          System.getenv(fallback)
            ?: (project.findProperty(fallback) as? String)
            ?: keystoreProps.getProperty(fallback)
            ?: envProps.getProperty(fallback)
        }
    }

    val candidatePath = getSecureProperty("RELEASE_KEYSTORE_PATH", "KEYSTORE_PATH")
    val releaseStoreFile = listOfNotNull(
      candidatePath?.let { file("${rootDir}/$it") },
      candidatePath?.let { file("${projectDir}/$it") },
      candidatePath?.let { file(it) },
      file("${rootDir}/release-key.jks"),
      file("${projectDir}/release-key.jks"),
      file("${rootDir}/app/release-key.jks"),
      file("${rootDir}/keystore/release-key.jks")
    ).firstOrNull { it.exists() && it.isFile }

    val releaseStorePassword = getSecureProperty("RELEASE_STORE_PASSWORD", "STORE_PASSWORD")
    val releaseKeyAlias = getSecureProperty("RELEASE_KEY_ALIAS", "KEY_ALIAS")
    val releaseKeyPassword = getSecureProperty("RELEASE_KEY_PASSWORD", "KEY_PASSWORD")

    val hasReleaseSigning = releaseStoreFile != null && releaseStoreFile.exists() &&
      !releaseStorePassword.isNullOrBlank() &&
      !releaseKeyAlias.isNullOrBlank() &&
      !releaseKeyPassword.isNullOrBlank()

    if (hasReleaseSigning) {
      create("release") {
        storeFile = releaseStoreFile
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }

    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isDebuggable = false
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debugConfig")
    }
    debug {
      isDebuggable = true
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  packaging {
    dex {
      useLegacyPackaging = true
    }
    jniLibs {
      useLegacyPackaging = true
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
      excludes += "META-INF/DEPENDENCIES"
      excludes += "META-INF/LICENSE"
      excludes += "META-INF/LICENSE.txt"
      excludes += "META-INF/license.txt"
      excludes += "META-INF/NOTICE"
      excludes += "META-INF/NOTICE.txt"
      excludes += "META-INF/notice.txt"
      excludes += "META-INF/ASL2.0"
      excludes += "META-INF/*.kotlin_module"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
    implementation("androidx.fragment:fragment-ktx:1.6.2") // Required for FragmentActivity (BiometricPrompt)
    // implementation("androidx.appcompat:appcompat:1.6.1") // Removed: Unused in Compose architecture
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
  implementation("dev.chrisbanes.haze:haze:0.6.2")
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.1")
  // implementation("br.com.devsrsouza.compose.icons:simple-icons:1.1.1") // Removed: Unused
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation("androidx.work:work-runtime-ktx:2.9.0")
  implementation(libs.vico.compose)
  implementation(libs.vico.compose.m3)
  implementation(libs.vico.core)
  // implementation(libs.vico.views) // Removed: Unused XML views library
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Firebase Auth with Google Sign-In requires all of the following to be uncommented together.
  // If you are using Firebase Auth with other providers (e.g. Email/Password), you may only need
  // firebase-auth.
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}

abstract class VerifyResourceIntegrityTask : DefaultTask() {
    @get:InputFiles
    abstract val resourceFiles: org.gradle.api.file.ConfigurableFileCollection

    @get:Internal
    abstract val rootDirProp: org.gradle.api.file.DirectoryProperty

    @get:Internal
    abstract val projectDirProp: org.gradle.api.file.DirectoryProperty

    @TaskAction
    fun verify() {
        val rootDir = rootDirProp.get().asFile
        val projectDir = projectDirProp.get().asFile
        val resFontZip = File(rootDir, "res_font.zip").takeIf { it.exists() } ?: File(rootDir, "res_fonts.zip")
        val resImagesZip = File(rootDir, "res_images.zip")

        val expectedHashes = mapOf(
            "space_mono_regular.ttf" to Triple(
                "508a2a382b46a55be24d9edb70ce7d59be695cd5808e641fda24c40864b0d5d2",
                "59c83e1fe244568db558bab69755a6dd",
                "FONT_BINARY_INTEGRITY_ERROR"
            ),
            "space_mono_bold.ttf" to Triple(
                "b87342616961e349e031bf1687371d14dc8ca7a751f8b1f39f0b800dbed48697",
                "6a367d177f91835c02d6f27b466489ec",
                "FONT_BINARY_INTEGRITY_ERROR"
            ),
            "modo_normal.webp" to Triple(
                "30d7de368840486be8b9116f7a6ea70a3950defc49331670b8fd32c9a723ff23",
                "d7cf220e3b4174330cf79a49969082db",
                "IMAGE_BINARY_INTEGRITY_ERROR"
            ),
            "modo_simple.webp" to Triple(
                "ee8414c0372f6268accbb2af2545881a05fa4461cd253b7fb1dd89141a1498f0",
                "15a8b4e5d30d9829d6c127202e9ef77f",
                "IMAGE_BINARY_INTEGRITY_ERROR"
            )
        )

        // Auto-extract from source ZIPs if needed to ensure byte-perfect state
        if (resImagesZip.exists()) {
            try {
                ZipFile(resImagesZip).use { zip ->
                    val normalEntry = zip.getEntry("modo_normal.webp")
                    if (normalEntry != null) {
                        val target = File(projectDir, "src/main/res/drawable/modo_normal.webp")
                        val bytes = zip.getInputStream(normalEntry).readBytes()
                        if (!target.exists() || !target.readBytes().contentEquals(bytes)) {
                            target.parentFile.mkdirs()
                            target.writeBytes(bytes)
                            println("Restored modo_normal.webp byte-for-byte from res_images.zip")
                        }
                    }
                    val simpleEntry = zip.getEntry("modo_simple.webp")
                    if (simpleEntry != null) {
                        val target = File(projectDir, "src/main/res/drawable/modo_simple.webp")
                        val bytes = zip.getInputStream(simpleEntry).readBytes()
                        if (!target.exists() || !target.readBytes().contentEquals(bytes)) {
                            target.parentFile.mkdirs()
                            target.writeBytes(bytes)
                            println("Restored modo_simple.webp byte-for-byte from res_images.zip")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Note on res_images.zip extraction: ${e.message}")
            }
        }

        if (resFontZip.exists()) {
            try {
                ZipFile(resFontZip).use { zip ->
                    val regularEntry = zip.getEntry("SpaceMono-Regular.ttf") ?: zip.getEntry("space_mono_regular.ttf")
                    if (regularEntry != null) {
                        val target = File(projectDir, "src/main/res/font/space_mono_regular.ttf")
                        val bytes = zip.getInputStream(regularEntry).readBytes()
                        if (!target.exists() || !target.readBytes().contentEquals(bytes)) {
                            target.parentFile.mkdirs()
                            target.writeBytes(bytes)
                            println("Restored space_mono_regular.ttf byte-for-byte from res_font.zip")
                        }
                    }
                    val boldEntry = zip.getEntry("SpaceMono-Bold.ttf") ?: zip.getEntry("space_mono_bold.ttf")
                    if (boldEntry != null) {
                        val target = File(projectDir, "src/main/res/font/space_mono_bold.ttf")
                        val bytes = zip.getInputStream(boldEntry).readBytes()
                        if (!target.exists() || !target.readBytes().contentEquals(bytes)) {
                            target.parentFile.mkdirs()
                            target.writeBytes(bytes)
                            println("Restored space_mono_bold.ttf byte-for-byte from res_font.zip")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Note on res_font.zip extraction: ${e.message}")
            }
        }

        for (file in resourceFiles.files) {
            val name = file.name
            val info = expectedHashes[name] ?: continue
            val expectedSha256 = info.first
            val expectedMd5 = info.second
            val errorCode = info.third
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

            // Verify MD5
            val md5Digest = MessageDigest.getInstance("MD5").digest(bytes)
            val actualMd5 = md5Digest.joinToString("") { "%02x".format(it) }
            if (!expectedMd5.equals(actualMd5, ignoreCase = true)) {
                throw GradleException("[$errorCode] MD5 mismatch for $name! Expected $expectedMd5, got $actualMd5")
            }

            // Level 2 Structural validation for TTF and WebP
            if (name.endsWith(".ttf", ignoreCase = true)) {
                validateTrueTypeStructure(name, bytes, errorCode)
            } else if (name.endsWith(".webp", ignoreCase = true)) {
                validateWebpStructure(name, bytes, errorCode)
            }
        }
        println("Binary resource integrity check (MD5, SHA-256 and structure) passed successfully.")
    }

    private fun ByteArray.indexOfSequence(seq: ByteArray): Int {
        if (seq.isEmpty() || this.size < seq.size) return -1
        for (i in 0..this.size - seq.size) {
            var found = true
            for (j in seq.indices) {
                if (this[i + j] != seq[j]) {
                    found = false
                    break
                }
            }
            if (found) return i
        }
        return -1
    }

    private fun validateWebpStructure(fileName: String, bytes: ByteArray, errorCode: String) {
        if (bytes.size < 12) {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: File too small (${bytes.size} bytes)")
        }
        // Check RIFF header: "RIFF"
        val riff = String(bytes.sliceArray(0..3))
        if (riff != "RIFF") {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Missing RIFF signature")
        }
        // Check WEBP magic: "WEBP"
        val webp = String(bytes.sliceArray(8..11))
        if (webp != "WEBP") {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Missing WEBP signature")
        }
        // Check header file size
        val riffSize = (bytes[4].toInt() and 0xFF) or
                ((bytes[5].toInt() and 0xFF) shl 8) or
                ((bytes[6].toInt() and 0xFF) shl 16) or
                ((bytes[7].toInt() and 0xFF) shl 24)
        if (riffSize + 8 != bytes.size) {
            throw GradleException("[$errorCode] $fileName failed WebP structural validation: Size in RIFF header ($riffSize + 8) does not match file size (${bytes.size})")
        }

        // Check for replacement character bytes EF BF BD
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
        // Check SFNT header: 0x00010000, "true", or "OTTO"
        val header = ((bytes[0].toInt() and 0xFF) shl 24) or
                     ((bytes[1].toInt() and 0xFF) shl 16) or
                     ((bytes[2].toInt() and 0xFF) shl 8) or
                     (bytes[3].toInt() and 0xFF)
        val validHeaders = listOf(0x00010000, 0x74727565, 0x4F54544F)
        if (header !in validHeaders) {
            throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Invalid SFNT header 0x${header.toString(16)}")
        }

        // Check for replacement character bytes EF BF BD
        val efBfBd = byteArrayOf(0xEF.toByte(), 0xBF.toByte(), 0xBD.toByte())
        for (i in 0 until bytes.size - 2) {
            if (bytes[i] == efBfBd[0] && bytes[i + 1] == efBfBd[1] && bytes[i + 2] == efBfBd[2]) {
                throw GradleException("[$errorCode] $fileName failed TrueType structural validation: Found corrupted Unicode replacement sequence EF BF BD at byte $i")
            }
        }

        // Read number of tables
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
    rootDirProp.set(layout.projectDirectory.dir(".."))
    projectDirProp.set(layout.projectDirectory)
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

