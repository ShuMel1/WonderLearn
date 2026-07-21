package com.compose.wonderlearn.domain

enum class Language(
  val code: String,
  val bcp47: String,
  val displayName: String,
  val flag: String,
  val ttsSupported: Boolean,
  val asrSupported: Boolean,
  val hasRecordedAudio: Boolean,
  val canBeTarget: Boolean,
  val canBeNative: Boolean,
) {
  ARMENIAN(
    code = "hy",
    bcp47 = "hy-AM",
    displayName = "Հայերեն",
    flag = "🇦🇲",
    ttsSupported = false,
    asrSupported = false,
    hasRecordedAudio = true,
    canBeTarget = true,
    canBeNative = true,
  ),
  ENGLISH(
    code = "en",
    bcp47 = "en-US",
    displayName = "English",
    flag = "🇬🇧",
    ttsSupported = true,
    asrSupported = true,
    hasRecordedAudio = false,
    canBeTarget = true,
    canBeNative = true,
  ),
  RUSSIAN(
    code = "ru",
    bcp47 = "ru-RU",
    displayName = "Русский",
    flag = "🇷🇺",
    ttsSupported = true,
    asrSupported = true,
    hasRecordedAudio = false,
    canBeTarget = true,
    canBeNative = true,
  ),
  ;

  companion object {
    val targets: List<Language> get() = entries.filter { it.canBeTarget }
    val natives: List<Language> get() = entries.filter { it.canBeNative }
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
