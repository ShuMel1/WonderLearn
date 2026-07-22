package com.compose.wonderlearn.data.content

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun httpClient(): HttpClient = HttpClient {
  install(ContentNegotiation) {
    json(Json { ignoreUnknownKeys = true })
  }
  install(HttpTimeout) {
    requestTimeoutMillis = 5_000
    connectTimeoutMillis = 3_000
  }
}
