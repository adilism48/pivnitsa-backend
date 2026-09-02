# Пивница Backend

## 🛠 Процесс разработки и ветвление

В качестве основной ветки разработки по умолчанию (Default Branch) используется **`develop`**.

### Назначение основных веток:
* **`develop`** — ветка активной разработки. Код из этой ветки автоматически собирается и разворачивается на **тестовом стенде**.
* **`main`** — стабильная ветка. Код попадает сюда только после успешного тестирования на тестовом стенде при релизе.

---

### Правила работы с репозиторием:
1. **Прямые пуши запрещены:** Вливание кода в ветки `develop` и `main` происходит исключительно через **Pull Request (PR)**.
2. **Создание новых веток:**
   * Все новые ветки создаются строго **от ветки `develop`**:
     ```bash
     git checkout develop
     git pull origin develop
     ```
   * Для новой функциональности используйте префикс `feature/`:  
     `git checkout -b feature/название-задачи`
   * Для исправления багов используйте префикс `bugfix/`:  
     `git checkout -b bugfix/описание-бага`

---

## Локальный запуск

Для запуска backend и PostgreSQL, Redis, Garage необходим Docker.

```bash
docker compose up -d --build
```

Чтобы загруженные в локальный S3 изображения были доступны по ссылке

```bash
docker exec -it garage_s3 /garage bucket website pivnitsa-media-local --allow
```

Проверить состояние контейнеров:

```bash
docker compose ps
```

Посмотреть логи backend:

```bash
docker compose logs -f backend
```

Остановить контейнеры:

```bash
docker compose down
```

Данные PostgreSQL сохраняются в именованном Docker volume `postgres-data`, поэтому обычный
`docker compose down` не удаляет базу. Для полного сброса локальных данных используйте:

```bash
docker compose down -v
```

После запуска доступны:

| Сервис | Адрес |
| --- | --- |
| Backend | <http://localhost:8080> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Health-check | <http://localhost:8080/actuator/health> |
| PostgreSQL | `localhost:5433` |

## API

### Health-check

Проверяет доступность backend-сервиса и подключённых компонентов.

```http
GET /actuator/health
```

Успешный ответ — `200 OK`:

```json
{
  "status": "UP"
}
```

Интерактивная документация реализованных endpoint'ов автоматически формируется
в формате OpenAPI и доступна в Swagger UI после запуска приложения.

### JWT-авторизация в Swagger

Чтобы вызвать защищённые endpoint'ы через Swagger UI:

1. Получите `accessToken` через `/api/v1/auth/login/verify-otp`.
2. Откройте <http://localhost:8080/swagger-ui.html>.
3. Нажмите **Authorize**.
4. Вставьте JWT без префикса `Bearer`.

Swagger автоматически добавит заголовок:

```http
Authorization: Bearer <accessToken>
```

### US-03 — регистрация по номеру телефона

#### 1. Отправить OTP

```http
POST /api/v1/auth/send-otp
Content-Type: application/json
```

```json
{
  "phone": "+996700123456",
  "channel": "SMS"
}
```

Успешный ответ — `200 OK`:

```json
{
  "message": "Код успешно отправлен."
}
```

#### 2. Подтвердить OTP

```http
POST /api/v1/auth/verify-otp
Content-Type: application/json
```

```json
{
  "phone": "+996700123456",
  "code": "123456"
}
```

Успешный ответ:

```json
{
  "message": "Номер телефона успешно подтвержден.",
  "token": "eyJ...",
  "stage": "PROFILE_REQUIRED"
}
```

Полученный токен является временным и используется только для завершения регистрации.

#### 3. Заполнить профиль

```http
POST /api/v1/auth/complete-profile
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "firstName": "Иван",
  "lastName": "Иванов",
  "termsAccepted": true,
  "privacyAccepted": true
}
```

Успешный ответ:

```json
{
  "message": "Профиль успешно заполнен.",
  "token": "eyJ...",
  "stage": "AUTHENTICATED",
  "user": {
    "id": 1,
    "firstName": "Иван",
    "lastName": "Иванов",
    "phone": "+996700123456"
  }
}
```

Полученный токен предоставляет полный доступ к приложению.

### US-04 — вход по номеру телефона

#### 1. Отправить OTP

```http
POST /api/v1/auth/login/send-otp
Content-Type: application/json
```

```json
{
  "phone": "+996700123456",
  "channel": "SMS"
}
```

Успешный ответ:

```json
{
  "message": "Код для входа успешно отправлен.",
  "retryAfterSeconds": 60
}
```

#### 2. Подтвердить OTP и войти

```http
POST /api/v1/auth/login/verify-otp
Content-Type: application/json
```

```json
{
  "phone": "+996700123456",
  "code": "123456"
}
```

