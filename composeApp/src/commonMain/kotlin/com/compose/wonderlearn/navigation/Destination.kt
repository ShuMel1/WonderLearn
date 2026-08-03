package com.compose.wonderlearn.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

  @Serializable
  data object Home : Destination

  @Serializable
  data object Categories : Destination

  @Serializable
  data class Words(val categoryId: String) : Destination

  @Serializable
  data class Detail(val itemId: String) : Destination

  @Serializable
  data class Quiz(val revise: Boolean = false) : Destination

  @Serializable
  data object Learned : Destination

  @Serializable
  data object Levels : Destination

  @Serializable
  data object Games : Destination

  @Serializable
  data class MemoryGame(val fromLevel: Boolean = false, val size: Int = -1) : Destination

  @Serializable
  data object OddOneOut : Destination

  @Serializable
  data object BubblePop : Destination

  @Serializable
  data object Avatars : Destination
}
