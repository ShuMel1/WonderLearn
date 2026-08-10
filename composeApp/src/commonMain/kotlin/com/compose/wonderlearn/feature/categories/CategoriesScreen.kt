package com.compose.wonderlearn.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.compose.wonderlearn.domain.Category
import com.compose.wonderlearn.ui.AppStrings
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.LocalNativeLanguage
import com.compose.wonderlearn.ui.WonderTopBar
import com.compose.wonderlearn.ui.WordImage
import com.compose.wonderlearn.ui.colorForCategory
import com.compose.wonderlearn.ui.onColorFor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesScreen(
  onCategoryClick: (Category) -> Unit,
  onBack: () -> Unit,
  viewModel: CategoriesViewModel = koinViewModel(),
) {
  val categories by viewModel.categories.collectAsStateWithLifecycle()

  Scaffold(
    containerColor = Color.Transparent,
    topBar = {
      WonderTopBar(
        title = AppStrings.home_learn(),
        onBack = onBack,
      )
    },
  ) { padding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(padding),
      contentPadding = PaddingValues(20.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      itemsIndexed(categories, key = { _, c -> c.id }) { _, category ->
        CategoryCard(category, onClick = { onCategoryClick(category) })
      }
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
        WordImage(
          imageRef = category.imageRef,
          emoji = category.emoji,
          emojiSize = 40.sp,
          contentDescription = null,
          modifier = Modifier.size(50.dp),
        )
      }
      val localized = AppStrings.categoryTitles[category.id]
      val learningLanguage = LocalLanguage.current
      val nativeLanguage = LocalNativeLanguage.current
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          localized?.invoke() ?: category.title,
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = onColor,
        )
        if (localized != null && learningLanguage != nativeLanguage) {
          Text(
            localized.forLanguage(learningLanguage),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = onColor.copy(alpha = 0.85f),
          )
        }
      }
    }
  }
}
