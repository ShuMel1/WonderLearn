package com.compose.wonderlearn.server

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
  val status: String,
  val contentVersion: Long,
)

@Serializable
data class ServiceInfo(
  val service: String,
  val contentVersion: Long,
  val endpoints: List<String>,
)

@Serializable
data class ErrorResponse(
  val error: String,
)
