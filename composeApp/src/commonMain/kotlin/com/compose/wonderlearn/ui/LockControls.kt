package com.compose.wonderlearn.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.ui.theme.Coral

/** Shown app-wide while the screen is locked. Long-press (hold) to unlock, so a stray tap won't. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnlockBar(onUnlock: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    contentAlignment = Alignment.TopCenter,
  ) {
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(50))
        .background(Coral)
        .combinedClickable(onClick = {}, onLongClick = onUnlock)
        .padding(horizontal = 20.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text("🔒", fontSize = 16.sp)
      Text(
        AppStrings.lock_hold_to_unlock(),
        color = androidx.compose.ui.graphics.Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
