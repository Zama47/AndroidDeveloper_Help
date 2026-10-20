# Разрешения

Здесь закрываются вопросы `corp_*`: манифест, runtime-запросы, отзыв разрешений и обход без пермиссий (Photo Picker).

### Добавление в манифест
- Все разрешения, к которым обращаетесь, обязаны быть в `AndroidManifest.xml` (`uses-permission`) — включая runtime-разрешения.
- Уровни: `normal` выдаётся при установке, `dangerous` — запрос в рантайме, `signature` — только конкретным подписям.
- Для legacy часто ставят `maxSdkVersion`: например `READ_EXTERNAL_STORAGE` с `maxSdkVersion="33"` — чтобы на новых версиях действовали гранулярные медиа-разрешения, а не общий legacy-доступ.

### Runtime-запрос
- Проверка: `ContextCompat.checkSelfPermission()`. Запрос: `ActivityResultContracts.RequestPermission` (современный путь вместо deprecated `requestPermissions()`).
- Правильный UX-порядок: первый запрос (метод возвращает false) → отказ → `shouldShowRequestPermissionRationale == true` → объяснить, зачем нужен доступ, и запросить снова.
- После «don't ask again» rationale возвращает false — повторный запрос откроет диалог молча и «откажет»; путь в настройки через `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`.
- Ловушка: запускать permission-запрос сразу при старте без объяснения — пользователи чаще отказывают именно так.

### Отзыв и повторные проверки
- Пользователь может отозвать разрешение из настроек в любой момент; на Android 11+ включён auto-revoke — сброс неиспользуемых приложений.
- Поэтому granted-состояние проверяют перед каждым обращением к защищённому API, а не только при первом запуске.
- Ловушка: полагать, что после первого grant состояние «вечное».

### Обход без пермиссий
- **Photo Picker** (`ActivityResultContracts.PickVisualMedia`) — системный UI выбора медиа: приложение получает одноразовый content-URI, READ-разрешение не нужно.
- Частичный доступ: `READ_MEDIA_VISUAL_USER_SELECTED` (API 34+) — доступ только к фото, которые выбрал пользователь.
- Scoped Storage / SAF (`ACTION_OPEN_DOCUMENT`) — выбор документа без манифестных пермиссий.

### Питфоллы и собеседование
- Спрашивают: порядок флоу запроса, когда `shouldShowRequestPermissionRationale` возвращает false, READ_MEDIA_IMAGES vs READ_EXTERNAL_STORAGE на API 33+.
- Смотрите шире: Intent Result API, `PermissionState` в Compose (Accompanist), graceful degradation — как ваша feature работает без доступа.
- Ключевая тема собеса — не «какой метод», а как не крашнуться и не раздражать пользователя: заглушки, альтернативные пути, объяснение перед запросом.
- Ловушка формулировок: «WRITE_MEDIA_IMAGES» не существует — внимательно сверяйте точные имена разрешений.
