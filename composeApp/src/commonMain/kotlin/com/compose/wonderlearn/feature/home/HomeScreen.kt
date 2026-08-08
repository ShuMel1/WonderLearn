package com.compose.wonderlearn.feature.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
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
import com.compose.wonderlearn.ui.theme.wonderBackground
import org.koin.compose.viewmodel.koinViewModel

private val CompactHeightThreshold = 600.dp
private val MascotMaxSize = 96.dp
private val MascotMinSize = 28.dp
private const val MascotHeightFraction = 0.55f
private val MascotBobDistance = 6.dp
private const val MascotBreathScale = 0.04f

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

  Box(modifier = Modifier.fillMaxSize().wonderBackground()) {
  Scaffold(
    containerColor = Color.Transparent,
  ) { padding ->
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
      val compact = maxHeight < CompactHeightThreshold
      Column(modifier = Modifier.fillMaxSize()) {
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
              IconChip("🔒", onClick = { showLock = true })
              AccountButton(
                displayName = accountState.activeProfile?.displayName,
                avatar = accountState.activeProfile?.avatarId,
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
        }
        BoxWithConstraints(
          modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          val mascotSize = minOf(MascotMaxSize, maxHeight * MascotHeightFraction)
          if (mascotSize >= MascotMinSize) {
            MascotButton(
              emoji = accountState.activeProfile?.avatarId ?: "🦉",
              size = mascotSize,
              onClick = onAvatars,
            )
          }
        }

        val gap = if (compact) 10.dp else 16.dp
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .padding(bottom = if (compact) 10.dp else 20.dp),
          verticalArrangement = Arrangement.spacedBy(gap),
        ) {
          AdventureBanner(compact = compact, onClick = onAdventure)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
          ) {
            HomeTile(Modifier.weight(1f), "📚", AppStrings.home_learn(), Sky, compact, onLearn)
            HomeTile(Modifier.weight(1f), "🎯", AppStrings.home_review(), Coral, compact, onReview)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap),
          ) {
            HomeTile(Modifier.weight(1f), "🎓", AppStrings.home_learned(), Sunny, compact, onLearned)
            HomeTile(Modifier.weight(1f), "🎮", AppStrings.games_title(), Grape, compact, onGames)
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
private fun MascotButton(emoji: String, size: Dp, onClick: () -> Unit) {
  val label = AppStrings.avatars_title()
  val transition = rememberInfiniteTransition()
  val bob by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )
  val fontSize = with(LocalDensity.current) { size.toSp() }
  Box(
    modifier = Modifier
      .clip(CircleShape)
      .clickable(onClick = onClick)
      .semantics { contentDescription = label }
      .padding(12.dp)
      .graphicsLayer {
        translationY = -bob * MascotBobDistance.toPx()
        scaleX = 1f + bob * MascotBreathScale
        scaleY = 1f + bob * MascotBreathScale
      },
    contentAlignment = Alignment.Center,
  ) {
    Text(emoji, fontSize = fontSize)
  }
}

@Composable
private fun AdventureBanner(compact: Boolean, onClick: () -> Unit) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = Grape),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = if (compact) 12.dp else 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text("🗺️", fontSize = if (compact) 32.sp else 40.sp)
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          AppStrings.home_adventure(),
          fontSize = if (compact) 19.sp else 22.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
        )
        Text(
          AppStrings.home_adventure_sub(),
          fontSize = if (compact) 13.sp else 14.sp,
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
  compact: Boolean,
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
      modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 14.dp else 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = if (compact) 34.sp else 48.sp)
      }
      Text(
        label,
        fontSize = if (compact) 17.sp else 20.sp,
        fontWeight = FontWeight.Bold,
        color = onColor,
      )
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
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.surface)
      .clickable(onClick = onClick)
      .padding(10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(icon, fontSize = 18.sp)
  }
}
