package com.zama.androiddeveloper_help

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zama.androiddeveloper_help.quiz.MainViewModel
import com.zama.androiddeveloper_help.ui.ArticleScreen
import com.zama.androiddeveloper_help.ui.DeckEditorScreen
import com.zama.androiddeveloper_help.ui.DecksScreen
import com.zama.androiddeveloper_help.ui.HomeScreen
import com.zama.androiddeveloper_help.ui.ProfileScreen
import com.zama.androiddeveloper_help.ui.ReviewScreen
import com.zama.androiddeveloper_help.ui.QuizScreen
import com.zama.androiddeveloper_help.ui.TopicScreen
import com.zama.androiddeveloper_help.ui.theme.AndroidDeveloper_HelpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidDeveloper_HelpTheme {
                AppNav()
            }
        }
    }
}

class MainViewModelFactory(private val app: App) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(
            contentRepository = app.contentRepository,
            progressRepository = app.progressRepository,
            deckRepository = app.deckRepository,
            profileRepository = app.profileRepository
        ) as T
    }
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(LocalContext.current.applicationContext as App)
    )
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val home by viewModel.home.collectAsState()
                val dueCount by viewModel.dueCount.collectAsState()
                HomeScreen(
                    topics = home.topics,
                    loading = home.loading,
                    error = home.error,
                    onTopicClick = { topic ->
                        viewModel.loadArticle(topic.id)
                        viewModel.startQuiz(topic.id)
                        navController.navigate("topic/${topic.id}")
                    },
                    onDecksClick = { navController.navigate("decks") },
                    deckCount = home.decks.size,
                    onProfileClick = { navController.navigate("profile") },
                    dueCount = dueCount,
                    onReviewClick = { navController.navigate("review") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("decks") {
                val home by viewModel.home.collectAsState()
                DecksScreen(
                    decks = home.decks,
                    onBack = { navController.popBackStack() },
                    onDeckClick = { deck -> navController.navigate("deck/${deck.id}") },
                    onCreateDeck = { title, description -> viewModel.createDeck(title, description) }
                )
            }
            composable("deck/{deckId}") { entry ->
                val deckId = entry.arguments?.getString("deckId") ?: return@composable
                val deck by viewModel.observeDeck(deckId)
                    .collectAsState(initial = null)
                val cards by viewModel.observeDeckCards(deckId)
                    .collectAsState(initial = emptyList())

                DeckEditorScreen(
                    deck = deck,
                    cards = cards,
                    onBack = { navController.popBackStack() },
                    onDeckSave = { title, description -> viewModel.renameDeck(deckId, title, description) },
                    onDeckDelete = {
                        viewModel.deleteDeck(deckId)
                        navController.popBackStack("decks", false)
                    },
                    onDeckResetProgress = { viewModel.resetDeckProgress(deckId) },
                    onCardSave = { cardId, front, back, options, correctIndex ->
                        viewModel.saveCard(deckId, cardId, front, back, options, correctIndex)
                    },
                    onCardDelete = { cardId -> viewModel.deleteCard(cardId) },
                    onStartQuiz = {
                        viewModel.startUserQuiz(deckId)
                        navController.navigate("quiz/$deckId")
                    }
                )
            }
            composable("quiz/{deckId}") { entry ->
                QuizScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack("decks", false) }
                )
            }
            composable("review") {
                ReviewScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.exitReview(); navController.popBackStack() }
                )
            }
            composable("topic/{topicId}") { entry ->
                val topicId = entry.arguments?.getString("topicId") ?: return@composable
                val home by viewModel.home.collectAsState()
                val topic = home.topics.find { it.id == topicId } ?: return@composable

                TopicScreen(
                    title = topic.title,
                    onBack = { navController.popBackStack() },
                    articleContent = { ArticleScreen(viewModel = viewModel) },
                    quizContent = {
                        QuizScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack("home", false) }
                        )
                    }
                )
            }
        }
    }
}
