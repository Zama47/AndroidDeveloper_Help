package com.zama.androiddeveloper_help.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ContentRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun loadTopics(): List<Topic> = withContext(Dispatchers.IO) {
        val raw = context.assets.open("content/topics.json")
            .bufferedReader()
            .use { it.readText() }
        json.decodeFromString<TopicsFile>(raw).topics.sortedBy { it.orderIndex }
    }

    suspend fun loadQuestions(topicId: String): List<Question> = withContext(Dispatchers.IO) {
        val candidates = listOf(
            "content/$topicId.json",
            "content/${topicId.replace('_', '-')}.json",
            "content/${topicId.replace('-', '_')}.json"
        )
        val raw = candidates.firstNotNullOfOrNull { path ->
            runCatching { context.assets.open(path) }.getOrNull()
        }?.bufferedReader()?.use { it.readText() }
            ?: throw java.io.FileNotFoundException("No questions file for topic '$topicId'")
        json.decodeFromString<QuestionsFile>(raw).questions
    }

    suspend fun loadArticle(articleName: String): String = withContext(Dispatchers.IO) {
        val candidates = listOf(
            "content/articles/$articleName.md",
            "content/articles/${articleName.replace('_', '-')}.md",
            "content/articles/${articleName.replace('-', '_')}.md"
        )
        val path = candidates.firstOrNull { path ->
            runCatching { context.assets.open(path).close() }.isSuccess
        } ?: candidates.first()
        context.assets.open(path)
            .bufferedReader()
            .use { it.readText() }
    }
}