Успешный ответ:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "stage": "AUTHENTICATED",
  "user": {
    "id": 1,
    "firstName": "Иван",
    "lastName": "Иванов",
    "phone": "+996700123456",
    "email": "ivan@example.com"
  }
}
```

#### 3. Проверить сохранённую сессию

```http
GET /api/v1/users/me
Authorization: Bearer <accessToken>
```

Успешный ответ:

```json
{
  "id": 1,
  "firstName": "Иван",
  "lastName": "Иванов",
  "phone": "+996700123456",
  "email": "ivan@example.com"
}
```

Мобильное приложение сохраняет токен в защищённом хранилище и вызывает `/api/v1/users/me` при запуске:

- `200` — открыть главный экран без повторного OTP;
- `401/403` — удалить токен и показать экран входа.

Access token действует 24 часа.

### US-05 — просмотр и редактирование профиля

Все endpoint'ы US-05 требуют JWT с уровнем доступа `FULL_ACCESS`.

#### 1. Получить текущий профиль

```http
GET /api/v1/users/me
Authorization: Bearer <accessToken>
```

Успешный ответ — `200 OK`:

```json
{
  "id": 1,
  "firstName": "Иван",
  "lastName": "Иванов",
  "phone": "+996700123456",
  "email": "ivan@example.com"
}
```

#### 2. Обновить профиль

Телефон не передаётся в теле запроса и не изменяется этим endpoint'ом.

```http
PUT /api/v1/users/me
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "firstName": "Иван",
  "lastName": "Петров",
  "email": "ivan.petrov@example.com"
}
```

Успешный ответ — `200 OK`:

```json
{
  "id": 1,
  "firstName": "Иван",
  "lastName": "Петров",
  "phone": "+996700123456",
  "email": "ivan.petrov@example.com"
}
```

#### 3. Отправить OTP для смены телефона

Код отправляется на новый, ещё не занятый номер.

```http
POST /api/v1/users/me/phone-change/send-otp
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "newPhone": "+996555123456",
  "channel": "SMS"
}
```

Успешный ответ — `200 OK`:

```json
{
  "message": "Код отправлен на новый номер",
  "retryAfterSeconds": 60
}
```

Если номер уже принадлежит другому пользователю, сервер возвращает `409 Conflict`.

#### 4. Подтвердить смену телефона

```http
POST /api/v1/users/me/phone-change/confirm
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "newPhone": "+996555123456",
  "code": "123456"
}
```

Успешный ответ содержит новый JWT и обновлённый профиль:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "firstName": "Иван",
    "lastName": "Петров",
    "phone": "+996555123456",
    "email": "ivan.petrov@example.com"
  }
}
```

После успешной смены номера клиент должен заменить сохранённый access token значением
`accessToken` из ответа.

### US-06 — Выход из учётной записи

Блокирует текущий JWT-токен пользователя путем добавления его jti в черный список (Blacklist) в Redis.

```http
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
```

Успешный ответ — `204 No content`:

### US-08 — Баннеры мероприятий для главного экрана

Выдает лимитированный и отсортированный по дате и статусу список мероприятий для баннера главного экрана. Минимальный лимит 1 максимальный 50.
Выводит только со статусом `PUBLISHED`

```http
GET /api/v1/events/banners?limit=2
```

Успешный ответ — `200 OK`:

```json
{
  "eventBanners": [
    {
      "id": 1,
      "title": "Event 1",
      "bannerUrl": "IMG-URL",
      "startsAt": "2026-08-08T20:19:23.315808Z"
    },
    {
      "id": 3,
      "title": "Event 2",
      "bannerUrl": "IMG-URL",
      "startsAt": "2026-08-12T20:19:23.315808Z"
    }
  ]
}
```

### US-11 — Список всех предстоящих мероприятий

Возвращает постраничный список предстоящих опубликованных мероприятий.

Endpoint доступен гостям без JWT.

В список попадают только мероприятия:

- со статусом `PUBLISHED`;
- с датой начала не раньше текущего времени.

Мероприятия сортируются по дате начала — ближайшие отображаются первыми.

```http
GET /api/v1/events?page=0&size=10
```
Параметры:

Параметр	Значение по умолчанию	Описание
page	0	Номер страницы, начиная с нуля
size	10	Количество элементов, допустимый диапазон — от 1 до 50

Успешный ответ — 200 OK:
```json
{
  "items": [
    {
      "id": 1,
      "title": "DJ Night Party",
      "bannerUrl": "http://pivnitsa-media-local.localhost:3902/banners/event.jpg",
      "startsAt": "2026-08-25T21:00:00Z"
    },
    {
      "id": 2,
      "title": "Live Music Evening",
      "bannerUrl": null,
      "startsAt": "2026-08-30T20:30:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "hasNext": true
}
```

Если hasNext равен true, клиент запрашивает следующую страницу:

```http
GET /api/v1/events?page=1&size=10
```

Если предстоящих опубликованных мероприятий нет:

```json
{
  "items": [],
  "page": 0,
  "size": 10,
  "hasNext": false
}
```
Если изображение отсутствует, bannerUrl возвращается как null, а клиент должен показать запасное изображение.

### US-12 — Поделиться мероприятием

Возвращает HTML-страницу веб-шеринга (Landing Page) мероприятия. 
Страница содержит OpenGraph-метатеги для формирования карточки предпросмотра в мессенджерах и соцсетях, 
а также ссылки для открывания приложения через Deep Link или перехода в соответствующий магазин приложений 
(App Store / Google Play в зависимости от User-Agent).

