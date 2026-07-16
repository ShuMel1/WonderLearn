package com.compose.wonderlearn.domain

enum class Language(
  val code: String,
  val bcp47: String,
  val displayName: String,
  val flag: String,
) {
  ARMENIAN("hy", "hy-AM", "Հայերեն", "🇦🇲"),
  ENGLISH("en", "en-US", "English", "🇬🇧"),
  RUSSIAN("ru", "ru-RU", "Русский", "🇷🇺"),
}

data class Category(
  val id: String,
  val title: String,
  val emoji: String,
)

data class VocabularyItem(
  val id: String,
  val categoryId: String,
  val emoji: String,
  val translations: Map<Language, String>,
) {
  fun text(language: Language): String =
    translations[language] ?: translations.getValue(Language.ENGLISH)
}
