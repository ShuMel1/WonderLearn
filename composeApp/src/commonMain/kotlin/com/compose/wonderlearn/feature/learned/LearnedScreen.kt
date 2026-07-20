package com.compose.wonderlearn.feature.learned

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.VocabularyItem
import com.compose.wonderlearn.resources.action_revise
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.learned_empty
import com.compose.wonderlearn.resources.learned_title
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.colorForCategory
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnedScreen(
  onRevise: () -> Unit,
  onBack: () -> Unit,
  viewModel: LearnedViewModel = koinViewModel(),
) {
  val items by viewModel.items.collectAsStateWithLifecycle()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      WonderTopBar(
        title = "🎓 ${stringResource(Res.string.learned_title)}",
        onBack = onBack,
      )
    },
    floatingActionButton = {
      if (items.isNotEmpty()) {
        ExtendedFloatingActionButton(onClick = onRevise) {
          Text(
            "🎓  ${stringResource(Res.string.action_revise)}",
            fontWeight = FontWeight.Bold,
          )
        }
      }
    },
  ) { padding ->
    if (items.isEmpty()) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          stringResource(Res.string.learned_empty),
          fontSize = 18.sp,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      return@Scaffold
    }
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      items(items, key = { it.id }) { item ->
        LearnedCard(item)
      }
    }
  }
}

@Composable
private fun LearnedCard(item: VocabularyItem) {
  val accent = colorForCategory(item.categoryId)
  Card(
    modifier = Modifier.fillMaxWidth(),
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
        Text(item.emoji, fontSize = 34.sp)
      }
      Text(
        item.text(LocalLanguage.current),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      Text("✓", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent)
    }
  }
}
