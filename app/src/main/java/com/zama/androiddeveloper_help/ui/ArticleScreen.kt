package com.zama.androiddeveloper_help.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.zama.androiddeveloper_help.quiz.MainViewModel

@Composable
fun ArticleScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.article.collectAsStateWithLifecycle()

    when {
        state.loading -> Column(
            modifier = modifier.fillMaxSize(),
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
        else -> MarkdownSections(
            markdown = state.content,
            modifier = modifier
        )
    }
}

@Composable
private fun MarkdownSections(markdown: String, modifier: Modifier = Modifier) {
    val sections = splitBySecondLevelHeadings(markdown)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
    ) {
        items(sections) { section ->
            Markdown(
                content = section,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

internal fun splitBySecondLevelHeadings(markdown: String): List<String> {
    val lines = markdown.lines()
    val sections = mutableListOf<StringBuilder>()
    var current = StringBuilder()
    for (line in lines) {
        if (line.startsWith("## ") && current.isNotBlank()) {
            sections.add(current)
            current = StringBuilder()
        }
        current.appendLine(line)
    }
    if (current.isNotBlank()) sections.add(current)
    return sections.map { it.toString().trim() }.filter { it.isNotBlank() }
}

@Composable
fun InlineMarkdownText(text: String, modifier: Modifier = Modifier) {
    Markdown(
        content = text,
        modifier = modifier
    )
}

@Composable
fun MarkdownHeading1(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
}
