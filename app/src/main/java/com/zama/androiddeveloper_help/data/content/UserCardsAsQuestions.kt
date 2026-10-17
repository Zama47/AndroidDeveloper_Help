package com.zama.androiddeveloper_help.data.content

import com.zama.androiddeveloper_help.data.Question
import com.zama.androiddeveloper_help.data.QuestionType
import com.zama.androiddeveloper_help.data.domain.UserCard

/**
 * Преобразование пользовательской карточки в модель Question,
 * чтобы карточки проходили через один и тот же квиз-движок.
 */
object UserCardsAsQuestions {

    fun toQuestion(card: UserCard): Question {
        val isMcq = card.options.isNotEmpty() && card.correctIndex in card.options.indices
        return Question(
            id = "card:${card.id}",
            type = if (isMcq) QuestionType.MCQ else QuestionType.CARD,
            level = 1,
            text = card.front,
            options = if (isMcq) card.options else null,
            correctAnswer = if (isMcq) card.correctIndex.toString() else card.back,
            explanation = card.back
        )
    }

    fun toQuestions(cards: List<UserCard>): List<Question> = cards.map(::toQuestion)

    /** Извлекает id пользовательской карточки из id вопроса вида "card:<id>". */
    fun cardId(question: Question): String? =
        if (question.id.startsWith("card:")) question.id.removePrefix("card:") else null
}
