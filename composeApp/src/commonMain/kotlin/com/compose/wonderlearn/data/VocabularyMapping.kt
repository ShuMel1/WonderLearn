package com.compose.wonderlearn.data

import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.VocabularyItem

internal data class TranslationRow(
  val wordId: String,
  val categoryId: String,
  val emoji: String,
  val languageCode: String,
  val text: String,
)

internal fun List<TranslationRow>.toItems(): List<VocabularyItem> =
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
