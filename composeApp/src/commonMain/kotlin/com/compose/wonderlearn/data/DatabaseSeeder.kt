package com.compose.wonderlearn.data

import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.Language

internal object DatabaseSeeder {

  fun seedIfEmpty(database: WonderLearnDatabase) {
    val queries = database.wonderLearnQueries
    if (queries.countCategories().executeAsOne() > 0L) return
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
