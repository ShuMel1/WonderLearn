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

private const val KEY_TARGET_LANGUAGE = "language"
private const val KEY_NATIVE_LANGUAGE = "nativeLanguage"

class SqlDelightLanguagePreferences(
  database: WonderLearnDatabase,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : LanguagePreferences {

  private val queries = database.wonderLearnQueries

  override fun nativeLanguage(): Flow<Language?> = languageFor(KEY_NATIVE_LANGUAGE)

  override fun targetLanguage(): Flow<Language?> = languageFor(KEY_TARGET_LANGUAGE)

  override suspend fun setNativeLanguage(language: Language) {
    withContext(dispatcher) { queries.upsertSetting(KEY_NATIVE_LANGUAGE, language.code) }
  }

  override suspend fun setTargetLanguage(language: Language) {
    withContext(dispatcher) { queries.upsertSetting(KEY_TARGET_LANGUAGE, language.code) }
  }

  private fun languageFor(key: String): Flow<Language?> =
    queries.selectSetting(key).asFlow().mapToOneOrNull(dispatcher)
      .map { code -> code?.let { c -> Language.entries.firstOrNull { it.code == c } } }
}
