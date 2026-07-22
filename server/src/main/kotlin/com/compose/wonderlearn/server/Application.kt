package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.ContentManifest
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
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
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      call.application.environment.log.error("Unhandled failure", cause)
      call.respond(HttpStatusCode.InternalServerError, ErrorResponse("internal_error"))
    }
  }

  routing {
    get("/health") {
      call.respond(HealthResponse(status = "ok", contentVersion = contentStore.manifest().version))
    }

    get("/v1/content/manifest") {
      val since = call.request.queryParameters["since"]?.toLongOrNull()
      val manifest = contentStore.manifest()
      if (since != null && since >= manifest.version) {
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
