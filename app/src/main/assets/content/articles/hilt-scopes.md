# Компоненты и скоупы

Здесь закрываются вопросы `hls_*`: дерево компонентов, `@Singleton` vs `@ViewModelScoped` vs `@ActivityScoped`, `@Module`/`@InstallIn`.

### Дерево компонентов
- Корень — `SingletonComponent` (@HiltAndroidApp); для Activity/ViewModel/Fragment генерируются свои компоненты.
- Иерархия: потомок видит биндинги родителя, обратное — ошибка компиляции. Скоуп жёстко привязан к компоненту (`@Singleton` ↔ `SingletonComponent`), несовпадение — compile-time ошибка.

### Основные скоупы и их жизнь
| Скоуп | Компонент | Живёт |
|---|---|---|
| `@Singleton` | SingletonComponent | весь процесс |
| `@ActivityRetainedScoped` | ActivityRetainedComponent | переживает config change (как ViewModelStore); умирает при finish/process death |
| `@ViewModelScoped` | ViewModelComponent | одна ViewModel |
| `@ActivityScoped` | ActivityComponent | одна Activity: НЕ переживает поворот |
| `@FragmentScoped` / `@ViewScoped` | FragmentComponent / ViewComponent | один Fragment / View |

- Unscoped binding = фабрика: новый экземпляр на каждый запрос. `@Reusable` — «можно переиспользовать, но не обязан».

### @Module и @InstallIn
- `@Module @InstallIn(SingletonComponent::class)` — модуль установлен в конкретный компонент; его биндинги доступны поддереву.
- @Binds-биндинги живут в abstract-классах, @Provides — в object/class-модулях.
- Ловушка: `@InstallIn(ViewModelComponent::class)` + `@Singleton` — ошибка компиляции; имени ApplicationComponent в Hilt нет (это Dagger 2-наследие).

### Когда что скоупить
- Скоуп стоит только там, где важно единство: shared-кэш,.OkHttp-пул, соединения → @Singleton; зависимость с жизненным циклом ViewModel → @ViewModelScoped.
- Лишний скоуп — риск утечек: @Singleton внутри держит Context Activity = утечка всего экрана.

### Питфоллы и собеседование
- Спрашивают: разницу @Singleton и @ActivityRetainedScoped, что с unscoped-биндингом, почему скоуп обязан матчиться с компонентом.
- Смотрите шире: скоупы внутри иерархии, `@Reusable`, `EntryPointAccessors.fromApplication/fromActivity`.
- Ловушка: «@Singleton переживает process death» — нет, при новом процессе всё создаётся заново.
