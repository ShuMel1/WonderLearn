package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.shared.ContentManifestRoute
import com.compose.wonderlearn.shared.HealthRoute
import com.compose.wonderlearn.shared.RootRoute
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
  val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
  embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module(contentStore: ContentStore = ResourceContentStore()) {
  install(ContentNegotiation) {
    json(Json { prettyPrint = false; ignoreUnknownKeys = true })
  }
  install(CallLogging)
  install(Resources)
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      call.application.environment.log.error("Unhandled failure", cause)
      call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error"))
    }
  }

  routing {
    get<RootRoute> {
      call.respond(
        ServiceInfo(
          service = "wonderlearn-api",
          contentVersion = contentStore.manifest().version,
          endpoints = listOf("/health", "/v1/content/manifest"),
        ),
      )
    }

    get<HealthRoute> {
      call.respond(HealthResponse(status = "ok", contentVersion = contentStore.manifest().version))
    }

    get<ContentManifestRoute> { route ->
      val manifest = contentStore.manifest()
      if (route.since != null && route.since >= manifest.version) {
        call.respond(HttpStatusCode.NotModified)
      } else {
        call.respond(manifest)
      }
    }
  }
}

/** Where the server reads content from. A packaged file today, a database once content is editable. */
interface ContentStore {
  fun manifest(): ContentManifest
}

class ResourceContentStore(
  private val resourcePath: String = "/vocabulary.json",
) : ContentStore {

  private val json = Json { ignoreUnknownKeys = true }

  private val cached: ContentManifest by lazy {
    val text = javaClass.getResourceAsStream(resourcePath)
      ?.bufferedReader()
      ?.use { it.readText() }
      ?: error("content manifest not found at $resourcePath")
    json.decodeFromString(text)
  }

  override fun manifest(): ContentManifest = cached
}
