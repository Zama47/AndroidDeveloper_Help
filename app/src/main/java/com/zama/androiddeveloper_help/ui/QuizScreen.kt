package com.zama.androiddeveloper_help.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.zama.androiddeveloper_help.data.QuestionType
import com.zama.androiddeveloper_help.quiz.MainViewModel
import com.zama.androiddeveloper_help.quiz.QuizUiState

@Composable
fun QuizScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.quiz.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            state.loading -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator()
            }
            state.error != null -> Text(
                text = "Ошибка: ${state.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
            state.finished -> QuizResult(
                state = state,
                onRestart = viewModel::restartQuiz,
                onBack = onBack
            )
            else -> QuizContent(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun QuizContent(state: QuizUiState, viewModel: MainViewModel) {
    val question = state.questions.getOrNull(state.currentIndex) ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Вопрос ${state.currentIndex + 1} из ${state.questions.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                LevelDots(level = question.level)
            }
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1f) / state.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Markdown(
                content = question.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
            when (question.type) {
                QuestionType.MCQ, QuestionType.TRUE_FALSE -> {
                    question.options?.forEachIndexed { index, option ->
                        OptionRow(
                            index = index,
                            text = option,
                            selected = state.selectedOption == index,
                            submitted = state.submitted,
                            isCorrect = index.toString() == question.correctAnswer,
                            onClick = { viewModel.selectOption(index) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                QuestionType.INPUT -> {
                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = viewModel::onInputChange,
                        label = { Text("Ваш ответ") },
                        enabled = !state.submitted,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                QuestionType.CARD -> {
                    Text(
                        text = "Открытый вопрос. Сформулируйте ответ мысленно, затем откройте эталон и оцените себя честно.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (state.submitted) {
                Spacer(Modifier.height(16.dp))
                ExplanationCard(
                    question = question,
                    revealed = state.revealed,
                    lastCorrect = state.lastCorrect
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            when {
                !state.submitted -> Button(
                    onClick = viewModel::submitAnswer,
                    enabled = when (question.type) {
                        QuestionType.MCQ, QuestionType.TRUE_FALSE -> state.selectedOption != null
                        QuestionType.INPUT -> state.inputText.isNotBlank()
                        QuestionType.CARD -> true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (question.type == QuestionType.CARD) "Показать ответ" else "Ответить")
                }
                question.type == QuestionType.CARD && state.lastCorrect == null -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.markSelfAssessed(knewIt = false) }, modifier = Modifier.weight(1f)) {
                            Text("Не знал")
                        }
                        Button(onClick = { viewModel.markSelfAssessed(knewIt = true) }, modifier = Modifier.weight(1f)) {
                            Text("Знал")
                        }
                    }
                }
                else -> Button(
                    onClick = viewModel::nextQuestion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.currentIndex + 1 >= state.questions.size) "Завершить" else "Дальше")
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    index: Int,
    text: String,
    selected: Boolean,
    submitted: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val container = when {
        submitted && isCorrect -> MaterialTheme.colorScheme.primaryContainer
        submitted && selected -> MaterialTheme.colorScheme.errorContainer
        selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Card(
        onClick = onClick,
        enabled = !submitted,
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${'a' + index})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ExplanationCard(
    question: com.zama.androiddeveloper_help.data.Question,
    revealed: Boolean,
    lastCorrect: Boolean?
) {
    val container = when (lastCorrect) {
        true -> MaterialTheme.colorScheme.primaryContainer
        false -> MaterialTheme.colorScheme.errorContainer
        null -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (lastCorrect) {
                true -> Text("Верно!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                false -> Text("Неверно", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                null -> {}
            }
            if (revealed) {
                Spacer(Modifier.height(8.dp))
                Markdown(
                    content = question.correctAnswer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
            Markdown(
                content = question.explanation,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuizResult(state: QuizUiState, onRestart: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Готово!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "${state.correctCount} из ${state.questions.size}",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        val total = state.questions.size
        val percent = if (total > 0) state.correctCount * 100 / total else 0
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Пройти заново")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("К темам")
        }
    }
}

@Composable
private fun LevelDots(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            FilterChip(
                selected = i < level,
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = when (i) {
                            0 -> "база"
                            1 -> "сред."
                            else -> "слож."
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
    }
}
