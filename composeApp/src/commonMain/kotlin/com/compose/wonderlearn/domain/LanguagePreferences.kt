package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

interface LanguagePreferences {
  /** The language the child already speaks, used for prompts and translations. */
  fun nativeLanguage(): Flow<Language?>

  /** The language being learned, which words are shown, spoken and scored in. */
  fun targetLanguage(): Flow<Language?>

  suspend fun setNativeLanguage(language: Language)

  suspend fun setTargetLanguage(language: Language)
}
