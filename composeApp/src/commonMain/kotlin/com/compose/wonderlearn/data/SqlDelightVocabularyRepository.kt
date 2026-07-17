package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.compose.wonderlearn.db.CategoryEntity
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private data class TranslationRow(
  val wordId: String,
  val categoryId: String,
  val emoji: String,
  val languageCode: String,
  val text: String,
)

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

  private fun seed() {
    queries.transaction {
      SeedData.categories.forEach {
        queries.insertCategory(it.id, it.title, it.emoji, it.sortIndex)
      }
      SeedData.words.forEach { word ->
        queries.insertWord(word.id, word.categoryId, word.emoji)
        queries.insertTranslation(word.id, Language.ARMENIAN.code, word.armenian)
        queries.insertTranslation(word.id, Language.ENGLISH.code, word.english)
        queries.insertTranslation(word.id, Language.RUSSIAN.code, word.russian)
      }
    }
  }
}

private fun CategoryEntity.toDomain() = Category(id = id, title = title, emoji = emoji)

private fun List<TranslationRow>.toItems(): List<VocabularyItem> =
  groupBy { it.wordId }.map { (_, rows) ->
    val first = rows.first()
    VocabularyItem(
      id = first.wordId,
      categoryId = first.categoryId,
      emoji = first.emoji,
      translations = rows.mapNotNull { row ->
        languageOf(row.languageCode)?.let { it to row.text }
      }.toMap(),
    )
  }

private fun languageOf(code: String): Language? =
  Language.entries.firstOrNull { it.code == code }
