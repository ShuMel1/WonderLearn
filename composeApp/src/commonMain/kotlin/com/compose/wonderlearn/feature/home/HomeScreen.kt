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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.compose.wonderlearn.ui.LocalLockUi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.feature.account.AccountButton
import com.compose.wonderlearn.feature.account.AccountSheet
import com.compose.wonderlearn.feature.account.AccountViewModel
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onLearn: () -> Unit,
  onReview: () -> Unit,
  onLearned: () -> Unit,
  onGames: () -> Unit,
) {
  val accountViewModel: AccountViewModel = koinViewModel()
  val accountState by accountViewModel.state.collectAsStateWithLifecycle()
  val homeViewModel: HomeViewModel = koinViewModel()
  val daily by homeViewModel.dailyProgress.collectAsStateWithLifecycle()
  var showAccount by remember { mutableStateOf(false) }

  if (showAccount) {
    AccountSheet(onDismiss = { showAccount = false }, viewModel = accountViewModel)
  }

  val lockUi = LocalLockUi.current
  var showLock by remember { mutableStateOf(false) }
  var showPinningHelp by remember { mutableStateOf(false) }
  if (showLock) {
    AlertDialog(
      onDismissRequest = { showLock = false },
      title = { Text(AppStrings.lock_start()) },
      text = { Text(if (lockUi.supported) AppStrings.lock_explain() else AppStrings.lock_ios_guide()) },
      confirmButton = {
        if (lockUi.supported) {
          TextButton(onClick = {
            showLock = false
            if (!lockUi.requestLock()) showPinningHelp = true
          }) {
            Text(AppStrings.lock_button(), fontWeight = FontWeight.Bold)
          }
        } else {
          TextButton(onClick = { showLock = false }) { Text(AppStrings.action_save()) }
        }
      },
      dismissButton = {
        if (lockUi.supported) {
          TextButton(onClick = { showLock = false }) { Text(AppStrings.action_cancel()) }
        }
      },
    )
  }
  if (showPinningHelp) {
    AlertDialog(
      onDismissRequest = { showPinningHelp = false },
      title = { Text(AppStrings.lock_start()) },
      text = { Text(AppStrings.lock_pinning_off()) },
      confirmButton = {
        TextButton(onClick = { showPinningHelp = false }) { Text(AppStrings.action_save()) }
      },
    )
  }

  var celebrateGoal by remember { mutableStateOf(false) }
  var seenGoalReached by remember { mutableStateOf<Boolean?>(null) }
  LaunchedEffect(daily.goalReached) {
    val previous = seenGoalReached
    seenGoalReached = daily.goalReached
    if (previous == false && daily.goalReached) celebrateGoal = true
  }

  Box(modifier = Modifier.fillMaxSize()) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
  ) { padding ->
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StatChip("🔥", daily.streakDays.toString())
          StatChip("⭐", daily.totalXp.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          IconChip("🔒", onClick = { showLock = true })
          AccountButton(
            displayName = accountState.activeProfile?.displayName,
            onClick = { showAccount = true },
          )
        }
      }
      DailyGoalCard(
        wordsToday = daily.wordsToday,
        dailyGoal = daily.dailyGoal,
        goalReached = daily.goalReached,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      )
      Column(
        modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Text("🦉", fontSize = 96.sp)
        Text(
          "${AppStrings.app_name()} ✨",
          fontSize = 40.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
          AppStrings.home_tagline(),
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
          HomeTile(Modifier.weight(1f), "📚", AppStrings.home_learn(), Sky, onLearn)
          HomeTile(Modifier.weight(1f), "🎯", AppStrings.home_review(), Coral, onReview)
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          HomeTile(Modifier.weight(1f), "🎓", AppStrings.home_learned(), Sunny, onLearned)
          HomeTile(Modifier.weight(1f), "🧩", AppStrings.memory_title(), Grape, onGames)
        }
      }
    }
  }
    ConfettiBurst(
      visible = celebrateGoal,
      modifier = Modifier.fillMaxSize(),
    )
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

@Composable
private fun StatChip(icon: String, value: String) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Text(icon, fontSize = 16.sp)
    Text(
      value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun DailyGoalCard(
  wordsToday: Int,
  dailyGoal: Int,
  goalReached: Boolean,
  modifier: Modifier = Modifier,
) {
  val fraction = if (dailyGoal <= 0) 1f else (wordsToday.toFloat() / dailyGoal).coerceIn(0f, 1f)
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          if (goalReached) AppStrings.home_goal_done() else AppStrings.home_daily_goal(),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          "$wordsToday / $dailyGoal",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = if (goalReached) Sky else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
        color = Sky,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    }
  }
}

@Composable
private fun IconChip(icon: String, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .clip(androidx.compose.foundation.shape.CircleShape)
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onClick)
      .padding(10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(icon, fontSize = 18.sp)
  }
}
