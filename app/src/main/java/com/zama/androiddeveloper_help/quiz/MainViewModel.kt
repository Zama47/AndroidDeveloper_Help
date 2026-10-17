package com.zama.androiddeveloper_help.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zama.androiddeveloper_help.data.ContentRepository
import com.zama.androiddeveloper_help.data.Question
import com.zama.androiddeveloper_help.data.QuestionType
import com.zama.androiddeveloper_help.data.Topic
import com.zama.androiddeveloper_help.data.content.UserCardsAsQuestions
import com.zama.androiddeveloper_help.data.domain.UserCard
import com.zama.androiddeveloper_help.data.domain.UserDeck
import com.zama.androiddeveloper_help.data.repository.DeckRepository
import com.zama.androiddeveloper_help.data.repository.ProfileRepository
import com.zama.androiddeveloper_help.data.repository.ProgressRepository
import com.zama.androiddeveloper_help.gamification.Gamification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class QuizUiState(
    val topicId: String? = null,
    val deckId: String? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOption: Int? = null,
    val inputText: String = "",
    val submitted: Boolean = false,
    val lastCorrect: Boolean? = null,
    val revealed: Boolean = false,
    val correctCount: Int = 0,
    val xpGained: Long = 0,
    val finished: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

data class HomeUiState(
    val topics: List<Topic> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val decks: List<UserDeck> = emptyList()
)

class MainViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: ProgressRepository,
    private val deckRepository: DeckRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _quiz = MutableStateFlow(QuizUiState())
    val quiz: StateFlow<QuizUiState> = _quiz.asStateFlow()

    private val _article = MutableStateFlow<ArticleUiState>(ArticleUiState())
    val article: StateFlow<ArticleUiState> = _article.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val topics = contentRepository.loadTopics()
                _home.update { HomeUiState(topics = topics, loading = false) }
            } catch (e: Exception) {
                _home.update { HomeUiState(loading = false, error = e.message) }
            }
        }
        viewModelScope.launch {
            deckRepository.observeDecks().collect { decks ->
                _home.update { it.copy(decks = decks) }
            }
        }
    }

    fun startQuiz(topicId: String) {
        viewModelScope.launch {
            _quiz.update { QuizUiState(topicId = topicId, loading = true) }
            try {
                val questions = contentRepository.loadQuestions(topicId)
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
        val state = _quiz.value
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val userAnswer = when (question.type) {
            QuestionType.MCQ, QuestionType.TRUE_FALSE -> state.selectedOption?.toString()
            QuestionType.INPUT -> state.inputText
            QuestionType.CARD -> null
        }
        when (val result = QuizEngine.check(question, userAnswer)) {
            is AnswerResult.Reveal -> _quiz.update { it.copy(submitted = true, revealed = true) }
            is AnswerResult.Correct -> applyAnswer(correct = true)
            is AnswerResult.Incorrect -> applyAnswer(correct = false)
        }
    }

    fun markSelfAssessed(knewIt: Boolean) {
        applyAnswer(correct = knewIt)
    }

    private fun applyAnswer(correct: Boolean) {
        val state = _quiz.value
        val question = state.questions.getOrNull(state.currentIndex) ?: return
        val topicId = state.topicId
        val xp = if (correct) Gamification.XP_PER_CORRECT else Gamification.XP_PER_INCORRECT
        _quiz.update {
            it.copy(
                submitted = true,
                lastCorrect = correct,
                correctCount = it.correctCount + if (correct) 1 else 0,
                xpGained = it.xpGained + xp
            )
        }
        viewModelScope.launch {
            if (state.deckId != null) {
                UserCardsAsQuestions.cardId(question)?.let { cardId ->
                    deckRepository.recordCardAnswer(cardId, correct, System.currentTimeMillis())
                }
            } else if (topicId != null) {
                progressRepository.recordAnswer(topicId, question.id, correct, System.currentTimeMillis())
            }
            profileRepository.addXp(xp)
        }
    }

    fun startUserQuiz(deckId: String) {
        viewModelScope.launch {
            _quiz.update { QuizUiState(deckId = deckId, loading = true) }
            val cards = deckRepository.observeCards(deckId).first()
            val questions = UserCardsAsQuestions.toQuestions(cards)
            _quiz.update { it.copy(questions = questions, loading = false, finished = questions.isEmpty()) }
        }
    }

    fun nextQuestion() {
        _quiz.update { state ->
            val next = state.currentIndex + 1
            if (next >= state.questions.size) {
                finishSession(state)
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

    private fun finishSession(state: QuizUiState): QuizUiState {
        viewModelScope.launch {
            profileRepository.touchStreak(LocalDate.now())
            val profile = profileRepository.current()
            val bonus = Gamification.sessionXp(state.correctCount, state.questions.size, profile.currentStreak)
            profileRepository.addXp(bonus)
            if (bonus > 0) {
                _quiz.update { if (it.finished) it.copy(xpGained = it.xpGained + bonus) else it }
            }
        }
        return state.copy(finished = true)
    }

    fun restartQuiz() {
        val state = _quiz.value
        val deckId = state.deckId
        val topicId = state.topicId
        when {
            deckId != null -> startUserQuiz(deckId)
            topicId != null -> startQuiz(topicId)
        }
    }

    fun observeDeck(deckId: String): Flow<UserDeck?> =
        deckRepository.observeDecks().map { decks -> decks.find { it.id == deckId } }

    fun observeDeckCards(deckId: String): Flow<List<UserCard>> = deckRepository.observeCards(deckId)

    fun createDeck(title: String, description: String, icon: String = "layers") {
        viewModelScope.launch { deckRepository.createDeck(title, description, icon) }
    }

    fun renameDeck(deckId: String, title: String, description: String) {
        viewModelScope.launch {
            val entity = deckRepository.getDeck(deckId)
            if (entity != null) {
                deckRepository.updateDeck(entity.copy(title = title, description = description))
            }
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch { deckRepository.deleteDeck(deckId) }
    }

    fun saveCard(
        deckId: String,
        cardId: String?,
        front: String,
        back: String,
        options: List<String> = emptyList(),
        correctIndex: Int = -1
    ) {
        viewModelScope.launch {
            val isNew = cardId == null
            deckRepository.upsertCard(
                deckId = deckId,
                cardId = cardId,
                front = front,
                back = back,
                options = options,
                correctIndex = correctIndex
            )
            if (isNew) {
                profileRepository.addXp(Gamification.XP_PER_CARD_CREATE)
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch { deckRepository.deleteCard(cardId) }
    }

    fun resetDeckProgress(deckId: String) {
        viewModelScope.launch { deckRepository.resetDeckProgress(deckId) }
    }

    fun loadArticle(fileName: String) {
        viewModelScope.launch {
            _article.update { ArticleUiState(loading = true) }
            try {
                val text = contentRepository.loadArticle(fileName)
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
