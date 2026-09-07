package com.compose.wonderlearn.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.LocalNativeLanguage
import com.compose.wonderlearn.ui.WonderTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * The active profile's learning (target) language, its own dedicated page (not folded together
 * with [NativeLanguageScreen]). The target list keeps the native language in the list too — see
 * `(Language.targets + listOfNotNull(nativeLanguage)).distinct()` below — so e.g. Armenian stays
 * selectable as a *learning* language even for a profile whose native language is Armenian.
 * Selecting an entry updates the preference for the currently active profile only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningLanguageScreen(
  onBack: () -> Unit,
  viewModel: AccountViewModel = koinViewModel(),
) {
  val language = LocalLanguage.current
  val nativeLanguage = LocalNativeLanguage.current

  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.account_learning_language(), onBack = onBack) },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      (Language.targets + listOfNotNull(nativeLanguage)).distinct().forEach { entry ->
        AccountRow(
          leading = entry.flag,
          label = entry.displayName,
          selected = entry == language,
          onClick = { viewModel.chooseTargetLanguage(entry) },
        )
      }
    }
  }
}
