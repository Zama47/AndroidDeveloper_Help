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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zama.androiddeveloper_help.data.ContentRepository
import com.zama.androiddeveloper_help.quiz.MainViewModel
import com.zama.androiddeveloper_help.ui.ArticleScreen
import com.zama.androiddeveloper_help.ui.HomeScreen
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

class MainViewModelFactory(private val repository: ContentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(repository) as T
    }
}

@Composable
fun AppNav(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(ContentRepository(androidx.compose.ui.platform.LocalContext.current))
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
                HomeScreen(
                    topics = home.topics,
                    loading = home.loading,
                    error = home.error,
                    onTopicClick = { topic ->
                        viewModel.loadArticle(topic.id)
                        viewModel.startQuiz(topic.id)
                        navController.navigate("topic/${topic.id}")
                    }
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
