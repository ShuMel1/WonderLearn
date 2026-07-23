package com.compose.wonderlearn.data.content

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun httpClient(): HttpClient = HttpClient {
  install(Resources)
  defaultRequest {
    url(serverBaseUrl)
  }
  install(ContentNegotiation) {
    json(Json { ignoreUnknownKeys = true })
  }
  install(HttpTimeout) {
    requestTimeoutMillis = 60_000
    connectTimeoutMillis = 15_000
  }
}
