package com.compose.wonderlearn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.wonderBackground(): Modifier {
  val base = MaterialTheme.colorScheme.background
  return this.drawBehind {
    drawRect(base)
    fun blob(color: Color, cx: Float, cy: Float, alpha: Float, spread: Float) {
      val radius = size.width * spread
      val center = Offset(size.width * cx, size.height * cy)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(color.copy(alpha = alpha), Color.Transparent),
          center = center,
          radius = radius,
        ),
        radius = radius,
        center = center,
      )
    }
    blob(Grape, cx = 0.02f, cy = 0.0f, alpha = 0.42f, spread = 0.75f)
    blob(Sunny, cx = 1.0f, cy = 0.05f, alpha = 0.45f, spread = 0.72f)
    blob(Teal, cx = 0.98f, cy = 0.52f, alpha = 0.38f, spread = 0.7f)
    blob(Coral, cx = 0.0f, cy = 0.88f, alpha = 0.4f, spread = 0.72f)
    blob(Sky, cx = 1.0f, cy = 1.0f, alpha = 0.36f, spread = 0.7f)
  }
}
