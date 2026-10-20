# Preferences и DataStore

Вопросы раздела закрывают хранение мелких пар «ключ-значение»: SharedPreferences, два вида DataStore и переход между ними.

### SharedPreferences

- Синхронный API: `getString(...)`, `edit().commit()` (блокирует поток, возвращает Boolean) или `apply()` (write-behind, без результата)
- Ловушка: `commit()` на main thread — jank/ANR при большой записи
- Под капотом — XML-файлы в каталоге приложения

### Preferences DataStore

- Асинхронный мир: `data: Flow<Preferences>` для чтения, `suspend edit { it[key] = value }` для записи
- Ключи типизированы функциями-фабриками: `stringPreferencesKey`, `booleanPreferencesKey`, `intPreferencesKey`
- Никаких синхронных методов — ANR не прилетит; `catch { emptyPreferences() }` для повреждённого файла

### Proto DataStore

- Схема в `.proto`: типизированный объект, дефолтные значения, компил-тайм проверки
- Чтение — `Flow<T>`, запись — `update { }` (suspend), сериализует protobuf
- Выбор: «несколько скаляров» → Preferences DS; «строгое сообщение со схемой» → Proto

### Миграция SP → DataStore

- `addMigration(SharedPreferencesMigration(context, "имя_prefs"))`: переносится автоматически при первом чтении, старый файл удаляется
- В mapping-блоке раскладываете старые ключи в новые типизированные; несоответствие типа — исключение

### Питфоллы и собеседование
- Всегда спрашивают: commit vs apply, почему DataStore, чем Proto отличается
- Отвечайте через async-API и запреты: «SP умеет блокировать поток и ловить ANR», а DataStore это снимает принципиально
- Совет: для нового кода Preferences DataStore; SP — только legacy
