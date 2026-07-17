package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

const val LEARNED_THRESHOLD = 3

data class QuizRound(
  val options: List<VocabularyItem>,
  val target: VocabularyItem,
)

interface LearningRepository {
  /** A quiz round drawn from not-yet-learned words, or null when everything is learned. */
  suspend fun nextRound(): QuizRound?

  /** Records a correct answer; returns true if the word just became learned. */
  suspend fun recordCorrect(wordId: String): Boolean

  /** Resets a word's streak after a wrong answer. */
  suspend fun recordWrong(wordId: String)

  fun learnedItems(): Flow<List<VocabularyItem>>
}
