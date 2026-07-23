import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val keystoreProperties = Properties().apply {
  val file = rootProject.file("keystore.properties")
  if (file.exists()) file.inputStream().use { load(it) }
}
val hasReleaseSigning = keystoreProperties.getProperty("storeFile") != null

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64(),
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
      linkerOpts("-lsqlite3")
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
      implementation(libs.navigation.compose)
      implementation(libs.lifecycle.viewmodel)
      implementation(libs.lifecycle.viewmodel.compose)
      implementation(libs.lifecycle.runtime.compose)
      implementation(libs.koin.core)
      implementation(projects.shared)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.resources)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.json)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.sqldelight.runtime)
      implementation(libs.sqldelight.coroutines)
    }
    androidMain.dependencies {
      implementation(compose.uiTooling)
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.core.ktx)
      implementation(libs.koin.android)
      implementation(libs.sqldelight.android.driver)
      implementation(libs.ktor.client.okhttp)
    }
    iosMain.dependencies {
      implementation(libs.sqldelight.native.driver)
      implementation(libs.ktor.client.darwin)
    }
    androidUnitTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.sqldelight.sqlite.driver)
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "com.compose.wonderlearn.resources"
}

sqldelight {
  databases {
    create("WonderLearnDatabase") {
      packageName.set("com.compose.wonderlearn.db")
    }
  }
}

android {
  namespace = "com.compose.wonderlearn"
  compileSdk = 37

  defaultConfig {
    applicationId = "com.compose.wonderlearn"
    minSdk = 29
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"
  }

  buildFeatures {
    buildConfig = true
  }

  signingConfigs {
    if (hasReleaseSigning) {
      create("release") {
        storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
        storePassword = keystoreProperties.getProperty("storePassword")
        keyAlias = keystoreProperties.getProperty("keyAlias")
        keyPassword = keystoreProperties.getProperty("keyPassword")
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName(if (hasReleaseSigning) "release" else "debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

tasks.withType<Test>().configureEach {
  inputs.file("src/commonMain/composeResources/files/content/vocabulary.json")
    .withPathSensitivity(PathSensitivity.RELATIVE)
}
