package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.LanguagePreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val KEY_LANGUAGE = "language"

class SqlDelightLanguagePreferences(
  database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LanguagePreferences {

  private val queries = database.wonderLearnQueries

  override fun selectedLanguage(): Flow<Language?> =
    queries.selectSetting(KEY_LANGUAGE).asFlow().mapToOneOrNull(dispatcher)
      .map { code -> code?.let { c -> Language.entries.firstOrNull { it.code == c } } }

  override suspend fun setLanguage(language: Language) {
    withContext(dispatcher) {
      queries.upsertSetting(KEY_LANGUAGE, language.code)
    }
  }
}
