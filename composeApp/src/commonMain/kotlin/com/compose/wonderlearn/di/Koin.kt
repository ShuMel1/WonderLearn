package com.compose.wonderlearn.di

import com.compose.wonderlearn.data.InMemoryVocabularyRepository
import com.compose.wonderlearn.domain.VocabularyRepository
import com.compose.wonderlearn.feature.categories.CategoriesViewModel
import com.compose.wonderlearn.feature.detail.WordDetailViewModel
import com.compose.wonderlearn.feature.quiz.QuizViewModel
import com.compose.wonderlearn.feature.words.WordListViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

val appModule = module {
  single<VocabularyRepository> { InMemoryVocabularyRepository() }
  viewModel { CategoriesViewModel(get()) }
  viewModel { params -> WordListViewModel(params.get(), get()) }
  viewModel { params -> WordDetailViewModel(params.get(), get(), get()) }
  viewModel { QuizViewModel(get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  startKoin {
    appDeclaration()
    modules(appModule, platformModule)
  }
}
