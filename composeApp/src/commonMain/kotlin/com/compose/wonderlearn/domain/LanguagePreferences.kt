package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

interface LanguagePreferences {
  /** The chosen learning language, or null if the user hasn't picked one yet. */
  fun selectedLanguage(): Flow<Language?>

  suspend fun setLanguage(language: Language)
}
