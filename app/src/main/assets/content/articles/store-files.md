# Файлы и scoped storage

Вопросы раздела — про файловую часть Android: приватные каталоги, общие коллекции, MediaStore и раздача файлов другим приложениям.

### Приватные каталоги (без разрешений)

- `filesDir` — внутренняя память, постоянные данные; `cacheDir` — очищаемый системой кэш
- `getExternalFilesDir()` / `getExternalCacheDir()` — то же на внешнем хранилище (`Android/data/<pkg>/`), с API 19 без пермишенов
- Правило: восстановимые данные → cache, уникальные пользовательские → files

### Scoped storage (Android 10+)

- Прямые пути в «общую» файловую систему (Environment.getExternalStorageDirectory) мертвы
- Общие медиа пишутся через `MediaStore`: `ContentResolver.insert(Uri, ContentValues)`, доступ — по `content://`-URI
- Чтение чужих медиа — `READ_MEDIA_IMAGES/VIDEO/AUDIO` (Android 13+), точные координаты файла — `ACCESS_MEDIA_LOCATION`
- Всё, что создаёт приложение, может обновлять само; чужие файлы — через системные диалоги/SAF

### Раздача файлов другим приложениям

- С Android 7 прямой `file://`-URI в Intent → `FileUriExposedException`
- Канонично: `FileProvider` (наследник ContentProvider, конфиг `file_paths.xml`) → `getUriForFile()` → `content://` + `FLAG_GRANT_READ_URI_PERMISSION`
- Ловушка: grant-разрешение действует на приёмник Intent, а не «навсегда»

### Питфоллы и собеседование
- «Как дать другой программе открыть ваш файл?» — ответ всегда начинается с FileProvider
- «Нужно ли разрешение для записи фото?» — в app-specific dir нет; в галерею — MediaStore без разрешений на своё
- Про cacheDir спросят indirectly: что будет при нехватке места (система чистит → данные восстановимы)
