# Jetpack Compose: декларативный UI

## Модель: UI = f(state)

В XML вы говорили «что сделать» (findViewById, setText, notify). В Compose вы описываете, **какой UI соответствует состоянию**, а фреймворк сам обновляет изменившиеся части:

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("Нажато $count раз") }
}
```

## Композиция и рекомпозиция

- **Композиция** — первый вызов composable-функций, построение дерева UI.
- **Recomposition** — перезапуск только тех функций, чьи входные данные изменились; остальные пропускаются (skipping).
- Compose помнит, какие `State` читались в каждой функции, и помечает их устаревшими при изменении.

## Состояние

```kotlin
var name by remember { mutableStateOf("") }        // живёт в композиции
var scroll by rememberSaveable { mutableStateOf(0) } // переживает поворот
val state by viewModel.uiState.collectAsStateWithLifecycle() // из ViewModel
```

**Правила:**

1. Без `remember` состояние сбрасывается каждую рекомпозицию.
2. `remember` не переживает поворот и гибель процесса → `rememberSaveable` или ViewModel.
3. Источник истины — ViewModel (переживает configuration change).
4. `derivedStateOf` — для производных значений, чтобы не будить UI на каждый чих.
5. `equals` у `data class` состояния обязателен — StateFlow и skipping это используют.

## State hoisting

Composable не владеет состоянием, а принимает `value` + `onValueChange`:

```kotlin
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(value = query, onValueChange = onQueryChange)
}
```

Выигрыш: stateless → переиспользуем, тестируем, один источник истины.

## Эффекты

| Эффект | Назначение |
|--------|-----------|
| `LaunchedEffect(key) { }` | suspend-логика при входе; перезапуск при смене ключа, отмена при выходе |
| `DisposableEffect(key) { onDispose { } }` | регистрация listener/подписки с гарантированным cleanup |
| `rememberUpdatedState` | долгоживущий callback видит свежие значения |
| `SideEffect { }` | не-Compose вызовы (напр. аналитика) на каждую успешную композицию |
| `rememberCoroutineScope()` | запуск корутин из onClick (для анимаций, скролла) |

```kotlin
LaunchedEffect(query) {          // перезапускается на каждый новый query
    delay(300)                    // debounce
    viewModel.search(query)
}
```

## Модификаторы

`Modifier` — неизменяемая конфигурация, применяемая **слева направо**:

```kotlin
Modifier.padding(16).clickable { }  // клик внутри отступа
Modifier.clickable { }.padding(16)  // клик по всей области, отступ снаружи
```

Порядок влияет на результат. Первый параметр composable — `modifier: Modifier = Modifier` (конвенция API).

## Lazy-списки

```kotlin
LazyColumn {
    items(users, key = { it.id }) { user ->
        UserRow(user)
    }
}
```

- Создают только видимые элементы (виртуализация как у RecyclerView).
- `key` обязателен для корректного reuse и анимаций.
- Нельзя вкладывать в `Column` + `verticalScroll` (вложенный скролл).

## Навигация

```kotlin
val nav = rememberNavController()
NavHost(nav, startDestination = "topics") {
    composable("topics") { TopicsScreen { id -> nav.navigate("quiz/$id") } }
    composable("quiz/{id}") { backStackEntry -> QuizScreen(backStackEntry.arguments?.getString("id")) }
}
```

## Производительность

- **Stability**: нестабильные параметры (`List`, мутабельные классы) ломают skipping → `ImmutableList`/`@Immutable`/Kotlin 2.x compiler inference.
- Читайте состояние **поближе к UI** — перезапускается меньше поддерево.
- `derivedStateOf` для значений, вычисляемых из часто меняющихся.
- Тяжёлую работу — во ViewModel/`withContext(Default)`, не в composable.
- `collectAsStateWithLifecycle` вместо вечного `collect`.

## Аннотации и метки

- `@Composable` — функция UI.
- `@Preview` — рендер в Android Studio (без запуска приложения).
- `@Stable / @Immutable` — подсказка компилятору про тип.
- `@OptIn(ExperimentalMaterial3Api::to)` — экспериментальные API.

## Чек-лист для собеседования

1. Recomposition — точечная, а не полный redraw; мешает ей нестабильность параметров.
2. `remember` vs `rememberSaveable` vs ViewModel — три уровня переживания.
3. Hoisting: stateless composable + value/onValueChange.
4. Эффекты — единственный законный способ выхода за пределы композиции.
5. `Modifier` применяется слева направо — порядок критичен.
