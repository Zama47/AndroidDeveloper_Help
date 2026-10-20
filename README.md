<div align="center">

# 📱 Android Developer Help

**Подготовка к собеседованию Android-разработчика: статьи, квизы, свои колоды,
геймификация и интервальное повторение — всё в одном приложении.**

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.10-blueviolet)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/jetpack%20compose-material3-brightgreen)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/room-2.8.4-blue)](https://developer.android.com/training/data-storage/room)
[![Min SDK](https://img.shields.io/badge/minSdk-24-green)](https://developer.android.com/about/versions/nougat)
[![License](https://img.shields.io/badge/license-MIT-yellow)](LICENSE)

*Pet-проект: Android + Jetpack Compose (Material 3), история разработки — в ветках.*

</div>

---

## ✨ Возможности

- **Темы и подразделы** — 13 тем, каждая разбита на подразделы (у каждого —
  своя теория и свой набор карточек): Kotlin, Android Core, Compose, Coroutines,
  Room, Hilt, Architecture, System Design, Testing, Performance, Сеть и API,
  Хранение данных, View-система.
- **Квизы** — 4 типа вопросов: выбор одного, правда/ложь, ввод ответа,
  открытый вопрос-карточка с самооценкой «знал / не знал».
- **Свои колоды и карточки** 🗂 — создавай собственные тесты: MCQ-вопросы
  с вариантами или флеш-карты. Всё проходит через единый квиз-движок.
- **Солянка** 🎲 — перемешанный тест из выбранных тем + опционально свои карточки.
- **Повторение** 🔁 — интервальное повторение (упрощённый SM-2): верные ответы
  растят интервал (`1 → 2 → 6 → рост по ease-фактору`), ошибки сбрасывают
  прогресс. Очередь «к повторению сегодня» — на главном экране.
- **Геймификация** — XP за ответы и сессии, уровни по корневой формуле,
  ежедневный стрик 🔥, 12 достижений 🏆 и экран профиля.
- **Статистика** — прогресс-бары по каждой теме, счётчик «к повторению на сегодня».

## 🏗 Стек и архитектура

| Слой | Технологии |
|------|------------|
| UI | Jetpack Compose, Material 3, Navigation Compose, markdown-рендер (mikepenz) |
| Данные | Room (прогресс, колоды карточек, профиль, достижения), kotlinx.serialization |
| Логика | MVVM (ViewModel + StateFlow), SM-2 repetition engine, Gamification engine |
| Тесты | JUnit: QuizEngine, Gamification, SpacedRepetition |

```
app/src/main/java/com/zama/androiddeveloper_help/
├── data/
│   ├── db/          # Room: entities, DAOs, database, converters
│   ├── domain/      # доменные модели + маппинг
│   ├── repository/  # Content, Progress, Deck, Profile репозитории
│   ├── repetition/  # движок интервального повторения (SM-2)
│   └── content/     # маппинг пользовательских карточек в Question
├── gamification/    # XP, уровни, достижения
├── quiz/            # QuizEngine + MainViewModel
└── ui/              # Home, Topic, Quiz, Decks, Review, Solyanka, Profile
```

Контент (вопросы и статьи) — JSON + Markdown в `app/src/main/assets/content/`,
можно добавлять новые темы без изменения кода.

## 🚀 Сборка

```bash
git clone https://github.com/Zama47/AndroidDeveloper_Help.git
# открыть в Android Studio (Ladybug+) или
./gradlew :app:assembleDebug
```

## 🧪 Тесты

```bash
./gradlew :app:testDebugUnitTest
```

Покрыты квиз-движок (4 типа вопросов), система XP/уровней и достижений,
SM-2 лестница интервалов.

## 🗺 Roadmap

- [ ] Экспорт/импорт колод в JSON (шаринг через intent)
- [ ] Тест «слабые места» — темы с % верных < 50%
- [ ] Bottom navigation вместо быстрых карточек
- [ ] Хаптика и анимации ответов
- [ ] Ежедневное напоминание о повторении
- [ ] Тёмная/светлая тема с динамическими цветами (Material You)

## 🔀 Процесс разработки

История разработки ведётся по [Conventional Commits](https://www.conventionalcommits.org),
каждая фича — в отдельной ветке (`feat/*`, `docs/*`, `test/*`):

```
git log --oneline
09fca03 docs: add project README with features, stack and structure
9681463 test(repetition): gamification and SM-2 unit tests, fix interval ladder off-by-one
032c7bc feat(home): redesign home screen with hero level card ...
7f01d94 feat(mixed): solyanka quiz mode ...
5c06836 feat(review): SM-2 spaced repetition review screen ...
```

## 📄 License

MIT — see [LICENSE](LICENSE).
