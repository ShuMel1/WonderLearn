package com.compose.wonderlearn.feature.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.theme.Sky
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
  onBack: () -> Unit,
  viewModel: MemoryGameViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { WonderTopBar(title = AppStrings.memory_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        state.cards.chunked(3).forEach { rowCards ->
          Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            rowCards.forEach { card ->
              MemoryCardTile(
                card = card,
                onClick = { viewModel.onCardClick(card.cardId) },
                modifier = Modifier.weight(1f),
              )
            }
            repeat(3 - rowCards.size) { Box(Modifier.weight(1f)) }
          }
        }

        if (state.won) {
          Text(
            AppStrings.memory_won(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Sky,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Button(
            onClick = { viewModel.newGame() },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(56.dp),
          ) {
            Text("🔁  ${AppStrings.memory_again()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    ConfettiBurst(visible = state.won, modifier = Modifier.fillMaxSize())
  }
}

@Composable
private fun MemoryCardTile(
  card: MemoryCard,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val faceUp = card.revealed || card.matched
  val rotation by animateFloatAsState(
    targetValue = if (faceUp) 180f else 0f,
    animationSpec = tween(durationMillis = 400),
    label = "cardFlip",
  )
  val density = LocalDensity.current.density
  Box(
    modifier = modifier
      .aspectRatio(1f)
      .graphicsLayer {
        rotationY = rotation
        cameraDistance = 12f * density
      }
      .clip(RoundedCornerShape(20.dp))
      .background(if (rotation > 90f) MaterialTheme.colorScheme.surface else Sky)
      .alpha(if (card.matched) 0.55f else 1f)
      .clickable(enabled = !faceUp, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    if (rotation > 90f) {
      WordImage(
        imageRef = card.imageRef,
        emoji = card.emoji,
        emojiSize = 44.sp,
        contentDescription = null,
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp)
          .graphicsLayer { rotationY = 180f },
      )
    } else {
      Text("❓", fontSize = 36.sp)
    }
  }
}
