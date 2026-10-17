package com.zama.androiddeveloper_help.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopicsFile(
    val version: Int,
    val topics: List<Topic>
)

@Serializable
data class Topic(
    val id: String,
    @SerialName("parentId") val parentId: String? = null,
    val title: String,
    val description: String,
    val level: Int,
    @SerialName("orderIndex") val orderIndex: Int,
    val icon: String
)

@Serializable
data class QuestionsFile(
    val version: Int,
    val topic: String,
    val questions: List<Question>
)

@Serializable
data class Question(
    val id: String,
    val type: QuestionType,
    val level: Int,
    val text: String,
    val options: List<String>? = null,
    val correctAnswer: String,
    val explanation: String,
    val tags: List<String> = emptyList(),
    val difficulty: Double? = null
)

@Serializable
enum class QuestionType {
    @SerialName("MCQ") MCQ,
    @SerialName("TRUE_FALSE") TRUE_FALSE,
    @SerialName("INPUT") INPUT,
    @SerialName("CARD") CARD
}
