package com.compose.wonderlearn.feature.levels

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.GameSize
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.jungle_leaf_1
import com.compose.wonderlearn.resources.jungle_leaf_2
import com.compose.wonderlearn.resources.jungle_leaf_3
import com.compose.wonderlearn.resources.jungle_tree
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Teal
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val NODE_SIZE = 78.dp
private val BIAS = listOf(0f, 0.62f, 0f, -0.62f)

// Jungle-toned backdrop for this screen only — layered on top of the shared app background,
// which every other screen keeps unchanged.
private val JungleLight = Color(0xFFD8F3DC)
private val JungleMid = Color(0xFF52B788)
private val JungleDeep = Color(0xFF1B4332)

private val PATH_FOLIAGE: List<DrawableResource> =
  listOf(Res.drawable.jungle_leaf_1, Res.drawable.jungle_leaf_2, Res.drawable.jungle_leaf_3)

// Purely decorative — turns the path into a little jungle. Critters are sparse (every 4th node)
// so spotting one still feels like something; foliage is dense (every node) to fill the margins.
// The leaves/tree are procedurally drawn assets (composeResources/drawable/jungle_*.png) rather
// than plain emoji or a scraped "free" asset pack of unknown license.
private val PATH_CRITTERS = listOf("🐒", "🦜", "🐍")

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

@Composable
private fun badgeFor(def: LevelDef): String =
  if (def.kind == LevelKind.MEMORY) {
    when (def.size) {
      GameSize.EASY -> AppStrings.memory_easy()
      GameSize.MEDIUM -> AppStrings.memory_medium()
      GameSize.HARD -> AppStrings.memory_hard()
      null -> ""
    }
  } else {
    "✅ ${def.answersToWin}"
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(JungleLight, JungleMid, JungleDeep))),
  ) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = { WonderTopBar(title = AppStrings.today_adventure_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Row(
          modifier = Modifier.align(Alignment.CenterHorizontally),
          horizontalArrangement = Arrangement.spacedBy((-10).dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Image(painterResource(Res.drawable.jungle_tree), null, Modifier.size(56.dp))
          Image(painterResource(Res.drawable.jungle_leaf_2), null, Modifier.size(40.dp))
          Image(painterResource(Res.drawable.jungle_leaf_1), null, Modifier.size(44.dp))
          Image(painterResource(Res.drawable.jungle_leaf_3), null, Modifier.size(40.dp))
          Image(painterResource(Res.drawable.jungle_tree), null, Modifier.size(56.dp))
        }
        Text(
          "⭐ $doneCount / ${state.nodes.size}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        if (rewardEarned) {
          Text(
            AppStrings.today_adventure_complete(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
          )
        }
        state.nodes.forEachIndexed { index, node ->
          val isLast = index == state.nodes.lastIndex
          val nodeBias = BIAS[node.def.index % BIAS.size]
          // Foliage on the side opposite the node — its own zigzag bias when there is one,
          // otherwise a fixed gentle offset so center-biased nodes still get some greenery.
          val foliageBias = if (nodeBias != 0f) -nodeBias else if (node.def.index % 4 == 0) 0.4f else -0.4f
          Box(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.align(BiasAlignment(nodeBias, 0f)),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              LevelNodeButton(
                node = node,
                onClick = {
                  viewModel.onStart(node.def)
                  onPlay(node.def)
                },
              )
              // Marks the final level of today's path — where finishing everything pays out the
              // reward — sitting beside the node rather than crowding its circle.
              if (isLast) {
                Text("🏆", fontSize = 30.sp)
              }
            }
            Image(
              painter = painterResource(PATH_FOLIAGE[node.def.index % PATH_FOLIAGE.size]),
              contentDescription = null,
              modifier = Modifier
                .size(42.dp)
                .align(BiasAlignment(foliageBias, 0f))
                .alpha(0.8f),
            )
          }
          // A critter in the gap the path's zigzag leaves opposite a left-biased node, sparse
          // enough (only after nodes 3, 7, 11) to read as background flavor, not clutter.
          if (!isLast && node.def.index % 4 == 3) {
            Box(modifier = Modifier.fillMaxWidth()) {
              PathCritter(
                emoji = PATH_CRITTERS[(node.def.index / 4) % PATH_CRITTERS.size],
                bobMillis = 1500 + (node.def.index % 3) * 250,
                modifier = Modifier.align(BiasAlignment(0.75f, 0f)),
              )
            }
          }
        }
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

/** A small decorative animal that gently bobs up and down, same idle-animation style as the home
 * screen's avatar. [bobMillis] varies per instance so a row of critters doesn't bob in lockstep. */
@Composable
private fun PathCritter(emoji: String, bobMillis: Int, modifier: Modifier = Modifier) {
  val idle = rememberInfiniteTransition(label = "critterIdle")
  val offsetY by idle.animateFloat(
    initialValue = -8f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(
      animation = tween(bobMillis, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "critterBob",
  )
  Text(
    emoji,
    fontSize = 30.sp,
    modifier = modifier
      .graphicsLayer { translationY = offsetY }
      .alpha(0.7f),
  )
}

@Composable
private fun LevelNodeButton(
  node: LevelNode,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val locked = node.status == LevelStatus.LOCKED
  val done = node.status == LevelStatus.DONE
  val base = colorFor(node.def.kind)
  val fill = when {
    locked -> MaterialTheme.colorScheme.surfaceVariant
    done -> base
    else -> base
  }
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Box(
        modifier = Modifier
          .size(NODE_SIZE)
          .clip(CircleShape)
          .background(if (done) fill else fill.copy(alpha = if (locked) 1f else 0.95f))
          .then(if (locked) Modifier else Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          if (locked) "🔒" else emojiFor(node.def.kind),
          fontSize = 34.sp,
        )
      }
      if (done) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White),
          contentAlignment = Alignment.Center,
        ) {
          Text("⭐", fontSize = 16.sp)
        }
      }
    }
    Text(
      "${node.def.index}",
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = if (locked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    )
    if (!locked && !done) {
      Text(
        badgeFor(node.def),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
