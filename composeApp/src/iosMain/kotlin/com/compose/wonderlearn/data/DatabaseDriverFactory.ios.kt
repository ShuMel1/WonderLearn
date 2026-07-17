package com.compose.wonderlearn.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.compose.wonderlearn.db.WonderLearnDatabase

actual class DatabaseDriverFactory {
  actual fun createDriver(): SqlDriver =
    NativeSqliteDriver(WonderLearnDatabase.Schema, "wonderlearn.db")
}
