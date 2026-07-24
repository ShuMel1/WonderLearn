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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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

/** A simple maths gate a young child can't solve, so only a grown-up can unlock. */
@Composable
fun ParentGate(onPass: () -> Unit, onDismiss: () -> Unit) {
  val a = remember { kotlin.random.Random.nextInt(3, 10) }
  val b = remember { kotlin.random.Random.nextInt(3, 10) }
  val answer = a * b
  var input by remember { mutableStateOf("") }

  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = { androidx.compose.material3.Text(AppStrings.lock_gate_prompt()) },
    text = {
      androidx.compose.foundation.layout.Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        androidx.compose.material3.Text(
          "$a × $b = ?",
          fontSize = 30.sp,
          fontWeight = FontWeight.ExtraBold,
        )
        androidx.compose.material3.OutlinedTextField(
          value = input,
          onValueChange = { new -> input = new.filter { it.isDigit() }.take(3) },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            imeAction = androidx.compose.ui.text.input.ImeAction.Done,
          ),
          keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { if (input.toIntOrNull() == answer) onPass() else input = "" },
          ),
        )
      }
    },
    confirmButton = {
      androidx.compose.material3.TextButton(
        onClick = { if (input.toIntOrNull() == answer) onPass() else input = "" },
        enabled = input.isNotBlank(),
      ) { androidx.compose.material3.Text(AppStrings.lock_unlock(), fontWeight = FontWeight.Bold) }
    },
    dismissButton = {
      androidx.compose.material3.TextButton(onClick = onDismiss) {
        androidx.compose.material3.Text(AppStrings.action_cancel())
      }
    },
  )
}
