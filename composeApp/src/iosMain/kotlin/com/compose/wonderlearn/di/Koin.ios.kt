package com.compose.wonderlearn.di

import com.compose.wonderlearn.speech.IosTextToSpeaker
import com.compose.wonderlearn.speech.TextToSpeaker
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
  single<TextToSpeaker> { IosTextToSpeaker() }
}
