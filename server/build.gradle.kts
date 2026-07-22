plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  application
}

sourceSets["main"].kotlin.srcDir("../shared/src/commonMain/kotlin")

dependencies {
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.content.negotiation)
  implementation(libs.ktor.serialization.json)
  implementation(libs.ktor.server.call.logging)
  implementation(libs.ktor.server.cors)
  implementation(libs.ktor.server.status.pages)
  implementation(libs.logback.classic)
  testImplementation(libs.ktor.server.test.host)
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(17)
}

application {
  mainClass.set("com.compose.wonderlearn.server.ApplicationKt")
}
