package com.compose.wonderlearn.feature.oddoneout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.feature.levels.LevelProgressBar
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import org.koin.compose.viewmodel.koinViewModel

private val CorrectGreen = Color(0xFF35C46A)
private val WrongRed = Color(0xFFED5757)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OddOneOutScreen(
  onBack: () -> Unit,
  viewModel: OddOneOutViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { WonderTopBar(title = AppStrings.odd_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        LevelProgressBar()
        Text(
          AppStrings.odd_prompt(),
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Column(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          state.options.chunked(2).forEach { rowItems ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              rowItems.forEach { item ->
                OddTile(
                  item = item,
                  revealed = state.solved && item.id == state.oddId,
                  wrong = item.id == state.wrongId,
                  enabled = !state.solved,
                  onClick = { viewModel.onSelect(item) },
                  modifier = Modifier.weight(1f),
                )
              }
            }
          }
        }
      }
    }
    ConfettiBurst(visible = state.solved, modifier = Modifier.fillMaxSize())
  }
}

@Composable
private fun OddTile(
  item: VocabularyItem,
  revealed: Boolean,
  wrong: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val border = when {
    revealed -> BorderStroke(4.dp, CorrectGreen)
    wrong -> BorderStroke(4.dp, WrongRed)
    else -> null
  }
  val shake by animateFloatAsState(
    targetValue = if (wrong) 1f else 0f,
    animationSpec = tween(durationMillis = 120),
    label = "oddWrong",
  )
  Card(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(24.dp),
    border = border,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .graphicsLayer { scaleX = 1f - 0.04f * shake; scaleY = 1f - 0.04f * shake },
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      WordImage(
        imageRef = item.imageRef,
        emoji = item.emoji,
        emojiSize = 56.sp,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().padding(16.dp),
      )
    }
  }
}
