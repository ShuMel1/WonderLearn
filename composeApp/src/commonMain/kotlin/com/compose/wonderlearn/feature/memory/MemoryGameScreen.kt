package com.compose.wonderlearn.feature.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.compose.wonderlearn.ui.theme.BrandPrimary
import com.compose.wonderlearn.ui.theme.Sky
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
  onBack: () -> Unit,
  fromLevel: Boolean = false,
  size: Int = -1,
  viewModel: MemoryGameViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(fromLevel, size) {
    if (fromLevel && size in Difficulty.entries.indices) {
      viewModel.newGame(Difficulty.entries[size])
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = { WonderTopBar(title = AppStrings.memory_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (!fromLevel) {
          DifficultySelector(
            selected = state.difficulty,
            onSelect = { viewModel.newGame(it) },
          )
        }
        BoxWithConstraints(
          modifier = Modifier.fillMaxWidth().weight(1f),
          contentAlignment = Alignment.Center,
        ) {
          val cols = state.columns
          val rowCount = (state.cards.size + cols - 1) / cols
          if (rowCount > 0) {
            val gap = 6.dp
            val cell = minOf(
              (maxWidth - gap * (cols - 1)) / cols,
              (maxHeight - gap * (rowCount - 1)) / rowCount,
            )
            Column(
              verticalArrangement = Arrangement.spacedBy(gap),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              state.cards.chunked(cols).forEach { rowCards ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                  rowCards.forEach { card ->
                    MemoryCardTile(
                      card = card,
                      onClick = { viewModel.onCardClick(card.cardId) },
                      modifier = Modifier.size(cell),
                    )
                  }
                }
              }
            }
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
private fun DifficultySelector(
  selected: Difficulty,
  onSelect: (Difficulty) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Difficulty.entries.forEach { difficulty ->
      val active = difficulty == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(50))
          .background(if (active) BrandPrimary else MaterialTheme.colorScheme.surface)
          .clickable { onSelect(difficulty) }
          .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          difficulty.label(),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}

@Composable
private fun Difficulty.label(): String = when (this) {
  Difficulty.EASY -> AppStrings.memory_easy()
  Difficulty.MEDIUM -> AppStrings.memory_medium()
  Difficulty.HARD -> AppStrings.memory_hard()
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
      .background(if (rotation > 90f) MaterialTheme.colorScheme.surface else BrandPrimary)
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
      Box(
        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
      ) {
        OwlMark(modifier = Modifier.fillMaxSize(0.5f))
      }
    }
  }
}

@Composable
private fun OwlMark(modifier: Modifier = Modifier) {
  val color = Color.White.copy(alpha = 0.35f)
  Canvas(modifier = modifier.aspectRatio(1f)) {
    val w = size.width
    val cx = w / 2f
    val cy = size.height / 2f
    val eyeR = w * 0.18f
    val gap = eyeR * 1.15f
    val stroke = w * 0.05f
    val leftEye = Offset(cx - gap, cy)
    val rightEye = Offset(cx + gap, cy)

    fun ear(center: Offset, tipDx: Float) {
      val path = Path().apply {
        moveTo(center.x - eyeR * 0.6f, center.y - eyeR * 0.85f)
        lineTo(center.x + tipDx, center.y - eyeR * 2f)
        lineTo(center.x + eyeR * 0.6f, center.y - eyeR * 0.85f)
        close()
      }
      drawPath(path, color)
    }
    ear(leftEye, -eyeR * 0.3f)
    ear(rightEye, eyeR * 0.3f)

    drawCircle(color, eyeR, leftEye, style = Stroke(width = stroke))
    drawCircle(color, eyeR, rightEye, style = Stroke(width = stroke))
    drawCircle(color, eyeR * 0.32f, leftEye)
    drawCircle(color, eyeR * 0.32f, rightEye)

    val beak = Path().apply {
      moveTo(cx - eyeR * 0.35f, cy + eyeR * 0.7f)
      lineTo(cx + eyeR * 0.35f, cy + eyeR * 0.7f)
      lineTo(cx, cy + eyeR * 1.5f)
      close()
    }
    drawPath(beak, color)
  }
}
