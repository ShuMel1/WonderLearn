package com.compose.wonderlearn.shared

import kotlinx.serialization.Serializable

@Serializable
data class ContentManifest(
  val version: Long,
  val categories: List<ManifestCategory>,
  val words: List<ManifestWord>,
)

@Serializable
data class ManifestCategory(
  val id: String,
  val title: String,
  val emoji: String,
  val sortIndex: Long,
  val imageRef: String? = null,
)

@Serializable
data class ManifestWord(
  val id: String,
  val categoryId: String,
  val emoji: String,
  val translations: Map<String, String>,
  val imageRef: String? = null,
)
