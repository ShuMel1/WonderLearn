package com.compose.wonderlearn.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.action_back
import org.jetbrains.compose.resources.stringResource

private val BackArrow: ImageVector = ImageVector.Builder(
  name = "BackArrow",
  defaultWidth = 24.dp,
  defaultHeight = 24.dp,
  viewportWidth = 24f,
  viewportHeight = 24f,
  autoMirror = true,
).apply {
  path(fill = SolidColor(Color.Black)) {
    moveTo(20f, 11f)
    horizontalLineTo(7.83f)
    lineTo(13.42f, 5.41f)
    lineTo(12f, 4f)
    lineTo(4f, 12f)
    lineTo(12f, 20f)
    lineTo(13.41f, 18.59f)
    lineTo(7.83f, 13f)
    horizontalLineTo(20f)
    verticalLineTo(11f)
    close()
  }
}.build()

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
          Icon(
            imageVector = BackArrow,
            contentDescription = stringResource(Res.string.action_back),
            tint = content,
          )
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
