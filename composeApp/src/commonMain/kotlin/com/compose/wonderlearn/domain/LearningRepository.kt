package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

const val LEARNED_THRESHOLD = 3

data class QuizRound(
  val options: List<VocabularyItem>,
  val target: VocabularyItem,
)

interface LearningRepository {
  /** A quiz round drawn from words not yet learned in [language], or null when all are learned. */
  suspend fun nextRound(language: Language): QuizRound?

  /** Records a correct answer; returns true if the word just became learned in [language]. */
  suspend fun recordCorrect(wordId: String, language: Language): Boolean

  /** Resets a word's streak in [language] after a wrong answer. */
  suspend fun recordWrong(wordId: String, language: Language)

  fun learnedItems(language: Language): Flow<List<VocabularyItem>>
}
