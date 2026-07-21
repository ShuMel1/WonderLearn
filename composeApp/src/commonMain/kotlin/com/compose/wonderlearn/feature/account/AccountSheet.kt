package com.compose.wonderlearn.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.Language
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.account_add_child
import com.compose.wonderlearn.resources.account_child_name
import com.compose.wonderlearn.resources.account_learning_language
import com.compose.wonderlearn.resources.account_open
import com.compose.wonderlearn.resources.account_title
import com.compose.wonderlearn.resources.account_who_is_learning
import com.compose.wonderlearn.resources.action_cancel
import com.compose.wonderlearn.resources.action_save
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.theme.Sky
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountButton(
  displayName: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val label = stringResource(Res.string.account_open)
  Card(
    onClick = onClick,
    shape = CircleShape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier.semantics { contentDescription = label },
  ) {
    Box(
      modifier = Modifier.size(44.dp).background(Sky.copy(alpha = 0.25f)),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        displayName.initial(),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
  onDismiss: () -> Unit,
  viewModel: AccountViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val language = LocalLanguage.current
  val sheetState = rememberModalBottomSheetState()

  var adding by remember { mutableStateOf(false) }
  var newName by remember { mutableStateOf("") }

  val submit: () -> Unit = {
    viewModel.addChild(newName)
    newName = ""
    adding = false
  }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        stringResource(Res.string.account_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 12.dp),
      )

      SectionLabel(stringResource(Res.string.account_who_is_learning))
      state.profiles.forEach { profile ->
        AccountRow(
          leading = profile.displayName.initial(),
          label = profile.displayName,
          selected = profile.id == state.activeProfileId,
          onClick = { viewModel.switchProfile(profile.id) },
        )
      }

      if (adding) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text(stringResource(Res.string.account_child_name)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(onClick = { adding = false; newName = "" }) {
            Text(stringResource(Res.string.action_cancel))
          }
          TextButton(onClick = submit, enabled = newName.isNotBlank()) {
            Text(stringResource(Res.string.action_save), fontWeight = FontWeight.Bold)
          }
        }
      } else {
        AccountRow(
          leading = "+",
          label = stringResource(Res.string.account_add_child),
          selected = false,
          onClick = { adding = true },
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

      SectionLabel(stringResource(Res.string.account_learning_language))
      Language.entries.forEach { entry ->
        AccountRow(
          leading = entry.flag,
          label = entry.displayName,
          selected = entry == language,
          onClick = { viewModel.chooseLanguage(entry) },
        )
      }
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(bottom = 8.dp),
  )
}

@Composable
private fun AccountRow(
  leading: String,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val background =
    if (selected) Sky.copy(alpha = 0.20f) else Color.Transparent
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(background)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Box(
      modifier = Modifier.size(36.dp).clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center,
    ) {
      Text(leading, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
    Text(
      label,
      fontSize = 18.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    if (selected) {
      Text("✓", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Sky)
    }
  }
}

private fun String?.initial(): String =
  this?.trim()?.firstOrNull()?.uppercase() ?: "?"
