package com.zama.androiddeveloper_help.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zama.androiddeveloper_help.data.ContentRepository
import com.zama.androiddeveloper_help.data.Question
import com.zama.androiddeveloper_help.data.QuestionType
import com.zama.androiddeveloper_help.data.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val topicId: String? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: Int? = null,
    val inputText: String = "",
    val submitted: Boolean = false,
    val lastCorrect: Boolean? = null,
    val revealed: Boolean = false,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

data class HomeUiState(
    val topics: List<Topic> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class MainViewModel(private val repository: ContentRepository) : ViewModel() {

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _quiz = MutableStateFlow(QuizUiState())
    val quiz: StateFlow<QuizUiState> = _quiz.asStateFlow()

    private val _article = MutableStateFlow<ArticleUiState>(ArticleUiState())
    val article: StateFlow<ArticleUiState> = _article.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val topics = repository.loadTopics()
                _home.update { HomeUiState(topics = topics, loading = false) }
            } catch (e: Exception) {
                _home.update { HomeUiState(loading = false, error = e.message) }
            }
        }
    }

    fun startQuiz(topicId: String) {
        viewModelScope.launch {
            _quiz.update { QuizUiState(topicId = topicId, loading = true) }
            try {
                val questions = repository.loadQuestions(topicId)
                _quiz.update { it.copy(questions = questions, loading = false, finished = questions.isEmpty()) }
            } catch (e: Exception) {
                _quiz.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun selectOption(index: Int) {
        _quiz.update { if (it.submitted) it else it.copy(selectedOption = index) }
    }

    fun onInputChange(text: String) {
        _quiz.update { if (it.submitted) it else it.copy(inputText = text) }
    }

    fun submitAnswer() {
        _quiz.update { state ->
            val question = state.questions.getOrNull(state.currentIndex) ?: return@update state
            val userAnswer = when (question.type) {
                QuestionType.MCQ, QuestionType.TRUE_FALSE -> state.selectedOption?.toString()
                QuestionType.INPUT -> state.inputText
                QuestionType.CARD -> null
            }
            when (val result = QuizEngine.check(question, userAnswer)) {
                is AnswerResult.Reveal -> state.copy(submitted = true, revealed = true)
                AnswerResult.Correct -> state.copy(
                    submitted = true,
                    lastCorrect = true,
                    correctCount = state.correctCount + 1
                )
                AnswerResult.Incorrect -> state.copy(submitted = true, lastCorrect = false)
            }
        }
    }

    fun markSelfAssessed(knewIt: Boolean) {
        _quiz.update { state ->
            state.copy(
                submitted = true,
                lastCorrect = knewIt,
                correctCount = state.correctCount + if (knewIt) 1 else 0
            )
        }
    }

    fun nextQuestion() {
        _quiz.update { state ->
            val next = state.currentIndex + 1
            if (next >= state.questions.size) {
                state.copy(finished = true)
            } else {
                state.copy(
                    currentIndex = next,
                    selectedOption = null,
                    inputText = "",
                    submitted = false,
                    lastCorrect = null,
                    revealed = false
                )
            }
        }
    }

    fun restartQuiz() {
        val topicId = _quiz.value.topicId ?: return
        startQuiz(topicId)
    }

    fun loadArticle(fileName: String) {
        viewModelScope.launch {
            _article.update { ArticleUiState(loading = true) }
            try {
                val text = repository.loadArticle(fileName)
                _article.update { ArticleUiState(content = text, loading = false) }
            } catch (e: Exception) {
                _article.update { ArticleUiState(loading = false, error = e.message) }
            }
        }
    }
}

data class ArticleUiState(
    val content: String = "",
    val loading: Boolean = false,
    val error: String? = null
)
