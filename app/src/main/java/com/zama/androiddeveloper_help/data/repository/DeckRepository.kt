package com.zama.androiddeveloper_help.data.repository

import com.zama.androiddeveloper_help.data.db.UserCardDao
import com.zama.androiddeveloper_help.data.db.UserCardEntity
import com.zama.androiddeveloper_help.data.db.UserDeckDao
import com.zama.androiddeveloper_help.data.db.UserDeckEntity
import com.zama.androiddeveloper_help.data.domain.UserCard
import com.zama.androiddeveloper_help.data.domain.UserDeck
import com.zama.androiddeveloper_help.data.domain.toDomain
import com.zama.androiddeveloper_help.data.repetition.SrsItem
import com.zama.androiddeveloper_help.data.repetition.SpacedRepetition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class DeckRepository(
    private val deckDao: UserDeckDao,
    private val cardDao: UserCardDao
) {

    fun observeDecks(): Flow<List<UserDeck>> =
        combine(deckDao.observeAll(), cardDao.observeAll()) { decks, cards ->
            val counts = cards.groupingBy { it.deckId }.eachCount()
            decks.map { it.toDomain(counts[it.id] ?: 0) }
        }

    fun observeCards(deckId: String): Flow<List<UserCard>> =
        cardDao.observeByDeck(deckId).map { cards -> cards.map { it.toDomain() } }

    fun observeAllCards(): Flow<List<UserCard>> =
        cardDao.observeAll().map { cards -> cards.map { it.toDomain() } }

    suspend fun getDeck(id: String): UserDeckEntity? = deckDao.get(id)

    suspend fun createDeck(title: String, description: String, icon: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        deckDao.upsert(
            UserDeckEntity(id = id, title = title, description = description, icon = icon, createdAt = now, updatedAt = now)
        )
        return id
    }

    suspend fun updateDeck(deck: UserDeckEntity) {
        deckDao.upsert(deck.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDeck(id: String) {
        cardDao.deleteByDeck(id)
        deckDao.delete(id)
    }

    /** Создаёт/обновляет карточку. Прогресс SRS сохраняется при редактировании текстов. */
    suspend fun upsertCard(
        deckId: String,
        cardId: String? = null,
        front: String,
        back: String,
        options: List<String> = emptyList(),
        correctIndex: Int = -1
    ): UserCard {
        val now = System.currentTimeMillis()
        val existing = cardId?.let { cardDao.get(it) }
        val entity = (existing ?: UserCardEntity(
            id = UUID.randomUUID().toString(),
            deckId = deckId,
            front = front,
            back = back,
            createdAt = now,
            updatedAt = now
        )).copy(
            front = front,
            back = back,
            options = options,
            correctIndex = correctIndex,
            updatedAt = now
        )
        cardDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun deleteCard(id: String) = cardDao.delete(id)

    suspend fun recordCardAnswer(cardId: String, correct: Boolean, nowMillis: Long) {
        val card = cardDao.get(cardId) ?: return
        val srs = SpacedRepetition.review(
            item = SrsItem(
                streak = card.streak,
                intervalDays = card.intervalDays,
                easeFactor = card.easeFactor,
                dueAtMillis = card.dueAtMillis
            ),
            correct = correct,
            nowMillis = nowMillis
        )
        cardDao.upsert(
            card.copy(
                streak = srs.streak,
                intervalDays = srs.intervalDays,
                easeFactor = srs.easeFactor,
                dueAtMillis = srs.dueAtMillis,
                lastReviewedMillis = nowMillis,
                lastCorrect = correct
            )
        )
    }

    suspend fun dueCards(nowMillis: Long): List<UserCardEntity> = cardDao.due(nowMillis)

    suspend fun dueCount(nowMillis: Long = System.currentTimeMillis()): Int =
        cardDao.dueCount(nowMillis)

    suspend fun resetDeckProgress(deckId: String) = cardDao.resetProgress(deckId)
}
