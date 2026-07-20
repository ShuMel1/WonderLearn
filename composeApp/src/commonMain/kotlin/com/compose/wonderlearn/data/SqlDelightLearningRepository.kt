package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.LEARNED_THRESHOLD
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.domain.QuizRound
import com.compose.wonderlearn.domain.VocabularyItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightLearningRepository(
  database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LearningRepository {

  private val queries = database.wonderLearnQueries

  override suspend fun nextRound(language: Language, mode: QuizMode): QuizRound? =
    withContext(dispatcher) {
    val pool = when (mode) {
      QuizMode.LEARN -> queries.selectUnlearnedWordsWithTranslations(language.code, ::TranslationRow)
      QuizMode.REVISE -> queries.selectLearnedWordsWithTranslations(language.code, ::TranslationRow)
    }.executeAsList().toItems()
    if (pool.isEmpty()) return@withContext null

    val categoryId = pool.map { it.categoryId }.distinct().random()
    val target = pool.filter { it.categoryId == categoryId }.random()

    val inCategory = queries.selectWordsWithTranslationsByCategory(categoryId, ::TranslationRow)
      .executeAsList().toItems()
    val distractors = inCategory.filter { it.id != target.id }.shuffled().take(3)
    QuizRound(options = (distractors + target).shuffled(), target = target)
  }

  override suspend fun recordCorrect(wordId: String, language: Language): Boolean =
    withContext(dispatcher) {
      queries.transactionWithResult {
        queries.ensureProgress(wordId, language.code)
        queries.incrementStreak(wordId, language.code)
        queries.selectStreak(wordId, language.code).executeAsOne() >= LEARNED_THRESHOLD
      }
    }

  override suspend fun recordWrong(wordId: String, language: Language, mode: QuizMode) =
    withContext(dispatcher) {
      queries.transaction {
        queries.ensureProgress(wordId, language.code)
        when (mode) {
          QuizMode.LEARN -> queries.resetStreak(wordId, language.code)
          QuizMode.REVISE -> queries.decayStreak(wordId, language.code)
        }
      }
    }

  override fun learnedItems(language: Language): Flow<List<VocabularyItem>> =
    queries.selectLearnedWordsWithTranslations(language.code, ::TranslationRow)
      .asFlow().mapToList(dispatcher)
      .map { rows -> rows.toItems() }
}
