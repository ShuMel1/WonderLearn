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
    SeedCategory("vehicles", "Vehicles", "🚗", 4),
    SeedCategory("nature", "Nature", "🌳", 5),
  )

  val words = listOf(
    // Fruits
    SeedWord("apple", "fruits", "🍎", "Խնձոր", "Apple", "Яблоко"),
    SeedWord("banana", "fruits", "🍌", "Բանան", "Banana", "Банан"),
    SeedWord("orange", "fruits", "🍊", "Նարինջ", "Orange", "Апельсин"),
    SeedWord("grapes", "fruits", "🍇", "Խաղող", "Grapes", "Виноград"),
    SeedWord("strawberry", "fruits", "🍓", "Ելակ", "Strawberry", "Клубника"),
    SeedWord("watermelon", "fruits", "🍉", "Ձմերուկ", "Watermelon", "Арбуз"),
    SeedWord("pear", "fruits", "🍐", "Տանձ", "Pear", "Груша"),
    SeedWord("cherry", "fruits", "🍒", "Բալ", "Cherry", "Вишня"),
    SeedWord("lemon", "fruits", "🍋", "Կիտրոն", "Lemon", "Лимон"),
    // Animals
    SeedWord("dog", "animals", "🐶", "Շուն", "Dog", "Собака"),
    SeedWord("cat", "animals", "🐱", "Կատու", "Cat", "Кошка"),
    SeedWord("horse", "animals", "🐴", "Ձի", "Horse", "Лошадь"),
    SeedWord("lion", "animals", "🦁", "Առյուծ", "Lion", "Лев"),
    SeedWord("elephant", "animals", "🐘", "Փիղ", "Elephant", "Слон"),
    SeedWord("bear", "animals", "🐻", "Արջ", "Bear", "Медведь"),
    SeedWord("rabbit", "animals", "🐰", "Նապաստակ", "Rabbit", "Кролик"),
    SeedWord("fox", "animals", "🦊", "Աղվես", "Fox", "Лиса"),
    SeedWord("cow", "animals", "🐮", "Կով", "Cow", "Корова"),
    SeedWord("monkey", "animals", "🐵", "Կապիկ", "Monkey", "Обезьяна"),
    // Colors
    SeedWord("red", "colors", "🔴", "Կարմիր", "Red", "Красный"),
    SeedWord("blue", "colors", "🔵", "Կապույտ", "Blue", "Синий"),
    SeedWord("green", "colors", "🟢", "Կանաչ", "Green", "Зелёный"),
    SeedWord("yellow", "colors", "🟡", "Դեղին", "Yellow", "Жёлтый"),
    SeedWord("black", "colors", "⚫", "Սև", "Black", "Чёрный"),
    SeedWord("white", "colors", "⚪", "Սպիտակ", "White", "Белый"),
    SeedWord("orange_color", "colors", "🟠", "Նարնջագույն", "Orange", "Оранжевый"),
    SeedWord("purple", "colors", "🟣", "Մանուշակագույն", "Purple", "Фиолетовый"),
    // Food
    SeedWord("bread", "food", "🍞", "Հաց", "Bread", "Хлеб"),
    SeedWord("milk", "food", "🥛", "Կաթ", "Milk", "Молоко"),
    SeedWord("cheese", "food", "🧀", "Պանիր", "Cheese", "Сыр"),
    SeedWord("egg", "food", "🥚", "Ձու", "Egg", "Яйцо"),
    SeedWord("water", "food", "💧", "Ջուր", "Water", "Вода"),
    SeedWord("rice", "food", "🍚", "Բրինձ", "Rice", "Рис"),
    SeedWord("meat", "food", "🍖", "Միս", "Meat", "Мясо"),
    SeedWord("soup", "food", "🍲", "Ապուր", "Soup", "Суп"),
    SeedWord("honey", "food", "🍯", "Մեղր", "Honey", "Мёд"),
    // Vehicles
    SeedWord("car", "vehicles", "🚗", "Մեքենա", "Car", "Машина"),
    SeedWord("bus", "vehicles", "🚌", "Ավտոբուս", "Bus", "Автобус"),
    SeedWord("train", "vehicles", "🚆", "Գնացք", "Train", "Поезд"),
    SeedWord("plane", "vehicles", "✈️", "Ինքնաթիռ", "Plane", "Самолёт"),
    SeedWord("bicycle", "vehicles", "🚲", "Հեծանիվ", "Bicycle", "Велосипед"),
    SeedWord("boat", "vehicles", "🛶", "Նավակ", "Boat", "Лодка"),
    SeedWord("truck", "vehicles", "🚚", "Բեռնատար", "Truck", "Грузовик"),
    // Nature
    SeedWord("tree", "nature", "🌳", "Ծառ", "Tree", "Дерево"),
    SeedWord("flower", "nature", "🌸", "Ծաղիկ", "Flower", "Цветок"),
    SeedWord("sun", "nature", "☀️", "Արև", "Sun", "Солнце"),
    SeedWord("moon", "nature", "🌙", "Լուսին", "Moon", "Луна"),
    SeedWord("star", "nature", "⭐", "Աստղ", "Star", "Звезда"),
    SeedWord("rain", "nature", "🌧️", "Անձրև", "Rain", "Дождь"),
    SeedWord("mountain", "nature", "⛰️", "Սար", "Mountain", "Гора"),
    SeedWord("river", "nature", "🏞️", "Գետ", "River", "Река"),
  )
}
