# DI и базовые аннотации

Здесь закрываются вопросы `hlb_*`: `@HiltAndroidApp`, `@AndroidEntryPoint`, `@Inject` и конструкторная инъекция.

### @HiltAndroidApp — корень всего
- Ставится над Application; генерируется класс-наследник, создающий дерево компонентов (корень — `SingletonComponent`).
- Без неё остальные аннотации молча не работают, а `@AndroidEntryPoint` падает в runtime.
- По умолчанию Hilt сам создаёт свои компоненты, но можно оставить Application-логику — они не конфликтуют.

### @AndroidEntryPoint
- Точки входа: Activity, Fragment, View, Service, BroadcastReceiver.
- Для Fragment обязательно, чтобы его Activity тоже была `@AndroidEntryPoint` — иначе runtime-ошибка при attach.
- Поля `@Inject lateinit var` заполняются после `super.onCreate()/onAttach()` — до этого `lateinit` не инициализирован.

### @Inject — два смысла
- На конструкторе: объект создаётся Hilt сам, без модуля — constructor injection.
- На поле: точка внедрения; работает только в `@AndroidEntryPoint`-классах (не во ViewModel — там конструктор).
- Частые ошибки: пометить поле, но забыть @AndroidEntryPoint; создать `MyService()` вручную — обход контейнера, теряется вся ценность DI.

### Constructor injection как дефолт
- `class Repo @Inject constructor(private val api: Api)` — Hilt рекурсивно резолвит параметры.
- Требования: все зависимости предоставляемы, нет циклов (цикл — ошибка компиляции).
- Модуль нужен, если: сторонний класс нельзя аннотировать, тип — интерфейс с несколькими реализациями, нужна скоуп-логика.
- ViewModel: `@HiltViewModel class MyVM @Inject constructor(repo: Repo)`; в Compose — `hiltViewModel()`.

### Питфоллы и собеседование
- Спрашивают: чем field injection плох, как Hilt разрешает зависимости (compile-time, не reflection), к чему приводит забытый @AndroidEntryPoint у Activity с Fragment внутри.
- Смотрите шире: `EntryPointAccessors` (для ContentProvider и мест без точки входа), `@HiltViewModel`, `@HiltWorker` для WorkManager.
- Отличие от Koin: compile-time строгая проверка vs runtime-DSL; field injection — только для системных компонентов, чей конструктор система контролирует сама.
