package com.compose.wonderlearn.feature.speak

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.ConfettiBurst
import com.compose.wonderlearn.ui.MicPermission
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.rememberMicPermission
import org.koin.compose.viewmodel.koinViewModel

private val CorrectGreen = Color(0xFF35C46A)
private val ListeningRed = Color(0xFFED5757)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakGameScreen(
  onBack: () -> Unit,
  viewModel: SpeakGameViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val mic = rememberMicPermission()

  LaunchedEffect(state.phase) {
    if (state.phase == SpeakPhase.CORRECT) {
      kotlinx.coroutines.delay(1600)
      viewModel.next()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = Color.Transparent,
      topBar = { WonderTopBar(title = AppStrings.speak_title(), onBack = onBack) },
    ) { padding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
      ) {
        if (state.phase == SpeakPhase.UNAVAILABLE) {
          Text(
            AppStrings.speak_unavailable(),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
          )
          return@Column
        }

        WordImage(
          imageRef = state.word?.imageRef,
          emoji = state.word?.emoji ?: "",
          emojiSize = 96.sp,
          contentDescription = null,
          modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            state.prompt,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            "🔊",
            fontSize = 30.sp,
            modifier = Modifier.clickable(enabled = !state.speaking) { viewModel.replay() },
          )
        }

        MicButton(
          listening = state.phase == SpeakPhase.LISTENING,
          onTap = {
            if (mic.status == MicPermission.GRANTED) viewModel.listen() else mic.request()
          },
        )

        Text(
          text = feedback(state, mic.status),
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = when (state.phase) {
            SpeakPhase.CORRECT -> CorrectGreen
            SpeakPhase.TRY_AGAIN -> ListeningRed
            else -> MaterialTheme.colorScheme.onBackground
          },
          modifier = Modifier.fillMaxWidth(),
        )

        if (state.phase == SpeakPhase.TRY_AGAIN && state.heard != null) {
          Text(
            "${AppStrings.speak_you_said()} ${state.heard}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }

        if (state.phase != SpeakPhase.LISTENING && state.phase != SpeakPhase.CORRECT) {
          TextButton(onClick = { viewModel.next() }) {
            Text(AppStrings.speak_next(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
    ConfettiBurst(visible = state.phase == SpeakPhase.CORRECT, modifier = Modifier.fillMaxSize())
  }
}

@Composable
private fun MicButton(listening: Boolean, onTap: () -> Unit) {
  val pulse = rememberInfiniteTransition(label = "mic")
  val scale by pulse.animateFloat(
    initialValue = 1f,
    targetValue = if (listening) 1.12f else 1f,
    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
    label = "micScale",
  )
  Surface(
    onClick = onTap,
    enabled = !listening,
    shape = CircleShape,
    color = if (listening) ListeningRed else MaterialTheme.colorScheme.primary,
    modifier = Modifier.size(96.dp).scale(scale),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text("🎤", fontSize = 44.sp)
    }
  }
}

@Composable
private fun feedback(state: SpeakState, permission: MicPermission): String = when {
  permission == MicPermission.DENIED -> AppStrings.speak_permission()
  state.phase == SpeakPhase.LISTENING -> AppStrings.speak_listening()
  state.phase == SpeakPhase.CORRECT -> AppStrings.speak_correct()
  state.phase == SpeakPhase.TRY_AGAIN -> AppStrings.speak_try_again()
  else -> AppStrings.speak_prompt()
}
