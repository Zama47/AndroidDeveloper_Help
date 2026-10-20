# XML-вёрстка и иерархия View

Вопросы раздела закрывают механику View-системы: как XML превращается в объекты и как View-система измеряет, раскладывает и рисует иерархию.

### LayoutInflater

- `LayoutInflater` парсит XML и строит дерево View в рантайме (`setContentView`, `onCreateView` фрагмента)
- Ссылки на элементы — `findViewById` или типизированный `ViewBinding` (compile-time проверки)

### Три прохода

- **measure**: рекурсивно сверху вниз; родитель передаёт `MeasureSpec` (mode + size) ребёнку, тот вызывает `setMeasuredDimension`
- Mode: `EXACTLY` (фиксированный dp, `match_parent`), `AT_MOST` (край — `wrap_content`), `UNSPECIFIED` (редкий)
- **layout**: назначение позиций `left/top/right/bottom` (размеры уже определены)
- **draw**: фон → `onDraw` → дети; `invalidate()` — перерисовка, `requestLayout()` — пересчёт measure+layout

### ConstraintLayout

- Constraints, chains, `Barrier`, `Guideline`, dimension ratio — плоская иерархия вместо вложенных LinearLayout
- Меньше вложенности → меньше рекурсивной work в measure/layout → меньше jank (актуально для item RecyclerView)
- Ловушка: `LinearLayout` с `weight` может мерить детей дважды — родительная задача «а почему медленно»

### Питфоллы и собеседование
- Спрашивают порядок measure/layout/draw и разницу `invalidate` vs `requestLayout`
- MeasureSpec при `wrap_content` — стандартная ловушка для junior
- Кастомный View: задаём собственные размеры в `onMeasure`, рисуем строго в Canvas из параметра `onDraw`, синхронизируем через `postInvalidate` из форм-потоков
