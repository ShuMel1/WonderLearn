package com.compose.wonderlearn.data

import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.domain.Language.ARMENIAN
import com.compose.wonderlearn.domain.Language.ENGLISH
import com.compose.wonderlearn.domain.Language.RUSSIAN
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.domain.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryVocabularyRepository : VocabularyRepository {

  override fun categories(): Flow<List<Category>> = flowOf(SEED_CATEGORIES)

  override fun itemsForCategory(categoryId: String): Flow<List<VocabularyItem>> =
    flowOf(SEED_ITEMS.filter { it.categoryId == categoryId })

  override suspend fun item(id: String): VocabularyItem? =
    SEED_ITEMS.firstOrNull { it.id == id }
}

private val SEED_CATEGORIES = listOf(
  Category("fruits", "Fruits", "🍎"),
  Category("animals", "Animals", "🐶"),
  Category("colors", "Colors", "🎨"),
  Category("food", "Food", "🍞"),
)

private fun item(
  id: String,
  categoryId: String,
  emoji: String,
  armenian: String,
  english: String,
  russian: String,
) = VocabularyItem(
  id = id,
  categoryId = categoryId,
  emoji = emoji,
  translations = mapOf(ARMENIAN to armenian, ENGLISH to english, RUSSIAN to russian),
)

private val SEED_ITEMS = listOf(
  item("apple", "fruits", "🍎", "Խնձոր", "Apple", "Яблоко"),
  item("banana", "fruits", "🍌", "Բանան", "Banana", "Банан"),
  item("orange", "fruits", "🍊", "Նարինջ", "Orange", "Апельсин"),
  item("grapes", "fruits", "🍇", "Խաղող", "Grapes", "Виноград"),
  item("dog", "animals", "🐶", "Շուն", "Dog", "Собака"),
  item("cat", "animals", "🐱", "Կատու", "Cat", "Кошка"),
  item("horse", "animals", "🐴", "Ձի", "Horse", "Лошадь"),
  item("lion", "animals", "🦁", "Առյուծ", "Lion", "Лев"),
  item("red", "colors", "🔴", "Կարմիր", "Red", "Красный"),
  item("blue", "colors", "🔵", "Կապույտ", "Blue", "Синий"),
  item("green", "colors", "🟢", "Կանաչ", "Green", "Зелёный"),
  item("yellow", "colors", "🟡", "Դեղին", "Yellow", "Жёлтый"),
  item("bread", "food", "🍞", "Հաց", "Bread", "Хлеб"),
  item("milk", "food", "🥛", "Կաթ", "Milk", "Молоко"),
  item("cheese", "food", "🧀", "Պանիր", "Cheese", "Сыр"),
  item("egg", "food", "🥚", "Ձու", "Egg", "Яйцо"),
)
