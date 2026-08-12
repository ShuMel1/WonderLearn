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

@Serializable
data class GameCount(
  val gameId: String,
  val starts: Int,
)

@Serializable
data class NameCount(
  val name: String,
  val count: Int,
)

@Serializable
data class DayCount(
  val day: String,
  val count: Int,
)

@Serializable
data class UsageStats(
  val distinctInstalls: Int,
  val newInstalls: Int,
  val totalOpens: Int,
  val opensToday: Int,
  val activeToday: Int,
  val activeLast7Days: Int,
  val byPlatform: List<NameCount>,
  val byVersion: List<NameCount>,
  val opensPerDay: List<DayCount>,
  val newInstallsPerDay: List<DayCount>,
  val games: List<GameCount>,
)
