package com.compose.wonderlearn.feature.language

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.language_native_title
import com.compose.wonderlearn.resources.language_target_title
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.colorForIndex
import com.compose.wonderlearn.ui.onColorFor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguagePickerScreen(
  role: LanguageRole,
  exclude: Language? = null,
  onBack: (() -> Unit)? = null,
  viewModel: LanguagePickerViewModel = koinViewModel(),
) {
  val choices = when (role) {
    LanguageRole.NATIVE -> Language.natives
    LanguageRole.TARGET -> Language.targets
  }.filter { it != exclude }
  val title = when (role) {
    LanguageRole.NATIVE -> Res.string.language_native_title
    LanguageRole.TARGET -> Res.string.language_target_title
  }
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = { WonderTopBar(onBack = onBack) },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text("🦉", fontSize = 80.sp)
      Text(
        stringResource(title),
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
      )

      choices.forEachIndexed { index, language ->
        LanguageCard(
          language = language,
          index = index,
          onClick = {
            viewModel.choose(language, role)
            onBack?.invoke()
          },
        )
      }
    }
  }
}

@Composable
private fun LanguageCard(language: Language, index: Int, onClick: () -> Unit) {
  val color = colorForIndex(index)
  Card(
    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = color),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(language.flag, fontSize = 40.sp)
      Text(
        language.displayName,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = onColorFor(color),
      )
    }
  }
}
