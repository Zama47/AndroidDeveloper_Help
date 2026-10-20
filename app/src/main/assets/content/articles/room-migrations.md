# Миграции и версии

Здесь закрываются вопросы `rmm_*`: version bump, `Migration` SQL, destructive fallback, экспорт схемы.

### Версия и обязательность миграции
- У каждой БД есть `version` в `@Database`; при повышении Room ищет путь миграции или падает с `IllegalStateException`.
- Room строит граф: `Room.databaseBuilder(...).addMigrations(MIGRATION_1_2, MIGRATION_2_3)` — путь по цепочке, можно прямую `MIGRATION_1_3`.
- `identity hash` и валидация схемы при открытии: расхождение = крэш, а не «молчаливое несоответствие». Это защитная механика.

### Migration
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE questions ADD COLUMN difficulty REAL NOT NULL DEFAULT 0.5")
    }
}
```
- Рецепт для структурных изменений (SQLite < 3.25): CREATE new → INSERT INTO new SELECT → DROP old → RENAME; DROP TABLE уносит индексы/триггеры — пересоздайте их.

### fallbackToDestructiveMigration
- Создаёт базу заново: **все данные пользователя пропадают**. Допустимо: чистый кэш, debug-сборки.
- Если fallback всё же нужен — ограничивайте его (`fallbackToDestructiveMigrationOnDowngrade()`), а не глобально.

### exportSchema и тесты
- `exportSchema = true` в `@Database` + `room.schemaLocation` в Gradle → JSON-файлы схемы каждой версии; храните их в git.
- `MigrationTestHelper` (androidTest) грузит схему vN, вставляет данные, прогоняет миграцию и валидирует результат; AutoMigration (Room 2.4+) автоматизирует простые изменения.

### Питфоллы и собеседование
- Спрашивают: что будет без миграции, риски destructive fallback, как протестировать миграцию, можно ли мигрировать «через несколько версий».
- Смотрите шире: валидация схемы при открытии, `SupportSQLiteDatabase.execSQL`, связка schema JSON ↔ AutoMigration.
- Опытный ответ вокруг пользователя: fallback — только там, где потеря данных допустима; для остального Migration + schema в git.
