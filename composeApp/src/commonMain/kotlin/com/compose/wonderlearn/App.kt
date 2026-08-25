package com.compose.wonderlearn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import com.compose.wonderlearn.feature.levels.LevelsScreen
import com.compose.wonderlearn.feature.games.GamesScreen
import com.compose.wonderlearn.domain.LevelKind
import com.compose.wonderlearn.domain.LevelRunController
import org.koin.compose.koinInject
import com.compose.wonderlearn.feature.memory.MemoryGameScreen
import com.compose.wonderlearn.feature.bubblepop.BubblePopScreen
import com.compose.wonderlearn.feature.oddoneout.OddOneOutScreen
import com.compose.wonderlearn.feature.speak.SpeakGameScreen
import com.compose.wonderlearn.feature.avatars.AvatarsScreen
import com.compose.wonderlearn.feature.quiz.QuizScreen
import com.compose.wonderlearn.feature.words.WordListScreen
import com.compose.wonderlearn.domain.Analytics
import com.compose.wonderlearn.domain.GameId
import com.compose.wonderlearn.navigation.Destination
import com.compose.wonderlearn.ui.LocalLanguage
import com.compose.wonderlearn.ui.PlatformBackHandler
import com.compose.wonderlearn.ui.theme.WonderLearnTheme
import com.compose.wonderlearn.ui.theme.wonderBackground
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(onExit: () -> Unit = {}) {
  WonderLearnTheme {
    val appViewModel: AppViewModel = koinViewModel()
    val state by appViewModel.state.collectAsStateWithLifecycle()
    val native = state.nativeLanguage
    val target = state.targetLanguage

    Box(Modifier.fillMaxSize().wonderBackground()) {
    when {
      state.loading -> {}
      native == null -> LanguagePickerScreen(role = LanguageRole.NATIVE)
      target == null -> LanguagePickerScreen(role = LanguageRole.TARGET, native = native)
      else -> {
        CompositionLocalProvider(
          LocalLanguage provides target,
          LocalNativeLanguage provides native,
        ) {
          AppNavHost(onExit = onExit)
        }
      }
    }
    }
  }
}

@Composable
private fun AppNavHost(onExit: () -> Unit) {
  val analytics = koinInject<Analytics>()
  val navController = rememberNavController()
  val currentEntry by navController.currentBackStackEntryAsState()
  val atRoot = currentEntry == null || navController.previousBackStackEntry == null
  PlatformBackHandler(enabled = atRoot, onBack = onExit)

  val levelRun: LevelRunController = koinInject()
  val completedLevel by levelRun.completed.collectAsStateWithLifecycle()
  LaunchedEffect(completedLevel) {
    if (completedLevel != null) {
      navController.popBackStack(Destination.Levels, inclusive = false)
    }
  }
  val backOnRoad = currentEntry?.destination?.route?.substringBefore('/')?.endsWith("Destination.Levels") == true
  LaunchedEffect(currentEntry) {
    if (backOnRoad && levelRun.active.value != null && levelRun.completed.value == null) {
      levelRun.clear()
    }
  }
  NavHost(
    navController = navController,
    startDestination = Destination.Home,
  ) {
    composable<Destination.Home> {
      HomeScreen(
        onLearn = { navController.navigate(Destination.Categories) },
        onReview = { navController.navigate(Destination.Quiz()) },
        onLearned = { navController.navigate(Destination.Learned) },
        onGames = { navController.navigate(Destination.Games) },
        onAvatars = { navController.navigate(Destination.Avatars) },
        onAdventure = { navController.navigate(Destination.Levels) },
      )
    }
    composable<Destination.Levels> {
      LevelsScreen(
        onPlay = { level ->
          when (level.kind) {
            LevelKind.LEARN -> navController.navigate(Destination.Quiz(revise = false))
            LevelKind.MEMORY -> navController.navigate(Destination.MemoryGame(fromLevel = true, size = (level.size?.ordinal ?: 0)))
            LevelKind.BUBBLE_POP -> navController.navigate(Destination.BubblePop)
            LevelKind.ODD_ONE_OUT -> navController.navigate(Destination.OddOneOut)
          }
        },
        onBack = { navController.popBackStack() },
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
    composable<Destination.Games> {
      GamesScreen(
        onMemoryMatch = { navController.navigate(Destination.MemoryGame()) },
        onOddOneOut = { navController.navigate(Destination.OddOneOut) },
        onBubblePop = { navController.navigate(Destination.BubblePop) },
        onSpeak = { navController.navigate(Destination.SpeakGame) },
        onBack = { navController.popBackStack() },
      )
    }
    composable<Destination.MemoryGame> { entry ->
      LaunchedEffect(Unit) { analytics.gameStarted(GameId.MEMORY_MATCH) }
      val route = entry.toRoute<Destination.MemoryGame>()
      MemoryGameScreen(
        fromLevel = route.fromLevel,
        size = route.size,
        onBack = { navController.popBackStack() },
      )
    }
    composable<Destination.OddOneOut> {
      LaunchedEffect(Unit) { analytics.gameStarted(GameId.ODD_ONE_OUT) }
      OddOneOutScreen(onBack = { navController.popBackStack() })
    }
    composable<Destination.BubblePop> {
      LaunchedEffect(Unit) { analytics.gameStarted(GameId.BUBBLE_POP) }
      BubblePopScreen(onBack = { navController.popBackStack() })
    }
    composable<Destination.SpeakGame> {
      LaunchedEffect(Unit) { analytics.gameStarted(GameId.SAY_THE_WORD) }
      SpeakGameScreen(onBack = { navController.popBackStack() })
    }
    composable<Destination.Avatars> {
      AvatarsScreen(onBack = { navController.popBackStack() })
    }
  }
}
