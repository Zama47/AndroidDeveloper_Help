# UI-тесты в Compose

Здесь закрываются вопросы `tsc_*`: `createComposeRule`, semantics, `onNodeWithText`/`onNodeWithTag`, проверки состояния.

### Два правила
- `createComposeRule()` — для проверки отдельного composable: сами вызываете `setContent {}`, Activity не нужна.
- `createAndroidComposeRule<MainActivity>()` — для целого Activity: E2E-интенции, темы, реальный lifecycle.
- С Coroutines: правило работает на main thread — для suspend-подготовки используйте `runTest` + отдельный await-механизм (Turbine), не блокируйте главный поток.

### Semantics — «компас» тестов
- Все матчеры работают по semantics-дереву, а не по байтам рисования: текст, contentDescription, роли, действия (click).
- `Modifier.testTag("...")` — язык-независимый якорь; `onNodeWithTag` — его пара.
- `onNodeWithText("Войти")` — по тексту, но рушится при локализации (годится для пользовательских сценариев, не для стабильных локаторов).
- `onNodeWithContentDescription("...")` — сочетает accessibility и тесты; `onAllNodesWithText/Tag` — когда совпадений несколько.

### Действия и проверки
- Действия: `performClick`, `performTextInput`, `performScrollToNode`, `performTouchInput { swipeLeft() }`.
- Проверки: `assertIsDisplayed`, `assertExists`, `assertTextEquals`, `assertHasClickAction`, `assertIsEnabled`.
- Синхронизация: `waitForIdle()` — дожидается стабилизации композиции/анимаций; `waitUntil { ... }` — для асинхронных данных; `mainClock.advanceTimeBy` — анимации/таймы.
- Ловушка: `Thread.sleep` — закономерный источник флаков; Compose-тесты — за синхронизацией по готовности.

### Contract-подход
- Валидируйте не «как рисуется», а контракт: при клике появляется состояние X — assert по semantics, а не по пикселям.
- Для визуального сравнения — отдельный мир: screenshot-тесты (Paparazzi/Roborazzi).

### Питфоллы и собеседование
- Спрашивают: чем `createComposeRule` отличается от `createAndroidComposeRule`, как искать элементы, зачем semantics, как тестировать поведение после клика.
- Смотрите шире: `waitUntil`, `MainTestClock`, `SemanticsMatcher` (свои предикаты), скриншот-тесты (Paparazzi/Roborazzi) — визуальное сравнение отдельно от semantics.
- Ловушка: E2E-тест на `createComposeRule` с Thread.sleep — невалидно или флак; для E2E — `createAndroidComposeRule` + Espresso-Intents/UiAutomator.
