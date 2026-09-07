package com.compose.wonderlearn.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.theme.Sky

/** The avatar/initial button on Home that opens [Destination.AccountMenu][com.compose.wonderlearn.navigation.Destination.AccountMenu]. */
@Composable
fun AccountButton(
  displayName: String?,
  avatar: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val label = AppStrings.account_open()
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
        avatar ?: displayName.initial(),
        fontSize = if (avatar != null) 22.sp else 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@Composable
internal fun SectionLabel(text: String) {
  Text(
    text,
    fontSize = 14.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier = Modifier.padding(bottom = 8.dp),
  )
}

@Composable
internal fun AccountRow(
  leading: String,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  trailingEdit: String? = null,
  onEdit: (() -> Unit)? = null,
  trailingChevron: Boolean = false,
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
    if (trailingChevron) {
      Text("›", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (onEdit != null) {
      Text(
        "✎",
        fontSize = 20.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .clip(CircleShape)
          .clickable(onClick = onEdit)
          .semantics { contentDescription = trailingEdit ?: "" }
          .padding(6.dp),
      )
    }
  }
}

internal fun String?.initial(): String =
  this?.trim()?.firstOrNull()?.uppercase() ?: "?"
