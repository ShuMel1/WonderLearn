package com.compose.wonderlearn

import android.app.Application
import com.compose.wonderlearn.di.initKoin
import org.koin.android.ext.koin.androidContext

class WonderLearnApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin {
      androidContext(this@WonderLearnApp)
    }
  }
}
