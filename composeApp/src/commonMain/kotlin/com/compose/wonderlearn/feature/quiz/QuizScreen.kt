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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.action_listen
import com.compose.wonderlearn.resources.pronunciation_unavailable
import com.compose.wonderlearn.resources.quiz_correct
import com.compose.wonderlearn.resources.quiz_prompt
import com.compose.wonderlearn.resources.quiz_score
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val CorrectGreen = Color(0xFF35C46A)
private val WrongRed = Color(0xFFED5757)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
  onBack: () -> Unit,
  viewModel: QuizViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val unavailable = stringResource(Res.string.pronunciation_unavailable)

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            "${stringResource(Res.string.quiz_score)}: ${state.score}",
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Text("←", fontSize = 26.sp, color = MaterialTheme.colorScheme.onBackground)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
        ),
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        if (state.solved) stringResource(Res.string.quiz_correct) else stringResource(Res.string.quiz_prompt),
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = if (state.solved) CorrectGreen else MaterialTheme.colorScheme.onBackground,
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Language.entries.forEach { language ->
          FilledTonalButton(onClick = {
            if (!viewModel.replay(language)) {
              scope.launch { snackbarHostState.showSnackbar(unavailable) }
            }
          }) {
            Text("${language.flag} 🔊", fontSize = 16.sp)
          }
        }
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
      Text(item.emoji, fontSize = 72.sp)
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
