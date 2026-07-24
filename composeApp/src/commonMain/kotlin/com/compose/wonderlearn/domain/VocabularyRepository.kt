package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
  fun categories(): Flow<List<Category>>
  fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>>
  suspend fun item(id: String): VocabularyItem?

  /** A random selection of [count] distinct words, for games that draw from the whole vocabulary. */
  suspend fun randomItems(count: Int): List<VocabularyItem>
}
