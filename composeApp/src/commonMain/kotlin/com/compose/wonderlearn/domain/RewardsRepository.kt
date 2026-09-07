package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

/** A welcome bounty so a new child can unlock an avatar or two right away. */
const val STARTING_GOLD = 20

/** Gold earned once per finished activity (a completed Quiz set, Memory board, Bubble Pop
 * five-streak) — a first-pass number, easy to retune once real usage is observed. */
const val GOLD_PER_ACTIVITY = 2

/** Gold earned once per day for opening the daily check-in. */
const val GOLD_PER_CHECKIN = 5

/** Gems earned once per day for clearing the full Daily Adventure path. */
const val GEMS_PER_DAILY_ADVENTURE = 3

/** How much Gold one Gold→Gems exchange costs, and how many Gems it yields. */
const val GOLD_PER_EXCHANGE = 10
const val GEMS_PER_EXCHANGE = 1

/** An avatar a child can wear. Price 0 avatars are free from the start; the rest unlock with Gems. */
data class AvatarItem(
  val emoji: String,
  val price: Int,
)

val AVATARS: List<AvatarItem> = listOf(
  AvatarItem("🦉", 0),
  AvatarItem("🐱", 0),
  AvatarItem("🐶", 0),
  AvatarItem("🦊", 0),
  AvatarItem("🐰", 0),
  AvatarItem("🐼", 0),
  AvatarItem("🦁", 5),
  AvatarItem("🐸", 5),
  AvatarItem("🐵", 7),
  AvatarItem("🐷", 7),
  AvatarItem("🐨", 8),
  AvatarItem("🐯", 8),
  AvatarItem("🐮", 9),
  AvatarItem("🐝", 10),
  AvatarItem("🐧", 12),
  AvatarItem("🐢", 12),
  AvatarItem("🦋", 13),
  AvatarItem("🦄", 15),
  AvatarItem("🐙", 15),
  AvatarItem("🐬", 18),
  AvatarItem("🐳", 23),
  AvatarItem("🦖", 30),
)

/** The avatars a child can wear without spending anything. */
val FREE_AVATARS: List<String> = AVATARS.filter { it.price == 0 }.map { it.emoji }

interface RewardsRepository {
  /** The active child's current spendable Gold balance (earned minus spent). Earned through
   * everyday play — finishing an activity, opening the daily check-in. */
  fun gold(): Flow<Int>

  /** The active child's current spendable Gem balance (earned minus spent). The rarer, more
   * valuable currency — earned only by clearing a full Daily Adventure day. */
  fun gems(): Flow<Int>

  /** Credits Gold for finishing an activity (a Quiz set, a Memory board, a Bubble Pop streak). */
  suspend fun earnGold(amount: Int)

  /** Credits Gems, e.g. for clearing a full Daily Adventure day. */
  suspend fun earnGems(amount: Int)

  /** Emojis of the paid avatars the active child has unlocked (free avatars are always available). */
  fun unlockedAvatars(): Flow<Set<String>>

  /** Unlocks the avatar if affordable in Gems and not already unlocked. Returns true if unlocked. */
  suspend fun unlockAvatar(emoji: String, priceGems: Int): Boolean

  /** Trades [GOLD_PER_EXCHANGE] Gold for [GEMS_PER_EXCHANGE] Gem. False if Gold is insufficient. */
  suspend fun exchangeGoldForGems(): Boolean

  /** Claims today's check-in bonus if it hasn't been claimed yet today. True exactly once per day. */
  suspend fun claimDailyCheckIn(): Boolean

  /** Epoch-days (within roughly the last week) the check-in has been claimed on, for the popup's strip. */
  fun checkInThisWeek(): Flow<Set<Long>>
}
