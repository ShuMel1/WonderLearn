package com.compose.wonderlearn.data.analytics

import com.compose.wonderlearn.data.content.isDebugBuild
import com.compose.wonderlearn.data.ioDispatcher
import com.compose.wonderlearn.domain.Analytics
import com.compose.wonderlearn.domain.GameId
import com.compose.wonderlearn.shared.AnalyticsEvent
import com.compose.wonderlearn.shared.EventsRoute
import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch

private const val LOG_TAG = "Wisekins-Analytics"

/**
 * Sends anonymous usage events to our own server. Fire-and-forget: failures are swallowed so
 * analytics can never block the UI or interrupt a child mid-game.
 */
class HttpAnalytics(
  private val client: HttpClient,
  private val installId: InstallId,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher),
) : Analytics {

  override fun gameStarted(game: GameId) = send(name = "game_start", gameId = game.id)

  @OptIn(ExperimentalTime::class)
  private fun send(name: String, gameId: String? = null) {
    if (isDebugBuild) platformLog(LOG_TAG, listOfNotNull(name, gameId).joinToString(" "))
    scope.launch {
      try {
        val event = AnalyticsEvent(
          name = name,
          gameId = gameId,
          platform = platformName,
          appVersion = appVersionName,
          installId = installId.value(),
          timestamp = Clock.System.now().toEpochMilliseconds(),
        )
        client.post(EventsRoute()) {
          contentType(ContentType.Application.Json)
          setBody(event)
        }
      } catch (_: Exception) {
      }
    }
  }
}
