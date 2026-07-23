package com.compose.wonderlearn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.feature.app.AppViewModel
import com.compose.wonderlearn.feature.categories.CategoriesScreen
import com.compose.wonderlearn.feature.detail.WordDetailScreen
import com.compose.wonderlearn.feature.home.HomeScreen
import com.compose.wonderlearn.feature.language.LanguagePickerScreen
import com.compose.wonderlearn.feature.language.LanguageRole
import com.compose.wonderlearn.ui.LocalNativeLanguage
import com.compose.wonderlearn.feature.learned.LearnedScreen
import com.compose.wonderlearn.feature.quiz.QuizScreen
import com.compose.wonderlearn.feature.words.WordListScreen
import com.compose.wonderlearn.navigation.Destination
import com.compose.wonderlearn.ui.LocalLanguage
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.compose.wonderlearn.ui.LockUi
import com.compose.wonderlearn.ui.LocalLockUi
import com.compose.wonderlearn.ui.UnlockBar
import com.compose.wonderlearn.ui.rememberAppLockController
import com.compose.wonderlearn.ui.PlatformBackHandler
import com.compose.wonderlearn.ui.theme.WonderLearnTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(onExit: () -> Unit = {}) {
  WonderLearnTheme {
    val appViewModel: AppViewModel = koinViewModel()
    val state by appViewModel.state.collectAsStateWithLifecycle()
    val native = state.nativeLanguage
    val target = state.targetLanguage

    when {
      state.loading -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
      native == null -> LanguagePickerScreen(role = LanguageRole.NATIVE, exclude = target)
      target == null -> LanguagePickerScreen(role = LanguageRole.TARGET, exclude = native)
      else -> {
        val appLock = rememberAppLockController()
        var locked by remember { mutableStateOf(false) }
        val lockUi = LockUi(
          supported = appLock.lockSupported,
          locked = locked,
          requestLock = { appLock.lock().also { if (it) locked = true } },
          requestUnlock = { appLock.unlock(); locked = false },
        )
        CompositionLocalProvider(
          LocalLanguage provides target,
          LocalNativeLanguage provides native,
          LocalLockUi provides lockUi,
        ) {
          Box(Modifier.fillMaxSize()) {
            AppNavHost(onExit = onExit)
            if (locked) UnlockBar(onUnlock = lockUi.requestUnlock)
          }
        }
      }
    }
  }
}

@Composable
private fun AppNavHost(onExit: () -> Unit) {
  val navController = rememberNavController()
  val currentEntry by navController.currentBackStackEntryAsState()
  val atRoot = currentEntry == null || navController.previousBackStackEntry == null
  PlatformBackHandler(enabled = atRoot, onBack = onExit)
  NavHost(
    navController = navController,
    startDestination = Destination.Home,
  ) {
    composable<Destination.Home> {
      HomeScreen(
        onLearn = { navController.navigate(Destination.Categories) },
        onReview = { navController.navigate(Destination.Quiz()) },
        onLearned = { navController.navigate(Destination.Learned) },
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
    composable<Destination.Quiz> { entry ->
      val route = entry.toRoute<Destination.Quiz>()
      QuizScreen(
        mode = if (route.revise) QuizMode.REVISE else QuizMode.LEARN,
        onRevise = { navController.navigate(Destination.Quiz(revise = true)) },
        onBack = { navController.popBackStack() },
      )
    }
    composable<Destination.Learned> {
      LearnedScreen(
        onRevise = { navController.navigate(Destination.Quiz(revise = true)) },
        onBack = { navController.popBackStack() },
      )
    }
  }
}
