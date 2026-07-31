package com.compose.wonderlearn.feature.bubblepop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.feature.levels.LevelProgressBar
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.theme.Bubblegum
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import com.compose.wonderlearn.ui.theme.Teal
import org.koin.compose.viewmodel.koinViewModel

private const val RISE_MS = 5000
private val BUBBLE_SIZE = 78.dp
private val bubbleColors = listOf(Sky, Coral, Sunny, Grape, Teal, Bubblegum)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblePopScreen(
  onBack: () -> Unit,
  viewModel: BubblePopViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val rise = remember { Animatable(0f) }

  LaunchedEffect(state.roundKey) {
    if (state.roundKey == 0) return@LaunchedEffect
    rise.snapTo(0f)
    rise.animateTo(1f, tween(durationMillis = RISE_MS, easing = LinearEasing))
    viewModel.onEscaped()
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = { WonderTopBar(title = AppStrings.bubble_title(), onBack = onBack) },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      LevelProgressBar()
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("⭐ ${state.score}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { viewModel.replay() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
          Text("🔊  ${state.targetText}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
      }

      BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
        val fieldWidth = maxWidth
        val fieldHeight = maxHeight
        state.bubbles.forEach { bubble ->
          if (bubble.id in state.poppedWrong) return@forEach
          val x = (fieldWidth * bubble.x - BUBBLE_SIZE / 2)
            .coerceIn(0.dp, fieldWidth - BUBBLE_SIZE)
          val y = (fieldHeight - BUBBLE_SIZE) - fieldHeight * rise.value
          val color = bubbleColors[(bubble.id) % bubbleColors.size]
          Box(
            modifier = Modifier
              .offset(x = x, y = y)
              .size(BUBBLE_SIZE)
              .clip(CircleShape)
              .background(color.copy(alpha = 0.85f))
              .clickable { viewModel.onPop(bubble) },
            contentAlignment = Alignment.Center,
          ) {
            WordImage(
              imageRef = bubble.item.imageRef,
              emoji = bubble.item.emoji,
              emojiSize = 34.sp,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
            )
          }
        }
      }
    }
  }
}
