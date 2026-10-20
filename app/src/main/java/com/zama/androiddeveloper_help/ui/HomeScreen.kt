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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zama.androiddeveloper_help.data.Topic

@Composable
fun HomeScreen(
    topics: List<Topic>,
    loading: Boolean,
    error: String?,
    onTopicClick: (Topic) -> Unit,
    onDecksClick: () -> Unit,
    deckCount: Int = 0,
    onProfileClick: () -> Unit = {},
    dueCount: Int = 0,
    onReviewClick: () -> Unit = {},
    onSolyankaClick: () -> Unit = {},
    topicStats: Map<String, com.zama.androiddeveloper_help.quiz.TopicStats> = emptyMap(),
    profile: com.zama.androiddeveloper_help.quiz.ProfileUiState =
        com.zama.androiddeveloper_help.quiz.ProfileUiState(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        val parents = topics.filter { it.parentId == null }.sortedBy { it.orderIndex }
        val childrenByParent = topics.filter { it.parentId != null }.groupBy { it.parentId!! }
        val groupedTopics: List<Pair<Topic, List<Topic>>> = parents.map { parent ->
            parent to childrenByParent[parent.id].orEmpty().sortedBy { it.orderIndex }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Android Developer Help",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp)
            )
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = "🧑‍💻", style = MaterialTheme.typography.titleLarge)
            }
        }
        Text(
            text = "Подготовка к собеседованию: теория и вопросы по темам",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when {
            loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            }
            error != null -> {
                Text(
                    text = "Ошибка загрузки: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    HeroCard(
                        profile = profile,
                        dueCount = dueCount,
                        onOpenLevel = onProfileClick
                    )
                }
                item {
                    ReviewCard(dueCount = dueCount, onClick = onReviewClick)
                }
                item {
                    MixedCard(onClick = onSolyankaClick)
                }
                item {
                    MyDecksCard(deckCount = deckCount, onClick = onDecksClick)
                }
                item {
                    Text(
                        text = "Темы для подготовки",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(groupedTopics, key = { it.first.id }) { (parent, children) ->
                    TopicGroup(
                        parent = parent,
                        children = children,
                        stats = topicStats,
                        onParentClick = { onTopicClick(parent) },
                        onChildClick = onTopicClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TopicGroup(
    parent: Topic,
    children: List<Topic>,
    stats: Map<String, com.zama.androiddeveloper_help.quiz.TopicStats>,
    onParentClick: () -> Unit,
    onChildClick: (Topic) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Column {
        Card(
            onClick = onParentClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = topicIcon(parent.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = parent.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LevelStars(level = parent.level)
                    }
                    Text(
                        text = if (children.isEmpty()) parent.description
                        else "${children.size} ${pluralSubsections(children.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (children.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        if (expanded) {
            children.forEach { child ->
                ChildTopicRow(
                    child = child,
                    stats = stats[child.id],
                    onClick = { onChildClick(child) }
                )
            }
        }
    }
}

private fun pluralSubsections(count: Int): String = when {
    count % 10 == 1 && count % 100 != 11 -> "подраздел"
    count % 10 in 2..4 && (count % 100 < 10 || count % 100 > 20) -> "подраздела"
    else -> "подразделов"
}

@Composable
private fun ChildTopicRow(
    child: Topic,
    stats: com.zama.androiddeveloper_help.quiz.TopicStats?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.SubdirectoryArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = child.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stats?.let { "Решено ${it.solved} из ${it.total}" } ?: child.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroCard(
    profile: com.zama.androiddeveloper_help.quiz.ProfileUiState,
    dueCount: Int,
    onOpenLevel: () -> Unit
) {
    Card(
        onClick = onOpenLevel,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧑‍💻", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Уровень ${profile.level}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${profile.profile.totalXp} XP • ещё ${profile.xpToNextLevel} до уровня ${profile.level + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔥${profile.profile.currentStreak}", style = MaterialTheme.typography.titleLarge)
                    if (dueCount > 0) {
                        Text("🔁$dueCount", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { profile.levelProgress },
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )
        }
    }
}

@Composable
private fun MixedCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🎲", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Солянка",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Перемешать вопросы из разных тем",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(dueCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (dueCount > 0) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔁", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (dueCount > 0) "Повторение: $dueCount на сегодня"
                else "Повторение — всё по расписанию",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MyDecksCard(deckCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🗂", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Мои колоды",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (deckCount > 0) "$deckCount ${if (deckCount == 1) "колода" else "колод"} — свои тесты и карточки"
                    else "Создай свои тесты и карточки",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun TopicCard(
    topic: Topic,
    stats: com.zama.androiddeveloper_help.quiz.TopicStats?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = topicIcon(topic.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LevelStars(level = topic.level)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topic.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stats != null && stats.total > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { stats.solved.toFloat() / stats.total },
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (stats.solved == 0) "${
                            stats.total
                        } вопросов ждут первого ответа"
                        else "Решено ${stats.solved} из ${stats.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelStars(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { i ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (i < level) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

private fun topicIcon(name: String): ImageVector = when (name) {
    "code" -> Icons.Filled.Code
    "layers" -> Icons.Filled.Layers
    "phone_android" -> Icons.Filled.PhoneAndroid
    "speed" -> Icons.Filled.Speed
    else -> Icons.Filled.CheckCircle
}
