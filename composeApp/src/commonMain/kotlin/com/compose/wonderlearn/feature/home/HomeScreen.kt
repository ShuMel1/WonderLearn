package com.compose.wonderlearn.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.app_name
import com.compose.wonderlearn.resources.coming_soon
import com.compose.wonderlearn.resources.home_learn
import com.compose.wonderlearn.resources.home_learned
import com.compose.wonderlearn.resources.home_review
import com.compose.wonderlearn.resources.home_stories
import com.compose.wonderlearn.resources.home_tagline
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.feature.account.AccountButton
import com.compose.wonderlearn.feature.account.AccountSheet
import com.compose.wonderlearn.feature.account.AccountViewModel
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onLearn: () -> Unit,
  onReview: () -> Unit,
  onLearned: () -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val comingSoon = stringResource(Res.string.coming_soon)
  val showComingSoon: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(comingSoon) } }

  val accountViewModel: AccountViewModel = koinViewModel()
  val accountState by accountViewModel.state.collectAsStateWithLifecycle()
  var showAccount by remember { mutableStateOf(false) }

  if (showAccount) {
    AccountSheet(onDismiss = { showAccount = false }, viewModel = accountViewModel)
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End,
      ) {
        AccountButton(
          displayName = accountState.activeProfile?.displayName,
          onClick = { showAccount = true },
        )
      }
      Column(
        modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Text("🦉", fontSize = 96.sp)
        Text(
          "${stringResource(Res.string.app_name)} ✨",
          fontSize = 40.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          stringResource(Res.string.home_tagline),
          fontSize = 18.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          HomeTile(Modifier.weight(1f), "📚", stringResource(Res.string.home_learn), Sky, onLearn)
          HomeTile(Modifier.weight(1f), "🎯", stringResource(Res.string.home_review), Coral, onReview)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          HomeTile(Modifier.weight(1f), "🎓", stringResource(Res.string.home_learned), Sunny, onLearned)
          HomeTile(Modifier.weight(1f), "📖", stringResource(Res.string.home_stories), Grape, showComingSoon)
        }
      }
    }
  }
}

@Composable
private fun HomeTile(
  modifier: Modifier,
  emoji: String,
  label: String,
  color: Color,
  onClick: () -> Unit,
) {
  val onColor = if (color == Sunny) Color(0xFF33304A) else Color.White
  Card(
    modifier = modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = color),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 48.sp) }
      Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onColor)
    }
  }
}
