package com.compose.wonderlearn.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.allDrawableResources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

sealed interface ImageSource {
  data class Bundled(val name: String) : ImageSource
  data class Remote(val url: String) : ImageSource
  data class LocalFile(val path: String) : ImageSource
}

fun imageSourceOf(imageRef: String?): ImageSource? = when {
  imageRef.isNullOrBlank() -> null
  imageRef.startsWith("http://") || imageRef.startsWith("https://") -> ImageSource.Remote(imageRef)
  imageRef.startsWith(LOCAL_FILE_PREFIX) -> ImageSource.LocalFile(imageRef.removePrefix(LOCAL_FILE_PREFIX))
  imageRef.startsWith(BUNDLED_PREFIX) -> ImageSource.Bundled(imageRef.removePrefix(BUNDLED_PREFIX))
  else -> ImageSource.Bundled(imageRef)
}

@OptIn(ExperimentalResourceApi::class)
private fun ImageSource?.resolveBundled(): DrawableResource? = when (this) {
  is ImageSource.Bundled -> Res.allDrawableResources[name]
  is ImageSource.Remote -> null
  is ImageSource.LocalFile -> null
  null -> null
}

@Composable
fun WordImage(
  imageRef: String?,
  emoji: String,
  emojiSize: TextUnit,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) {
  val drawable = remember(imageRef) { imageSourceOf(imageRef).resolveBundled() }
  if (drawable == null) {
    Text(emoji, fontSize = emojiSize, color = MaterialTheme.colorScheme.onBackground)
  } else {
    Image(
      painter = painterResource(drawable),
      contentDescription = contentDescription,
      contentScale = ContentScale.Fit,
      modifier = modifier,
    )
  }
}

private const val BUNDLED_PREFIX = "bundled:"
private const val LOCAL_FILE_PREFIX = "file:"
