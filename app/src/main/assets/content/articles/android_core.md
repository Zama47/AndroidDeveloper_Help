# Android Core: жизненный цикл, компоненты, контекст

## Компоненты Android

| Компонент | Роль | Когда |
|-----------|------|-------|
| **Activity** | экран с UI | главное взаимодействие |
| **Fragment** | переиспользуемая часть UI | вкладки, диалоги, встраивание |
| **Service** | работа без UI | фоновые задачи (редко напрямую — см. WorkManager) |
| **BroadcastReceiver** | реакция на события | системные broadcast, кастомные action |
| **ContentProvider** | доступ к данным для других процессов | медиа, контакты, FileProvider |

Всё объявляется в `AndroidManifest.xml` (+ `<uses-permission>`, intent-фильтры).

## Жизненный цикл Activity

```
onCreate → onStart → onResume → [Running] → onPause → onStop → onDestroy
                                   ↑              │
                              onRestart ←─────────┘ (если вернулись)
```

- **onCreate** — инициализация; `setContentView`, ViewModel, восстановление `savedInstanceState`.
- **onResume** — активити фокусируется, интерактивна.
- **onPause** — потеря фокуса (может быть ещё видна).
- **onStop** — полностью скрыта; тут освобождают тяжёлые ресурсы.
- **onDestroy** — уничтожение; `viewModelScope` отменится в `onCleared`.

При **возврате** на экран (без уничтожения): `onRestart → onStart → onResume` — `onCreate` НЕ вызывается.

## Configuration change vs Process death

| Событие | Что происходит | Что переживает |
|---------|----------------|----------------|
| Поворот (config change) | `onDestroy + onCreate` | **ViewModel**, rememberSaveable |
| Гибель процесса (system kill) | весь процесс умер | только **Bundle/SavedStateHandle**, Room/дисковое |

```kotlin
// Восстановление после обоих случаев
class FormViewModel(saved: SavedStateHandle) : ViewModel() {
    val text: StateFlow<String> = saved.getStateFlow("text", "")
}
```

`onSaveInstanceState` вызывается перед остановкой (config change и kill); НЕ вызывается при обычном завершении пользователем (back).

## Context

- **Application Context** — глобальный, без window/темы; безопасен в синглтонах и репозиториях.
- **Activity Context** — нужен диалогам, `inflate`, `startActivity`; **никогда не хранить** в долгоживущих объектах (утечка).

Хранение Activity-ссылки в singleton = классическая memory leak. Ищите через LeakCanary.

## Intents и back stack

```kotlin
// Явный
startActivity(Intent(this, QuizActivity::class.java).apply {
    putExtra("topicId", id)          // Parcelable/String/primitive
})

// Неявный
startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com")))
```

- Системная **Back** — закрывает верхний Activity в task (стек задач).
- **Up** (стрелка в toolbar) — возврат по иерархии приложения.
- Для Android 11+ неявные запросы к чужим приложениям требуют `<queries>` в манифесте.

## Permissions

- **Normal** (INTERNET, VIBRATE) — молча при установке.
- **Dangerous** (камера, геолокация, уведомления) — запрос в рантайме:

```kotlin
val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) startCamera() else showRationale()
}
launcher.launch(Manifest.permission.CAMERA)
```

Правило: запрашивать в контексте, когда пользователь понимает ЗАЧЕМ (не сразу при первом открытии).

## ANR vs Jank

- **Jank** — просадка FPS (кадр > 16.6 мс): подтормаживания, анимации рывками.
- **ANR** — главный поток заблокирован: input > 5 с, broadcast > 10 с, service > 20 с → диалог «не отвечает», возможен kill.

Общее лекарство: **вся тяжёлая работа — из главного потока** (корутины + Dispatchers.IO/Default, WorkManager).

## Фоновые компоненты

- **Service** — тонкий; обычный может быть убит. **Foreground Service** — постоянное уведомление + `foregroundServiceType`, приоритет выше, ограничения запуска с фона.
- **WorkManager** — дефолт для отложенных/повторяющихся/гарантированных задач:

```kotlin
val work = OneTimeWorkRequestBuilder<SyncWorker>()
    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
    .build()
WorkManager.getInstance(ctx).enqueueUniqueWork("sync", ExistingWorkPolicy.REPLACE, work)
```

- **BroadcastReceiver** — короткая реакция (< 10 с в главном потоке); долгое — внутри запускать WorkManager.
- **Doze/App Standby** — система откладывает некритичные задачи; полагайтесь на WorkManager.

## Утечки памяти

GC убирает только недостижимые объекты. Утечка = есть ссылка, а объект уже не нужен.

Типичные виновники:

1. статическое поле с Activity/Context;
2. неотписанные listener/callback;
3. корутина во внешнем scope, живущая дольше экрана;
4. `Handler` со старыми сообщениями.

Профилактика: конструкторная зависимость на Application Context, `repeatOnLifecycle` для подписок, `viewModelScope`, LeakCanary в debug.

## Ресурсы и темы

- `res/values/strings.xml` — локализация; `values-night` — тёмная тема.
- Тема в Compose: `MaterialTheme(colorScheme = ...)` поверх `MaterialTheme` из XML.
- Конфигурации (dpi, locale) пересоздают Activity — держите состояние вне View.

## Чек-лист для собеседования

1. Порядок: `onCreate → onStart → onResume`; возврат — без `onCreate`.
2. Config change убивает Activity, но не ViewModel; process death убивает всё, кроме Bundle/диска.
3. Application Context в синглтонах, Activity Context — только точечно.
4. ANR — блокировка main; jank — просадка FPS; лечение — фон из main.
5. Долгая гарантированная работа — WorkManager, не Service.
6. Runtime-разрешения — через Activity Result API.
