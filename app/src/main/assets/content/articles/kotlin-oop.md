# ООП: data, sealed, generics

Эта статья закрывает вопросы kto_001–kto_007: data class, sealed-иерархии, object, дженерики c variance (in/out) и делегирование через `by`.

### data class
- Генерирует equals/hashCode/toString/copy/componentN из свойств primary конструктора и только их — свойства из тела класса игнорируются
- Следствие: «хвостовые» var-поля тела рассинхронизируют equals — держите все значимые поля в конструкторе
- copy() с именованными аргументами — дешёвый способ «изменить» immutable-модель

### sealed
- `sealed class/interface` — закрытая иерархия: все прямые наследники объявлены заранее, в том же модуле (для sealed class — ещё и в том же пакете)
- Главный выигрыш — exhaustive `when`: без ветки else, при добавлении состояния компилятор корректирует все места использования
- Состояния без данных — `data object Loading` (Kotlin 1.9+); не собирайте состояние boolean-флагами — sealed исключает недостижимые комбинации

### Дженерики и variance
- `out T` — производитель (тип только в return): `List<out T>` ковариантен, `List<String>` является `List<Any>`
- `in T` — потребитель (тип только в параметрах): контравариантность
- `MutableList<T>` инвариантен: и `add(T)` (вход), и `get()` (выход) — иначе подсыпали бы Int в список строк

### Делегирование и object
- `class UserService(logger: Logger) : Logger by logger` — переадресация реализации интерфейса объекту без boilerplate; отдельные методы можно переопределить
- `object` — синглтон-декларация (экземпляр создаётся при первом обращении); companion object — статик-подобный член класса
- Не путать: `by lazy`, `by viewModels()`, `by mutableStateOf()` — property-делегаты, другой механизм с тем же ключевым словом

### Питфоллы и собеседование
- Топ-вопросы: «что попадает в equals у data class», «чем sealed отличается от abstract», «почему List ковариантен, а MutableList нет»
- Ловушка: `else` в when по sealed-типу отменяет exhaustive-проверку — новый вариант пройдёт тихо в else
- Практика: state-screen-model = sealed + data class/object; репозиторий-декоратор = interface delegation
