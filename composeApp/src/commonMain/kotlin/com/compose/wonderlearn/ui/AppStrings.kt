package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable
import com.compose.wonderlearn.domain.Language

/**
 * UI strings keyed to the language chosen in the app, so the interface follows the child's
 * language rather than the device locale. Generated from the strings.xml resource files.
 */
class LocalizedString(private val values: Map<Language, String>) {
  internal val languages: Set<Language> get() = values.keys

  @Composable
  operator fun invoke(): String {
    val language = LocalNativeLanguage.current
    return values[language] ?: values.getValue(Language.ENGLISH)
  }
}

object AppStrings {
  val app_name = LocalizedString(mapOf(Language.ARMENIAN to "WonderLearn", Language.ENGLISH to "WonderLearn", Language.RUSSIAN to "WonderLearn"))
  val title_words = LocalizedString(mapOf(Language.ARMENIAN to "Բառեր", Language.ENGLISH to "Words", Language.RUSSIAN to "Слова"))
  val action_back = LocalizedString(mapOf(Language.ARMENIAN to "Հետ", Language.ENGLISH to "Back", Language.RUSSIAN to "Назад"))
  val action_pronounce = LocalizedString(mapOf(Language.ARMENIAN to "Արտասանել", Language.ENGLISH to "Pronounce", Language.RUSSIAN to "Произнести"))
  val pronunciation_unavailable = LocalizedString(mapOf(Language.ARMENIAN to "Այս լեզվի արտասանությունը շուտով կլինի 🔊", Language.ENGLISH to "Pronunciation for this language is coming soon 🔊", Language.RUSSIAN to "Произношение для этого языка скоро появится 🔊"))
  val home_tagline = LocalizedString(mapOf(Language.ARMENIAN to "Ի՞նչ ես ուզում անել", Language.ENGLISH to "What do you want to do?", Language.RUSSIAN to "Что хочешь сделать?"))
  val home_learn = LocalizedString(mapOf(Language.ARMENIAN to "Սովորել", Language.ENGLISH to "Learn", Language.RUSSIAN to "Учить"))
  val home_review = LocalizedString(mapOf(Language.ARMENIAN to "Կրկնել", Language.ENGLISH to "Review", Language.RUSSIAN to "Повторить"))
  val home_progress = LocalizedString(mapOf(Language.ARMENIAN to "Առաջընթաց", Language.ENGLISH to "Progress", Language.RUSSIAN to "Прогресс"))
  val home_stories = LocalizedString(mapOf(Language.ARMENIAN to "Հեքիաթներ", Language.ENGLISH to "Stories", Language.RUSSIAN to "Сказки"))
  val coming_soon = LocalizedString(mapOf(Language.ARMENIAN to "Շուտով ✨", Language.ENGLISH to "Coming soon ✨", Language.RUSSIAN to "Скоро ✨"))
  val quiz_prompt = LocalizedString(mapOf(Language.ARMENIAN to "Ո՞րն է սա", Language.ENGLISH to "Which one is this?", Language.RUSSIAN to "Что это?"))
  val action_listen = LocalizedString(mapOf(Language.ARMENIAN to "Լսել", Language.ENGLISH to "Listen", Language.RUSSIAN to "Слушать"))
  val quiz_correct = LocalizedString(mapOf(Language.ARMENIAN to "Ապրե՛ս 🎉", Language.ENGLISH to "Great job! 🎉", Language.RUSSIAN to "Молодец! 🎉"))
  val quiz_learned = LocalizedString(mapOf(Language.ARMENIAN to "Բառը սովորեցիր! 🎓", Language.ENGLISH to "Word learned! 🎓", Language.RUSSIAN to "Слово выучено! 🎓"))
  val quiz_all_learned = LocalizedString(mapOf(Language.ARMENIAN to "Դու սովորեցիր բոլոր բառերը! 🎉", Language.ENGLISH to "You have learned every word! 🎉", Language.RUSSIAN to "Ты выучил все слова! 🎉"))
  val quiz_score = LocalizedString(mapOf(Language.ARMENIAN to "Միավոր", Language.ENGLISH to "Score", Language.RUSSIAN to "Счёт"))
  val home_learned = LocalizedString(mapOf(Language.ARMENIAN to "Սովորած", Language.ENGLISH to "Learned", Language.RUSSIAN to "Изучено"))
  val learned_title = LocalizedString(mapOf(Language.ARMENIAN to "Սովորած բառեր", Language.ENGLISH to "Learned Words", Language.RUSSIAN to "Изученные слова"))
  val learned_empty = LocalizedString(mapOf(Language.ARMENIAN to "Դեռ սովորած բառեր չկան։ Խաղա «Կրկնել» և ճիշտ պատասխանիր բառին 3 անգամ անընդմեջ։", Language.ENGLISH to "No learned words yet. Play Review and answer a word right 3 times in a row to master it!", Language.RUSSIAN to "Пока нет изученных слов. Играй в «Повторить» и ответь правильно 3 раза подряд, чтобы выучить слово!"))
  val action_repeat = LocalizedString(mapOf(Language.ARMENIAN to "Կրկնել", Language.ENGLISH to "Repeat", Language.RUSSIAN to "Повторить"))
  val action_revise = LocalizedString(mapOf(Language.ARMENIAN to "Ամրապնդել", Language.ENGLISH to "Revise", Language.RUSSIAN to "Закрепить"))
  val revise_empty = LocalizedString(mapOf(Language.ARMENIAN to "Դեռ ամրապնդելու բան չկա։ Սկզբում սովորիր մի քանի բառ։", Language.ENGLISH to "Nothing to revise yet. Learn some words first!", Language.RUSSIAN to "Пока нечего закреплять. Сначала выучи несколько слов!"))
  val revise_done = LocalizedString(mapOf(Language.ARMENIAN to "Լավ ամրապնդեցիր! 🎉", Language.ENGLISH to "Great revising! 🎉", Language.RUSSIAN to "Отлично закрепил! 🎉"))
  val action_previous = LocalizedString(mapOf(Language.ARMENIAN to "Նախորդը", Language.ENGLISH to "Previous", Language.RUSSIAN to "Предыдущее"))
  val action_next = LocalizedString(mapOf(Language.ARMENIAN to "Հաջորդը", Language.ENGLISH to "Next", Language.RUSSIAN to "Следующее"))
  val account_title = LocalizedString(mapOf(Language.ARMENIAN to "Կարգավորումներ", Language.ENGLISH to "Settings", Language.RUSSIAN to "Настройки"))
  val account_open = LocalizedString(mapOf(Language.ARMENIAN to "Հաշիվ", Language.ENGLISH to "Account", Language.RUSSIAN to "Аккаунт"))
  val account_who_is_learning = LocalizedString(mapOf(Language.ARMENIAN to "Ո՞վ է սովորում", Language.ENGLISH to "Who is learning?", Language.RUSSIAN to "Кто учится?"))
  val account_add_child = LocalizedString(mapOf(Language.ARMENIAN to "Ավելացնել երեխա", Language.ENGLISH to "Add child", Language.RUSSIAN to "Добавить ребёнка"))
  val account_child_name = LocalizedString(mapOf(Language.ARMENIAN to "Անուն", Language.ENGLISH to "Name", Language.RUSSIAN to "Имя"))
  val account_learning_language = LocalizedString(mapOf(Language.ARMENIAN to "Սովորելու լեզուն", Language.ENGLISH to "Learning language", Language.RUSSIAN to "Язык изучения"))
  val action_save = LocalizedString(mapOf(Language.ARMENIAN to "Պահպանել", Language.ENGLISH to "Save", Language.RUSSIAN to "Сохранить"))
  val action_cancel = LocalizedString(mapOf(Language.ARMENIAN to "Չեղարկել", Language.ENGLISH to "Cancel", Language.RUSSIAN to "Отмена"))
  val language_native_title = LocalizedString(mapOf(Language.ARMENIAN to "Ո՞ր լեզվով ես խոսում", Language.ENGLISH to "Which language do you speak?", Language.RUSSIAN to "На каком языке ты говоришь?"))
  val language_target_title = LocalizedString(mapOf(Language.ARMENIAN to "Ի՞նչ ես ուզում սովորել", Language.ENGLISH to "What do you want to learn?", Language.RUSSIAN to "Что хочешь выучить?"))
  val account_my_language = LocalizedString(mapOf(Language.ARMENIAN to "Ես խոսում եմ", Language.ENGLISH to "I speak", Language.RUSSIAN to "Я говорю"))
  val account_rename = LocalizedString(mapOf(Language.ARMENIAN to "Վերանվանել", Language.ENGLISH to "Rename", Language.RUSSIAN to "Переименовать"))
  val account_delete = LocalizedString(mapOf(Language.ARMENIAN to "Ջնջել", Language.ENGLISH to "Delete", Language.RUSSIAN to "Удалить"))
  val account_delete_confirm = LocalizedString(mapOf(Language.ARMENIAN to "Ջնջե՞լ այս երեխային և ամբողջ առաջընթացը", Language.ENGLISH to "Delete this child and all their progress?", Language.RUSSIAN to "Удалить этого ребёнка и весь его прогресс?"))
  val account_edit = LocalizedString(mapOf(Language.ARMENIAN to "Խմբագրել պրոֆիլը", Language.ENGLISH to "Edit profile", Language.RUSSIAN to "Изменить профиль"))
}
