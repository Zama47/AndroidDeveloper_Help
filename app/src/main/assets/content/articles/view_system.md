# View-система

Тема про классический UI-стек Android (XML/View) — база, которую спрашивают даже в командах на Compose.

Схема изучения: механика отрисовки как фундамент, затем списки как самый частый сценарий, затем динамика — анимации и переходы.

### Что внутри

- **XML-вёрстка и иерархия** — LayoutInflater, три прохода measure/layout/draw, ConstraintLayout
- **RecyclerView** — Adapter/ViewHolder/LayoutManager, переиспользование, DiffUtil, Paging-намёк
- **Анимации и переходы** — property-анимации, MotionLayout, Transition, shared elements

### К чему готовить

Собеседование проверяет механику, а не заученные аннотации: как View измеряется (MeasureSpec, EXACTLY/AT_MOST/UNSPECIFIED), почему глубокая вложенность даёт jank, чем property-анимация отличается от tween, что делает DiffUtil вместо notifyDataSetChanged.

Для кастомного View ждут onMeasure/onDraw и requestLayout vs invalidate; для списков — историю переиспользования ViewHolder и типовые оптимизации (bind без аллокаций, фиксированный размер, Paging для больших данных).
