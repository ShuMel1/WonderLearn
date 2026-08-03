package com.compose.wonderlearn.feature.levels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.LEVELS
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.domain.LevelRunController
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.theme.Sky
import org.koin.compose.koinInject

@Composable
fun LevelProgressBar(modifier: Modifier = Modifier) {
  val runController: LevelRunController = koinInject()
  val run by runController.active.collectAsStateWithLifecycle()
  val streak by runController.streak.collectAsStateWithLifecycle()

  val active = run
  val def = active?.let { r -> LEVELS.firstOrNull { it.id == r.levelId } }
  val goalAnswers = active?.goal ?: 0
  val doneAnswers = streak.coerceAtMost(goalAnswers)
  val fraction = if (goalAnswers > 0) doneAnswers.toFloat() / goalAnswers else 0f
  val label = def?.let { labelFor(it.kind) } ?: ""
  val streakLevel = def != null && def.kind != LevelKind.MEMORY

  AnimatedVisibility(
    visible = active != null && def != null && streakLevel,
    enter = slideInVertically { -it },
    exit = slideOutVertically { -it },
    modifier = modifier,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          "$label   ✅ $doneAnswers / $goalAnswers",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        LinearProgressIndicator(
          progress = { fraction },
          modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
          color = Sky,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun labelFor(kind: LevelKind): String = when (kind) {
  LevelKind.LEARN -> AppStrings.home_learn()
  LevelKind.MEMORY -> AppStrings.memory_title()
  LevelKind.BUBBLE_POP -> AppStrings.bubble_title()
  LevelKind.ODD_ONE_OUT -> AppStrings.odd_title()
}
