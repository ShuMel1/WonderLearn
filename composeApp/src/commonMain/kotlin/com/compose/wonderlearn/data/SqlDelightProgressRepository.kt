package com.compose.wonderlearn.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.compose.wonderlearn.db.WonderLearnDatabase
import com.compose.wonderlearn.domain.DEFAULT_DAILY_GOAL
import com.compose.wonderlearn.domain.DailyProgress
import com.compose.wonderlearn.domain.ProfileRepository
import com.compose.wonderlearn.domain.ProgressRepository
import com.compose.wonderlearn.domain.TimeProvider
import com.compose.wonderlearn.domain.XP_PER_CORRECT
import com.compose.wonderlearn.domain.XP_PER_WORD_LEARNED
import com.compose.wonderlearn.domain.computeStreak
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightProgressRepository(
  database: WonderLearnDatabase,
  private val profiles: ProfileRepository,
  private val time: TimeProvider,
  private val dispatcher: CoroutineDispatcher = ioDispatcher,
) : ProgressRepository {

  private val queries = database.wonderLearnQueries

  override suspend fun recordCorrectAnswer() = add(words = 0L, xp = XP_PER_CORRECT.toLong())

  override suspend fun recordWordLearned() = add(words = 1L, xp = XP_PER_WORD_LEARNED.toLong())

  private suspend fun add(words: Long, xp: Long) {
    withContext(dispatcher) {
      val profileId = profiles.currentProfileId()
      val day = time.todayEpochDay()
      queries.transaction {
        queries.ensureDailyActivity(profileId, day)
        queries.addDailyActivity(words, xp, profileId, day)
      }
    }
  }

  override fun dailyGoal(): Flow<Int> =
    queries.selectSetting(KEY_DAILY_GOAL).asFlow().mapToOneOrNull(dispatcher)
      .map { it?.toIntOrNull() ?: DEFAULT_DAILY_GOAL }

  override suspend fun setDailyGoal(goal: Int) {
    withContext(dispatcher) { queries.upsertSetting(KEY_DAILY_GOAL, goal.toString()) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun dailyProgress(): Flow<DailyProgress> =
    combine(profiles.activeProfileId(), dailyGoal()) { profileId, goal -> profileId to goal }
      .flatMapLatest { (profileId, goal) ->
        queries.selectActiveDays(profileId).asFlow().mapToList(dispatcher).map { activeDays ->
          val today = time.todayEpochDay()
          val todayRow = queries.selectDayActivity(profileId, today).executeAsOneOrNull()
          DailyProgress(
            wordsToday = (todayRow?.wordsLearned ?: 0L).toInt(),
            dailyGoal = goal,
            streakDays = computeStreak(today, activeDays),
            totalXp = queries.selectTotalXp(profileId).executeAsOne().toInt(),
          )
        }
      }
}

private const val KEY_DAILY_GOAL = "dailyGoal"
