package com.compose.wonderlearn.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.compose.wonderlearn.db.WonderLearnDatabase

actual class DatabaseDriverFactory(private val context: Context) {
  actual fun createDriver(): SqlDriver =
    AndroidSqliteDriver(WonderLearnDatabase.Schema, context, "wonderlearn.db")
}
