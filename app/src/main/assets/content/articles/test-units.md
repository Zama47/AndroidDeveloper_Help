# Unit-тесты и корутины

Здесь закрываются вопросы `tsu_*`: AAA, `runTest`, TestDispatcher, `Dispatchers.setMain`, parameterized-тесты.

### AAA — базовый каркас
- **Arrange** — подготовка данных и зависимостей (можно в @Before/fixtures); **Act** — ровно один вызов тестируемого метода; **Assert** — проверка результата.
- Один тест = один сценарий; несколько независимых Act — размытая ответственность: падение не указывает, где сломалось.
- Хороший тест краснеет по одной и той же причине — детерминированность важнее покрытия.

### runTest и виртуальное время
- `runTest {}` работает с TestScope: `delay()` не тормозит реальные часы — advanceTimeBy/advanceUntilIdle «проматывают» время.
- Это ключ к тестам таймеров/дебаунсов: `viewModel.debounce(); advanceTimeBy(500)` — стабильно, а не sleep(500).
- Условие: корутины должны жить на TestDispatcher; код, форсирующий Dispatchers.Default/IO, привнесёт реальное время.
- runBlocking — для простых suspend-вызовов; для логики с delay — только runTest.

### Подмена Main-диспетчера
- В JVM-тесте нет Application/Main: в `@Before` — `Dispatchers.setMain(UnconfinedTestDispatcher())` (или StandardTestDispatcher), в `@After` — `resetMain()`.
- Без подмены `viewModelScope` упадёт с «Module with the Main dispatcher failed to initialize».
- Альтернатива короче: инжектить `DispatcherProvider` — тогда подмена не нужна вовсе.

### Структура теста ViewModel
- Collect-объект: `launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect { states += it } }`, Act, Assert по списку состояний.
- Забыли `resetMain()` → «залипший» тестовый Main в следующем тест-классе.
- Ловушка: тесты корутин — это управление временем: виртуальный time-механизм вместо sleep.

### Питфоллы и собеседование
- Спрашивают: зачем runTest и виртуальное время, что делать с Dispatchers.Main, чем Fake отличается от Mock.
- Смотрите шире: `TestCoroutineScheduler`, `StandardTestDispatcher` vs `UnconfinedTestDispatcher`, Turbine.
