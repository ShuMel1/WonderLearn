package com.compose.wonderlearn.ui

import androidx.compose.runtime.Composable
import com.compose.wonderlearn.domain.Language

/**
 * UI strings keyed to the language chosen in the app, so the interface follows the child's
 * language rather than the device locale. Generated from the strings.xml resource files.
 */
class LocalizedString(private val values: Map<Language, String>) {
  internal val languages: Set<Language> get() = values.keys

  fun forLanguage(language: Language): String =
    values[language] ?: values.getValue(Language.ENGLISH)

  @Composable
  operator fun invoke(): String = forLanguage(LocalNativeLanguage.current)
}

object AppStrings {
  val app_name = LocalizedString(mapOf(Language.ARMENIAN to "Wisekins", Language.ENGLISH to "Wisekins", Language.RUSSIAN to "Wisekins"))
  val title_words = LocalizedString(mapOf(Language.ARMENIAN to "Բառեր", Language.ENGLISH to "Words", Language.RUSSIAN to "Слова"))
  val action_back = LocalizedString(mapOf(Language.ARMENIAN to "Հետ", Language.ENGLISH to "Back", Language.RUSSIAN to "Назад"))
  val action_pronounce = LocalizedString(mapOf(Language.ARMENIAN to "Արտասանել", Language.ENGLISH to "Pronounce", Language.RUSSIAN to "Произнести"))
  val pronunciation_unavailable = LocalizedString(mapOf(Language.ARMENIAN to "Այս լեզվի արտասանությունը շուտով կլինի 🔊", Language.ENGLISH to "Pronunciation for this language is coming soon 🔊", Language.RUSSIAN to "Произношение для этого языка скоро появится 🔊"))
  val home_tagline = LocalizedString(mapOf(Language.ARMENIAN to "Ի՞նչ ես ուզում անել", Language.ENGLISH to "What do you want to do?", Language.RUSSIAN to "Что хочешь сделать?"))
  val home_learn = LocalizedString(mapOf(Language.ARMENIAN to "Սովորել", Language.ENGLISH to "Learn", Language.RUSSIAN to "Учить"))
  val home_adventure = LocalizedString(mapOf(Language.ARMENIAN to "Արկած", Language.ENGLISH to "Adventure", Language.RUSSIAN to "Приключение"))
  val home_adventure_sub = LocalizedString(mapOf(Language.ARMENIAN to "Անցիր մակարդակները", Language.ENGLISH to "Play through the levels", Language.RUSSIAN to "Пройди уровни"))
  val levels_title = LocalizedString(mapOf(Language.ARMENIAN to "Ճանապարհ", Language.ENGLISH to "Adventure", Language.RUSSIAN to "Путь"))
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
  val account_about = LocalizedString(mapOf(Language.ARMENIAN to "Հավելվածի մասին", Language.ENGLISH to "About", Language.RUSSIAN to "О приложении"))
  val account_version = LocalizedString(mapOf(Language.ARMENIAN to "Տարբերակ", Language.ENGLISH to "Version", Language.RUSSIAN to "Версия"))