У каждого Event из API есть shareUrl:
```http
GET www.example.com/events/{id}
```
Ответ: HTML страница

### US-28 — Push о новых вечеринках

#### Регистрация device-токена

```http
POST /api/v1/devices/tokens
```
```json
{
  "token": "fcm-device-token",
  "deviceType": "ANDROID"
}
```

Успешный ответ — 200 OK

#### Удаление device-токена

```http
DELETE /api/v1/devices/tokens?token=fcm-device-token
```

Удаляется только токен, принадлежащий авторизованному пользователю. Успешный ответ — 204 No Content

#### Payload push-уведомления

Отправляется через Firebase Cloud Messaging всем гостям, подписанным на уведомления о мероприятиях (event_notifications_enabled = true), в момент публикации мероприятия:

```json
{
  "notification": {
    "title": "Новое мероприятие!",
    "body": "Event title"
  },
  "data": {
    "type": "OPEN_EVENT",
    "eventId": "1"
  }
}
```

По нажатию на push мобильное приложение читает type/eventId из data и открывает карточку мероприятия с этим id.

### US-32 — Создание и публикация мероприятий (админ)

Создание, редактирование, удаление и публикация мероприятий администратором. Создание и редактирование выполняются как `multipart/form-data` с полями `title`, `description`, `status`, `startsAt`, `endsAt` и `file` (баннер).

Нельзя создать мероприятие сразу со статусом `ARCHIVED`.

```http
POST /api/v1/admin/events
Content-Type: multipart/form-data
```

Успешный ответ — 201 Created:
```json
{
  "id": 1,
  "title": "Example event",
  "description": "Example event description",
  "bannerUrl": "https://media.example.com/banners/image.png",
  "status": "PUBLISHED",
  "canonicalUrl": "https://example.com/events/1",
  "startsAt": "2026-09-03T09:04:16.596Z",
  "endsAt": "2026-09-04T09:04:16.596Z",
  "createdAt": "2026-09-02T09:04:16.596Z",
  "updatedAt": "2026-09-02T09:04:16.596Z"
}
```

#### Редактирование мероприятия

Поля обновляются частично — если поле не передано, оно остаётся без изменений. Файл баннера передавать не обязательно: если новый файл не прикреплён, текущий баннер сохраняется.

Редактирование архивированного мероприятия (`status = ARCHIVED`) запрещено.

```http
PUT /api/v1/admin/events/{id}
Content-Type: multipart/form-data
```

Успешный ответ — 200 OK:
```json
{
  "id": 1,
  "title": "Example event",
  "description": "Example event description",
  "bannerUrl": "https://media.example.com/banners/image.png",
  "status": "PUBLISHED",
  "canonicalUrl": "https://example.com/events/1",
  "startsAt": "2026-09-03T09:04:16.596Z",
  "endsAt": "2026-09-04T09:04:16.596Z",
  "createdAt": "2026-09-02T09:04:16.596Z",
  "updatedAt": "2026-09-02T09:04:16.596Z"
}
```

#### Удаление мероприятия

Удаляет мероприятие и связанный с ним баннер.

```http
DELETE /api/v1/admin/events/{id}
```

Успешный ответ — 204 No content

#### Публикация мероприятия

Переводит мероприятие из статуса `DRAFT` в `PUBLISHED`.

```http
PATCH /api/v1/admin/events/{id}/publish
```

Успешный ответ — 200 OK

### US-33 — Список бронирований для администратора

Возвращает список бронирований за указанный период времени для административной панели с информацией о клиенте, столике, статусе бронирования и статусе оплаты.

Выборка осуществляется в интервале `from=...`&`to=...`. Если параметр `to` не передан, он автоматически устанавливается как `from + 24 часа`
```http
GET /api/v1/admin/bookings?from=2026-08-25T00:00:00%2B06:00
```

Успешный ответ — 200 OK:
```json
{
  "id": 1,
  "tableNumber": "T-1",
  "bookingAt": "2026-08-25T19:00:00+06:00",
  "firstName": "Jane",
  "lastName": "Doe",
  "guestPhone": "+996500112233",
  "guestsCount": 4,
  "amount": 1500.00,
  "bookingStatus": "CONFIRMED",
  "paymentStatus": "SUCCEEDED",
  "cancellationReason": null
}
```

#### Отмена брони с указанием причины.

```http
PATCH /api/v1/admin/bookings/{id}/cancel
```
```json
{
  "reason": "string"
}
```

Успешный ответ — 204 No content

## База данных

В локальном окружении используется PostgreSQL 17. Схема создаётся автоматически
при запуске приложения с помощью Flyway.

Начальная миграция:

```text
src/main/resources/db/migration/V1__create_initial_schema.sql
```

Основные таблицы:

- `users` — пользователи приложения;
- `club_tables` — столики клуба;
- `events` — мероприятия;
- `bookings` — бронирования;
- `payments` — платежи;
- `flyway_schema_history` — история применённых миграций.

Проверить применённые миграции:

```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Для подключения через pgAdmin:

```text
Host: 127.0.0.1
Port: 5433
Database: pivnitsa
Username: pivnitsa
```
