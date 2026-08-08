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
    val radius = size.width * 0.6f
    fun blob(color: Color, cx: Float, cy: Float, alpha: Float) {
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
    blob(Grape, cx = 0.02f, cy = 0.0f, alpha = 0.14f)
    blob(Sunny, cx = 1.0f, cy = 0.04f, alpha = 0.16f)
    blob(Teal, cx = 0.98f, cy = 0.55f, alpha = 0.12f)
    blob(Coral, cx = 0.0f, cy = 0.9f, alpha = 0.12f)
    blob(Sky, cx = 1.0f, cy = 1.0f, alpha = 0.10f)
  }
}
