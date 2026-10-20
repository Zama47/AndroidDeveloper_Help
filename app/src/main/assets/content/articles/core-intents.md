# Intents и deep link

Здесь закрываются вопросы `cori_*`: явные и неявные интенты, extras, deep link, launchMode и задачи.

### Явный vs неявный
- Явный: `Intent(this, DetailActivity::class.java)` — точный компонент, обычно внутри своего приложения.
- Неявный: описывает действие (`ACTION_VIEW`, `ACTION_SEND`, `ACTION_DIAL`) и данные/тип; система ищет обработчики по `intent-filter`.
- При отсутствии обработчика — `ActivityNotFoundException`; проверяйте через `resolveActivity()` / `Intent.resolveActivity(packageManager)`.
- С Android 11 действует Package Visibility: для поиска чужих обработчиков нужен `<queries>` в манифесте.

### Extras и данные
- `putExtra`/`getExtra` — ключ-значение; для сложных структур — `Bundle`, Parcelable/Serializable (лучше Parcelable).
- Данные URI — `intent.data`, MIME — `intent.type`; `setDataAndType` задаёт оба сразу.
- Ловушка: `setData()` сбрасывает установленный `type` и наоборот — используйте `setDataAndType`.

### Deep link
- Манифест: `intent-filter` с `ACTION_VIEW`, `category BROWSABLE`, `category DEFAULT` и `scheme`/`host`. Для HTTPS App Links — `autoVerify` и `assetlinks.json` на сервере.
- Повторный заход при открытом экране: `launchMode="singleTask"` (или `singleTop`) → Intent идёт в `onNewIntent()`, а не в `onCreate`.
- `singleTop` работает только если Activity на вершине стека — между задачами он не спасает от копий.
- Обработку URI делают в обоих путях: `onCreate` и `onNewIntent` (не забудьте `setIntent`).

### Tasks и back-stack
- Флаги: `FLAG_ACTIVITY_NEW_TASK`, `CLEAR_TOP`, `SINGLE_TOP` — управляют стеком из кода.
- `taskAffinity` и `allowTaskReparenting` — движение Activity между задачами.
- Стек задач сохраняется для недавних; `finishOnTaskLaunch` и `noHistory` — крайние случаи, когда Activity не пересоздаётся при возврате в задачу.

### Питфоллы и собеседование
- Спрашивают чаще всего: чем неявный отличаются от явного, что будет при повторном deep link (`onNewIntent`), зачем `BROWSABLE`, что будет без `createChooser`.
- Смотрите шире:PendingIntent (для нотификаций и приложений с отложенными действиями), App Links vs Custom Scheme (myapp://) — App Links надёжнее, потому что HTTPS-домен и верификация.
- Ловушка: объявить intent-filter, но обрабатывать Intent только в onCreate — повторный заход молча покажет старое содержимое.
- Строгий вопрос: «deep link открывается в вашем браузере, а не в приложении» — обычно забыт `category BROWSABLE` в манифесте или невалидный URL.
