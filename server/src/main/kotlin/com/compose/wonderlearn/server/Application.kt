package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.AnalyticsEvent
import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.shared.ContentManifestRoute
import com.compose.wonderlearn.shared.EventsRoute
import com.compose.wonderlearn.shared.EventsSummaryRoute
import com.compose.wonderlearn.shared.HealthRoute
import com.compose.wonderlearn.shared.PrivacyRoute
import com.compose.wonderlearn.shared.RootRoute
import com.compose.wonderlearn.shared.SupportRoute
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import java.io.File
import kotlinx.serialization.json.Json

fun main() {
  val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
  embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module(
  contentStore: ContentStore = ResourceContentStore(),
  eventStore: EventStore = JsonlEventStore(
    File(System.getenv("ANALYTICS_FILE") ?: "analytics-events.jsonl"),
  ),
  summaryToken: String? = System.getenv("ANALYTICS_TOKEN"),
) {
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

    get<PrivacyRoute> {
      call.respondText(privacyPolicyHtml, ContentType.Text.Html)
    }

    get<SupportRoute> {
      call.respondText(supportPageHtml, ContentType.Text.Html)
    }

    get<ContentManifestRoute> { route ->
      val manifest = contentStore.manifest()
      if (route.since != null && route.since >= manifest.version) {
        call.respond(HttpStatusCode.NotModified)
      } else {
        call.respond(manifest)
      }
    }

    post<EventsRoute> {
      val event = call.receive<AnalyticsEvent>()
      eventStore.record(event)
      call.respond(HttpStatusCode.Accepted)
    }

    get<EventsSummaryRoute> {
      val authorized = !summaryToken.isNullOrBlank() &&
        call.request.headers["Authorization"] == "Bearer $summaryToken"
      if (!authorized) {
        call.respond(HttpStatusCode.NotFound)
      } else {
        call.respond(eventStore.summary())
      }
    }
  }
}

private val privacyPolicyHtml: String by lazy {
  object {}.javaClass.getResourceAsStream("/privacy.html")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?: "<!DOCTYPE html><title>Privacy Policy</title><h1>Wisekins Privacy Policy</h1>"
}

private val supportPageHtml: String by lazy {
  object {}.javaClass.getResourceAsStream("/support.html")
    ?.bufferedReader()
    ?.use { it.readText() }
    ?: "<!DOCTYPE html><title>Support</title><h1>Wisekins Support</h1>"
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
