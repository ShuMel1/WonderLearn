package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.LEARNED_THRESHOLD
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.domain.QuizRound
import com.compose.wonderlearn.domain.VocabularyItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightLearningRepository(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LearningRepository {

  private val queries = database.wonderLearnQueries

  override suspend fun nextRound(language: Language, mode: QuizMode): QuizRound? =
    withContext(dispatcher) {
      val profileId = profiles.currentProfileId()
      val pool = when (mode) {
        QuizMode.LEARN ->
          queries.selectUnlearnedWordsWithTranslations(profileId, language.code, ::TranslationRow)
        QuizMode.REVISE ->
          queries.selectLearnedWordsWithTranslations(profileId, language.code, ::TranslationRow)
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
      val profileId = profiles.currentProfileId()
      queries.transactionWithResult {
        queries.ensureProgress(profileId, wordId, language.code)
        queries.incrementStreak(profileId, wordId, language.code)
        queries.selectStreak(profileId, wordId, language.code).executeAsOne() >= LEARNED_THRESHOLD
      }
    }

  override suspend fun recordWrong(wordId: String, language: Language, mode: QuizMode) =
    withContext(dispatcher) {
      val profileId = profiles.currentProfileId()
      queries.transaction {
        queries.ensureProgress(profileId, wordId, language.code)
        when (mode) {
          QuizMode.LEARN -> queries.resetStreak(profileId, wordId, language.code)
          QuizMode.REVISE -> queries.decayStreak(profileId, wordId, language.code)
        }
      }
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun learnedItems(language: Language): Flow<List<VocabularyItem>> =
    profiles.activeProfileId().flatMapLatest { profileId ->
      queries.selectLearnedWordsWithTranslations(profileId, language.code, ::TranslationRow)
        .asFlow().mapToList(dispatcher)
        .map { rows -> rows.toItems() }
    }
}
