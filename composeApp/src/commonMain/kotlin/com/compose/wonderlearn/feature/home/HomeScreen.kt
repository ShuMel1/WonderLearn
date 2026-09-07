package com.compose.wonderlearn.feature.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.GOLD_PER_CHECKIN
import com.compose.wonderlearn.feature.account.AccountButton
import com.compose.wonderlearn.feature.account.AccountSheet
import com.compose.wonderlearn.feature.account.AccountViewModel
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.owl_coin
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.theme.Coral
import com.compose.wonderlearn.ui.theme.Grape
import com.compose.wonderlearn.ui.theme.Sky
import com.compose.wonderlearn.ui.theme.Sunny
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onLearn: () -> Unit,
  onReview: () -> Unit,
  onGames: () -> Unit,
  onAvatars: () -> Unit,
  onAdventure: () -> Unit,
) {
  val accountViewModel: AccountViewModel = koinViewModel()
  val accountState by accountViewModel.state.collectAsStateWithLifecycle()
  val homeViewModel: HomeViewModel = koinViewModel()
  val daily by homeViewModel.dailyProgress.collectAsStateWithLifecycle()
  val gold by homeViewModel.gold.collectAsStateWithLifecycle()
  val gems by homeViewModel.gems.collectAsStateWithLifecycle()
  val checkedInToday by homeViewModel.checkedInToday.collectAsStateWithLifecycle()
  val checkInDays by homeViewModel.checkInDaysThisWeek.collectAsStateWithLifecycle()
  var showAccount by remember { mutableStateOf(false) }
  var showCheckIn by remember { mutableStateOf(false) }

  LaunchedEffect(checkedInToday) {
    if (!checkedInToday) showCheckIn = true
  }

  if (showAccount) {
    AccountSheet(onDismiss = { showAccount = false }, viewModel = accountViewModel)
  }

  if (showCheckIn) {
    CheckInDialog(
      today = homeViewModel.todayEpochDay,
      daysThisWeek = checkInDays,
      claimedToday = checkedInToday,
      onClaim = {
        homeViewModel.claimCheckIn()
        showCheckIn = false
      },
      onDismiss = { showCheckIn = false },
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
          StatChip(icon = "🔥", value = daily.streakDays.toString(), onClick = { showCheckIn = true })
          StatChip(icon = "💎", value = gems.toString(), onClick = onAdventure)
          StatChip(iconPainter = painterResource(Res.drawable.owl_coin), value = gold.toString(), onClick = onAvatars)
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
        HomeTile(Modifier.fillMaxWidth(), "🎮", AppStrings.games_title(), Grape, onGames)
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
      Text("🏆", fontSize = 22.sp)
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
private fun StatChip(
  value: String,
  icon: String? = null,
  iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
  onClick: (() -> Unit)? = null,
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surface)
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    if (iconPainter != null) {
      Image(iconPainter, contentDescription = null, modifier = Modifier.size(18.dp))
    } else if (icon != null) {
      Text(icon, fontSize = 16.sp)
    }
    Text(
      value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/**
 * Shown once per day on Home if today's check-in hasn't been claimed yet. Deliberately simple —
 * no animation polish here, that's Improvement #5's job once the currency moments are all in.
 */
/**
 * Opens two ways: automatically once per day (first Home composition, if not yet claimed) and
 * anytime after via tapping the 🔥 streak chip — [claimedToday] switches it between the claimable
 * state and a read-only "come back tomorrow" state so reopening after claiming can't double-pay.
 */
@Composable
private fun CheckInDialog(
  today: Long,
  daysThisWeek: Set<Long>,
  claimedToday: Boolean,
  onClaim: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(AppStrings.checkin_title(), fontWeight = FontWeight.ExtraBold) },
    text = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          if (claimedToday) AppStrings.checkin_already_claimed() else AppStrings.checkin_subtitle(),
          textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          repeat(7) { i ->
            val day = today - (6 - i)
            val filled = day in daysThisWeek
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (filled) Sunny else MaterialTheme.colorScheme.surfaceVariant)
                .then(
                  if (day == today) Modifier.border(2.dp, Sunny, CircleShape) else Modifier,
                ),
            )
          }
        }
        if (!claimedToday) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painterResource(Res.drawable.owl_coin), contentDescription = null, modifier = Modifier.size(44.dp))
            Text("+$GOLD_PER_CHECKIN", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Sunny)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = if (claimedToday) onDismiss else onClaim) {
        Text(
          if (claimedToday) AppStrings.checkin_got_it() else AppStrings.checkin_claim(),
          fontWeight = FontWeight.Bold,
        )
      }
    },
  )
}
