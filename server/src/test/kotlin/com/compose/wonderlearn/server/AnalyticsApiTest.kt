package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.ContentManifest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsApiTest {

  private val contentStore = object : ContentStore {
    override fun manifest(): ContentManifest =
      ContentManifest(version = 1, categories = emptyList(), words = emptyList())
  }

  private val file = File.createTempFile("analytics-test", ".jsonl").apply { deleteOnExit() }
  private val eventStore = JsonlEventStore(file)

  @AfterTest
  fun cleanup() {
    file.delete()
  }

  private fun event(gameId: String): String =
    """{"name":"game_start","gameId":"$gameId","platform":"android","appVersion":"1.0.0","installId":"test-install","timestamp":1}"""

  @Test
  fun recordsEventsAndCountsGameStarts() = testApplication {
    application { module(contentStore = contentStore, eventStore = eventStore, summaryToken = "secret") }

    listOf("memory_match", "memory_match", "bubble_pop").forEach { game ->
      val response = client.post("/v1/events") {
        contentType(ContentType.Application.Json)
        setBody(event(game))
      }
      assertEquals(HttpStatusCode.Accepted, response.status)
    }

    val summary = client.get("/v1/events/summary") {
      header(HttpHeaders.Authorization, "Bearer secret")
    }
    assertEquals(HttpStatusCode.OK, summary.status)
    val body = summary.bodyAsText()
    assertTrue(body.contains("\"gameId\":\"memory_match\",\"starts\":2"), "got $body")
    assertTrue(body.contains("\"gameId\":\"bubble_pop\",\"starts\":1"), "got $body")
  }

  @Test
  fun summaryIsHiddenWithoutTheToken() = testApplication {
    application { module(contentStore = contentStore, eventStore = eventStore, summaryToken = "secret") }

    assertEquals(HttpStatusCode.NotFound, client.get("/v1/events/summary").status)
    assertEquals(
      HttpStatusCode.NotFound,
      client.get("/v1/events/summary") { header(HttpHeaders.Authorization, "Bearer wrong") }.status,
    )
  }
}
