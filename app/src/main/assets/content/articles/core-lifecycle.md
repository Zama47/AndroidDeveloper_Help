# Жизненный цикл Activity

Здесь закрываются вопросы `corl_*`: порядок колбэков, поворот экрана, process death, `lifecycleScope`/`repeatOnLifecycle`.

### Порядок и смысл колбэков
- Создание: `onCreate` → `onStart` → `onResume`; уход: `onPause` → `onStop` → `onDestroy`.
- Перекрытие без потери видимости (обычный диалог, прозрачное окно) — только `onPause`, Activity остаётся в `Started`. Полное перекрытие — `onPause` + `onStop`.
- `onRestart` предшествует `onStart` при возврате из состояния Stopped.
- Ловушка: обычный диалог не переводит Activity в `onStop` — самый популярный вопрос на собесах.

### Поворот экрана
- По умолчанию Activity уничтожается и создаётся заново — config change.
- `ViewModel` переживает config change, так как его держит `ViewModelStore`; `onSaveInstanceState` / `SavedStateHandle` переживают и config change, и process death.
- `android:configChanges="orientation|screenSize"` в манифесте подавляет пересоздание и шлёт event в `onConfigurationChanged` — обычно антипаттерн: вся ответственность на вас.

### Process death
- Система убивает процесс для освобождения памяти — Activity пересоздаётся «на пустом месте».
- Восстановление: `onCreate(savedInstanceState)` и `onRestoreInstanceState` (вызывается после `onStart`).
- Потеряется: поля ViewModel, статические кэши, корутины, in-memory коллекции.
- Идеология: бизнес-состояние — в Room/DataStore, UI-состояние — в `Bundle`/`SavedStateHandle`.

### lifecycleScope и сбор Flow
- `lifecycleScope.launch { collect }` продолжит собирать и в фоне — это риск для UI-логики.
- Правильная формула: `lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.STARTED) { collect } }`.
- `repeatOnLifecycle` отменяет корутину при уходе ниже состояния и перезапускает при возврате.
- Для Fragment используйте `viewLifecycleOwner` — иначе утечки View Binding и колбэки от «мёртвых» View.

### Питфоллы и собеседование
- Чаще всего спрашивают: какой колбэк при диалоге, что переживает config change vs process death, чем `lifecycleScope` отличается от `viewLifecycleOwner`.
- Смотрите шире: `Lifecycle.State` (INITIALIZED…DESTROYED), `Lifecycle.Event`, `DefaultLifecycleObserver`.
- Запомните связь ViewModel ↔ ViewModelStore ↔ `viewModelScope`; `SavedStateHandle` — не «ещё один Bundle», а способ сохранить состояние ViewModel при process death.
