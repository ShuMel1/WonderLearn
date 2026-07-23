package com.compose.wonderlearn.feature.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.colorForCategory
import com.compose.wonderlearn.ui.onColorFor
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
  categoryId: String,
  onItemClick: (VocabularyItem) -> Unit,
  onBack: () -> Unit,
  viewModel: WordListViewModel = koinViewModel { parametersOf(categoryId) },
) {
  val items by viewModel.items.collectAsStateWithLifecycle()
  val playingId by viewModel.playingId.collectAsStateWithLifecycle()
  val language = LocalLanguage.current
  val accent = colorForCategory(categoryId)

  val snackbarHostState = remember { SnackbarHostState() }
  val unavailableMessage = AppStrings.pronunciation_unavailable()
  LaunchedEffect(viewModel) {
    viewModel.unavailable.collect { snackbarHostState.showSnackbar(unavailableMessage) }
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      WonderTopBar(
        title = AppStrings.title_words(),
        onBack = onBack,
        containerColor = accent,
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      items(items, key = { it.id }) { item ->
        WordCard(
          item = item,
          accent = accent,
          playing = playingId == item.id,
          onPlay = { viewModel.play(item, language) },
          onClick = { onItemClick(item) },
        )
      }
    }
  }
}

@Composable
private fun WordCard(
  item: VocabularyItem,
  accent: Color,
  playing: Boolean,
  onPlay: () -> Unit,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(
        modifier = Modifier.size(64.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
      ) {
        WordImage(
          imageRef = item.imageRef,
          emoji = item.emoji,
          emojiSize = 34.sp,
          contentDescription = null,
          modifier = Modifier.size(44.dp),
        )
      }
      Text(
        item.text(LocalLanguage.current),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      val listenLabel = AppStrings.action_listen()
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(if (playing) accent else accent.copy(alpha = 0.18f))
          .clickable(onClick = onPlay)
          .semantics { contentDescription = listenLabel },
        contentAlignment = Alignment.Center,
      ) {
        Text(if (playing) "🔊" else "🔈", fontSize = 24.sp)
      }
    }
  }
}
