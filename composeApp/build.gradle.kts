import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
      implementation(libs.navigation.compose)
      implementation(libs.lifecycle.viewmodel)
      implementation(libs.lifecycle.viewmodel.compose)
      implementation(libs.lifecycle.runtime.compose)
      implementation(libs.koin.core)
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
    }
    iosMain.dependencies {
      implementation(libs.sqldelight.native.driver)
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
    versionName = "1.0"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}