  val categoryTitles: Map<String, LocalizedString> = mapOf(
    "fruits" to LocalizedString(mapOf(Language.ARMENIAN to "Մրգեր", Language.ENGLISH to "Fruits", Language.RUSSIAN to "Фрукты", Language.SPANISH to "Frutas", Language.FRENCH to "Fruits", Language.GERMAN to "Obst")),
    "vegetables" to LocalizedString(mapOf(Language.ARMENIAN to "Բանջարեղեն", Language.ENGLISH to "Vegetables", Language.RUSSIAN to "Овощи", Language.SPANISH to "Verduras", Language.FRENCH to "Légumes", Language.GERMAN to "Gemüse")),
    "animals" to LocalizedString(mapOf(Language.ARMENIAN to "Կենդանիներ", Language.ENGLISH to "Animals", Language.RUSSIAN to "Животные", Language.SPANISH to "Animales", Language.FRENCH to "Animaux", Language.GERMAN to "Tiere")),
    "birds" to LocalizedString(mapOf(Language.ARMENIAN to "Թռչուններ", Language.ENGLISH to "Birds", Language.RUSSIAN to "Птицы", Language.SPANISH to "Aves", Language.FRENCH to "Oiseaux", Language.GERMAN to "Vögel")),
    "sea" to LocalizedString(mapOf(Language.ARMENIAN to "Ծովային կենդանիներ", Language.ENGLISH to "Sea animals", Language.RUSSIAN to "Морские животные", Language.SPANISH to "Animales marinos", Language.FRENCH to "Animaux marins", Language.GERMAN to "Meerestiere")),
    "insects" to LocalizedString(mapOf(Language.ARMENIAN to "Միջատներ", Language.ENGLISH to "Insects", Language.RUSSIAN to "Насекомые", Language.SPANISH to "Insectos", Language.FRENCH to "Insectes", Language.GERMAN to "Insekten")),
    "colors" to LocalizedString(mapOf(Language.ARMENIAN to "Գույներ", Language.ENGLISH to "Colors", Language.RUSSIAN to "Цвета", Language.SPANISH to "Colores", Language.FRENCH to "Couleurs", Language.GERMAN to "Farben")),
    "food" to LocalizedString(mapOf(Language.ARMENIAN to "Ուտելիք", Language.ENGLISH to "Food", Language.RUSSIAN to "Еда", Language.SPANISH to "Comida", Language.FRENCH to "Nourriture", Language.GERMAN to "Essen")),
    "meals" to LocalizedString(mapOf(Language.ARMENIAN to "Ուտեստներ", Language.ENGLISH to "Meals", Language.RUSSIAN to "Блюда", Language.SPANISH to "Comidas", Language.FRENCH to "Repas", Language.GERMAN to "Mahlzeiten")),
    "sweets" to LocalizedString(mapOf(Language.ARMENIAN to "Քաղցրավենիք", Language.ENGLISH to "Sweets", Language.RUSSIAN to "Сладости", Language.SPANISH to "Dulces", Language.FRENCH to "Bonbons", Language.GERMAN to "Süßigkeiten")),
    "drinks" to LocalizedString(mapOf(Language.ARMENIAN to "Ըմպելիքներ", Language.ENGLISH to "Drinks", Language.RUSSIAN to "Напитки", Language.SPANISH to "Bebidas", Language.FRENCH to "Boissons", Language.GERMAN to "Getränke")),
    "body" to LocalizedString(mapOf(Language.ARMENIAN to "Մարմին", Language.ENGLISH to "Body", Language.RUSSIAN to "Тело", Language.SPANISH to "Cuerpo", Language.FRENCH to "Corps", Language.GERMAN to "Körper")),
    "people" to LocalizedString(mapOf(Language.ARMENIAN to "Մարդիկ", Language.ENGLISH to "People", Language.RUSSIAN to "Люди", Language.SPANISH to "Personas", Language.FRENCH to "Gens", Language.GERMAN to "Menschen")),
    "jobs" to LocalizedString(mapOf(Language.ARMENIAN to "Մասնագիտություններ", Language.ENGLISH to "Jobs", Language.RUSSIAN to "Профессии", Language.SPANISH to "Profesiones", Language.FRENCH to "Métiers", Language.GERMAN to "Berufe")),
    "clothes" to LocalizedString(mapOf(Language.ARMENIAN to "Հագուստ", Language.ENGLISH to "Clothes", Language.RUSSIAN to "Одежда", Language.SPANISH to "Ropa", Language.FRENCH to "Vêtements", Language.GERMAN to "Kleidung")),
    "home" to LocalizedString(mapOf(Language.ARMENIAN to "Տուն", Language.ENGLISH to "Home", Language.RUSSIAN to "Дом", Language.SPANISH to "Casa", Language.FRENCH to "Maison", Language.GERMAN to "Zuhause")),
    "kitchen" to LocalizedString(mapOf(Language.ARMENIAN to "Խոհանոց", Language.ENGLISH to "Kitchen", Language.RUSSIAN to "Кухня", Language.SPANISH to "Cocina", Language.FRENCH to "Cuisine", Language.GERMAN to "Küche")),
    "tools" to LocalizedString(mapOf(Language.ARMENIAN to "Գործիքներ", Language.ENGLISH to "Tools", Language.RUSSIAN to "Инструменты", Language.SPANISH to "Herramientas", Language.FRENCH to "Outils", Language.GERMAN to "Werkzeuge")),
    "school" to LocalizedString(mapOf(Language.ARMENIAN to "Դպրոց", Language.ENGLISH to "School", Language.RUSSIAN to "Школа", Language.SPANISH to "Escuela", Language.FRENCH to "École", Language.GERMAN to "Schule")),
    "numbers" to LocalizedString(mapOf(Language.ARMENIAN to "Թվեր", Language.ENGLISH to "Numbers", Language.RUSSIAN to "Числа", Language.SPANISH to "Números", Language.FRENCH to "Nombres", Language.GERMAN to "Zahlen")),
    "vehicles" to LocalizedString(mapOf(Language.ARMENIAN to "Տրանսպորտ", Language.ENGLISH to "Vehicles", Language.RUSSIAN to "Транспорт", Language.SPANISH to "Vehículos", Language.FRENCH to "Véhicules", Language.GERMAN to "Fahrzeuge")),
    "places" to LocalizedString(mapOf(Language.ARMENIAN to "Վայրեր", Language.ENGLISH to "Places", Language.RUSSIAN to "Места", Language.SPANISH to "Lugares", Language.FRENCH to "Lieux", Language.GERMAN to "Orte")),
    "nature" to LocalizedString(mapOf(Language.ARMENIAN to "Բնություն", Language.ENGLISH to "Nature", Language.RUSSIAN to "Природа", Language.SPANISH to "Naturaleza", Language.FRENCH to "Nature", Language.GERMAN to "Natur")),
    "weather" to LocalizedString(mapOf(Language.ARMENIAN to "Եղանակ", Language.ENGLISH to "Weather", Language.RUSSIAN to "Погода", Language.SPANISH to "Clima", Language.FRENCH to "Météo", Language.GERMAN to "Wetter")),
    "space" to LocalizedString(mapOf(Language.ARMENIAN to "Տիեզերք", Language.ENGLISH to "Space", Language.RUSSIAN to "Космос", Language.SPANISH to "Espacio", Language.FRENCH to "Espace", Language.GERMAN to "Weltraum")),
    "sports" to LocalizedString(mapOf(Language.ARMENIAN to "Սպորտ", Language.ENGLISH to "Sports", Language.RUSSIAN to "Спорт", Language.SPANISH to "Deportes", Language.FRENCH to "Sports", Language.GERMAN to "Sport")),
    "music" to LocalizedString(mapOf(Language.ARMENIAN to "Երաժշտություն", Language.ENGLISH to "Music", Language.RUSSIAN to "Музыка", Language.SPANISH to "Música", Language.FRENCH to "Musique", Language.GERMAN to "Musik")),
    "toys" to LocalizedString(mapOf(Language.ARMENIAN to "Խաղալիքներ", Language.ENGLISH to "Toys", Language.RUSSIAN to "Игрушки", Language.SPANISH to "Juguetes", Language.FRENCH to "Jouets", Language.GERMAN to "Spielzeug")),
  )
  val action_save = LocalizedString(mapOf(Language.ARMENIAN to "Պահպանել", Language.ENGLISH to "Save", Language.RUSSIAN to "Сохранить"))
  val action_cancel = LocalizedString(mapOf(Language.ARMENIAN to "Չեղարկել", Language.ENGLISH to "Cancel", Language.RUSSIAN to "Отмена"))
  val language_native_title = LocalizedString(mapOf(Language.ARMENIAN to "Ո՞ր լեզվով ես խոսում", Language.ENGLISH to "Which language do you speak?", Language.RUSSIAN to "На каком языке ты говоришь?"))
  val language_target_title = LocalizedString(mapOf(Language.ARMENIAN to "Ի՞նչ ես ուզում սովորել", Language.ENGLISH to "What do you want to learn?", Language.RUSSIAN to "Что хочешь выучить?"))
  val account_my_language = LocalizedString(mapOf(Language.ARMENIAN to "Ես խոսում եմ", Language.ENGLISH to "I speak", Language.RUSSIAN to "Я говорю"))
  val account_rename = LocalizedString(mapOf(Language.ARMENIAN to "Վերանվանել", Language.ENGLISH to "Rename", Language.RUSSIAN to "Переименовать"))
  val account_delete = LocalizedString(mapOf(Language.ARMENIAN to "Ջնջել", Language.ENGLISH to "Delete", Language.RUSSIAN to "Удалить"))
  val account_delete_confirm = LocalizedString(mapOf(Language.ARMENIAN to "Ջնջե՞լ այս երեխային և ամբողջ առաջընթացը", Language.ENGLISH to "Delete this child and all their progress?", Language.RUSSIAN to "Удалить этого ребёнка и весь его прогресс?"))
  val account_edit = LocalizedString(mapOf(Language.ARMENIAN to "Խմբագրել պրոֆիլը", Language.ENGLISH to "Edit profile", Language.RUSSIAN to "Изменить профиль"))
  val home_daily_goal = LocalizedString(mapOf(Language.ARMENIAN to "Օրվա նպատակ", Language.ENGLISH to "Daily goal", Language.RUSSIAN to "Цель дня"))
  val home_goal_done = LocalizedString(mapOf(Language.ARMENIAN to "Նպատակը կատարված է! 🎉", Language.ENGLISH to "Goal reached! 🎉", Language.RUSSIAN to "Цель достигнута! 🎉"))
  val memory_title = LocalizedString(mapOf(Language.ARMENIAN to "Զույգեր", Language.ENGLISH to "Match", Language.RUSSIAN to "Пары"))
  val memory_won = LocalizedString(mapOf(Language.ARMENIAN to "Դու գտար բոլոր զույգերը! 🎉", Language.ENGLISH to "You found every pair! 🎉", Language.RUSSIAN to "Ты нашёл все пары! 🎉"))
  val games_title = LocalizedString(mapOf(Language.ARMENIAN to "Խաղեր", Language.ENGLISH to "Games", Language.RUSSIAN to "Игры"))
  val avatars_title = LocalizedString(mapOf(Language.ARMENIAN to "Կերպարներ", Language.ENGLISH to "My Avatars", Language.RUSSIAN to "Аватары"))
  val odd_title = LocalizedString(mapOf(Language.ARMENIAN to "Գտիր ավելորդը", Language.ENGLISH to "Odd One Out", Language.RUSSIAN to "Найди лишнее"))
  val odd_prompt = LocalizedString(mapOf(Language.ARMENIAN to "Ո՞րն է ավելորդը", Language.ENGLISH to "Which one is different?", Language.RUSSIAN to "Что здесь лишнее?"))
  val bubble_title = LocalizedString(mapOf(Language.ARMENIAN to "Փուչիկներ", Language.ENGLISH to "Bubble Pop", Language.RUSSIAN to "Пузырьки"))
  val memory_easy = LocalizedString(mapOf(Language.ARMENIAN to "Հեշտ", Language.ENGLISH to "Easy", Language.RUSSIAN to "Легко"))
  val memory_medium = LocalizedString(mapOf(Language.ARMENIAN to "Միջին", Language.ENGLISH to "Medium", Language.RUSSIAN to "Средне"))
  val memory_hard = LocalizedString(mapOf(Language.ARMENIAN to "Դժվար", Language.ENGLISH to "Hard", Language.RUSSIAN to "Сложно"))
  val memory_again = LocalizedString(mapOf(Language.ARMENIAN to "Նորից", Language.ENGLISH to "Play again", Language.RUSSIAN to "Ещё раз"))
}
