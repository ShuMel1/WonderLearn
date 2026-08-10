package com.compose.wonderlearn.feature.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
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
  onAvatars: () -> Unit,
  onAdventure: () -> Unit,
) {
  val accountViewModel: AccountViewModel = koinViewModel()
  val accountState by accountViewModel.state.collectAsStateWithLifecycle()
  val homeViewModel: HomeViewModel = koinViewModel()
  val daily by homeViewModel.dailyProgress.collectAsStateWithLifecycle()
  val coins by homeViewModel.coins.collectAsStateWithLifecycle()
  val stars by homeViewModel.stars.collectAsStateWithLifecycle()
  var showAccount by remember { mutableStateOf(false) }

  if (showAccount) {
    AccountSheet(onDismiss = { showAccount = false }, viewModel = accountViewModel)
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
    containerColor = Color.Transparent,
  ) { padding ->
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StatChip("🔥", daily.streakDays.toString())
          StatChip("⭐", stars.toString(), onClick = onAdventure)
          StatChip("🪙", coins.toString(), onClick = onAvatars)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          AccountButton(
            displayName = accountState.activeProfile?.displayName,
            avatar = accountState.activeProfile?.avatarId,
            onClick = { showAccount = true },
          )
        }
      }
      }
      val avatar = accountState.activeProfile?.avatarId ?: "🦉"
      val idle = rememberInfiniteTransition(label = "avatarIdle")
      val scale by idle.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
          animation = tween(1400, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
        label = "avatarScale",
      )
      Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .clickable(onClick = onAvatars)
            .padding(12.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(avatar, fontSize = 96.sp)
        }
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        AdventureBanner(onClick = onAdventure)
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
          HomeTile(Modifier.weight(1f), "🎮", AppStrings.games_title(), Grape, onGames)
        }
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
private fun AdventureBanner(onClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Grape),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("🗺️", fontSize = 40.sp)
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          AppStrings.home_adventure(),
          fontSize = 22.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
        )
        Text(
          AppStrings.home_adventure_sub(),
          fontSize = 14.sp,
          color = Color.White.copy(alpha = 0.9f),
        )
      }
      Text("▶", fontSize = 24.sp, color = Color.White)
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

@Composable
private fun StatChip(icon: String, value: String, onClick: (() -> Unit)? = null) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surface)
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
