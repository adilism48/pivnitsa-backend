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

Для запуска backend и PostgreSQL необходим Docker.

```bash
docker compose up -d --build
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

### US-03 — регистрация по номеру телефона

#### 1. Отправить OTP

```http
POST /api/v1/auth/send-otp
Content-Type: application/json
```

```json
{
  "phone": "+77001234567",
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
  "phone": "+77001234567",
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
    "phone": "+77001234567"
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
  "phone": "+77001234567",
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
  "phone": "+77001234567",
  "code": "123456"
}
```

Успешный ответ:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "stage": "FULL_ACCESS",
  "user": {
    "id": 1,
    "firstName": "Иван",
    "lastName": "Иванов",
    "phone": "+77001234567"
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
  "phone": "+77001234567"
}
```

Мобильное приложение сохраняет токен в защищённом хранилище и вызывает `/api/v1/users/me` при запуске:

- `200` — открыть главный экран без повторного OTP;
- `401/403` — удалить токен и показать экран входа.

Access token действует 24 часа.

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
