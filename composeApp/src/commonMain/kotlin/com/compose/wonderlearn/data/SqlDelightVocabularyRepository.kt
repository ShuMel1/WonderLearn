package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.compose.wonderlearn.db.CategoryEntity
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.db.WordEntity
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.Language
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

  init {
    if (queries.countCategories().executeAsOne() == 0L) {
      seed()
    }
  }

  override fun categories(): Flow<List<Category>> =
    queries.selectAllCategories().asFlow().mapToList(Dispatchers.Default)
      .map { rows -> rows.map { it.toDomain() } }

  override fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>> =
    queries.selectWordsByCategory(categoryId).asFlow().mapToList(Dispatchers.Default)
      .map { rows -> rows.map { it.toDomain() } }

  override suspend fun item(id: String): VocabularyItem? =
    withContext(Dispatchers.Default) {
      queries.selectWordById(id).executeAsOneOrNull()?.toDomain()
    }

  private fun seed() {
    queries.transaction {
      SeedData.categories.forEach {
        queries.insertCategory(it.id, it.title, it.emoji, it.sortIndex)
      }
      SeedData.words.forEach {
        queries.insertWord(it.id, it.categoryId, it.emoji, it.armenian, it.english, it.russian)
      }
    }
  }
}

private fun CategoryEntity.toDomain() = Category(id = id, title = title, emoji = emoji)

private fun WordEntity.toDomain() = VocabularyItem(
  id = id,
  categoryId = categoryId,
  emoji = emoji,
  translations = mapOf(
    Language.ARMENIAN to armenian,
    Language.ENGLISH to english,
    Language.RUSSIAN to russian,
  ),
)
