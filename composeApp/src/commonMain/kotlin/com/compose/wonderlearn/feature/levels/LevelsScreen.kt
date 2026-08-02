package com.compose.wonderlearn.feature.levels

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.GameSize
import com.compose.wonderlearn.domain.LEVELS
import com.compose.wonderlearn.domain.LevelDef
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Teal
import org.koin.compose.viewmodel.koinViewModel

private val NODE_SIZE = 78.dp
private val BIAS = listOf(0f, 0.62f, 0f, -0.62f)

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
  onHome: () -> Unit,
  viewModel: LevelsViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val justCompleted by viewModel.justCompleted.collectAsStateWithLifecycle()
  val doneCount = state.nodes.count { it.status == LevelStatus.DONE }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      topBar = { WonderTopBar(title = AppStrings.levels_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Text(
          "⭐ $doneCount / ${state.nodes.size}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        state.nodes.forEach { node ->
          Box(modifier = Modifier.fillMaxWidth()) {
            LevelNodeButton(
              node = node,
              modifier = Modifier.align(BiasAlignment(BIAS[node.def.index % BIAS.size], 0f)),
              onClick = {
                viewModel.onStart(node.def)
                onPlay(node.def)
              },
            )
          }
        }
      }
    }

    ConfettiBurst(
      visible = justCompleted != null,
      modifier = Modifier.fillMaxSize(),
    )
  }

  val completedId = justCompleted
  if (completedId != null) {
    val doneDef = LEVELS.firstOrNull { it.id == completedId }
    val nextDef = doneDef?.let { d -> LEVELS.firstOrNull { it.index == d.index + 1 } }
    LevelCompleteDialog(
      onDismiss = { viewModel.clearCompleted() },
      onHome = {
        viewModel.clearCompleted()
        onHome()
      },
      onRepeat = doneDef?.let { d ->
        {
          viewModel.clearCompleted()
          viewModel.onStart(d)
          onPlay(d)
        }
      },
      onNext = nextDef?.let { n ->
        {
          viewModel.clearCompleted()
          viewModel.onStart(n)
          onPlay(n)
        }
      },
    )
  }
}

@Composable
private fun LevelCompleteDialog(
  onDismiss: () -> Unit,
  onHome: () -> Unit,
  onRepeat: (() -> Unit)?,
  onNext: (() -> Unit)?,
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text("🎉", fontSize = 56.sp)
        Text(
          AppStrings.level_complete(),
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        if (onNext != null) {
          Button(
            onClick = onNext,
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().height(54.dp),
          ) {
            Text("▶  ${AppStrings.level_next()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          if (onRepeat != null) {
            OutlinedButton(
              onClick = onRepeat,
              shape = RoundedCornerShape(50),
              modifier = Modifier.weight(1f).height(50.dp),
            ) {
              Text("🔁  ${AppStrings.level_repeat()}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
          }
          OutlinedButton(
            onClick = onHome,
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f).height(50.dp),
          ) {
            Text("🏠  ${AppStrings.level_home()}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun LevelNodeButton(
  node: LevelNode,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  val locked = node.status == LevelStatus.LOCKED
  val done = node.status == LevelStatus.DONE
  val current = node.status == LevelStatus.CURRENT
  val base = colorFor(node.def.kind)
  val fill = when {
    locked -> MaterialTheme.colorScheme.surfaceVariant
    done -> base
    else -> base
  }
  val pulse = rememberInfiniteTransition(label = "nodePulse")
  val pulseScale by pulse.animateFloat(
    initialValue = 1f,
    targetValue = 1.1f,
    animationSpec = infiniteRepeatable(
      animation = tween(720, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "nodePulseScale",
  )
  val scale = if (current) pulseScale else 1f
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Box(
        modifier = Modifier
          .size(NODE_SIZE)
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
          }
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
