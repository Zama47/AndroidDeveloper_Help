# RecyclerView

Вопросы раздела — про основной список Android: устройство Adapter/ViewHolder/LayoutManager, диффы и переиспользование.

### Компоненты

- `Adapter` — данные, создание и привязка `ViewHolder`; `ViewHolder` — кэш ссылок на виджеты item'а
- `LayoutManager` — раскладка: `LinearLayoutManager`, `GridLayoutManager`, `StaggeredGridLayoutManager`
- Опционально: `ItemDecoration` (отступы/разделители), `ItemAnimator`, `RecycledViewPool`

### Переиспользование

- `onCreateViewHolder` — редко (создание/inflate), `onBindViewHolder` — постоянно (дешёвый bind)
- Ушедший за экран ViewHolder → Scrap / Cache / RecycledViewPool; отдельные viewType — отдельные пулы
- Правило оптимизации: в bind — только set-операции, без аллокаций и inflate

### DiffUtil и ListAdapter

- `ListAdapter` + `AsyncListDiffer` считает diff в фоне → точечные insert/update/remove с анимациями
- `areItemsTheSame` (тот же id) vs `areContentsTheSame` (то же содержимое); `getChangePayload` — частичное обновление
- `notifyDataSetChanged()` — антипаттерн: полный rebind, нет анимаций, дёргает скролл

### Paging намёк

- Тысячи+ элементов → Paging 3: `PagingSource`-страницы, `loadState`, интеграция с DiffUtil через `PagingDataAdapter`

### Питфоллы и собеседование
- «Почему ViewHolder нужен?» — кэш ссылок по item, `findViewById` один раз, а не на каждый bind
- Сравнение `RecyclerView` и `ListView`: переработка, LayoutManager, декорации, обязательный ViewHolder
- Спрашивают, что случится без `LayoutManager` (краш) и как обновлять список «умно» (ListAdapter/DiffUtil)
