package com.compose.wonderlearn.domain

import kotlinx.coroutines.flow.Flow

/** How many XP a child earns per coin — coins are a spendable view over the XP they've earned. */
const val XP_PER_COIN = 10

/** A welcome bounty so a new child can unlock an avatar or two right away. */
const val STARTING_COINS = 20

/** An avatar a child can wear. Price 0 avatars are free from the start; the rest unlock with coins. */
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
  AvatarItem("🦁", 20),
  AvatarItem("🐸", 20),
  AvatarItem("🐵", 25),
  AvatarItem("🐷", 25),
  AvatarItem("🐨", 30),
  AvatarItem("🐯", 30),
  AvatarItem("🐮", 35),
  AvatarItem("🐝", 40),
  AvatarItem("🐧", 45),
  AvatarItem("🐢", 45),
  AvatarItem("🦋", 50),
  AvatarItem("🦄", 60),
  AvatarItem("🐙", 60),
  AvatarItem("🐬", 70),
  AvatarItem("🐳", 90),
  AvatarItem("🦖", 120),
)

/** The avatars a child can wear without spending anything. */
val FREE_AVATARS: List<String> = AVATARS.filter { it.price == 0 }.map { it.emoji }

interface RewardsRepository {
  /** The active child's current spendable coin balance (earned minus spent). */
  fun coins(): Flow<Int>

  /** Emojis of the paid avatars the active child has unlocked (free avatars are always available). */
  fun unlockedAvatars(): Flow<Set<String>>

  /** Unlocks the avatar if affordable and not already unlocked. Returns true if it is now unlocked. */
  suspend fun unlockAvatar(emoji: String, price: Int): Boolean
}
