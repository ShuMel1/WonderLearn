package com.compose.wonderlearn.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.WonderTopBar
import org.koin.compose.viewmodel.koinViewModel

/**
 * Add/rename/delete kids — the less-frequent profile *management* actions, split out from the
 * quick-switch strip on [AccountMenuScreen] onto their own page so that strip stays uncluttered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProfilesScreen(
  onBack: () -> Unit,
  viewModel: AccountViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  var adding by remember { mutableStateOf(false) }
  var newName by remember { mutableStateOf("") }
  var editingId by remember { mutableStateOf<String?>(null) }
  var editName by remember { mutableStateOf("") }
  var pendingDeleteId by remember { mutableStateOf<String?>(null) }

  val submit: () -> Unit = {
    viewModel.addChild(newName)
    newName = ""
    adding = false
  }

  pendingDeleteId?.let { id ->
    AlertDialog(
      onDismissRequest = { pendingDeleteId = null },
      title = { Text(AppStrings.account_delete_confirm()) },
      confirmButton = {
        TextButton(onClick = {
          viewModel.deleteProfile(id)
          if (editingId == id) editingId = null
          pendingDeleteId = null
        }) { Text(AppStrings.account_delete(), fontWeight = FontWeight.Bold) }
      },
      dismissButton = {
        TextButton(onClick = { pendingDeleteId = null }) { Text(AppStrings.action_cancel()) }
      },
    )
  }

  Scaffold(
    containerColor = Color.Transparent,
    topBar = { WonderTopBar(title = AppStrings.account_menu_manage_kids(), onBack = onBack) },
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
      state.profiles.forEach { profile ->
        if (editingId == profile.id) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = editName,
              onValueChange = { editName = it },
              singleLine = true,
              shape = RoundedCornerShape(16.dp),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
              keyboardActions = KeyboardActions(onDone = {
                viewModel.renameProfile(profile.id, editName)
                editingId = null
              }),
              modifier = Modifier.weight(1f),
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { editingId = null }) { Text(AppStrings.action_cancel()) }
            if (state.profiles.size > 1) {
              TextButton(onClick = { pendingDeleteId = profile.id }) {
                Text(AppStrings.account_delete(), color = MaterialTheme.colorScheme.error)
              }
            }
            TextButton(
              onClick = {
                viewModel.renameProfile(profile.id, editName)
                editingId = null
              },
              enabled = editName.isNotBlank(),
            ) { Text(AppStrings.action_save(), fontWeight = FontWeight.Bold) }
          }
        } else {
          AccountRow(
            leading = profile.avatarId ?: profile.displayName.initial(),
            label = profile.displayName,
            selected = profile.id == state.activeProfileId,
            onClick = { viewModel.switchProfile(profile.id) },
            trailingEdit = AppStrings.account_edit(),
            onEdit = { editingId = profile.id; editName = profile.displayName },
          )
        }
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
            label = { Text(AppStrings.account_child_name()) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          TextButton(onClick = { adding = false; newName = "" }) {
            Text(AppStrings.action_cancel())
          }
          TextButton(onClick = submit, enabled = newName.isNotBlank()) {
            Text(AppStrings.action_save(), fontWeight = FontWeight.Bold)
          }
        }
      } else {
        AccountRow(
          leading = "+",
          label = AppStrings.account_add_child(),
          selected = false,
          onClick = { adding = true },
        )
      }
    }
  }
}
