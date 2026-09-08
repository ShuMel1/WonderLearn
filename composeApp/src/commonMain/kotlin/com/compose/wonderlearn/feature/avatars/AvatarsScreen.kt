package com.compose.wonderlearn.feature.avatars

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.domain.AVATARS
import com.compose.wonderlearn.domain.AvatarItem
import com.compose.wonderlearn.domain.GEMS_PER_EXCHANGE
import com.compose.wonderlearn.domain.GOLD_PER_EXCHANGE
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.owl_coin
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.pressScale
import com.compose.wonderlearn.ui.theme.Sunny
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

private val WornGreen = Color(0xFF35C46A)
private const val COIN_SOUND = "files/sounds/coin.wav"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarsScreen(
  onBack: () -> Unit,
  viewModel: AvatarsViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val exchangeFailed by viewModel.exchangeFailed.collectAsStateWithLifecycle()
  val exchangeSucceeded by viewModel.exchangeSucceeded.collectAsStateWithLifecycle()
  val justUnlocked by viewModel.justUnlocked.collectAsStateWithLifecycle()
  var pendingPurchase by remember { mutableStateOf<AvatarItem?>(null) }
  val coinSound = remember { AudioPlayer() }

  LaunchedEffect(exchangeFailed) {
    if (exchangeFailed) viewModel.consumeExchangeFailed()
  }

  LaunchedEffect(exchangeSucceeded) {
    if (exchangeSucceeded) {
      launch { runCatching { coinSound.play(Res.readBytes(COIN_SOUND)) } }
      viewModel.consumeExchangeSucceeded()
    }
  }

  pendingPurchase?.let { avatar ->
    AlertDialog(
      onDismissRequest = { pendingPurchase = null },
      title = { Text(AppStrings.avatars_buy_confirm_title()) },
      text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(avatar.emoji, fontSize = 48.sp)
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("💎", fontSize = 16.sp)
            Text(avatar.price.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
          }
          Text(AppStrings.avatars_buy_confirm_body(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
      confirmButton = {
        TextButton(onClick = { viewModel.onAvatarClick(avatar); pendingPurchase = null }) {
          Text(AppStrings.action_buy(), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingPurchase = null }) { Text(AppStrings.action_cancel()) }
      },
    )
  }

  Box(Modifier.fillMaxSize()) {
  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.avatars_title(), onBack = onBack) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        state.current ?: "🦉",
        fontSize = 88.sp,
        modifier = Modifier.padding(top = 12.dp),
      )
      Row(
        modifier = Modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        GoldChip(state.gold)
        GemsChip(state.gems)
      }
      ExchangeButton(
        enabled = state.gold >= GOLD_PER_EXCHANGE,
        failed = exchangeFailed,
        onClick = viewModel::onExchangeClick,
      )

      AVATARS.chunked(4).forEach { rowAvatars ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          rowAvatars.forEach { avatar ->
            val available = avatar.price == 0 || avatar.emoji in state.unlocked
            AvatarTile(
              avatar = avatar,
              available = available,
              worn = avatar.emoji == state.current,
              affordable = state.gems >= avatar.price,
              onClick = {
                when {
                  available -> viewModel.onAvatarClick(avatar)
                  // Can't afford it yet — nothing to confirm, the dimmed price already says why.
                  state.gems >= avatar.price -> pendingPurchase = avatar
                }
              },
              modifier = Modifier.weight(1f),
            )
          }
          repeat(4 - rowAvatars.size) { Box(Modifier.weight(1f)) }
        }
      }
      Box(Modifier.padding(bottom = 16.dp))
    }
  }

  justUnlocked?.let { emoji ->
    UnlockCelebrationOverlay(emoji = emoji, onDismiss = { viewModel.consumeJustUnlocked() })
  }
  }
}

@Composable
private fun GoldChip(gold: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(Sunny.copy(alpha = 0.30f))
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Image(painterResource(Res.drawable.owl_coin), contentDescription = null, modifier = Modifier.size(22.dp))
    Text(gold.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

@Composable
private fun GemsChip(gems: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text("💎", fontSize = 20.sp)
    Text(gems.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
  }
}

/** Trades [GOLD_PER_EXCHANGE] Gold for [GEMS_PER_EXCHANGE] Gem — the only way Gold buys anything
 * on its own; unlocking avatars themselves spends Gems. */
@Composable
private fun ExchangeButton(enabled: Boolean, failed: Boolean, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .padding(bottom = 8.dp)
      .clip(RoundedCornerShape(50))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .then(if (enabled) Modifier.pressScale(onClick = onClick) else Modifier)
      .alpha(if (enabled) 1f else 0.5f)
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text("$GOLD_PER_EXCHANGE", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Image(painterResource(Res.drawable.owl_coin), contentDescription = null, modifier = Modifier.size(16.dp))
    Text("→", fontSize = 14.sp)
    Text("$GEMS_PER_EXCHANGE", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Text("💎", fontSize = 14.sp)
  }
}

@Composable
private fun AvatarTile(
  avatar: AvatarItem,
  available: Boolean,
  worn: Boolean,
  affordable: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(18.dp))
      .background(if (worn) WornGreen.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
      .then(if (worn) Modifier.border(BorderStroke(3.dp, WornGreen), RoundedCornerShape(18.dp)) else Modifier)
      .clip(RoundedCornerShape(18.dp))
      .pressScale(onClick = onClick)
      .padding(6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(avatar.emoji, fontSize = 38.sp, modifier = Modifier.alpha(if (available) 1f else 0.30f))
      when {
        worn -> Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WornGreen)
        available -> {}
        else -> Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          modifier = Modifier.alpha(if (affordable) 1f else 0.5f),
        ) {
          Text("💎", fontSize = 11.sp)
          Text(avatar.price.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

/**
 * Celebrates a fresh purchase: the newly-unlocked avatar grows in from nothing, spins to a stop,
 * settles, then fades out on its own — same fly/grow/spin `Animatable` language as the check-in
 * coin and Adventure diamond flourishes, just without a flight (there's no single "source" tile
 * this one should fly from — every row could hold the one just bought). Tap dismisses early.
 */
@Composable
private fun UnlockCelebrationOverlay(emoji: String, onDismiss: () -> Unit) {
  val grow = remember { Animatable(0f) }
  val spin = remember { Animatable(0f) }
  var settled by remember { mutableStateOf(false) }

  LaunchedEffect(emoji) {
    grow.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    spin.animateTo(360f, animationSpec = tween(durationMillis = 700, easing = CubicBezierEasing(0.05f, 0.6f, 0.15f, 1f)))
    settled = true
    delay(1500)
    onDismiss()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.45f))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss,
      ),
    contentAlignment = Alignment.Center,
  ) {
    ConfettiBurst(visible = true, modifier = Modifier.fillMaxSize(), playSound = false)
    val density = LocalDensity.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
        emoji,
        fontSize = (140 * grow.value).sp,
        modifier = Modifier.graphicsLayer { rotationY = spin.value; cameraDistance = 8f * density.density },
      )
      if (settled) {
        Text(
          AppStrings.avatars_unlocked(),
          fontSize = 24.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White,
        )
      }
    }
  }
}
