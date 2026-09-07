package com.compose.wonderlearn.feature.levels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.adventure_map_bg
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Teal
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

// Fractional (x, y) centers of the 12 stepping-stone markers baked into adventure_map_bg.png,
// measured directly against the 1254x1254 source image — index 0 is the bottom-left starting
// stone, index 11 is the stone beside the cottage at the top. Scale-independent: multiply by the
// rendered square's side to get the on-screen position regardless of device size.
private val MARKER_FRACTIONS = listOf(
  Offset(0.1595f, 0.9370f),
  Offset(0.4347f, 0.8293f),
  Offset(0.7098f, 0.8055f),
  Offset(0.6898f, 0.6740f),
  Offset(0.3908f, 0.6300f),
  Offset(0.4347f, 0.5144f),
  Offset(0.7775f, 0.4506f),
  Offset(0.5901f, 0.3549f),
  Offset(0.3190f, 0.2990f),
  Offset(0.5144f, 0.2352f),
  Offset(0.7098f, 0.1874f),
  Offset(0.5861f, 0.1316f),
)

private fun emojiFor(kind: LevelKind): String = when (kind) {
  LevelKind.LEARN -> "📚"
  LevelKind.MEMORY -> "🧩"
  LevelKind.BUBBLE_POP -> "🫧"
  LevelKind.ODD_ONE_OUT -> "🔍"
}

private fun colorFor(kind: LevelKind): Color = when (kind) {
  LevelKind.LEARN -> Sky
  LevelKind.MEMORY -> Grape
  LevelKind.BUBBLE_POP -> Teal
  LevelKind.ODD_ONE_OUT -> Coral
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
  onPlay: (LevelDef) -> Unit,
  onBack: () -> Unit,
  viewModel: LevelsViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val justCompleted by viewModel.justCompleted.collectAsStateWithLifecycle()
  val rewardEarned by viewModel.rewardEarned.collectAsStateWithLifecycle()
  val doneCount = state.nodes.count { it.status == LevelStatus.DONE }
  // The stone art only has 12 slots. Today's pool is expected to be exactly 12 (see
  // dailyAdventureLevels's default count = pool.size), but this clamps defensively rather than
  // crashing or overrunning the art if that ever changes.
  val mappedNodes = state.nodes.take(MARKER_FRACTIONS.size)

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = { WonderTopBar(title = AppStrings.today_adventure_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          "⭐ $doneCount / ${state.nodes.size}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (rewardEarned) {
          Text(
            AppStrings.today_adventure_complete(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        AdventureMap(
          nodes = mappedNodes,
          onPlay = { def ->
            viewModel.onStart(def)
            onPlay(def)
          },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }

    ConfettiBurst(
      visible = justCompleted != null,
      modifier = Modifier.fillMaxSize(),
    )
  }

  LaunchedEffect(justCompleted) {
    if (justCompleted != null) {
      kotlinx.coroutines.delay(1800)
      viewModel.clearCompleted()
    }
  }
}

@Composable
private fun AdventureMap(
  nodes: List<LevelNode>,
  onPlay: (LevelDef) -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.TopCenter) {
    val squareSize = minOf(maxWidth, maxHeight)
    val nodeSize = squareSize * 0.12f

    // Which stone the car sits on: the first CURRENT node, or the last one once everything's
    // done. Snaps there on first composition (no intro slide from the start of the path); every
    // change after that — a level just finished, the next stone becomes CURRENT — eases over.
    val targetIndex = nodes.indexOfFirst { it.status == LevelStatus.CURRENT }
      .let { if (it >= 0) it else nodes.size - 1 }
      .coerceIn(0, MARKER_FRACTIONS.size - 1)
    val carFractionX = remember { Animatable(MARKER_FRACTIONS[targetIndex].x) }
    val carFractionY = remember { Animatable(MARKER_FRACTIONS[targetIndex].y) }
    var carInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(targetIndex) {
      val target = MARKER_FRACTIONS[targetIndex]
      if (!carInitialized) {
        carFractionX.snapTo(target.x)
        carFractionY.snapTo(target.y)
        carInitialized = true
      } else {
        val spec = tween<Float>(durationMillis = 700, easing = FastOutSlowInEasing)
        coroutineScope {
          launch { carFractionX.animateTo(target.x, spec) }
          launch { carFractionY.animateTo(target.y, spec) }
        }
      }
    }
    val carFraction = Offset(carFractionX.value, carFractionY.value)

    Box(modifier = Modifier.size(squareSize)) {
      Image(
        painter = painterResource(Res.drawable.adventure_map_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().clip(MapShape),
      )

      nodes.forEachIndexed { index, node ->
        val fraction = MARKER_FRACTIONS[index]
        LevelNodeButton(
          node = node,
          size = nodeSize,
          modifier = Modifier.markerOffset(fraction, squareSize, nodeSize),
          onClick = { onPlay(node.def) },
        )
        if (index == MARKER_FRACTIONS.lastIndex) {
          Text(
            "🏆",
            fontSize = (nodeSize.value * 0.4f).sp,
            modifier = Modifier.markerOffset(
              Offset(fraction.x + 0.09f, fraction.y - 0.09f),
              squareSize,
              nodeSize * 0.6f,
            ),
          )
        }
      }

      Text(
        "🚗",
        fontSize = (nodeSize.value * 0.55f).sp,
        modifier = Modifier.markerOffset(carFraction, squareSize, nodeSize * 0.8f),
      )
    }
  }
}

private val MapShape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)

/** Centers an [elementSize]-sized element on the point [fraction] describes, as a fraction of a
 * [squareSize]-sided square measured from that square's top-left corner. */
private fun Modifier.markerOffset(fraction: Offset, squareSize: Dp, elementSize: Dp): Modifier =
  this.offset(
    x = squareSize * fraction.x - elementSize / 2,
    y = squareSize * fraction.y - elementSize / 2,
  )

@Composable
private fun LevelNodeButton(
  node: LevelNode,
  size: Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val locked = node.status == LevelStatus.LOCKED
  val done = node.status == LevelStatus.DONE
  val fill = colorFor(node.def.kind)
  val badgeSize = size * 0.36f

  Box(modifier = modifier.size(size), contentAlignment = Alignment.TopEnd) {
    Box(
      modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(if (locked) MaterialTheme.colorScheme.surfaceVariant else fill.copy(alpha = if (done) 1f else 0.95f))
        .then(if (locked) Modifier else Modifier.clickable(onClick = onClick)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        if (locked) "🔒" else emojiFor(node.def.kind),
        fontSize = (size.value * 0.44f).sp,
      )
    }
    if (done) {
      Box(
        modifier = Modifier
          .size(badgeSize)
          .clip(CircleShape)
          .background(Color.White),
        contentAlignment = Alignment.Center,
      ) {
        Text("⭐", fontSize = (badgeSize.value * 0.6f).sp)
      }
    }
  }
}
