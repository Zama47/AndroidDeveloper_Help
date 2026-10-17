package com.zama.androiddeveloper_help.data.domain

import com.zama.androiddeveloper_help.data.db.UserCardEntity
import com.zama.androiddeveloper_help.data.db.UserDeckEntity
import com.zama.androiddeveloper_help.data.db.UserProfileEntity

data class UserDeck(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val createdAt: Long,
    val updatedAt: Long,
    val cardCount: Int
)

data class UserCard(
    val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    val options: List<String>,
    val correctIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val streak: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val dueAtMillis: Long = 0,
    val lastReviewedMillis: Long = 0,
    val lastCorrect: Boolean? = null
)

fun UserDeckEntity.toDomain(cardCount: Int = 0): UserDeck = UserDeck(
    id = id, title = title, description = description, icon = icon,
    createdAt = createdAt, updatedAt = updatedAt, cardCount = cardCount
)

fun UserCardEntity.toDomain(): UserCard = UserCard(
    id = id, deckId = deckId, front = front, back = back,
    options = options, correctIndex = correctIndex,
    createdAt = createdAt, updatedAt = updatedAt,
    streak = streak, intervalDays = intervalDays, easeFactor = easeFactor,
    dueAtMillis = dueAtMillis, lastReviewedMillis = lastReviewedMillis,
    lastCorrect = lastCorrect
)

data class UserProfile(
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveEpochDay: Long = -1,
    val totalSessions: Int = 0,
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val cardsCreated: Int = 0
) {
    fun toEntity(): UserProfileEntity = UserProfileEntity(
        id = 0,
        totalXp = totalXp,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActiveEpochDay = lastActiveEpochDay,
        totalSessions = totalSessions,
        totalCorrect = totalCorrect,
        totalIncorrect = totalIncorrect,
        cardsCreated = cardsCreated
    )
}

fun UserProfileEntity.toProfile(): UserProfile = UserProfile(
    totalXp = totalXp,
    currentStreak = currentStreak,
    longestStreak = longestStreak,
    lastActiveEpochDay = lastActiveEpochDay,
    totalSessions = totalSessions,
    totalCorrect = totalCorrect,
    totalIncorrect = totalIncorrect,
    cardsCreated = cardsCreated
)
