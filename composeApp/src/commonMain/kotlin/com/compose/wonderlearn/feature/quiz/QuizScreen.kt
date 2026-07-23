package com.compose.wonderlearn.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val CorrectGreen = Color(0xFF35C46A)
private val WrongRed = Color(0xFFED5757)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
  mode: QuizMode,
  onRevise: () -> Unit,
  onBack: () -> Unit,
  viewModel: QuizViewModel = koinViewModel { parametersOf(mode) },
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val unavailable = AppStrings.pronunciation_unavailable()
  LaunchedEffect(viewModel) {
    viewModel.unavailable.collect { snackbarHostState.showSnackbar(unavailable) }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      WonderTopBar(
        title = when (state.mode) {
          QuizMode.REVISE -> "${AppStrings.action_revise()}: ${state.score}"
          QuizMode.LEARN -> "${AppStrings.quiz_score()}: ${state.score}"
        },
        onBack = onBack,
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    if (state.allLearned) {
      val revising = state.mode == QuizMode.REVISE
      Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
      ) {
        Text(if (revising) "🎓" else "🏆", fontSize = 96.sp)
        Text(
          if (revising) AppStrings.revise_empty() else AppStrings.quiz_all_learned(),
          fontSize = 26.sp,
          fontWeight = FontWeight.ExtraBold,
          color = CorrectGreen,
          textAlign = TextAlign.Center,
        )
        if (!revising) {
          Button(
            onClick = onRevise,
            shape = RoundedCornerShape(50),
            modifier = Modifier.height(56.dp),
          ) {
            Text(
              "🎓  ${AppStrings.action_revise()}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
      return@Scaffold
    }
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      val prompt = when {
        state.solved && state.justLearned -> AppStrings.quiz_learned()
        state.solved && state.mode == QuizMode.REVISE -> AppStrings.revise_done()
        state.solved -> AppStrings.quiz_correct()
        else -> AppStrings.quiz_prompt()
      }
      Text(
        prompt,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (state.solved) CorrectGreen else MaterialTheme.colorScheme.onBackground,
      )

      FilledTonalButton(onClick = { viewModel.replay() }, enabled = !state.speaking) {
        Text("🔁  ${AppStrings.action_repeat()}", fontSize = 16.sp)
      }

      val options = state.options
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        options.chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            rowItems.forEach { item ->
              QuizTile(
                modifier = Modifier.weight(1f),
                item = item,
                isCorrect = state.solved && item.id == state.target?.id,
                isWrong = state.wrongId == item.id,
                onClick = { viewModel.onSelect(item) },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuizTile(
  modifier: Modifier,
  item: VocabularyItem,
  isCorrect: Boolean,
  isWrong: Boolean,
  onClick: () -> Unit,
) {
  val border = when {
    isCorrect -> BorderStroke(4.dp, CorrectGreen)
    isWrong -> BorderStroke(4.dp, WrongRed)
    else -> null
  }
  Card(
    modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    border = border,
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      WordImage(
        imageRef = item.imageRef,
        emoji = item.emoji,
        emojiSize = 72.sp,
        contentDescription = null,
        modifier = Modifier.fillMaxSize().padding(24.dp),
      )
      if (isCorrect || isWrong) {
        val badgeColor = if (isCorrect) CorrectGreen else WrongRed
        Box(
          modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            .size(34.dp).clip(CircleShape).background(badgeColor),
          contentAlignment = Alignment.Center,
        ) {
          Text(if (isCorrect) "✓" else "✗", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
