package com.compose.wonderlearn.feature.bubblepop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
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
import org.koin.core.parameter.parametersOf

/** Default pace — half the speed of the original 5s round, per the slow-by-default toggle. */
private const val SLOW_RISE_MS = 10000

/** "Original" pace from before the speed toggle existed; the fast option. */
private const val FAST_RISE_MS = 5000

/** The warning phase always covers the same ~1.5 real seconds, regardless of the chosen pace. */
private const val WARNING_MS = 1500

private val BUBBLE_SIZE = 78.dp
private val bubbleColors = listOf(Sky, Coral, Sunny, Grape, Teal, Bubblegum)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubblePopScreen(
  onBack: () -> Unit,
  fromLevel: Boolean = false,
  viewModel: BubblePopViewModel = koinViewModel { parametersOf(fromLevel) },
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val rise = remember { Animatable(0f) }
  val riseMs = if (state.fastMode) FAST_RISE_MS else SLOW_RISE_MS
  val warningFraction = 1f - (WARNING_MS.toFloat() / riseMs)

  LaunchedEffect(state.roundKey, state.fastMode) {
    if (state.roundKey == 0 || state.rewardPending) return@LaunchedEffect
    rise.snapTo(0f)
    rise.animateTo(1f, tween(durationMillis = riseMs, easing = LinearEasing))
    viewModel.onEscaped()
  }

  // Freezes the bubbles mid-air the moment the coin-streak reward triggers; claimStreakReward()
  // bumps roundKey again on dismiss, which restarts the LaunchedEffect above from a fresh round.
  LaunchedEffect(state.rewardPending) {
    if (state.rewardPending) rise.stop()
  }

  Box(modifier = Modifier.fillMaxSize()) {
  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.bubble_title(), onBack = onBack) },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      LevelProgressBar()
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("⭐ ${state.score}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
          if (!fromLevel) {
            CoinStreakMeter(streak = state.streak, modifier = Modifier.size(28.dp))
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          SpeedToggleButton(fastMode = state.fastMode, onClick = viewModel::toggleSpeed)
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
      }

      val timeLeft = 1f - rise.value
      val isWarning = rise.value >= warningFraction
      LinearProgressIndicator(
        progress = { timeLeft },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp)
          .height(8.dp)
          .clip(RoundedCornerShape(50)),
        color = if (isWarning) Coral else Sky,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )

      BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp)) {
        val fieldWidth = maxWidth
        val fieldHeight = maxHeight
        state.bubbles.forEach { bubble ->
          if (bubble.id in state.poppedWrong) return@forEach
          val x = (fieldWidth * bubble.x - BUBBLE_SIZE / 2)
            .coerceIn(0.dp, fieldWidth - BUBBLE_SIZE)
          val y = (fieldHeight - BUBBLE_SIZE) - fieldHeight * rise.value
          val baseColor = bubbleColors[(bubble.id) % bubbleColors.size]
          // In the warning phase, blend every bubble toward Coral so the impending miss reads
          // as urgency rather than a sudden, unexplained round reset.
          val color = if (isWarning) {
            val warningStrength = ((rise.value - warningFraction) / (1f - warningFraction)).coerceIn(0f, 1f)
            lerp(baseColor, Coral, warningStrength)
          } else {
            baseColor
          }
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

    if (!fromLevel) {
      AnimatedVisibility(
        visible = state.rewardPending,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { viewModel.claimStreakReward() },
          contentAlignment = Alignment.Center,
        ) {
          AnimatedVisibility(
            visible = state.rewardPending,
            enter = scaleIn(initialScale = 0.3f),
            exit = scaleOut(targetScale = 0.3f),
          ) {
            CoinStreakMeter(streak = COIN_STREAK_GOAL, modifier = Modifier.size(160.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun SpeedToggleButton(fastMode: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 8.dp),
  ) {
    Text(
      if (fastMode) "🐢" else "x2 ⏩",
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/**
 * A coin, its circular face divided into [COIN_STREAK_GOAL] stacked horizontal bands that fill
 * gold from the bottom up as [streak] rises — drawing everything (fill, dividers) inside a single
 * clip against the coin's own circle path turns plain full-width rectangles into correctly
 * chord-shaped bands for free.
 */
@Composable
private fun CoinStreakMeter(streak: Int, modifier: Modifier = Modifier) {
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
  Canvas(modifier = modifier) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val bandHeight = (radius * 2f) / COIN_STREAK_GOAL
    val circlePath = Path().apply {
      addOval(androidx.compose.ui.geometry.Rect(center = center, radius = radius))
    }
    clipPath(circlePath) {
      drawRect(color = trackColor, size = size)
      for (band in 0 until COIN_STREAK_GOAL) {
        if (streak > band) {
          val top = size.height - (band + 1) * bandHeight
          drawRect(color = Sunny, topLeft = Offset(0f, top), size = Size(size.width, bandHeight))
        }
      }
      for (band in 1 until COIN_STREAK_GOAL) {
        val y = size.height - band * bandHeight
        drawLine(color = outlineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.5f)
      }
    }
    drawCircle(color = outlineColor, radius = radius, center = center, style = Stroke(width = 3f))
  }
}
