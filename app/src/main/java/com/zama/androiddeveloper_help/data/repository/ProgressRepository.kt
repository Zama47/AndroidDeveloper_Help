package com.zama.androiddeveloper_help.data.repository

import com.zama.androiddeveloper_help.data.db.QuestionProgressDao
import com.zama.androiddeveloper_help.data.db.QuestionProgressEntity
import com.zama.androiddeveloper_help.data.repetition.SrsItem
import com.zama.androiddeveloper_help.data.repetition.SpacedRepetition
import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val dao: QuestionProgressDao) {

    fun observeAll(): Flow<List<QuestionProgressEntity>> = dao.observeAll()

    suspend fun get(topicId: String, questionId: String): QuestionProgressEntity? =
        dao.get(topicId, questionId)

    suspend fun byTopic(topicId: String): List<QuestionProgressEntity> = dao.byTopic(topicId)

    suspend fun recordAnswer(
        topicId: String,
        questionId: String,
        correct: Boolean,
        nowMillis: Long
    ) {
        val current = dao.get(topicId, questionId)
        val srs = SpacedRepetition.review(
            item = SrsItem(
                streak = current?.streak ?: 0,
                intervalDays = current?.intervalDays ?: 0,
                easeFactor = current?.easeFactor ?: 2.5,
                dueAtMillis = current?.dueAtMillis ?: 0
            ),
            correct = correct,
            nowMillis = nowMillis
        )
        dao.upsert(
            (current ?: QuestionProgressEntity(topicId = topicId, questionId = questionId)).copy(
                correctCount = (current?.correctCount ?: 0) + if (correct) 1 else 0,
                incorrectCount = (current?.incorrectCount ?: 0) + if (correct) 0 else 1,
                streak = srs.streak,
                intervalDays = srs.intervalDays,
                easeFactor = srs.easeFactor,
                dueAtMillis = srs.dueAtMillis,
                lastReviewedMillis = nowMillis,
                lastCorrect = correct
            )
        )
    }

    suspend fun dueQuestions(nowMillis: Long): List<QuestionProgressEntity> = dao.due(nowMillis)

    fun observeDueCount(nowMillis: Long = System.currentTimeMillis()): Flow<Int> =
        dao.observeDueCount(nowMillis)
}
