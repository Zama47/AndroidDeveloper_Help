# Отмена и структурированность

Эта статья закрывает вопросы crc_001–crc_006: structured concurrency, Job/SupervisorJob, кооперативная отмена, CancellationException и withTimeout.

### Structured concurrency
- Каждая корутина живёт в scope; отмена scope автоматически отменяет всех детей — никаких «висящих» фоновых работ, когда экран умер
- `Job` — «fail fast»: необработанная ошибка ребёнка отменяет родителя и, через него, всех остальных детей
- `SupervisorJob()`/`supervisorScope { }` — сбой одного ребёнка не трогает siblings; необработанные исключения детей идут в `CoroutineExceptionHandler` scope, а не вверх. С `viewModelScope` это уже встроено

### Кооперативная отмена
- `cancel()` ставит флаг, а не «убивает»: отмена срабатывает в точках suspension (`delay`, `withContext`) или в проверках `isActive`/`ensureActive()`/`yield()`
- CPU-цикл без suspension-точек отменился не паче; вставляйте `ensureActive()` в итерацию — бросит `CancellationException` сразу
- В долгих циклах лучше `ensureActive()` — выход с исключением гарантирован, а не «когда цикл сам заметит»

### CancellationException — не «просто Exception»
- `catch (e: Exception)` внутри корутины съедает и CancellationException — отмена не прорвётся; канон: перехватывайте конкретные типы, CancellationException перебрасывайте
- `finally` выполняется и при отмене — туда и идёт закрытие ресурсов; suspend внутри finally разрешён только в `withContext(NonCancellable) { close() }`

### Таймауты
- `withTimeout(5000) { ... }` бросает `TimeoutCancellationException` (наследник CancellationException); `withTimeoutOrNull` вернёт null
- Перехваченный внутри блока таймаут-тип «обезвреживает» отмену именно этого блока — оставшаяся часть продолжит; ловите снаружи и обрабатывайте как результат

### Питфоллы и собеседование
- Топ: «почему отмена кооперативная», «что будет, если поймать CancellationException», «SupervisorJob против Job»
- Ловушка: с `SupervisorJob` исключение ребёнка не отменяет siblings, но и не уходит вверх — нужен CoroutineExceptionHandler, иначе молчание
- Держите в голове: `runCatching { }` тоже поглощает CancellationException — в suspend-коде он опасен
