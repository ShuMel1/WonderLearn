package com.compose.wonderlearn.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.resources.Res
import com.compose.wonderlearn.resources.app_name
import com.compose.wonderlearn.ui.colorForCategory
import com.compose.wonderlearn.ui.onColorFor
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
  onCategoryClick: (Category) -> Unit,
  onBack: () -> Unit,
  viewModel: CategoriesViewModel = koinViewModel(),
) {
  val categories by viewModel.categories.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        IconButton(onClick = onBack) {
          Text("←", fontSize = 26.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(
          text = "${stringResource(Res.string.app_name)} ✨",
          fontSize = 32.sp,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground,
        )
      }
    }
    itemsIndexed(categories, key = { _, c -> c.id }) { _, category ->
      CategoryCard(category, onClick = { onCategoryClick(category) })
    }
  }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
  val color = colorForCategory(category.id)
  val onColor = onColorFor(color)
  Card(
    modifier = Modifier.fillMaxWidth().height(110.dp).clickable(onClick = onClick),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = color),
    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Box(
        modifier = Modifier.size(72.dp).clip(CircleShape).background(onColor.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center,
      ) {
        Text(category.emoji, fontSize = 40.sp)
      }
      Text(
        category.title,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = onColor,
      )
    }
  }
}
