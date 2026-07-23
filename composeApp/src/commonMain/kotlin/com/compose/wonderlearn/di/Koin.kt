package com.compose.wonderlearn.di

import com.compose.wonderlearn.audio.AudioPlayer
import com.compose.wonderlearn.data.DatabaseDriverFactory
import com.compose.wonderlearn.data.DefaultPronouncer
import com.compose.wonderlearn.data.content.BundledContentSource
import com.compose.wonderlearn.data.content.ContentSeeder
import com.compose.wonderlearn.data.content.BUNDLED_CONTENT
import com.compose.wonderlearn.data.content.ContentSource
import com.compose.wonderlearn.data.content.REMOTE_CONTENT
import com.compose.wonderlearn.data.content.RemoteContentSource
import com.compose.wonderlearn.data.content.httpClient
import com.compose.wonderlearn.data.SqlDelightLanguagePreferences
import com.compose.wonderlearn.data.SqlDelightLearningRepository
import com.compose.wonderlearn.data.SqlDelightProfileRepository
import com.compose.wonderlearn.data.SqlDelightProgressRepository
import com.compose.wonderlearn.data.SqlDelightVocabularyRepository
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.LanguagePreferences
import com.compose.wonderlearn.domain.LearningRepository
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.SystemTimeProvider
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.Pronouncer
import com.compose.wonderlearn.domain.QuizMode
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.account.AccountViewModel
import com.compose.wonderlearn.feature.home.HomeViewModel
import com.compose.wonderlearn.feature.app.AppViewModel
import com.compose.wonderlearn.feature.categories.CategoriesViewModel
import com.compose.wonderlearn.feature.detail.WordDetailViewModel
import com.compose.wonderlearn.feature.language.LanguagePickerViewModel
import com.compose.wonderlearn.feature.learned.LearnedViewModel
import com.compose.wonderlearn.feature.quiz.QuizViewModel
import com.compose.wonderlearn.feature.words.WordListViewModel
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
  single { WonderLearnDatabase(get<DatabaseDriverFactory>().createDriver()) }
  single { Json { ignoreUnknownKeys = true } }
  single { httpClient() }
  single<ContentSource>(named(BUNDLED_CONTENT)) { BundledContentSource(get()) }
  single<ContentSource>(named(REMOTE_CONTENT)) {
    RemoteContentSource(get()) { get<ContentSeeder>().storedVersion() }
  }
  single { ContentSeeder(get()) }
  single<VocabularyRepository> { SqlDelightVocabularyRepository(get()) }
  single<ProfileRepository> { SqlDelightProfileRepository(get()) }
  single<TimeProvider> { SystemTimeProvider() }
  single<ProgressRepository> { SqlDelightProgressRepository(get(), get(), get()) }
  single<LearningRepository> { SqlDelightLearningRepository(get(), get()) }
  single<LanguagePreferences> { SqlDelightLanguagePreferences(get()) }
  single { AudioPlayer() }
  single<Pronouncer> { DefaultPronouncer(get(), get()) }
  viewModel { AppViewModel(get(), get(), get(named(BUNDLED_CONTENT)), get(named(REMOTE_CONTENT))) }
  viewModel { AccountViewModel(get(), get(), get()) }
  viewModel { HomeViewModel(get()) }
  viewModel { LanguagePickerViewModel(get()) }
  viewModel { CategoriesViewModel(get()) }
  viewModel { params -> WordListViewModel(params.get(), get(), get()) }
  viewModel { params -> WordDetailViewModel(params.get(), get(), get()) }
  viewModel { (mode: QuizMode) -> QuizViewModel(get(), get(), get(), get(), mode) }
  viewModel { LearnedViewModel(get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  startKoin {
    appDeclaration()
    modules(appModule, platformModule)
  }
}
