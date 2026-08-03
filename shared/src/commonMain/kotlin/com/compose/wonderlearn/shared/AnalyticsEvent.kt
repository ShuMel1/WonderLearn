package com.compose.wonderlearn.shared

import kotlinx.serialization.Serializable

/**
 * A single anonymous usage event sent from the app to our own server. It carries no personal
 * information: [installId] is a random per-install identifier, never a profile, name or device id.
 */
@Serializable
data class AnalyticsEvent(
  val name: String,
  val gameId: String? = null,
  val platform: String,
  val appVersion: String,
  val installId: String,
  val timestamp: Long,
)
