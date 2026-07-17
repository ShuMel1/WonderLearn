package com.compose.wonderlearn.domain

interface Pronouncer {
  /** Speaks/plays the word in the given language. Returns false if no audio is available. */
  suspend fun pronounce(item: VocabularyItem, language: Language): Boolean
}
