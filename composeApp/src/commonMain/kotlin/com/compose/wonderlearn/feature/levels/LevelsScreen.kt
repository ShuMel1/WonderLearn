package com.compose.wonderlearn.feature.levels

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.domain.GEMS_PER_DAILY_ADVENTURE
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
import kotlinx.coroutines.delay
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

  var diamondPosition by remember { mutableStateOf(Offset.Zero) }
  var showDiamondFlourish by remember { mutableStateOf(false) }
  // rewardEarned only flips true the moment claimReward() succeeds — all 12 levels done, claimed
  // exactly once — distinct from finishing an individual level (which only gets the small
  // per-level confetti + car-slide below, no flourish).
  LaunchedEffect(rewardEarned) {
    if (rewardEarned) showDiamondFlourish = true
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Full-bleed blurred copy of the same map art fills the space around the crisp square below
    // (the square doesn't cover the whole screen on most aspect ratios) so the app's default
    // pastel gradient never shows through on this screen — the "blurred album art" technique.
    // Scaled beyond a plain Crop so the blur's own softening never reveals a see-through edge.
    Image(
      painter = painterResource(Res.drawable.adventure_map_bg),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxSize()
        .graphicsLayer { scaleX = 1.15f; scaleY = 1.15f }
        .blur(40.dp),
    )
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
          onDiamondPositioned = { diamondPosition = it },
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }

    // Auto-clear timing lives in LevelsViewModel (survives navigating away mid-delay) — this just
    // reflects whatever justCompleted currently is.
    ConfettiBurst(
      visible = justCompleted != null,
      modifier = Modifier.fillMaxSize(),
    )

    if (showDiamondFlourish) {
      DiamondFlourishOverlay(
        startPosition = diamondPosition,
        onDismiss = { showDiamondFlourish = false },
      )
    }
  }
}

/**
 * Celebrates clearing every level for the day: the 💎 marker on the last stone flies to screen
 * center, grows, spins, and settles, then shows the Gems earned — the same fly/grow/spin
 * `Animatable` sequence as Home's check-in coin flight, reused rather than rebuilt. Auto-dismisses
 * a couple seconds after settling, or immediately on tap (also matching the coin overlay).
 */
@Composable
private fun DiamondFlourishOverlay(startPosition: Offset, onDismiss: () -> Unit) {
  var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .onGloballyPositioned { coordinates = it }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss,
      ),
  ) {
    val coords = coordinates
    if (coords != null) {
      val density = LocalDensity.current
      val smallPx = with(density) { 34.dp.toPx() }
      val largePx = with(density) { 140.dp.toPx() }
      val startCenter = startPosition - coords.positionInRoot()
      val bounds = coords.boundsInRoot()
      val endCenter = Offset(bounds.width / 2f, bounds.height / 2f)

      val flight = remember { Animatable(0f) }
      val spin = remember { Animatable(0f) }
      var settled by remember { mutableStateOf(false) }
      val shineSound = remember { AudioPlayer() }

      LaunchedEffect(Unit) {
        launch { runCatching { shineSound.play(Res.readBytes(SHINE_SOUND)) } }
        flight.animateTo(
          1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
        spin.animateTo(720f, animationSpec = tween(durationMillis = 850, easing = CubicBezierEasing(0.05f, 0.6f, 0.15f, 1f)))
        settled = true
        delay(2000)
        onDismiss()
      }

      val t = flight.value.coerceIn(0f, 1f)
      val cx = startCenter.x + (endCenter.x - startCenter.x) * t
      val cy = startCenter.y + (endCenter.y - startCenter.y) * t
      val sizePx = smallPx + (largePx - smallPx) * t
      val sizeDp = with(density) { sizePx.toDp() }

      Text(
        "💎",
        fontSize = (sizeDp.value * 0.75f).sp,
        modifier = Modifier
          .offset { IntOffset((cx - sizePx / 2f).toInt(), (cy - sizePx / 2f).toInt()) }
          .graphicsLayer { rotationY = spin.value; cameraDistance = 8f * density.density },
      )

      if (settled) {
        Text(
          "+$GEMS_PER_DAILY_ADVENTURE 💎",
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
          modifier = Modifier.align(Alignment.Center).offset(y = 90.dp),
        )
      }
    }
  }
}

private const val SHINE_SOUND = "files/sounds/shine.wav"

@Composable
private fun AdventureMap(
  nodes: List<LevelNode>,
  onPlay: (LevelDef) -> Unit,
  onDiamondPositioned: (Offset) -> Unit,
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
    var lastCarIndex by remember { mutableStateOf(targetIndex) }
    // The bare 🚗 glyph faces a fixed direction regardless of travel — mirror it horizontally
    // when the next stone is on the opposite side, so it visually turns to face where it's going
    // on this zigzag path. Small deltas (the path running nearly straight up) leave it as-is
    // rather than flip-flopping on noise.
    var carFacingRight by remember { mutableStateOf(true) }
    LaunchedEffect(targetIndex) {
      val target = MARKER_FRACTIONS[targetIndex]
      if (!carInitialized) {
        carFractionX.snapTo(target.x)
        carFractionY.snapTo(target.y)
        carInitialized = true
      } else {
        val dx = target.x - MARKER_FRACTIONS[lastCarIndex].x
        if (kotlin.math.abs(dx) > 0.02f) carFacingRight = dx > 0f
        val spec = tween<Float>(durationMillis = 700, easing = FastOutSlowInEasing)
        coroutineScope {
          launch { carFractionX.animateTo(target.x, spec) }
          launch { carFractionY.animateTo(target.y, spec) }
        }
      }
      lastCarIndex = targetIndex
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
            "💎",
            fontSize = (nodeSize.value * 0.4f).sp,
            modifier = Modifier
              .markerOffset(
                Offset(fraction.x + 0.09f, fraction.y - 0.09f),
                squareSize,
                nodeSize * 0.6f,
              )
              .onGloballyPositioned { onDiamondPositioned(it.boundsInRoot().center) },
          )
        }
      }

      Text(
        "🚗",
        fontSize = (nodeSize.value * 0.55f).sp,
        modifier = Modifier
          .markerOffset(carFraction, squareSize, nodeSize * 0.8f)
          .graphicsLayer { scaleX = if (carFacingRight) -1f else 1f },
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
      Box(modifier = Modifier.size(badgeSize), contentAlignment = Alignment.Center) {
        Text(
          "⭐",
          fontSize = (badgeSize.value * 0.9f).sp,
          style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.45f), blurRadius = 6f)),
        )
      }
    }
  }
}
