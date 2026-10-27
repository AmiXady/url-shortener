# URL Shortener

> Тестовое задание с собеседования в компанию X на Java Middle. Условие — одна фраза:
> *«Сделать сервис сокращения ссылок: длинный URL → короткий, по короткому — редирект на оригинал»*.
> Требования (функциональные и нефункциональные), архитектуру и код нужно было построить самому.
> Весь путь — в [`docs/`](docs).

## Стек

Java 21 · Spring Boot 3.5 · PostgreSQL 17 + Flyway · Redis · Kafka · Docker Compose · Maven multi-module

## Сервисы

| Сервис                               | Порт | Что делает                                                              |
|--------------------------------------|------|-------------------------------------------------------------------------|
| [link-service](link-service)         | 8081 | Создание ссылок (по одной и пачкой), удаление, статистика для создателя |
| [redirect-service](redirect-service) | 8082 | Редирект `302` / `404` / `410`, кэш в Redis, отправка кликов в Kafka    |
| [stats-service](stats-service)       | 8083 | Клики из Kafka, отсев роботов, счётчики переходов                       |

```
link-service ──► Postgres (links) ◄── redirect-service ──► Redis
     │                                       │
     └──► stats-service ◄── Kafka ◄──────────┘
               │
               └──► Postgres (link_clicks)
```

## Запуск

```bash
# 1. Инфраструктура: Postgres, Redis, Kafka
docker compose -f docker/docker-compose.yml up -d

# 2. Сборка (тестам нужна запущенная инфраструктура)
mvn clean install -DskipTests

# 3. Сервисы — в этом порядке
mvn -pl link-service spring-boot:run
mvn -pl redirect-service spring-boot:run
mvn -pl stats-service spring-boot:run
```

## API

| Метод    | Путь                           | Описание                               | Успех                 |
|----------|--------------------------------|----------------------------------------|-----------------------|
| `POST`   | `:8081/api/links`              | Создать ссылку                         | `201`                 |
| `POST`   | `:8081/api/links/batch`        | Пачка до 1000 ссылок                   | `200`                 |
| `GET`    | `:8081/api/links/{code}/stats` | Статистика, нужен `X-Management-Token` | `200`                 |
| `DELETE` | `:8081/api/links/{code}`       | Удалить, нужен `X-Management-Token`    | `204`                 |
| `GET`    | `:8082/{code}`                 | Редирект                               | `302` / `404` / `410` |

## Попробовать

```bash
# создать — запомнить code и token
curl -X POST localhost:8081/api/links -H 'Content-Type: application/json' \
     -d '{"originalUrl":"https://example.com"}'

# перейти как браузер (curl без -A считается роботом)
curl -i -A "Mozilla/5.0 Safari/605.1.15" localhost:8082/<code>

# статистика
curl localhost:8081/api/links/<code>/stats -H 'X-Management-Token: <token>'
```

## Ключевые решения

- **Чтение и запись — разные сервисы**: переходов в 100 раз больше, требования строже (50 мс p99, 99,99%).
- **Код — 7 символов без регистра и путающихся `0 o 1 l i`**: ~27 млрд комбинаций, удобно набирать вручную.
- **Без регистрации**: токен управления выдаётся при создании, в базе — только SHA-256 хэш.
- **Мягкое удаление**: удалённая ссылка отвечает `410`, её код год нельзя занять.
- **Redis с TTL 10 с**: удалённая ссылка перестаёт работать не позже чем через 10 секунд.
- **Клики асинхронно через Kafka**: сбой статистики не ломает редирект.
- **Атомарные счётчики**: upsert на пачку, обновление по возрастанию id против дедлоков.

## Дальше

- [ ] UC-8: очистка устаревших ссылок
- [ ] Тесты: unit + Testcontainers
- [ ] Docker-образы сервисов
- [ ] API Gateway, Kubernetes