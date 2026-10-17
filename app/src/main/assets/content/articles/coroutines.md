# Coroutines и Flow: асинхронность в Android

## Зачем корутины

Классический Android-код блокировал потоки: `Thread`, `AsyncTask`, `Handler`. Проблемы — утечки, ANR, callback-ад (callback hell). Корутина — **возобновляемая единица выполнения**: в точке `suspend` выполнение приостанавливается, стек сохраняется, а **поток освобождается** для другой работы. Тысячи корутин живут на одном потоке.

```kotlin
suspend fun loadUser(id: String): User {
    val user = api.fetchUser(id)      // приостановка, поток свободен
    val posts = api.fetchPosts(id)    // приостановка
    return user.withPosts(posts)
}
```

## Запуск: launch, async, runBlocking

| Билдер | Возвращает | Когда использовать |
|--------|-----------|--------------------|
| `launch { }` | `Job` | эффект без результата (лог, сохранение) |
| `async { }` | `Deferred<T>` | нужен результат → `await()` |
| `runBlocking { }` | `T` | **блокирует** поток: только тесты и `main()` |
| `withContext(ctx) { }` | `T` | переключение контекста в рамках той же корутины |

```kotlin
val d1 = async { fetchA() }
val d2 = async { fetchB() }
val result = d1.await() + d2.await()   // параллельно
```

## Scope и структурированная конкурентность

`CoroutineScope` = контекст + иерархия `Job`. Правила:

- каждая корутина — ребёнок scope-родителя;
- scope завершается только после всех детей (никаких висячих корутин);
- отмена родителя отменяет всех потомков;
- ошибка ребёнка в обычном scope всплывает и ломает родителя.

На Android:

```kotlin
lifecycleScope.launch { ... }     // отменяется при onDestroy
viewModelScope.launch { ... }     // отменяется при onCleared
```

`viewModelScope` и `lifecycleScope` построены на **SupervisorJob**: ошибка одной корутины не отменяет соседние. Но необработанное исключение всё равно долетит до `CoroutineExceptionHandler` и **упадёт приложение** — бизнес-ошибки ловите `try/catch` и превращайте в `sealed state Error`.

## Диспетчеры

- **Main** — главный поток (UI, обновление StateFlow). `Main.immediate` не переключается, если уже в Main.
- **IO** — до 64 потоков под блокирующий ввод-вывод: файлы, SQLite, SharedPreferences.
- **Default** — пул размером с ядра CPU: JSON, сортировка, вычисления.
- **Unconfined** — продолжается в потоке, где возобновилась (тесты, редкие случаи).

```kotlin
withContext(Dispatchers.IO) {
    database.save(user)   // тяжёлая работа
}                         // контекст вернётся сам
```

`flowOn(Dispatchers.IO)` переключает только **продюсера** потока.

## Отмена

`job.cancel()` ставит флаг — это **кооперативная** отмена. Длинные циклы обязаны её проверять:

```kotlin
while (isActive) { work() }      // или ensureActive(), yield()
```

`delay` и другие suspend-функции отменяются сами, бросая `CancellationException`. Поймали его в `try/catch` — **обязательно пробросьте дальше**, иначе ломается механизм отмены. В `finally` suspend-функции вызывать нельзя — для cleanup используйте `withContext(NonCancellable) { }`.

## Flow

`Flow` — холодный асинхронный стрим: код продюсера выполняется только при `collect`.

```kotlin
fun search(query: String): Flow<List<User>> = flow {
    emit(api.search(query))
}
```

### Операторы

- трансформации: `map`, `filter`, `transform`, `scan`
- тайминг: `debounce(300)` (поиск), `sample`, `distinctUntilChanged`
- комбинирование: `combine` (последние значения обоих), `zip` (пары по порядку), `merge`
- `flowOn(ctx)` — контекст продюсера, `onEach`, `onStart/onComplete`

### StateFlow vs SharedFlow

| | StateFlow | SharedFlow |
|---|-----------|------------|
| Смысл | **состояние** (что есть) | **событие** (что случилось) |
| Есть ли значение | всегда (initial + последнее) | нет, только эмиты |
| Новый подписчик | сразу получает последнее | получит replay-буфер (если задан) |
| Дубликаты | схлопываются через `equals` | доставляются все |

```kotlin
// Во ViewModel
val uiState: StateFlow<UiState> = repo.observe()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

// Одноразовые события (навигация, снекбар)
private val _events = MutableSharedFlow<Event>()
```

`WhileSubscribed(5000)` — дорогой upstream отключается через 5 секунд после ухода со экрана (переживает поворот экрана).

### Подписка в Compose

```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

`collectAsStateWithLifecycle` прекращает сбор в `STOPPED` — не тратим ресурсы на фоне.

## Чек-лист для собеседования

1. `delay` не блокирует поток; `Thread.sleep` — блокирует (это ANR).
2. Ошибка в корутине всплывает по дереву → всегда catch или handler.
3. `CancellationException` не «ошибка»: поймал — пробросил.
4. `StateFlow` для состояния (equals обязателен), `SharedFlow` для событий.
5. `stateIn(WhileSubscribed)` — мост flow → StateFlow во ViewModel.
