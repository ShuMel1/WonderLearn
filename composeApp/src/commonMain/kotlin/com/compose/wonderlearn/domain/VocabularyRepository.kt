package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
  fun categories(): Flow<List<Category>>
  fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>>
  suspend fun item(id: String): VocabularyItem?
}
