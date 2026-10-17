# Архитектура Android-приложения: MVVM, MVI, Clean Architecture

## MVVM + Unidirectional Data Flow

Современный стандарт: **MVVM с однонаправленным потоком данных**.

```
Compose (View)  ──события (intent/event)──▶  ViewModel
     ▲                                          │
     └────── StateFlow<UiState> ◀───────────────┘
                       │
                       ▼
                  Repository (Model)
```

- **View** только рендерит состояние и шлёт события. Не содержит логики.
- **ViewModel** — координатор: принимает события, меняет состояние, вызывает данные. Не знает про Activity/Context.
- **Model** — репозитории и источники данных.

```kotlin
// UiState — единый неизменяемый стейт экрана
data class QuizUiState(
    val question: Question? = null,
    val selectedIndex: Int? = null,
    val isAnswerShown: Boolean = false,
    val score: Int = 0
)

class QuizViewModel(
    private val gradeAnswer: GradeAnswerUseCase,
    private val nextQuestion: NextQuestionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    fun onEvent(event: QuizEvent) {
        when (event) {
            is QuizEvent.AnswerSelected -> { /* reduce */ }
            QuizEvent.Next -> { /* reduce */ }
        }
    }
}
```

## MVI vs MVVM

**MVI** — строгая версия UDF:

- одно state-холдинг всего экрана (immutable data class);
- sealed `Intent` — все действия пользователя;
- `reduce(state, intent) → state` — чистая функция;
- побочные эффекты (навигация, тосты) — отдельным каналом.

**MVVM** — мягче: несколько Flow/LiveData, события через колбэки. На собеседовании честно: «MVVM в реализации, принципы UDF из MVI».

## Clean Architecture (три слоя)

```
presentation   Compose, ViewModel, UiState, навигация
      │
      ▼
domain         Модели (Question), интерфейсы (Repository),
               use cases — БЕЗ Android/Retrofit/Room
      ▲
      ▲
data           Room DAO, Retrofit API, реализации репозиториев,
               маппинг DTO → domain
```

**Правило зависимостей:** домен ничего не знает о внешнем мире; `data` реализует интерфейсы `domain`; `presentation` обращается к `domain`. Поэтому:

- домен тестируется чистыми JUnit-тестами;
- источник данных меняется без правки UI;
- фейковый репозиторий подставляется в тест одним моком.

### Use case: когда нужен

```kotlin
class GetDueCardsUseCase(
    private val repeats: RepeatRepository,
    private val questions: QuestionsRepository
) {
    operator fun invoke(limit: Int): Flow<List<Card>> =
        repeats.getDue(limit)
            .flatMapLatest { questions.observeByIds(it.map(Repeat::questionId)) }
}
```

Используйте, когда сценарий — не «просто вызов репозитория»: есть логика, мульти-источники, условия. Для CRUD-обёрток use case — избыточный boilerplate.

## Состояние и события

```kotlin
sealed interface QuizEvent {
    data class AnswerSelected(val index: Int) : QuizEvent
    object Next : QuizEvent
    object Finish : QuizEvent
}
```

Одноразовые эффекты (Toast, навигация) **не класть в state** — state переживает поворот, эффект повторится. Варианты:

1. отдельный `MutableSharedFlow<Effect>` (событийный канал);
2. state + явное поглощение после показа.

## SOLID — коротко

| Принцип | Суть | Пример в проекте |
|---------|------|------------------|
| **S**ingle Responsibility | одна причина изменяться | ViewModel не пишет в Room сам |
| **O**pen/Closed | расширять, не модифицировать | новый тип квиза = новая стратегия |
| **L**iskov | подтипы заменяемы | FakeRepository подставляется вместо Room |
| **I**nterface Segregation | маленькие интерфейсы | отдельные DAO на сущности |
| **D**ependency Inversion | зависимость от абстракции | ViewModel зависит от интерфейса |

## Управление зависимостями (DI)

Без DI класс сам создаёт зависимости — не тестируется. С DI:

```kotlin
class QuizViewModel(
    private val repo: QuestionsRepository,   // интерфейс
    private val grade: GradeAnswerUseCase
) : ViewModel()
```

Hilt автоматизирует граф: `@HiltViewModel`, `@Inject constructor`, `@Module` для Room/Retrofit. Тесты подменяют реализацию на фейк.

## Жизненный цикл

- **ViewModel** — переживает поворот, умирает с экраном (`onCleared` → отмена `viewModelScope`).
- **SavedStateHandle** — данные переживают гибель процесса (мелкие: id, текст формы).
- **Не хранить** в ViewModel: Context, View, ссылки на Activity.

## Modularity (когда расти)

Признаки, что пора делить Gradle-модули: падает скорость сборки, presentation импортирует DTO, команде тесно. Схема: `:app`, `:feature:quiz`, `:feature:topics`, `:core:domain`, `:core:data`, `:core:ui`. Начинать деление нужно с чистых пакетов — multi-module окупается на проектах среднего+ размера.

## Чек-лист для собеседования

1. UDF: события вверх, состояние вниз, один источник истины.
2. ViewModel без Context; state — immutable `data class` в `StateFlow`.
3. Clean Architecture: зависимости направлены внутрь, домен чист от Android.
4. Одноразовые эффекты — не в state.
5. Use case — только когда есть логика; pragmatic over ceremony.
6. Repository выбирает источник (кэш/сеть), скрывая это от ViewModel.
