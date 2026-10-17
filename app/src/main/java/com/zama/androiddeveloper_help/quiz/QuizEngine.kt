package com.zama.androiddeveloper_help.quiz

import com.zama.androiddeveloper_help.data.Question
import com.zama.androiddeveloper_help.data.QuestionType

sealed interface AnswerResult {
    data object Correct : AnswerResult
    data object Incorrect : AnswerResult
    data class Reveal(val reference: String) : AnswerResult
}

object QuizEngine {

    fun check(question: Question, userAnswer: String?): AnswerResult = when (question.type) {
        QuestionType.MCQ, QuestionType.TRUE_FALSE -> {
            val index = userAnswer?.toIntOrNull()
            if (index != null && index.toString() == question.correctAnswer) {
                AnswerResult.Correct
            } else {
                AnswerResult.Incorrect
            }
        }

        QuestionType.INPUT -> {
            val expected = question.correctAnswer.trim().lowercase()
            val actual = userAnswer?.trim()?.lowercase()
            if (actual != null && (actual == expected || expected.contains(actual) && actual.length > 3)) {
                AnswerResult.Correct
            } else {
                AnswerResult.Incorrect
            }
        }

        QuestionType.CARD -> AnswerResult.Reveal(question.correctAnswer)
    }

    fun isAnswered(result: AnswerResult): Boolean = result != AnswerResult.Incorrect || true

    fun nextIntervalDays(previous: Int?, correct: Boolean): Int {
        if (!correct) return 0
        return when (previous) {
            null -> 1
            0 -> 1
            1 -> 3
            3 -> 7
            else -> (previous * 2.5).toInt().coerceAtMost(60)
        }
    }
}
