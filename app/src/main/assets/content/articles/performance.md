# Производительность Android-приложения

## Метрики и бюджеты

| Метрика | Бюджет |
|---------|--------|
| Кадр при 60 FPS | **16.6 мс** (input → measure → layout → draw → GPU) |
| Cold start (TTID) | < 2–5 с (зависит от устройства; цель — минимальный первый кадр) |
| jank | доля пропущенных кадров ~0% в горячих сценариях |
| ANR | 0 (input > 5 с блокировки main = ANR) |

**Jank** — просадка FPS. **ANR** — полная блокировка главного потока. Разные проблемы, одно лекарство: не грузить main.

## Главный поток

В main происходит ввод, анимации, measure/layout/draw. Запрещено: сеть, диск, большие вычисления, синхронный Room/SharedPreferences. Средства контроля:

- **StrictMode** (debug): логирует/падает при disk/network в main.
- **Android Profiler**: CPU-trace показывает, кто занял кадр.
- **Perfetto**: системные трейсы глубже профайлера.

```kotlin
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectNetwork().detectDiskReads().penaltyLog().build()
    )
}
```

## Композиция: лишние рекомпозиции

1. **Stability**: параметры composable должны быть стабильными (`data class`, `@Immutable`, `ImmutableList`). `List`/`MutableState` ломают skipping.
2. **Читайте state ближе к UI** — перезапускается меньше поддерево.
3. **derivedStateOf** — производные значения без эмита на каждый скролл.
4. **State hoisting** — одно состояние на экран, не дублировать.
5. **key** в `LazyColumn` — корректный reuse.
6. `remember { }` — для дорогих вычислений и цепочек `Modifier`.

Диагностика: Layout Inspector → Recomposition Counts.

## Память

- Типичная утечка: singleton ссылается на Activity/Context.
- `onTrimMemory` — освобождать кэши при давлении памяти.
- Изображения: downscale под view, кэш Coil/Glide, WebP, ленивая загрузка.
- Аллокации в `onDraw`/цикле → частые GC → jank. Object pools — по замерам.

## Cold start

```
process start → Application.onCreate → Activity.onCreate → первый кадр
```

Оптимизации:

1. **Baseline Profile** — AOT критических путей (+20–30% к старту).
2. Отложенная инициализация SDK (App Startup или ручной lazy).
3. Никакой сети до первого кадра; первый экран — из кэша/Room.
4. **SplashScreen API** — системный сплэш вместо self-made Activity.
5. Замерять **Macrobenchmark** (TimeToInitialDisplay).

## Сеть и фон

- Объединять запросы, кэшировать ответы (Room — single source of truth).
- **Doze/App Standby** откладывают некритичные задачи — полагаться на WorkManager.
- Battery: батчинг, NetworkType-ограничения, push вместо polling, без foreground service без нужды.

## Строки и аллокации

```kotlin
// Плохо в цикле
var result = ""
for (s in parts) result += s

// Хорошо
val sb = StringBuilder()
for (s in parts) sb.append(s)
val result = sb.toString()
```

`String` неизменяем — каждый `+` аллоцирует. `StringBuilder` — мутабельный буфер.

## Release-сборка

- **R8**: tree shaking + обфускация + минификация → меньше APK. Keep-правила для Room/Gson/serialization/Hilt.
- `Log.d` — вырезать через R8 `assumenosideeffects` или Timber с отключаемым уровнем.
- Сравнивать производительность всегда на **release** (debug врёт в разы).

## Инструментарий

| Инструмент | Что даёт |
|------------|----------|
| Android Profiler | CPU/Memory/Network/Energy трейсы |
| Layout Inspector | дерево + recomposition counts (Compose) |
| StrictMode | нарушения main-thread в debug |
| Macrobenchmark | cold start, scroll frame timing на устройстве |
| Microbenchmark | наносекунды для горячих функций |
| Perfetto | глубокие системные трейсы |
| LeakCanary | утечки памяти автоматически |
| Baseline Profile | AOT критических путей |

## Чек-лист для собеседования

1. Бюджет кадра 16.6 мс; jank vs ANR — разные вещи.
2. main thread — только UI; всё остальное — корутины/диспетчеры/WorkManager.
3. Compose: стабильность параметров + derivedStateOf + key — основа плавности.
4. Утечки = ссылки на Context/Activity в долгоживущих объектах; ищем LeakCanary.
5. Cold start = Baseline Profile + ленивая инициализация + кэш первого экрана.
6. Меряем до/после: профилировщик и бенчмарки, не «на глаз».
