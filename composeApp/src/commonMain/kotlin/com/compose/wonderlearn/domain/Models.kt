package com.compose.wonderlearn.domain

enum class Language(
  val code: String,
  val bcp47: String,
  val displayName: String,
  val flag: String,
  val ttsSupported: Boolean,
  val asrSupported: Boolean,
  val hasRecordedAudio: Boolean,
) {
  ARMENIAN(
    code = "hy",
    bcp47 = "hy-AM",
    displayName = "Հայերեն",
    flag = "🇦🇲",
    ttsSupported = false,
    asrSupported = false,
    hasRecordedAudio = true,
  ),
  ENGLISH(
    code = "en",
    bcp47 = "en-US",
    displayName = "English",
    flag = "🇬🇧",
    ttsSupported = true,
    asrSupported = true,
    hasRecordedAudio = false,
  ),
  RUSSIAN(
    code = "ru",
    bcp47 = "ru-RU",
    displayName = "Русский",
    flag = "🇷🇺",
    ttsSupported = true,
    asrSupported = true,
    hasRecordedAudio = false,
  ),
  ;

  companion object {
    /**
     * Every language can be picked in either role, including the one already chosen for the
     * other role, so both lists are simply every entry.
     */
    val targets: List<Language> get() = entries
    val natives: List<Language> get() = entries
  }
}

data class Category(
  val id: String,
  val title: String,
  val emoji: String,
  val imageRef: String? = null,
)

data class VocabularyItem(
  val id: String,
  val categoryId: String,
  val emoji: String,
  val imageRef: String? = null,
  val translations: Map<Language, String>,
) {
  fun text(language: Language): String =
    translations[language] ?: translations.getValue(Language.ENGLISH)
}
