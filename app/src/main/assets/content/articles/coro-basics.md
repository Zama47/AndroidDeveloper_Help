# Корутины: основы

Эта статья закрывает вопросы crb_001–crb_006: suspend-функции, CoroutineScope и viewModelScope, Dispatchers, launch vs async.

### suspend — приостановка, а не блокировка
- `suspend`-функцию можно вызвать только из suspend-контекста: другой suspend-функции или внутри корутины (`launch`, `async`, `runBlocking`)
- В точках suspension (`delay`, `await`, `withContext`) корутина освобождает поток; лишь вычисляющийся код занимает поток, как обычный
- `runBlocking` блокирует поток до завершения — мост для `main()` и тестов, не для Android UI

### Scope и viewModelScope
- `viewModelScope` — scope, встроенный в ViewModel: отменяется в `onCleared()`, поэтому корутины не утекают; у Activity/Fragment — `lifecycleScope`
- Своё долгоживущее место (сеть, репозиторий) — собственный `CoroutineScope(SupervisorJob() + dispatcher)`, отменяемый руками
- Антипаттерн: `GlobalScope` — корутины переживают экран и живут «в никуда»

### Dispatchers
- `Main` — UI; `viewModelScope.launch` по умолчанию стартует на `Dispatchers.Main.immediate` (если уже на main, без пересадки через очередь)
- `IO` — блокирующий ввод-вывод (файлы, сокеты, «толстый» JDBC-стиль код), пул ~64 потока; `Default` — CPU-работа (парсинг, сортировки), пул по числу ядер
- Ловушка: заливать CPU-задачи в IO-пул — заблокируете потоки под белый день; репозиторий сам переключает контекст, ViewModel не обязан знать про потоки

### launch vs async
- `launch` возвращает `Job`: «запустил и забыл»; исключение в launch всплывает сразу
- `async` возвращает `Deferred<T>`: результат забирается через `await()`; два `async` подряд = параллельная работа
- `awaitAll` для N задач; structured concurrency — отмена scope отменяет всех детей

### Питфоллы и собеседование
- Клише-вопросы: «suspend блокирует поток?», «где выполнять CPU-работу?», «чем async отличается от launch»
- Ловушка: async, чей `await()` совмещён с последовательными вызовами — requests выполнятся по очереди; запускайте оба async до await
- Помните: viewModelScope работает на Main.immediate — сеть и CPU туда напрямую нельзя, только через withContext/right dispatcher
