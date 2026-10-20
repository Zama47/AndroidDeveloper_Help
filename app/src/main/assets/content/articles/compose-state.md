# Состояние и state hoisting

Кто владеет состоянием и как его правильно прокидывать вниз по дереву.

### remember и rememberSaveable
- `remember { mutableStateOf(...) }` — значение живёт между recompositions, но умирает при пересоздании Activity.
- `rememberSaveable` — сохраняется в `Bundle` (переживает поворот экрана), требует сериализуемого типа или `Saver`.
- Для данных, которые должны пережить процесс, — `ViewModel` + `SavedStateHandle`.

### State hoisting
- Паттерн: состояние поднимают к общему предку, вниз передают `value: T, onValueChange: (T) -> Unit`.
- Composable становится **stateless** — переиспользуемым и легко тестируемым.
- Single source of truth: у состояния один владелец; вниз — только копии, вверх — события.

### Питфоллы и собеседование
- «Почему mutableStateOf внутри ViewModel?» — правильно: `MutableStateFlow`/`StateFlow` в VM, Compose собирает через `collectAsStateWithLifecycle`.
- Не создавай состояние в глубоко вложенных composable, если оно нужно соседям — это почти всегда баг архитектуры.
- Ловушка: `remember` с ключом — `remember(key) { ... }` пересоздаёт значение при смене ключа, это способ сбросить состояние.
