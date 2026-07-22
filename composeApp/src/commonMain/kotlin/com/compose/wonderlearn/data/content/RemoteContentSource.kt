package com.compose.wonderlearn.data.content

import com.compose.wonderlearn.shared.ContentManifest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

/** Cleartext hosts are allowed for these in the Android network security config. */
expect val devServerBaseUrl: String

class RemoteContentSource(
  private val client: HttpClient,
  private val baseUrl: String = devServerBaseUrl,
  private val currentVersion: suspend () -> Long,
) : ContentSource {

  override suspend fun load(): ContentManifest? =
    try {
      val response = client.get("$baseUrl/v1/content/manifest") {
        parameter("since", currentVersion())
      }
      when {
        response.status == HttpStatusCode.NotModified -> null
        response.status.isSuccess() -> response.body<ContentManifest>()
        else -> null
      }
    } catch (e: Exception) {
      null
    }
}

/**
 * Prefers [primary], falling back to [fallback] when it yields nothing — an unreachable server,
 * a failed parse, or content the device already has.
 */
class FallbackContentSource(
  private val primary: ContentSource,
  private val fallback: ContentSource,
) : ContentSource {

  override suspend fun load(): ContentManifest? = primary.load() ?: fallback.load()
}
