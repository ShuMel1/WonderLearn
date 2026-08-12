package com.compose.wonderlearn.server

import com.compose.wonderlearn.shared.AnalyticsEvent
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Where usage events are recorded. A newline-delimited JSON file today, a database later — the
 * interface keeps the routes from caring which. Deploys must point [JsonlEventStore] at a path on
 * the persistent volume so counts survive redeploys.
 */
interface EventStore {
  fun record(event: AnalyticsEvent)
  fun summary(): List<GameCount>
  fun stats(): UsageStats
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

  override fun summary(): List<GameCount> = gameCounts(readEvents())

  override fun stats(): UsageStats {
    val events = readEvents()
    val today = LocalDate.now(ZoneOffset.UTC)

    val opens = events.filter { it.name == "app_open" }
    val installs = events.filter { it.name == "install" }

    // An install id carries one platform but may span app versions, so attribute each install to
    // the version it was last seen on.
    val latestByInstall = events.groupBy { it.installId }
      .mapValues { (_, evs) -> evs.maxByOrNull { it.timestamp }!! }

    val activeToday = events.filter { dayOf(it.timestamp) == today }
      .map { it.installId }.distinct().size
    val weekAgo = today.minusDays(6)
    val activeLast7 = events.filter { !dayOf(it.timestamp).isBefore(weekAgo) }
      .map { it.installId }.distinct().size

    return UsageStats(
      distinctInstalls = events.map { it.installId }.distinct().size,
      newInstalls = installs.size,
      totalOpens = opens.size,
      opensToday = opens.count { dayOf(it.timestamp) == today },
      activeToday = activeToday,
      activeLast7Days = activeLast7,
      byPlatform = counts(latestByInstall.values.map { it.platform }),
      byVersion = counts(latestByInstall.values.map { it.appVersion }),
      opensPerDay = perDay(opens, today, 14),
      newInstallsPerDay = perDay(installs, today, 14),
      games = gameCounts(events),
    )
  }

  private fun dayOf(timestamp: Long): LocalDate =
    Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate()

  private fun counts(values: List<String>): List<NameCount> =
    values.groupingBy { it }.eachCount()
      .entries.sortedByDescending { it.value }
      .map { NameCount(name = it.key, count = it.value) }

  private fun perDay(events: List<AnalyticsEvent>, today: LocalDate, days: Long): List<DayCount> {
    val from = today.minusDays(days - 1)
    return events.map { dayOf(it.timestamp) }
      .filter { !it.isBefore(from) }
      .groupingBy { it.toString() }.eachCount()
      .entries.sortedBy { it.key }
      .map { DayCount(day = it.key, count = it.value) }
  }

  private fun gameCounts(events: List<AnalyticsEvent>): List<GameCount> =
    events
      .filter { it.name == "game_start" && it.gameId != null }
      .groupingBy { it.gameId!! }
      .eachCount()
      .entries
      .sortedByDescending { it.value }
      .map { GameCount(gameId = it.key, starts = it.value) }

  private fun readEvents(): List<AnalyticsEvent> {
    if (!file.exists()) return emptyList()
    val lines = synchronized(lock) { file.readLines() }
    return lines.mapNotNull { line ->
      if (line.isBlank()) null
      else runCatching { json.decodeFromString(AnalyticsEvent.serializer(), line) }.getOrNull()
    }
  }
}
