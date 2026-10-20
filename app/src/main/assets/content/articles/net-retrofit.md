# Retrofit и OkHttp

Вопросы этого раздела — про декларативный HTTP-клиент: интерфейс-сервис Retrofit, аннотации, конвертеры и настройку OkHttp под капотом.

### Роли библиотек

- `OkHttp` — низкоуровневый HTTP-клиент: соединения, пул, кэш, перехватчики, таймауты
- `Retrofit` — декларативная обёртка над OkHttp: интерфейс + аннотации → готовые вызовы

### Интерфейс-сервис

- `@GET("users/{id}")` + `@Path("id")` — значение в пути URL
- `@Query` / `@QueryMap` — параметры после `?`
- `@Body` — JSON-тело (сериализует конвертер), `@Header` / `@HeaderMap` — заголовки
- `suspend fun` — канонический способ с корутинами; запрос не блокирует поток
- `Response<T>` как возвращаемый тип — если нужны код и заголовки, а не только тело

### Конвертеры

- `MoshiConverterFactory`, `kotlinx.serialization`, `GsonConverterFactory` — объект ↔ JSON
- Именно конвертер ставит корректный `Content-Type` для `@Body`

### OkHttp: interceptors

- `okhttp3.Interceptor` — точка входа в цепочку запроса/ответа
- `addInterceptor` — application-уровень (Authorization, логирование)
- `addNetworkInterceptor` — сетевой уровень (реально ушедшие запросы, редиректы)
- Стандарт: авторизация-интерсептор + `HttpLoggingInterceptor` (в релизе `NONE`)

### Питфоллы и собеседование
- Забыли `client(okHttpClient)` в `Retrofit.Builder` — таймауты и интерсепторы молча не работают
- `baseUrl` без финального слеша — ловушка компиляции-рантайма
- Спросят разницу Path/Query, зачем ConverterFactory, кто отвечает за ретраи (OkHttp/ваш код, не Retrofit)
