package com.compose.wonderlearn.data.content

import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.shared.ContentManifestRoute
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

class RemoteContentSource(
  private val client: HttpClient,
  private val currentVersion: suspend () -> Long,
) : ContentSource {

  override suspend fun load(): ContentManifest? =
    try {
      val response: HttpResponse = client.get(ContentManifestRoute(since = currentVersion()))
      when {
        response.status == HttpStatusCode.NotModified -> null
        response.status.isSuccess() -> response.body<ContentManifest>()
        else -> null
      }
    } catch (e: Exception) {
      null
    }
}
