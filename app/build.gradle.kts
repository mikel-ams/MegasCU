import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

apply(from = rootProject.file("gradle/integrity.gradle.kts"))

android {
  namespace = "com.ams.megascu"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.ams.megascu"
    minSdk = 30
    targetSdk = 36
    versionCode = 255
    versionName = "0.9.0-beta_(255)"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    fun secret(name: String): String? =
      System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: (project.findProperty(name) as? String)?.takeIf { it.isNotBlank() }

    val releaseKeystoreBase64 = secret("RELEASE_KEYSTORE_BASE64")
    val releaseStorePassword = secret("RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = secret("RELEASE_KEY_ALIAS")
    val releaseKeyPassword = secret("RELEASE_KEY_PASSWORD")

    val releaseStoreFile: File? = releaseKeystoreBase64?.let { base64Str ->
      val targetDir = File(layout.buildDirectory.get().asFile, "intermediates/keystore")
      targetDir.mkdirs()
      val tempKeystore = File(targetDir, "release-keystore.jks")
      try {
        val cleanBase64 = base64Str.replace("\n", "").replace("\r", "").trim()
        val decodedBytes = Base64.getDecoder().decode(cleanBase64)
        tempKeystore.writeBytes(decodedBytes)
        tempKeystore
      } catch (e: Exception) {
        null
      }
    } ?: secret("RELEASE_KEYSTORE_PATH")?.let { path ->
      val f = file(path)
      if (f.isFile) f else rootProject.file(path).takeIf(File::isFile)
    } ?: rootProject.file("keystore/release-key.jks").takeIf(File::isFile)

    create("release") {
      storeFile = releaseStoreFile
      storePassword = releaseStorePassword
      keyAlias = releaseKeyAlias
      keyPassword = releaseKeyPassword
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    release {
      isDebuggable = false
      isCrunchPngs = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val relConfig = signingConfigs.getByName("release")
      signingConfig = if (relConfig.storeFile?.isFile == true && !relConfig.storePassword.isNullOrBlank()) relConfig else signingConfigs.getByName("debug")
    }
    debug {
      isDebuggable = true
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  packaging {
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
      excludes += "META-INF/androidx/**"
      excludes += "META-INF/version-control-info.textproto"
    }
    dex {
      useLegacyPackaging = true
    }
    jniLibs {
      useLegacyPackaging = true
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

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.biometric)
  implementation(libs.haze)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.compose.icons.fontawesome)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.vico.compose)
  implementation(libs.vico.compose.m3)
  implementation(libs.vico.core)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

val validateReleaseSigningSecrets = tasks.register("validateReleaseSigningSecrets") {
  description = "Valida que los secretos obligatorios para la firma de producción (Release) estén configurados."
  val requiredSecrets = listOf(
    "RELEASE_KEYSTORE_BASE64",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD"
  )
  val secretProviders = requiredSecrets.associateWith { name ->
    providers.environmentVariable(name).orElse(providers.gradleProperty(name))
  }
  doLast {
    val missing = secretProviders.filter { (_, provider) ->
      !provider.isPresent || provider.get().isBlank()
    }.keys
    if (missing.isNotEmpty()) {
      throw GradleException(
        "Faltan los siguientes secretos requeridos para la firma Release: " +
          "${missing.joinToString(", ")}. Por favor, configúrelos en el panel de Secretos de AI Studio o como variables de entorno."
      )
    }
  }
}

tasks.matching { it.name in listOf("assembleRelease", "bundleRelease") }.configureEach {
  dependsOn(validateReleaseSigningSecrets)
}

