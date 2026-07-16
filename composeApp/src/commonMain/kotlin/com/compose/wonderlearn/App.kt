package com.compose.wonderlearn

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.compose.wonderlearn.feature.categories.CategoriesScreen
import com.compose.wonderlearn.feature.detail.WordDetailScreen
import com.compose.wonderlearn.feature.home.HomeScreen
import com.compose.wonderlearn.feature.quiz.QuizScreen
import com.compose.wonderlearn.feature.words.WordListScreen
import com.compose.wonderlearn.navigation.Destination
import com.compose.wonderlearn.ui.theme.WonderLearnTheme

@Composable
fun App() {
  WonderLearnTheme {
    val navController = rememberNavController()
    NavHost(
      navController = navController,
      startDestination = Destination.Home,
    ) {
      composable<Destination.Home> {
        HomeScreen(
          onLearn = { navController.navigate(Destination.Categories) },
          onReview = { navController.navigate(Destination.Quiz) },
        )
      }
      composable<Destination.Categories> {
        CategoriesScreen(
          onCategoryClick = { navController.navigate(Destination.Words(it.id)) },
          onBack = { navController.popBackStack() },
        )
      }
      composable<Destination.Words> { entry ->
        val route = entry.toRoute<Destination.Words>()
        WordListScreen(
          categoryId = route.categoryId,
          onItemClick = { navController.navigate(Destination.Detail(it.id)) },
          onBack = { navController.popBackStack() },
        )
      }
      composable<Destination.Detail> { entry ->
        val route = entry.toRoute<Destination.Detail>()
        WordDetailScreen(
          itemId = route.itemId,
          onBack = { navController.popBackStack() },
        )
      }
      composable<Destination.Quiz> {
        QuizScreen(onBack = { navController.popBackStack() })
      }
    }
  }
}
