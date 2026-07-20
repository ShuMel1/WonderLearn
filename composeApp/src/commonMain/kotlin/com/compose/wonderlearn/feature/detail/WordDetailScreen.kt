package com.compose.wonderlearn.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.action_repeat
import com.compose.wonderlearn.resources.pronunciation_unavailable
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.colorForCategory
import com.compose.wonderlearn.ui.onColorFor
import org.jetbrains.compose.resources.stringResource
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

  val snackbarHostState = remember { SnackbarHostState() }
  val unavailableMessage = stringResource(Res.string.pronunciation_unavailable)
  LaunchedEffect(viewModel) {
    viewModel.unavailable.collect { snackbarHostState.showSnackbar(unavailableMessage) }
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
        Text(item.emoji, fontSize = 130.sp)
      }

      Text(
        item.text(language),
        fontSize = 46.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth(),
      )

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
        Text("🔁  ${stringResource(Res.string.action_repeat)}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
