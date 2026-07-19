package com.compose.wonderlearn.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WonderTopBar(
  title: String = "",
  onBack: (() -> Unit)? = null,
  containerColor: Color = Color.Unspecified,
) {
  val accented = containerColor.isSpecified
  val background = if (accented) containerColor else MaterialTheme.colorScheme.background
  val content = if (accented) onColorFor(containerColor) else MaterialTheme.colorScheme.onBackground

  TopAppBar(
    title = {
      if (title.isNotEmpty()) {
        Text(
          text = title,
          fontSize = 22.sp,
          fontWeight = FontWeight.ExtraBold,
          color = content,
        )
      }
    },
    navigationIcon = {
      if (onBack != null) {
        IconButton(onClick = onBack) {
          Text("←", fontSize = 26.sp, color = content)
        }
      } else {
        Spacer(Modifier.width(16.dp))
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = background,
      titleContentColor = content,
      navigationIconContentColor = content,
    ),
  )
}
