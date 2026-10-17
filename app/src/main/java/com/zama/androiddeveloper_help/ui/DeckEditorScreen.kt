package com.zama.androiddeveloper_help.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.Icons.AutoMirrored.Filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zama.androiddeveloper_help.data.domain.UserCard
import com.zama.androiddeveloper_help.data.domain.UserDeck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckEditorScreen(
    deck: UserDeck?,
    cards: List<UserCard>,
    onBack: () -> Unit,
    onDeckSave: (title: String, description: String) -> Unit,
    onDeckDelete: () -> Unit,
    onDeckResetProgress: () -> Unit,
    onCardSave: (cardId: String?, front: String, back: String, options: List<String>, correctIndex: Int) -> Unit,
    onCardDelete: (String) -> Unit,
    onStartQuiz: () -> Unit
) {
    var showCardDialog by remember { mutableStateOf(false) }
    var editingCardId by remember { mutableStateOf<String?>(null) }
    var showDeckFields by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck?.title ?: "Колода") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                actions = {
                    IconButton(onClick = onDeckResetProgress) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Сбросить прогресс")
                    }
                    IconButton(onClick = onDeckDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить колоду")
                    }
                }
            )
        },
        bottomBar = {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onStartQuiz() },
                    enabled = cards.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Пройти тест")
                }
                Button(
                    onClick = {
                        editingCardId = null
                        showCardDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${
                        if (cards.size == 1) "1 карточка" else "${cards.size} карточек"
                    }",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                FilterChip(
                    selected = showDeckFields,
                    onClick = { showDeckFields = !showDeckFields },
                    label = { Text("Изменить колоду") }
                )
            }
            if (showDeckFields && deck != null) {
                DeckFields(deck = deck, onSave = {
                    onDeckSave(it.first, it.second)
                    showDeckFields = false
                })
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cards, key = { it.id }) { card ->
                    CardRow(
                        card = card,
                        onEdit = {
                            editingCardId = card.id
                            showCardDialog = true
                        },
                        onDelete = { onCardDelete(card.id) }
                    )
                }
            }
        }
    }

    if (showCardDialog) {
        CardEditDialog(
            card = cards.find { it.id == editingCardId },
            onDismiss = { showCardDialog = false },
            onSave = { front, back, options, correctIndex ->
                onCardSave(editingCardId, front, back, options, correctIndex)
                showCardDialog = false
            }
        )
    }
}

@Composable
private fun DeckFields(deck: UserDeck, onSave: (Pair<String, String>) -> Unit) {
    var title by remember { mutableStateOf(deck.title) }
    var description by remember { mutableStateOf(deck.description) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") }
            )
            Button(
                onClick = { onSave(title.trim() to description.trim()) },
                enabled = title.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) { Text("Сохранить") }
        }
    }
}

@Composable
private fun CardRow(card: UserCard, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = card.front,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (card.options.isEmpty()) card.back else card.options.getOrNull(card.correctIndex) ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (card.options.isEmpty()) "карточка"
                    else "вопрос (${card.options.size} вариантов)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Удалить карточку",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardEditDialog(
    card: UserCard?,
    onDismiss: () -> Unit,
    onSave: (front: String, back: String, options: List<String>, correctIndex: Int) -> Unit
) {
    var front by remember { mutableStateOf(card?.front ?: "") }
    var back by remember { mutableStateOf(card?.back ?: "") }
    var isMcq by remember { mutableStateOf(card?.options?.isNotEmpty() == true) }
    val options = remember { mutableStateListOf<String>().apply { addAll(card?.options ?: listOf("", "")) } }
    var correctIndex by remember { mutableStateOf(card?.correctIndex?.takeIf { it >= 0 } ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (card == null) "Новая карточка" else "Изменить карточку") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Вопрос / лицевая сторона") },
                    minLines = 2
                )
                FilterChip(
                    selected = isMcq,
                    onClick = { isMcq = !isMcq },
                    label = { Text("С вариантами ответа") }
                )
                if (isMcq) {
                    options.forEachIndexed { i, _ ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = correctIndex == i,
                                onClick = { correctIndex = i },
                                label = { Text("верно") }
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = options[i],
                                onValueChange = { if (i < options.size) options[i] = it },
                                label = { Text("Вариант ${'a' + i}") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { if (options.size > 2 && correctIndex > 0) correctIndex-- },
                                enabled = options.size > 2
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Удалить вариант",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    if (options.size < 4) {
                        TextButton(onClick = { options.add("") }) { Text("+ вариант") }
                    }
                } else {
                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        label = { Text("Ответ / обратная сторона") },
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = front.isNotBlank() && (isMcq.not() || options.all { it.isNotBlank() }),
                onClick = {
                    if (isMcq) {
                        onSave(front.trim(), back.trim(), options.toList(), correctIndex)
                    } else {
                        onSave(front.trim(), back.trim(), emptyList(), -1)
                    }
                }
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
