package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

const val LEARNED_THRESHOLD = 3

data class QuizRound(
  val options: List<VocabularyItem>,
  val target: VocabularyItem,
)

/**
 * [LEARN] quizzes words not yet mastered; a wrong answer clears the streak.
 * [REVISE] quizzes already-mastered words; a wrong answer costs a single step, so one
 * slip drops a word back into [LEARN] rather than erasing it.
 */
enum class QuizMode { LEARN, REVISE }

interface LearningRepository {
  /** A round drawn from the pool [mode] applies to in [language], or null when that pool is empty. */
  suspend fun nextRound(language: Language, mode: QuizMode): QuizRound?

  /** Records a correct answer; returns true if the word just became learned in [language]. */
  suspend fun recordCorrect(wordId: String, language: Language): Boolean

  /** Penalises a wrong answer in [language], by the amount [mode] dictates. */
  suspend fun recordWrong(wordId: String, language: Language, mode: QuizMode)

  fun learnedItems(language: Language): Flow<List<VocabularyItem>>
}
