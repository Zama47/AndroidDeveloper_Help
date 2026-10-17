package com.zama.androiddeveloper_help.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Прогресс пользователя по вопросу из встроенной темы. */
@Entity(
    tableName = "question_progress",
    primaryKeys = ["topicId", "questionId"],
    indices = [Index("topicId"), Index("dueAtMillis")]
)
data class QuestionProgressEntity(
    val topicId: String,
    val questionId: String,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    /** Сколько раз подряд ответили верно (для интервального повторения). */
    val streak: Int = 0,
    /** Текущий интервал повторения в днях (0 = новое / просрочено). */
    val intervalDays: Int = 0,
    /** Фактор лёгкости SM-2 (>= 1.3). */
    val easeFactor: Double = 2.5,
    /** Когда вопрос снова нужно повторить, epoch millis. */
    val dueAtMillis: Long = 0,
    val lastReviewedMillis: Long = 0,
    val lastCorrect: Boolean? = null
)

/** Колода, созданная пользователем (свои тесты и карточки). */
@Entity(tableName = "user_decks")
data class UserDeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val icon: String = "layers",
    val createdAt: Long,
    val updatedAt: Long
)

/** Карточка (вопрос/флеш-карта) внутри пользовательской колоды. */
@Entity(
    tableName = "user_cards",
    indices = [Index("deckId"), Index("dueAtMillis")]
)
data class UserCardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val front: String,
    val back: String,
    /** Если задан — карточка работает как MCQ в квизе. */
    val options: List<String> = emptyList(),
    /** Индекс правильного варианта в options (если MCQ). */
    val correctIndex: Int = -1,
    val createdAt: Long,
    val updatedAt: Long,
    val streak: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val dueAtMillis: Long = 0,
    val lastReviewedMillis: Long = 0,
    val lastCorrect: Boolean? = null
)

/** Единственный профиль пользователя (id = 0). */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val totalXp: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** День последней активности (epoch day), для стрика. */
    val lastActiveEpochDay: Long = -1,
    val totalSessions: Int = 0,
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val cardsCreated: Int = 0
)

/** Разблокированное достижение. */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long
)
