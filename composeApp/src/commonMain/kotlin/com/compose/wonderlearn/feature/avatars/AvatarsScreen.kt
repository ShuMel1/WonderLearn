package com.compose.wonderlearn.feature.avatars

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.AVATARS
import com.compose.wonderlearn.domain.AvatarItem
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.theme.Sunny
import org.koin.compose.viewmodel.koinViewModel

private val WornGreen = Color(0xFF35C46A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarsScreen(
  onBack: () -> Unit,
  viewModel: AvatarsViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

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
      CoinChip(state.coins, modifier = Modifier.padding(vertical = 12.dp))

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
              affordable = state.coins >= avatar.price,
              onClick = { viewModel.onAvatarClick(avatar) },
              modifier = Modifier.weight(1f),
            )
          }
          repeat(4 - rowAvatars.size) { Box(Modifier.weight(1f)) }
        }
      }
      Box(Modifier.padding(bottom = 16.dp))
    }
  }
}

@Composable
private fun CoinChip(coins: Int, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(50))
      .background(Sunny.copy(alpha = 0.30f))
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text("🪙", fontSize = 20.sp)
    Text(coins.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
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
      .clickable(onClick = onClick)
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
          Text("🪙", fontSize = 11.sp)
          Text(avatar.price.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}
