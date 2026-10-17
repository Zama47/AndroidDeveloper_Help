# Dependency Injection: Hilt

## Зачем DI

Класс сам создаёт зависимости (`val repo = RoomRepository(db)`) → нельзя подменить на фейк в тесте, связность растёт, «новый сервис» означает правку в десятке мест. **Конструкторная инъекция** — зависимости приходят снаружи:

```kotlin
class QuizViewModel(
    private val questions: QuestionsRepository,
    private val grade: GradeAnswerUseCase
) : ViewModel()
```

DI-контейнер (Hilt) строит граф автоматически: объявляете *как создать*, Hilt решает *кому дать*.

## Базовая настройка

```kotlin
// 1. build.gradle.kts
plugins { id("com.google.dagger.hilt.android"); id("ksp") }
dependencies {
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-compiler:2.51")
}

// 2. Application
@HiltAndroidApp
class App : Application()

// 3. Экран
@AndroidEntryPoint
class TopicsFragment : Fragment() { ... }

// 4. ViewModel
@HiltViewModel
class TopicsViewModel @Inject constructor(
    private val repo: TopicsRepository
) : ViewModel()

// 5. Получение
val vm: TopicsViewModel by viewModels()        // Fragment/Activity
val vm: TopicsViewModel = hiltViewModel()      // Compose
```

## Модули: @Provides vs @Binds

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient =
        OkHttpClient.Builder().build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder().client(client).baseUrl(BASE_URL).build()

    @Provides @Singleton
    fun provideApi(retrofit: Retrofit): ContentApi =
        retrofit.create(ContentApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    abstract fun bindRepo(impl: RoomQuestionsRepository): QuestionsRepository
}
```

- **@Provides** — тело функции (создаём чужой класс: Room, Retrofit).
- **@Binds** — интерфейс → существующая реализация (у impl должен быть `@Inject constructor`).
- Классы, которые вы пишете сами, вообще не нуждаются в модуле — хватает `@Inject constructor`.

## Скоупы (области жизни)

| Скоуп | Компонент | Живёт |
|-------|-----------|-------|
| `@Singleton` | SingletonComponent | весь процесс — база, Retrofit, репозитории |
| `@ActivityRetainedScoped` | ActivityRetainedComponent | переживает поворот |
| `@ViewModelScoped` | ViewModelComponent | одна ViewModel |
| `@ActivityScoped / @FragmentScoped` | Activity/Fragment | компонент UI |

Правило: **база и сеть — singleton; состояние экрана — ViewModel-скоуп; никогда не тащите Activity в Singleton** (утечка).

## Context и квалификаторы

```kotlin
@Inject @ApplicationContext lateinit var appContext: Context   // Application
@Inject @ActivityContext lateinit var activityContext: Context  // Activity
```

Два провайда одного типа? Разведите квалификаторами:

```kotlin
@Qualifier annotation class AuthOkHttp

@Provides @AuthOkHttp fun provideAuthClient(): OkHttpClient = ...
@Inject @AuthOkHttp lateinit var authClient: OkHttpClient
```

## Тестирование

**Unit-тесты — DI не нужен вообще**, конструктор это и даёт:

```kotlin
val vm = TopicsViewModel(FakeTopicsRepository(), TestDispatcher())
```

**Instrumented-тесты** — подмена модуля:

```kotlin
@HiltAndroidTest
@UninstallModules(ProdRepoModule::class)
class TopicsScreenTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides fun repo(): QuestionsRepository = FakeQuestionsRepository()
    }
}
```

## Hilt vs Koin vs ручной DI

| | Hilt (Dagger) | Koin | ручной конструктор |
|---|---|---|---|
| Проверка графа | **на этапе сборки** | в рантайме | компилятор сам |
| Build time | заметнее (KSP) | минимален | 0 |
| Boilerplate | аннотации | DSL-модули | передача руками |
| Кому подходит | средние/большие проекты | быстрый старт, малые команды | крошечные приложения |

Прагматика: до ~5 зависимостей хватает конструктора; дальше — Hilt или Koin; экзотические схемы — чистый Dagger.

## Продвинутые приёмы

- **@AssistedInject** — часть аргументов извне (id из навигации), часть из графа:

```kotlin
@HiltViewModel
class QuizViewModel @AssistedInject constructor(
    private val repo: QuestionsRepository,
    @Assisted val topicId: String
) : ViewModel()

@AssistedFactory
interface QuizVmFactory { fun create(topicId: String): QuizViewModel }
```

- **Multibinding** (`@IntoSet/@IntoMap`) — собрать набор трекеров/стратегий в Set/Map.
- **@EntryPoint** — доступ к графу из чужого кода (ContentProvider, инициализация SDK).
- **HiltWorkerFactory** — DI в WorkManager (`@HiltWorker` + `Configuration.Provider`).

## Чек-лист для собеседования

1. DI развязывает классы и делает их тестируемыми — граф создаёт контейнер лениво.
2. `@HiltAndroidApp` → `@AndroidEntryPoint` → `@HiltViewModel + @Inject`.
3. `@Provides` — создать, `@Binds` — привязать интерфейс.
4. Скоупы = время жизни: singleton ≠ object, и Activity в singleton = утечка.
5. Unit-тесты через конструктор без Hilt; instrumented — через `@UninstallModules`.
6. Hilt — компиляционный (KSP), Koin — runtime; выбираем под размер проекта.
