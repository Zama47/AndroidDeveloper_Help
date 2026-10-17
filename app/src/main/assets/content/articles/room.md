# Room и хранилища данных

## Зачем Room

Room — compile-time абстракция над SQLite: аннотационный процессор генерирует типизированный DAO и **проверяет SQL на этапе сборки**. Опечатка в колонке — ошибка компиляции, а не рантайм-краш. Плюсы: реактивные `Flow`-запросы, миграции, интеграция с Coroutines и Paging.

## Три кита

```kotlin
@Entity(tableName = "questions", indices = [Index("topicId")])
data class QuestionEntity(
    @PrimaryKey val id: String,          // стабильный id из контента
    val topicId: String,
    val text: String,
    val correctAnswer: String
)

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    fun observeByTopic(topicId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun clear()
}

@Database(entities = [QuestionEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
}
```

## Возвращаемые типы DAO

| Тип | Поведение |
|-----|-----------|
| `suspend fun` | однократное выполнение, не блокирует поток (диспетчер внутри Room) |
| `Flow<T>` | **наблюдаемый запрос**: повторный прогон при изменении затронутых таблиц |
| `LiveData<T>` | то же, но привязано к Lifecycle (legacy) |
| `PagingSource` | интеграция с Paging 3 |

```kotlin
@Transaction
suspend fun seedAll(list: List<QuestionEntity>) = insertAll(list)  // атомарно
```

## Связи

- **Один-ко-многим**: `@Relation(parentColumn, entityColumn)` в классе-обёртке (`TopicWithQuestions`) — Room делает два запроса и склеивает.
- **Многие-ко-многим**: промежуточная таблица `CrossRef` с составным `@PrimaryKey`.
- **JOIN** — обычный SQL в `@Query` + POJO-результат.
- `ForeignKey(..., onDelete = CASCADE)` — целостность на уровне схемы.

## Миграции

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE questions ADD COLUMN difficulty REAL NOT NULL DEFAULT 0.5")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

- Без миграции (и без `fallbackToDestructiveMigration`) — **крэш при открытии**.
- `fallbackToDestructiveMigration` допустим только для некритичных кэшей.
- `exportSchema = true` + schema JSON в git → тесты `MigrationTestHelper` проверяют миграцию.

Сложное изменение: создать новую таблицу → `INSERT INTO new SELECT` → `DROP old` → переименовать.

## TypeConverter

Не-стандартные типы превращаются в примитивы:

```kotlin
class Converters {
    @TypeConverter fun fromTags(v: List<String>): String = json.encodeToString(v)
    @TypeConverter fun toTags(v: String): List<String> = json.decodeFromString(v)
}
```

## Производительность

- Пакетная вставка `List<Entity>` в **одной транзакции** — в десятки раз быстрее поштучной.
- `@Index` на колонках `WHERE/JOIN/ORDER BY`.
- Запросы в главном потоке запрещены (`allowMainThreadQueries` — только дебаг).
- WAL по умолчанию: чтение не блокируется фоновой записью.
- Большие выборки — `LIMIT/OFFSET` или Paging 3.

## Засев стартового контента

```kotlin
override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)
    CoroutineScope(Dispatchers.IO).launch { seedFromAssets() }
}
```

В `seedFromAssets`: читаем `assets/content/*.json` → `kotlinx.serialization` → `insertAll` в `@Transaction`. Версию контента храним отдельно (SharedPreferences/meta-таблица): при обновлении версии в релизе — re-seed через `REPLACE` по стабильным id (прогресс пользователя не теряется, таблицы `answers/progress` не трогаем).

Альтернатива — `Room.databaseBuilder(...).createFromAsset("content.db")` с уже готовым файлом базы.

## DataStore вместо SharedPreferences

| SharedPreferences | DataStore |
|---|---|
| синхронные чтения, `apply()` с неопределённым моментом записи | **асинхронный** API на Flow/suspend |
| `SharedPreferences.Editor` с commit-гонками | транзакционные обновления, целостность |
| нет типобезопасности ключей | Preferences или **Proto DataStore** с типизированной схемой |

```kotlin
val Context.settings by preferencesDataStore("settings")
suspend fun saveOnboarded(v: Boolean) = settings.edit { it[KEY_ONBOARDED] = v }
val onboarded: Flow<Boolean> = settings.data.map { it[KEY_ONBOARDED] ?: false }
```

Для простых пар «ключ-значение» — DataStore; для структурированных данных — Room.

## Offline-first: типичный паттерн репозитория

```kotlin
class QuestionsRepository(
    private val dao: QuestionDao,
    private val api: ContentApi
) {
    fun observe(topicId: String): Flow<List<Question>> =
        dao.observeByTopic(topicId).map { it.map(QuestionEntity::toDomain) }

    suspend fun sync() {                       // обновление из сети
        val remote = api.fetchQuestions()
        dao.insertAll(remote.map(QuestionDto::toEntity))
    }
}
```

UI собирает `Flow` из Room (всегда актуально, офлайн-доступно), фоновый `sync` обновляет — классическая схема **single source of truth = локальная база**.

## Чек-лист для собеседования

1. Room = SQLite + compile-time SQL-проверки; entity/dao/database.
2. `Flow` в DAO = реактивный запрос на каждое изменение таблицы.
3. Миграции обязательны; иначе крэш или деструктивный fallback.
4. Пакетные вставки в одной транзакции + индексы по WHERE-колонкам.
5. DataStore вместо SharedPreferences для нового async-кода.
6. Один источник истины — Room; сеть только обновляет его.
