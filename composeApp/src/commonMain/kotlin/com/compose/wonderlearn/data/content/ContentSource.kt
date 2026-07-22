package com.compose.wonderlearn.data.content

import com.compose.wonderlearn.shared.ContentManifest
import com.compose.wonderlearn.resources.Res
import kotlinx.serialization.json.Json

private const val MANIFEST_PATH = "files/content/vocabulary.json"

/** Where vocabulary comes from. Bundled today, a server response once sync exists. */
interface ContentSource {
  suspend fun load(): ContentManifest?
}

class BundledContentSource(
  private val json: Json,
) : ContentSource {

  override suspend fun load(): ContentManifest? =
    try {
      json.decodeFromString<ContentManifest>(Res.readBytes(MANIFEST_PATH).decodeToString())
    } catch (e: Exception) {
      null
    }
}
