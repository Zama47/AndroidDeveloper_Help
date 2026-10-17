package com.zama.androiddeveloper_help

import com.zama.androiddeveloper_help.data.Question
import com.zama.androiddeveloper_help.data.QuestionType
import com.zama.androiddeveloper_help.quiz.AnswerResult
import com.zama.androiddeveloper_help.quiz.QuizEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizEngineTest {

    private fun mcq(correct: String) = Question(
        id = "q1",
        type = QuestionType.MCQ,
        level = 2,
        text = "What?",
        options = listOf("A", "B", "C"),
        correctAnswer = correct,
        explanation = "Because"
    )

    @Test
    fun `mcq correct answer`() {
        val result = QuizEngine.check(mcq("1"), "1")
        assertEquals(AnswerResult.Correct, result)
    }

    @Test
    fun `mcq wrong answer`() {
        val result = QuizEngine.check(mcq("1"), "2")
        assertEquals(AnswerResult.Incorrect, result)
    }

    @Test
    fun `mcq null answer is incorrect`() {
        val result = QuizEngine.check(mcq("1"), null)
        assertEquals(AnswerResult.Incorrect, result)
    }

    @Test
    fun `true_false correct`() {
        val question = Question(
            id = "q2",
            type = QuestionType.TRUE_FALSE,
            level = 1,
            text = "Statement",
            options = listOf("Верно", "Неверно"),
            correctAnswer = "0",
            explanation = "Yes"
        )
        assertEquals(AnswerResult.Correct, QuizEngine.check(question, "0"))
        assertEquals(AnswerResult.Incorrect, QuizEngine.check(question, "1"))
    }

    @Test
    fun `input exact match`() {
        val question = Question(
            id = "q3",
            type = QuestionType.INPUT,
            level = 1,
            text = "Term?",
            correctAnswer = "consistent hashing",
            explanation = "Ring"
        )
        assertEquals(AnswerResult.Correct, QuizEngine.check(question, "Consistent Hashing"))
        assertEquals(AnswerResult.Correct, QuizEngine.check(question, "consistent hashing"))
        assertEquals(AnswerResult.Incorrect, QuizEngine.check(question, "mod hashing"))
    }

    @Test
    fun `input partial match with long answer`() {
        val question = Question(
            id = "q4",
            type = QuestionType.INPUT,
            level = 2,
            text = "Budget?",
            correctAnswer = "16.6",
            explanation = "60fps"
        )
        assertEquals(AnswerResult.Correct, QuizEngine.check(question, "16.6"))
        assertEquals(AnswerResult.Incorrect, QuizEngine.check(question, "17"))
    }

    @Test
    fun `card always reveals`() {
        val question = Question(
            id = "q5",
            type = QuestionType.CARD,
            level = 3,
            text = "Design?",
            correctAnswer = "Do this",
            explanation = "Steps"
        )
        val result = QuizEngine.check(question, null)
        assertTrue(result is AnswerResult.Reveal)
        assertEquals("Do this", (result as AnswerResult.Reveal).reference)
    }

    @Test
    fun `next interval grows on correct and resets on wrong`() {
        assertEquals(1, QuizEngine.nextIntervalDays(null, correct = true))
        assertEquals(3, QuizEngine.nextIntervalDays(1, correct = true))
        assertEquals(7, QuizEngine.nextIntervalDays(3, correct = true))
        assertEquals(17, QuizEngine.nextIntervalDays(7, correct = true))
        assertEquals(0, QuizEngine.nextIntervalDays(7, correct = false))
    }

    @Test
    fun `next interval capped at 60`() {
        assertEquals(60, QuizEngine.nextIntervalDays(30, correct = true))
    }
}
