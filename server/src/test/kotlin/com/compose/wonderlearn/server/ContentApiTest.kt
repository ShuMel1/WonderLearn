package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.shared.ManifestCategory
import com.compose.wonderlearn.shared.ManifestWord
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentApiTest {

  private val manifest = ContentManifest(
    version = 7,
    categories = listOf(ManifestCategory("fruits", "Fruits", "🍎", 0)),
    words = listOf(
      ManifestWord("apple", "fruits", "🍎", mapOf("en" to "Apple", "hy" to "Խնձոր", "ru" to "Яблоко")),
    ),
  )

  private val store = object : ContentStore {
    override fun manifest(): ContentManifest = manifest
  }

  @Test
  fun theRootDescribesTheService() = testApplication {
    application { module(store) }
    val response = client.get("/")
    assertEquals(HttpStatusCode.OK, response.status)
    val body = response.bodyAsText()
    assertTrue(body.contains("wonderlearn-api"), "got $body")
    assertTrue(body.contains("/v1/content/manifest"), "root lists its endpoints, got $body")
  }

  @Test
  fun healthReportsTheContentVersion() = testApplication {
    application { module(store) }
    val body = client.get("/health").bodyAsText()
    assertTrue(body.contains("\"contentVersion\":7"), "got $body")
  }

  @Test
  fun manifestIsServedWhenNoVersionIsKnown() = testApplication {
    application { module(store) }
    val response = client.get("/v1/content/manifest")
    assertEquals(HttpStatusCode.OK, response.status)
    val returned = Json { ignoreUnknownKeys = true }
      .decodeFromString<ContentManifest>(response.bodyAsText())
    assertEquals(7, returned.version)
    assertEquals("apple", returned.words.single().id)
  }

  @Test
  fun staleClientsGetTheManifest() = testApplication {
    application { module(store) }
    assertEquals(HttpStatusCode.OK, client.get("/v1/content/manifest?since=6").status)
  }

  @Test
  fun currentClientsGetNotModified() = testApplication {
    application { module(store) }
    assertEquals(HttpStatusCode.NotModified, client.get("/v1/content/manifest?since=7").status)
  }

  @Test
  fun clientsAheadOfTheServerGetNotModified() = testApplication {
    application { module(store) }
    assertEquals(HttpStatusCode.NotModified, client.get("/v1/content/manifest?since=99").status)
  }
}
