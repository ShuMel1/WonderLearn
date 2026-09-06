package com.compose.wonderlearn.feature.bubblepop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.feature.levels.LevelProgressBar
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.owl_coin
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.theme.Bubblegum
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import com.compose.wonderlearn.ui.theme.Teal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val COIN_SOUND = "files/sounds/coin.wav"

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

  // Where the small meter sits on screen right now, in root coordinates — captured continuously
  // so the reward overlay knows where to fly the coin in from, however the layout is sized.
  var meterCenterInRoot by remember { mutableStateOf(Offset.Zero) }

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
            CoinStreakMeter(
              streak = state.streak,
              modifier = Modifier
                .size(28.dp)
                .onGloballyPositioned { meterCenterInRoot = it.boundsInRoot().center },
            )
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
        var scrimCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { viewModel.claimStreakReward() }
            .onGloballyPositioned { scrimCoordinates = it },
        ) {
          val coords = scrimCoordinates
          if (coords != null) {
            val density = LocalDensity.current
            val smallPx = with(density) { 28.dp.toPx() }
            val largePx = with(density) { 160.dp.toPx() }
            val startCenter = meterCenterInRoot - coords.positionInRoot()
            val bounds = coords.boundsInRoot()
            val endCenter = Offset(bounds.width / 2f, bounds.height / 2f)

            val flight = remember { Animatable(0f) }
            val spin = remember { Animatable(0f) }
            val coinSound = remember { AudioPlayer() }

            LaunchedEffect(state.rewardPending) {
              if (!state.rewardPending) return@LaunchedEffect
              launch { runCatching { coinSound.play(Res.readBytes(COIN_SOUND)) } }
              flight.snapTo(0f)
              spin.snapTo(0f)
              // Fly from the small meter to the center, growing as it goes …
              flight.animateTo(
                1f,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessLow,
                ),
              )
              // … then spin around its own axis and settle back facing forward (720° = 2 full
              // turns, landing exactly on the front face) with a decelerating "coming to rest" feel.
              spin.animateTo(
                720f,
                animationSpec = tween(durationMillis = 850, easing = CubicBezierEasing(0.05f, 0.6f, 0.15f, 1f)),
              )
            }

            val t = flight.value.coerceIn(0f, 1f)
            val cx = startCenter.x + (endCenter.x - startCenter.x) * t
            val cy = startCenter.y + (endCenter.y - startCenter.y) * t
            val coinSizePx = smallPx + (largePx - smallPx) * t

            Box(
              modifier = Modifier
                .offset { IntOffset((cx - coinSizePx / 2f).toInt(), (cy - coinSizePx / 2f).toInt()) }
                .size(with(density) { coinSizePx.toDp() })
                .graphicsLayer {
                  rotationY = spin.value
                  cameraDistance = 8f * density.density
                },
            ) {
              CoinStreakMeter(streak = COIN_STREAK_GOAL, modifier = Modifier.fillMaxSize())
            }
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
 * The owl-coin artwork, revealed in [COIN_STREAK_GOAL] stacked horizontal bands from the bottom up
 * as [streak] rises: a dim ghost of the coin sits underneath at all times, and each earned band
 * draws the same image again at full color clipped to just that band — real coin art with none of
 * the earlier hand-drawn circle math, since the image's own transparent background already gives
 * it its shape.
 */
@Composable
private fun CoinStreakMeter(streak: Int, modifier: Modifier = Modifier) {
  val coinPainter = painterResource(Res.drawable.owl_coin)
  Canvas(modifier = modifier) {
    val bandHeight = size.height / COIN_STREAK_GOAL
    with(coinPainter) {
      draw(size = size, alpha = 0.28f)
      for (band in 0 until COIN_STREAK_GOAL) {
        if (streak > band) {
          val top = size.height - (band + 1) * bandHeight
          clipRect(left = 0f, top = top, right = size.width, bottom = top + bandHeight) {
            draw(size = size)
          }
        }
      }
    }
  }
}
