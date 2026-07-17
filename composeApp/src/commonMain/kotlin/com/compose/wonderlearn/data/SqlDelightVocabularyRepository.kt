package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.compose.wonderlearn.db.CategoryEntity
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightVocabularyRepository(
  database: WonderLearnDatabase,
) : VocabularyRepository {

  private val queries = database.wonderLearnQueries

  override fun categories(): Flow<List<Category>> =
    queries.selectAllCategories().asFlow().mapToList(Dispatchers.Default)
      .map { rows -> rows.map { it.toDomain() } }

  override fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>> =
    queries.selectWordsWithTranslationsByCategory(categoryId, ::TranslationRow)
      .asFlow().mapToList(Dispatchers.Default)
      .map { rows -> rows.toItems() }

  override suspend fun item(id: String): VocabularyItem? =
    withContext(Dispatchers.Default) {
      queries.selectWordWithTranslationsById(id, ::TranslationRow)
        .executeAsList()
        .toItems()
        .firstOrNull()
    }
}

private fun CategoryEntity.toDomain() = Category(id = id, title = title, emoji = emoji)
