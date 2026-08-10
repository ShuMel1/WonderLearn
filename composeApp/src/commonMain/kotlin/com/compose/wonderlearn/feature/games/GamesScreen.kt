package com.compose.wonderlearn.feature.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
  onBack: () -> Unit,
  onMemoryMatch: () -> Unit,
  onOddOneOut: () -> Unit,
  onBubblePop: () -> Unit,
) {
  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.games_title(), onBack = onBack) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      GameCard("🧩", AppStrings.memory_title(), Grape, onMemoryMatch)
      GameCard("🔍", AppStrings.odd_title(), Teal, onOddOneOut)
      GameCard("🫧", AppStrings.bubble_title(), Sky, onBubblePop)
    }
  }
}

@Composable
private fun GameCard(emoji: String, label: String, color: Color, onClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = color),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 44.sp) }
      Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
  }
}
