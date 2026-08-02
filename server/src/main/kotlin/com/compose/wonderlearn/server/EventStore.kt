package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.AnalyticsEvent
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Where usage events are recorded. A newline-delimited JSON file today, a database later — the
 * interface keeps the routes from caring which. Deploys must point [JsonlEventStore] at a path on
 * the persistent volume so counts survive redeploys.
 */
interface EventStore {
  fun record(event: AnalyticsEvent)
  fun summary(): List<GameCount>
}

class JsonlEventStore(
  private val file: File,
  private val json: Json = Json { ignoreUnknownKeys = true },
) : EventStore {

  private val lock = Any()

  override fun record(event: AnalyticsEvent) {
    synchronized(lock) {
      file.parentFile?.mkdirs()
      file.appendText(json.encodeToString(AnalyticsEvent.serializer(), event) + "\n")
    }
  }

  override fun summary(): List<GameCount> {
    if (!file.exists()) return emptyList()
    val lines = synchronized(lock) { file.readLines() }
    return lines
      .mapNotNull { line ->
        if (line.isBlank()) null
        else runCatching { json.decodeFromString(AnalyticsEvent.serializer(), line) }.getOrNull()
      }
      .filter { it.name == "game_start" && it.gameId != null }
      .groupingBy { it.gameId!! }
      .eachCount()
      .entries
      .sortedByDescending { it.value }
      .map { GameCount(gameId = it.key, starts = it.value) }
  }
}
