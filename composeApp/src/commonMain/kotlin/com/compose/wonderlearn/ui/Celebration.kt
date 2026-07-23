package com.compose.wonderlearn.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Confetto(
  val startX: Float,
  val drift: Float,
  val fall: Float,
  val size: Float,
  val spin: Float,
  val color: Color,
)

private val confettiColors = listOf(
  Color(0xFF4FC3F7),
  Color(0xFFFF8A80),
  Color(0xFFFFD54F),
  Color(0xFFBA68C8),
  Color(0xFF81C784),
)

/**
 * A one-shot burst of confetti that plays whenever [visible] becomes true, then clears itself.
 * Purely decorative and non-interactive, so it can be laid over any screen.
 */
@Composable
fun ConfettiBurst(
  visible: Boolean,
  modifier: Modifier = Modifier,
  particleCount: Int = 44,
  durationMillis: Int = 1500,
) {
  var playing by remember { mutableStateOf(false) }
  val progress = remember { Animatable(0f) }

  LaunchedEffect(visible) {
    if (visible) {
      playing = true
      progress.snapTo(0f)
      progress.animateTo(1f, tween(durationMillis, easing = LinearEasing))
      playing = false
    }
  }

  if (!playing) return

  val pieces = remember {
    List(particleCount) {
      Confetto(
        startX = Random.nextFloat(),
        drift = Random.nextFloat() * 0.5f - 0.25f,
        fall = 0.7f + Random.nextFloat() * 0.5f,
        size = 14f + Random.nextFloat() * 16f,
        spin = Random.nextFloat() * 6f - 3f,
        color = confettiColors[Random.nextInt(confettiColors.size)],
      )
    }
  }

  Canvas(modifier) {
    val t = progress.value
    val alpha = (1f - t * t).coerceIn(0f, 1f)
    pieces.forEach { p ->
      val x = (p.startX + p.drift * t) * size.width
      val y = -40f + (p.fall * t) * (size.height + 80f)
      rotate(degrees = p.spin * t * 360f, pivot = Offset(x, y)) {
        drawRect(
          color = p.color.copy(alpha = alpha),
          topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
          size = Size(p.size, p.size * 0.6f),
        )
      }
    }
  }
}
