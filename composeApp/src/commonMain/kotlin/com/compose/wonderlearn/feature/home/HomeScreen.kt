package com.compose.wonderlearn.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.domain.CHECKIN_LADDER_SIZE
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
import kotlinx.coroutines.launch
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
  val checkInPosition by homeViewModel.checkInLadderPosition.collectAsStateWithLifecycle()
  val checkInReward by homeViewModel.checkInRewardToday.collectAsStateWithLifecycle()
  var showAccount by remember { mutableStateOf(false) }
  var showCheckIn by remember { mutableStateOf(false) }

  LaunchedEffect(checkedInToday) {
    if (!checkedInToday) showCheckIn = true
  }

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
          StatChip(icon = "🎁", onClick = { showCheckIn = true })
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

    // Composed last within this Box so it draws on top of the tiles/buttons above, not under them.
    if (showCheckIn) {
      CheckInOverlay(
        position = checkInPosition,
        rewardToday = checkInReward,
        claimedToday = checkedInToday,
        onClaim = {
          homeViewModel.claimCheckIn()
          showCheckIn = false
        },
        onDismiss = { showCheckIn = false },
      )
    }
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
  value: String? = null,
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
    if (value != null) {
      Text(
        value,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

/**
 * Shown once per day on Home if today's check-in hasn't been claimed yet. Deliberately simple —
 * no animation polish here, that's Improvement #5's job once the currency moments are all in.
 */
/**
 * A 7-slot reward ladder, not a calendar of the past week: slot 1 pays 2 Gold, climbing by 1 each
 * consecutive day, with slot 7 a deliberately bigger jackpot (and a bigger icon) for a full week
 * in a row — a missed day resets back to slot 1. Opens two ways: automatically once per day (first
 * Home composition, if not yet claimed) and anytime after via tapping the 🔥 streak chip. Slots
 * before [position] are dim "already claimed" presents, [position] itself is a bright, pulsing
 * present that's the actual claim button (tapping it flies the reward coin from that exact spot to
 * center, grows and spins it, then settles into a dimmed "done for today" present), and slots
 * after [position] are faint "disabled" presents previewing what's still ahead. Reopening after
 * claiming shows a read-only "come back tomorrow" state instead, so it can't double-pay.
 */
@Composable
private fun CheckInOverlay(
  position: Int,
  rewardToday: Int,
  claimedToday: Boolean,
  onClaim: () -> Unit,
  onDismiss: () -> Unit,
) {
  var claimIconCenter by remember { mutableStateOf(Offset.Zero) }
  var scrimCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  var claiming by remember { mutableStateOf(false) }
  var settled by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
        when {
          settled -> { onClaim(); onDismiss() }
          !claiming -> onDismiss()
        }
      }
      .onGloballyPositioned { scrimCoordinates = it },
    contentAlignment = Alignment.Center,
  ) {
    ConfettiBurst(visible = claiming, playSound = false, modifier = Modifier.fillMaxSize())

    Card(
      modifier = Modifier
        .padding(32.dp)
        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
      shape = RoundedCornerShape(28.dp),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Text(AppStrings.checkin_title(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(
          if (claimedToday) AppStrings.checkin_already_claimed() else AppStrings.checkin_subtitle(),
          textAlign = TextAlign.Center,
        )

        val pulse = rememberInfiniteTransition(label = "presentHintPulse")
        val pulseScale by pulse.animateFloat(
          initialValue = 0.9f,
          targetValue = 1.15f,
          animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
          label = "presentHintScale",
        )

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          for (slot in 1..CHECKIN_LADDER_SIZE) {
            val isBigSlot = slot == CHECKIN_LADDER_SIZE
            val baseSize = if (isBigSlot) 40.dp else 28.dp
            val isTodaySlot = slot == position
            val isClaimable = isTodaySlot && !claimedToday && !claiming
            val alreadyDone = slot < position || (isTodaySlot && claimedToday)
            Box(
              modifier = Modifier
                .size(baseSize)
                .then(if (isTodaySlot) Modifier.onGloballyPositioned { claimIconCenter = it.boundsInRoot().center } else Modifier)
                .then(
                  if (isClaimable) {
                    Modifier.clip(CircleShape).clickable(
                      interactionSource = remember { MutableInteractionSource() },
                      indication = null,
                    ) { claiming = true }
                  } else {
                    Modifier
                  },
                ),
              contentAlignment = Alignment.Center,
            ) {
              if (alreadyDone || isClaimable) {
                // A real gift box — bright and pulsing for today's unclaimed slot (the actual tap
                // target), dimmed to read as "already opened" once it's done.
                Text(
                  "🎁",
                  fontSize = (baseSize.value * 0.8f).sp,
                  modifier = Modifier
                    .alpha(if (isClaimable) 1f else 0.5f)
                    .graphicsLayer {
                      if (isClaimable) {
                        scaleX = pulseScale
                        scaleY = pulseScale
                      }
                    },
                )
              } else {
                // Slots still ahead stay a plain minimal outline — a preview, not a real reward yet.
                PresentIcon(
                  tint = MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.size(baseSize).alpha(0.6f),
                )
              }
            }
          }
        }

        if (isClaimableHint(claimedToday, claiming)) {
          Text(AppStrings.checkin_tap_hint(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (claimedToday) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Image(painterResource(Res.drawable.owl_coin), contentDescription = null, modifier = Modifier.size(22.dp))
            Text(
              "+$rewardToday " + AppStrings.checkin_earned_today(),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Sunny,
            )
          }
          TextButton(onClick = onDismiss) { Text(AppStrings.checkin_got_it(), fontWeight = FontWeight.Bold) }
        }
      }
    }

    val coords = scrimCoordinates
    if (claiming && coords != null) {
      val density = LocalDensity.current
      val smallPx = with(density) { 34.dp.toPx() }
      val largePx = with(density) { 140.dp.toPx() }
      val startCenter = claimIconCenter - coords.positionInRoot()
      val bounds = coords.boundsInRoot()
      val endCenter = Offset(bounds.width / 2f, bounds.height / 2f)

      val flight = remember { Animatable(0f) }
      val spin = remember { Animatable(0f) }
      val coinSound = remember { AudioPlayer() }

      LaunchedEffect(Unit) {
        launch { runCatching { coinSound.play(Res.readBytes(COIN_SOUND)) } }
        flight.animateTo(
          1f,
          animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
        spin.animateTo(
          720f,
          animationSpec = tween(durationMillis = 850, easing = CubicBezierEasing(0.05f, 0.6f, 0.15f, 1f)),
        )
        settled = true
      }

      val t = flight.value.coerceIn(0f, 1f)
      val cx = startCenter.x + (endCenter.x - startCenter.x) * t
      val cy = startCenter.y + (endCenter.y - startCenter.y) * t
      val coinSizePx = smallPx + (largePx - smallPx) * t

      Image(
        painter = painterResource(Res.drawable.owl_coin),
        contentDescription = null,
        modifier = Modifier
          .align(Alignment.TopStart)
          .offset { IntOffset((cx - coinSizePx / 2f).toInt(), (cy - coinSizePx / 2f).toInt()) }
          .size(with(density) { coinSizePx.toDp() })
          .graphicsLayer {
            rotationY = spin.value
            cameraDistance = 8f * density.density
          },
      )
    }
  }
}

private fun isClaimableHint(claimedToday: Boolean, claiming: Boolean) = !claimedToday && !claiming

/** A minimal line-art gift box — box, lid, ribbon, and a small bow — so ladder slots read as
 * "a present" rather than a plain dot, without pulling in a whole icon/asset dependency for it. */
@Composable
private fun PresentIcon(tint: Color, modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.09f
    val lidTop = h * 0.30f
    val lidBottom = h * 0.42f
    val boxLeft = w * 0.08f
    val boxRight = w * 0.92f

    // lid
    drawRoundRect(
      color = tint,
      topLeft = Offset(boxLeft, lidTop),
      size = Size(boxRight - boxLeft, lidBottom - lidTop),
      cornerRadius = CornerRadius(w * 0.1f),
      style = Stroke(width = stroke),
    )
    // box body
    drawRoundRect(
      color = tint,
      topLeft = Offset(boxLeft + w * 0.04f, lidBottom),
      size = Size(boxRight - boxLeft - w * 0.08f, h - lidBottom - h * 0.04f),
      cornerRadius = CornerRadius(w * 0.06f),
      style = Stroke(width = stroke),
    )
    // vertical ribbon
    drawLine(color = tint, start = Offset(w / 2f, lidTop), end = Offset(w / 2f, h - h * 0.04f), strokeWidth = stroke)
    // bow
    drawLine(color = tint, start = Offset(w / 2f, lidTop), end = Offset(w * 0.28f, lidTop - h * 0.18f), strokeWidth = stroke)
    drawLine(color = tint, start = Offset(w / 2f, lidTop), end = Offset(w * 0.72f, lidTop - h * 0.18f), strokeWidth = stroke)
  }
}

private const val COIN_SOUND = "files/sounds/coin.wav"
