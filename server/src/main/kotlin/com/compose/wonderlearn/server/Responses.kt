package com.compose.wonderlearn.server

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
  val status: String,
  val contentVersion: Long,
)

@Serializable
data class ErrorResponse(
  val error: String,
)
