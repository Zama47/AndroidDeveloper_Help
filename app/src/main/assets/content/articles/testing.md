# Тестирование Android-приложения

## Пирамида тестирования

```
      /\        E2E (мало, медленно, хрупко)
     /  \       — сценарий "регистрация → покупка"
    /----\      Instrumented (секунды, устройства)
   /      \     — Room DAO, Compose UI, Android API
  /--------\    Unit (миллисекунды, JVM)
 /          \   — бизнес-логика, ViewModel, маппинги
/____________\
```

| Уровень | Скорость | Где бегут | Что покрываем |
|---------|----------|-----------|---------------|
| Unit | мс | JVM (src/test) | UseCase, ViewModel, парсинг, маппинги |
| Instrumented | с | устройство/эмулятор (androidTest) | Room, Compose UI, WorkManager |
| E2E | мин | устройство + бэкенд | критичные пользовательские сценарии |

Правило: чем ниже по пирамиде — тем больше тестов пишем.

## Unit-тесты (src/test)

```kotlin
class GradeAnswerUseCaseTest {
    private val useCase = GradeAnswerUseCase()

    @Test
    fun `correct answer increments score`() {
        assertEquals(2, useCase.grade(userAnswer = 1, correct = 1).score)
    }

    @Test
    fun `wrong answer marks card for retry`() {
        assertTrue(useCase.grade(userAnswer = 0, correct = 1).needsRetry)
    }
}
```

Запуск: `./gradlew :app:testDebugUnitTest`. Структура — given/when/then, один тест = одно поведение, понятные сообщения в ассертах.

## Корутины и StateFlow в тестах

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loads questions and exposes content`() = runTest {
        val vm = QuizViewModel(FakeQuestionRepository(), StandardTestDispatcher())
        vm.onEvent(UiEvent.Start)
        advanceUntilIdle()
        assertEquals(UiState.Content(...), vm.state.value)
    }
}
```

Ключи: тестовый диспетчер вместо рабочего, виртуальное время (`delay` не ждёт реально), fake-репозиторий вместо сети/базы. Для Flow — **Turbine**: `turbine.test { assertEquals(Loading, awaitItem()) }`.

## Моки и фейки

- **MockK** — де-факто для Kotlin: `every { repo.get(1) } returns question`, `verify { repo.get(1) }`.
- **Fake** — рабочая упрощённая замена (`FakeQuestionRepository` на `HashMap`): тесты устойчивы к рефакторингу.
- Предпочтительнее: fake для зависимости по умолчанию, mock — когда важен вызов (verify контракта).

## Инструментальные тесты

**Room (DAO)** — in-memory база:

```kotlin
@RunWith(AndroidJUnit4::class)
class QuestionDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: QuestionDao

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.questionDao()
    }
    @After fun tearDown() { db.close() }

    @Test fun insertAndRead() = runBlocking {
        dao.insertAll(listOf(q1, q2))
        assertEquals(2, dao.getAll().size)
    }
}
```

Миграции — `MigrationTestHelper` с экспортированными schema-JSON.

**Compose UI**:

```kotlin
@get:Rule val compose = createComposeRule()

@Test fun selectingAnswerShowsExplanation() {
    compose.setContent { MaterialTheme { QuizScreen(...) } }
    compose.onNodeWithText("Дельта").performClick()
    compose.onNodeWithText("Пояснение...").assertIsDisplayed()
}
```

**Espresso** — для View/XML: `onView(withId(R.id.btn)).perform(click())`, асинхронность — через `IdlingResource`.

## Скриншот-тесты

**Paparazzi** / Compose Preview Screenshot Testing: рендерят composable на JVM и сравнивают PNG с эталоном. Ловят визуальные регрессии дизайн-системы без устройства.

## Стратегия для нашего квиза

1. **Unit (много)**: `GradeAnswerUseCase`, `GetDueCardsUseCase` (SM-2 выборка), `QuizViewModel` (переходы состояний, события), парсинг всех `content/*.json` (валидация схемы, уникальность id, индексы correctAnswer).
2. **Instrumented**: DAO Room (insert/select/Flow), `ContentSeeder` (после сида N вопросов), миграции.
3. **Compose UI**: клик по варианту → explanation, переход к результатам, state restoration.
4. **E2E smoke**: «пройти мини-квиз до конца» — один-два сценария.
5. **CI**: unit + lint на каждый PR; instrumented на эмуляторе (Gradle Managed Devices) на merge/ночь; скриншоты падений — артефакты.

## Flaky-тесты и детерминизм

Источники: `Thread.sleep`, реальная сеть, глобальное состояние, гонки времени. Лечение: `runTest`/виртуальное время, fake-источники, изоляция через `@Before`, in-memory БД. Retry в CI — временная маска, не лечение.

## Покрытие (Kover/Jacoco)

Метрика — сигнал, не цель. 100% строк ≠ отсутствие багов: важны проверка ошибок и краевых случаев (пустой список, null, ошибка сети), а не геттеры.

## Чек-лист

1. Пирамида: много unit, меньше instrumented, минимум E2E.
2. Тестируем поведение (вход→выход), не внутренности.
3. Корутины — только `runTest` + тестовый диспетчер, без sleep.
4. Зависимости — fake/mock, сеть и БД не в unit-тестах.
5. Контент JSON — unit-тест валидации схемы до релиза.
6. CI-гейт: unit + lint на PR; flakiness лечить причиной.
