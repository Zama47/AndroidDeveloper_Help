# Рендеринг и jank

Закрывает вопросы prf_001–prf_006: бюджет кадра, Choreographer, overdraw, layout, измерение jank.

### Бюджет кадра
- 60 FPS → **16.6 мс** на кадр; 90 Гц → 11.1 мс; 120 Гц → 8.3 мс. Внутри: input → animation → measure → layout → draw → Submit → GPU-композиция.
- Не уложился → **jank** (пропущенные кадры). Длительная блокировка main thread → **ANR** (input > 5 с) — уже самостоятельный дефект.
- Тестировать на настоящих high-refresh-устройствах: 8.3 мс прощает долю 16.6 мс оптимизаций.

### Choreographer и pipeline
- `Choreographer` получает VSYNC и запускает callback'и кадра; `SurfaceFlinger` композитит ниже — UI-код он не вызывает.
- Сигнал «Handmade jank» в Logcat: **Choreographer — Skipped N frames!**
- Тяжёлый `onBindViewHolder`/`onMeasure` виден в этом же стеке: при скролле каждый байнд — это следующий кадр.

### Overdraw и layout
- Debug GPU Overdraw: нет цвета — 1 закраска, синий — 1 лишняя, зелёный — 2, розовый — 3, красный — 4+. Типичный источник — три фона друг на друге (theme windowBackground + layout + card).
- Вложенные `LinearLayout` с `layout_weight` — двойной measure; лечится `ConstraintLayout`, `merge`, `ViewStub`.
- Ловушка: hardware layer (`setLayerType`) кэширует нарисованный слой — не помогает длинному measure/layout.

### Питфоллы и собеседование
- Спрашивают: «что такое Choreographer / SurfaceFlinger», «как диагностировать jank на 1 млн пользователей». Ответ-карта: Vitals (janky frames rate, перцентили) → reproducer → Perfetto/Profiler → fix → замер.
- FrameLifecycle в Perfetto (API 31+ — FrameTimeline) показывает конкретный кадр и его стадию: measure, layout, draw, GPU.
- В Compose jank чаще из-за лишних recomposition: Layout Inspector → Recomposition Counts до профилировщика.
- Не путайте overdraw и jank: overdraw — лишняя работа GPU, jank — пропуск deadline кадра; лечить нужно причину, показанную в трейсе.
