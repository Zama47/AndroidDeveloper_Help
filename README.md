# Android Developer Help

Учебное приложение для подготовки к собеседованию Android-разработчика: статьи
по 10 темам, квизы, свои тесты и карточки, геймификация и интервальное повторение.

## Возможности

- **Темы и статьи** — Kotlin, Android Core, Compose, Coroutines, Room, Hilt,
  Architecture, System Design, Testing, Performance. Теория + вопросы по теме.
- **Квизы** — 4 типа вопросов: выбор одного, правда/ложь, ввод ответа,
  открытый вопрос-карточка с самооценкой.
- **Свои колоды и карточки** — создавай свои тесты: MCQ-вопросы с вариантами или
  флеш-карты. Карточки проходят через единый квиз-движок.
- **Солянка 🎲** — перемешанный тест из выбранных тем (+ свои карточки).
- **Повторение 🔁** — интервальное повторение (упрощённый SM-2): верные ответы
  растят интервал (1 → 2 → 6 → рост по ease-фактору), ошибки сбрасывают прогресс.
- **Геймификация** — XP, уровни, ежедневный стрик, 12 достижений.
- **Статистика** — прогресс по каждой теме, счётчик «к повторению на сегодня».

## Стек

- Kotlin + Jetpack Compose (Material 3)
- Room (прогресс, колоды, профиль, достижения)
- Navigation Compose
- kotlinx.serialization, markdown-рендер (mikepenz)
- JUnit для логики (SM-2, Gamification, QuizEngine)

## Структура

```
app/src/main/java/com/zama/androiddeveloper_help/
├── data/
│   ├── db/          # Room: entity, dao, database
│   ├── domain/      # доменные модели + маппинг
│   ├── repository/  # Content, Progress, Deck, Profile репозитории
│   ├── repetition/  # SM-2 движок
│   └── content/     # маппинг пользовательских карточек в Question
├── gamification/    # XP/уровни/достижения
├── quiz/            # MainViewModel, QuizEngine
└── ui/              # экраны (Home, Topic, Quiz, Decks, Review, Solyanka, Profile)
```

Контент — JSON+MD в `app/src/main/assets/content/`.

## Тесты

```
./gradlew :app:testDebugUnitTest
```
