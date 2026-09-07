package com.compose.wonderlearn.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.DEFAULT_AVATAR
import com.compose.wonderlearn.domain.Profile
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.appVersionName
import com.compose.wonderlearn.ui.theme.Sky
import org.koin.compose.viewmodel.koinViewModel

/**
 * The landing page for [Destination.AccountMenu][com.compose.wonderlearn.navigation.Destination.AccountMenu] —
 * a quick profile-switch strip (switching is common enough to stay one tap, not buried in a
 * sub-page) plus a short menu into the less-frequent settings, each on its own page rather than
 * everything crammed into one long sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountMenuScreen(
  onBack: () -> Unit,
  onNativeLanguage: () -> Unit,
  onLearningLanguage: () -> Unit,
  onAvatar: () -> Unit,
  onManageKids: () -> Unit,
  viewModel: AccountViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.account_title(), onBack = onBack) },
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
      SectionLabel(AppStrings.account_who_is_learning())
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        state.profiles.forEach { profile ->
          ProfileChip(
            profile = profile,
            selected = profile.id == state.activeProfileId,
            onClick = { viewModel.switchProfile(profile.id) },
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

      AccountRow(leading = "🌐", label = AppStrings.account_menu_native_language(), selected = false, onClick = onNativeLanguage, trailingChevron = true)
      AccountRow(leading = "🎯", label = AppStrings.account_learning_language(), selected = false, onClick = onLearningLanguage, trailingChevron = true)
      AccountRow(leading = "🎨", label = AppStrings.account_menu_avatar(), selected = false, onClick = onAvatar, trailingChevron = true)
      AccountRow(leading = "👤", label = AppStrings.account_menu_manage_kids(), selected = false, onClick = onManageKids, trailingChevron = true)

      HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

      SectionLabel(AppStrings.account_about())
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          "🦉  ${AppStrings.app_name()}",
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          "${AppStrings.account_version()} ${appVersionName()}",
          fontSize = 15.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ProfileChip(profile: Profile, selected: Boolean, onClick: () -> Unit) {
  Column(
    modifier = Modifier.clickable(onClick = onClick),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(
      modifier = Modifier
        .size(52.dp)
        .clip(CircleShape)
        .background(if (selected) Sky.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
    ) {
      Text(profile.avatarId ?: DEFAULT_AVATAR, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
    Text(
      profile.displayName,
      fontSize = 13.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = if (selected) Sky else MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
