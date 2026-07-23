package com.compose.wonderlearn.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.LocalNativeLanguage
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.colorForCategory
import com.compose.wonderlearn.ui.onColorFor
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
  itemId: String,
  onBack: () -> Unit,
  viewModel: WordDetailViewModel = koinViewModel { parametersOf(itemId) },
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val item = state.item
  val language = LocalLanguage.current
  val nativeLanguage = LocalNativeLanguage.current

  val snackbarHostState = remember { SnackbarHostState() }
  val unavailableMessage = AppStrings.pronunciation_unavailable()
  LaunchedEffect(viewModel) {
    viewModel.unavailable.collect { snackbarHostState.showSnackbar(unavailableMessage) }
  }

  LaunchedEffect(item != null) {
    if (item != null) viewModel.pronounceOnOpen(language)
  }

  val accent = colorForCategory(item?.categoryId ?: "")
  val onAccent = onColorFor(accent)

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      WonderTopBar(
        onBack = onBack,
        containerColor = accent,
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    if (item == null) return@Scaffold
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
      Box(
        modifier = Modifier.size(220.dp).clip(CircleShape).background(accent.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
      ) {
        WordImage(
          imageRef = item.imageRef,
          emoji = item.emoji,
          emojiSize = 130.sp,
          contentDescription = item.text(language),
          modifier = Modifier.size(150.dp),
        )
      }

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          item.text(language),
          fontSize = 46.sp,
          fontWeight = FontWeight.ExtraBold,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.fillMaxWidth(),
        )
        if (nativeLanguage != language) {
          Text(
            item.text(nativeLanguage),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      Button(
        onClick = {
          viewModel.pronounce(language)
        },
        enabled = !state.speaking,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
          containerColor = accent,
          contentColor = onAccent,
          disabledContainerColor = accent.copy(alpha = 0.45f),
          disabledContentColor = onAccent.copy(alpha = 0.7f),
        ),
        modifier = Modifier.fillMaxWidth(0.7f).height(64.dp),
      ) {
        Text("🔁  ${AppStrings.action_repeat()}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      }

      if (state.hasSiblings) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          StepButton(
            glyph = "◀",
            label = AppStrings.action_previous(),
            accent = accent,
            onClick = { viewModel.previous(language) },
          )
          StepButton(
            glyph = "▶",
            label = AppStrings.action_next(),
            accent = accent,
            onClick = { viewModel.next(language) },
          )
        }
      }
    }
  }
}

@Composable
private fun StepButton(
  glyph: String,
  label: String,
  accent: Color,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .size(72.dp)
      .clip(CircleShape)
      .background(accent.copy(alpha = 0.22f))
      .clickable(onClick = onClick)
      .semantics { contentDescription = label },
    contentAlignment = Alignment.Center,
  ) {
    Text(glyph, fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground)
  }
}
