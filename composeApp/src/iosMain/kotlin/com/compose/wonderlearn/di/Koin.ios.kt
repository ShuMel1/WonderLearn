package com.compose.wonderlearn.di

import com.compose.wonderlearn.data.DatabaseDriverFactory
import com.compose.wonderlearn.speech.IosTextToSpeaker
import com.compose.wonderlearn.speech.TextToSpeaker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
  single { DatabaseDriverFactory() }
  single<TextToSpeaker> { IosTextToSpeaker() }
}
