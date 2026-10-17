package com.zama.androiddeveloper_help.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionProgressDao {

    @Query("SELECT * FROM question_progress")
    fun observeAll(): Flow<List<QuestionProgressEntity>>

    @Query("SELECT * FROM question_progress WHERE topicId = :topicId AND questionId = :questionId")
    suspend fun get(topicId: String, questionId: String): QuestionProgressEntity?

    @Upsert
    suspend fun upsert(progress: QuestionProgressEntity)

    @Query("SELECT * FROM question_progress WHERE dueAtMillis <= :now AND streak > 0")
    suspend fun due(now: Long): List<QuestionProgressEntity>

    @Query("SELECT COUNT(*) FROM question_progress WHERE dueAtMillis <= :now AND streak > 0")
    fun observeDueCount(now: Long): Flow<Int>

    @Query("SELECT * FROM question_progress WHERE topicId = :topicId")
    suspend fun byTopic(topicId: String): List<QuestionProgressEntity>

    @Query("SELECT COUNT(*) FROM question_progress WHERE lastCorrect = 1")
    suspend fun distinctCorrectCount(): Int

    @Query("DELETE FROM question_progress WHERE topicId = :topicId")
    suspend fun clearTopic(topicId: String)
}

@Dao
interface UserDeckDao {

    @Query("SELECT * FROM user_decks ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<UserDeckEntity>>

    @Query("SELECT * FROM user_decks WHERE id = :id")
    suspend fun get(id: String): UserDeckEntity?

    @Upsert
    suspend fun upsert(deck: UserDeckEntity)

    @Query("DELETE FROM user_decks WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface UserCardDao {

    @Query("SELECT * FROM user_cards WHERE deckId = :deckId ORDER BY createdAt ASC")
    fun observeByDeck(deckId: String): Flow<List<UserCardEntity>>

    @Query("SELECT * FROM user_cards ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<UserCardEntity>>

    @Query("SELECT * FROM user_cards WHERE deckId = :deckId")
    suspend fun byDeck(deckId: String): List<UserCardEntity>

    @Query("SELECT * FROM user_cards WHERE id = :id")
    suspend fun get(id: String): UserCardEntity?

    @Query("SELECT * FROM user_cards WHERE dueAtMillis <= :now")
    suspend fun due(now: Long): List<UserCardEntity>

    @Query("SELECT COUNT(*) FROM user_cards WHERE dueAtMillis <= :now")
    fun observeDueCount(now: Long): Flow<Int>

    @Upsert
    suspend fun upsert(card: UserCardEntity)

    @Upsert
    suspend fun upsertAll(cards: List<UserCardEntity>)

    @Query("DELETE FROM user_cards WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM user_cards WHERE deckId = :deckId")
    suspend fun deleteByDeck(deckId: String)

    @Query("UPDATE user_cards SET streak = 0, intervalDays = 0, easeFactor = 2.5, dueAtMillis = 0, lastReviewedMillis = 0, lastCorrect = NULL WHERE deckId = :deckId")
    suspend fun resetProgress(deckId: String)
}

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun get(): UserProfileEntity?

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
}

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun get(id: String): AchievementEntity?

    @Upsert
    suspend fun insert(achievement: AchievementEntity)
}
