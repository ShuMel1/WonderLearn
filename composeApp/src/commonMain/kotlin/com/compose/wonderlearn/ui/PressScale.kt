package com.compose.wonderlearn.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role

/**
 * A tappable's standard feedback across the app: a small scale-down while pressed, springing back
 * on release — the one bit of motion every primary button/tile/chip shares, so tapping around the
 * app reads as responsive rather than static. [role] mirrors [Modifier.clickable]'s own parameter
 * (e.g. [Role.Button] for a button-like tap target, left null for a plain tile).
 */
fun Modifier.pressScale(
  role: Role? = null,
  enabled: Boolean = true,
  onClick: () -> Unit,
): Modifier = composed {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (pressed) 0.94f else 1f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
    label = "pressScale",
  )
  this
    .graphicsLayer { scaleX = scale; scaleY = scale }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      enabled = enabled,
      role = role,
      onClick = onClick,
    )
}
