package com.compose.wonderlearn.domain

/** Stable identifiers for the games we measure. The [id] is what reaches the server. */
enum class GameId(val id: String) {
  MEMORY_MATCH("memory_match"),
  ODD_ONE_OUT("odd_one_out"),
  BUBBLE_POP("bubble_pop"),
}

/**
 * Records anonymous usage so we can see which games children reach for. Implementations must be
 * fire-and-forget: analytics can never block, slow or break the experience.
 */
interface Analytics {
  /** The app was launched. Fired once per cold start, and once ever as an install. */
  fun appOpened()

  fun gameStarted(game: GameId)
}
