package com.compose.wonderlearn.ui

import androidx.compose.ui.graphics.Color
import com.compose.wonderlearn.ui.theme.Bubblegum
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import com.compose.wonderlearn.ui.theme.Teal

private val Wheel = listOf(Coral, Teal, Grape, Sky, Sunny, Bubblegum)

private val ByCategory = mapOf(
  "fruits" to Coral,
  "animals" to Teal,
  "colors" to Sunny,
  "food" to Grape,
  "vehicles" to Sky,
  "nature" to Bubblegum,
)

fun colorForCategory(categoryId: String): Color =
  ByCategory[categoryId] ?: Wheel[(categoryId.hashCode() and Int.MAX_VALUE) % Wheel.size]

fun colorForIndex(index: Int): Color = Wheel[index % Wheel.size]

fun onColorFor(background: Color): Color =
  if (background == Sunny) Color(0xFF33304A) else Color.White
