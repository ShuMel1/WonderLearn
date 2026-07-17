package com.compose.wonderlearn.data

internal data class SeedCategory(
  val id: String,
  val title: String,
  val emoji: String,
  val sortIndex: Long,
)

internal data class SeedWord(
  val id: String,
  val categoryId: String,
  val emoji: String,
  val armenian: String,
  val english: String,
  val russian: String,
)

internal object SeedData {

  val categories = listOf(
    SeedCategory("fruits", "Fruits", "🍎", 0),
    SeedCategory("animals", "Animals", "🐶", 1),
    SeedCategory("colors", "Colors", "🎨", 2),
    SeedCategory("food", "Food", "🍞", 3),
  )

  val words = listOf(
    SeedWord("apple", "fruits", "🍎", "Խնձոր", "Apple", "Яблоко"),
    SeedWord("banana", "fruits", "🍌", "Բանան", "Banana", "Банан"),
    SeedWord("orange", "fruits", "🍊", "Նարինջ", "Orange", "Апельсин"),
    SeedWord("grapes", "fruits", "🍇", "Խաղող", "Grapes", "Виноград"),
    SeedWord("dog", "animals", "🐶", "Շուն", "Dog", "Собака"),
    SeedWord("cat", "animals", "🐱", "Կատու", "Cat", "Кошка"),
    SeedWord("horse", "animals", "🐴", "Ձի", "Horse", "Лошадь"),
    SeedWord("lion", "animals", "🦁", "Առյուծ", "Lion", "Лев"),
    SeedWord("red", "colors", "🔴", "Կարմիր", "Red", "Красный"),
    SeedWord("blue", "colors", "🔵", "Կապույտ", "Blue", "Синий"),
    SeedWord("green", "colors", "🟢", "Կանաչ", "Green", "Зелёный"),
    SeedWord("yellow", "colors", "🟡", "Դեղին", "Yellow", "Жёлтый"),
    SeedWord("bread", "food", "🍞", "Հաց", "Bread", "Хлеб"),
    SeedWord("milk", "food", "🥛", "Կաթ", "Milk", "Молоко"),
    SeedWord("cheese", "food", "🧀", "Պանիր", "Cheese", "Сыр"),
    SeedWord("egg", "food", "🥚", "Ձու", "Egg", "Яйцо"),
  )
}
