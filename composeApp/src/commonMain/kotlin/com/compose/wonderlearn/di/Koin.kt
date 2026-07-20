package com.compose.wonderlearn.di

import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.data.DatabaseDriverFactory
import com.compose.wonderlearn.data.DatabaseSeeder
import com.compose.wonderlearn.data.DefaultPronouncer
import com.compose.wonderlearn.data.SqlDelightLanguagePreferences
import com.compose.wonderlearn.data.SqlDelightLearningRepository
import com.compose.wonderlearn.data.SqlDelightVocabularyRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.app.AppViewModel
import com.compose.wonderlearn.feature.categories.CategoriesViewModel
import com.compose.wonderlearn.feature.detail.WordDetailViewModel
import com.compose.wonderlearn.feature.language.LanguagePickerViewModel
import com.compose.wonderlearn.feature.learned.LearnedViewModel
import com.compose.wonderlearn.feature.quiz.QuizViewModel
import com.compose.wonderlearn.feature.words.WordListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
  single { WonderLearnDatabase(get<DatabaseDriverFactory>().createDriver()).also(DatabaseSeeder::seedIfEmpty) }
  single<VocabularyRepository> { SqlDelightVocabularyRepository(get()) }
  single<LearningRepository> { SqlDelightLearningRepository(get()) }
  single<LanguagePreferences> { SqlDelightLanguagePreferences(get()) }
  single { AudioPlayer() }
  single<Pronouncer> { DefaultPronouncer(get(), get()) }
  viewModel { AppViewModel(get()) }
  viewModel { LanguagePickerViewModel(get()) }
  viewModel { CategoriesViewModel(get()) }
  viewModel { params -> WordListViewModel(params.get(), get()) }
  viewModel { params -> WordDetailViewModel(params.get(), get(), get()) }
  viewModel { QuizViewModel(get(), get(), get()) }
  viewModel { LearnedViewModel(get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  startKoin {
    appDeclaration()
    modules(appModule, platformModule)
  }
}
