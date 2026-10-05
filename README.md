# Fuel Station Monitor

Backend-сервис для мониторинга сети автозаправочных станций и доступности топлива.

Проект разработан на Java + Spring Boot. Основная задача — предоставить API для получения информации об АЗС, видах топлива, ценах и текущих остатках.

В процессе разработки проект был расширен техническими задачами, связанными не только с CRUD-операциями, но и с **конкурентным обновлением данных, optimistic locking, многопоточностью, тестированием и подготовкой сервиса к production-нагрузке**.

---

## Содержание

- [О проекте](#о-проекте)
- [Основная идея](#основная-идея)
- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Модель данных](#модель-данных)
- [REST API](#rest-api)
- [Конкурентное списание топлива](#конкурентное-списание-топлива)
- [Проблема Lost Update](#проблема-lost-update)
- [Воспроизведение проблемы](#воспроизведение-проблемы)
- [Решение через Optimistic Locking](#решение-через-optimistic-locking)
- [Почему не synchronized](#почему-не-synchronized)
- [Тестирование](#тестирование)
- [Результаты тестирования](#результаты-тестирования)
- [Логирование](#логирование)
- [Кэширование](#кэширование)
- [Обработка ошибок](#обработка-ошибок)
- [Безопасность API](#безопасность-api)
- [Как запустить проект](#как-запустить-проект)
- [Пример сценария](#пример-сценария)
- [Что было изучено](#что-было-изучено)
- [Возможное развитие](#возможное-развитие)
- [Заключение](#заключение)

---

# О проекте

**Fuel Station Monitor** — backend-приложение для мониторинга состояния сети автозаправочных станций.

Пользователь может получить информацию о:

- доступных АЗС;
- адресе и характеристиках АЗС;
- видах топлива;
- цене топлива;
- текущем количестве топлива;
- времени последнего обновления информации.

Основная задача приложения — предоставить единый backend API, через который можно получать актуальное состояние топлива на разных АЗС.

В текущем MVP источник данных моделируется через REST API.

В production-системе такие данные могли бы поступать из:

- внутренних систем АЗС;
- систем управления топливными колонками;
- телеметрии;
- интеграционных сервисов;
- операторских систем;
- брокера сообщений, например Kafka.

При этом обычный пользователь не должен самостоятельно изменять остаток топлива.

Поэтому API проекта логически разделён на:

- **read API** — получение информации пользователями;
- **internal/write API** — обновление состояния топлива и регистрация операций, которые в реальной системе могли бы поступать от интеграционных систем АЗС.

---

# Основная идея

Типичный сценарий работы:

```text
Пользователь
    |
    | GET /stations
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

Например, пользователь запрашивает список АЗС:

```http
GET /stations
```

Backend получает данные из PostgreSQL и возвращает информацию об АЗС.

Для изменения состояния топлива используется внутренний API.

Например:

```http
POST /stations/1/fuel/92/consume
```

может означать:

> На АЗС №1 было продано 30 литров АИ-92.

Это не означает, что пользователь вручную изменяет остаток топлива.

В production такой запрос могла бы отправлять интеграционная система АЗС после получения информации о продаже.

---

# Архитектура

Проект построен по классической многослойной архитектуре:

```text
                HTTP
                 |
                 v
        +----------------+
        |   Controller   |
        +----------------+
                 |
                 v
        +----------------+
        |    Service     |
        +----------------+
                 |
                 v
        +----------------+
        |   Repository   |
        +----------------+
                 |
                 v
        +----------------+
        |  PostgreSQL    |
        +----------------+
```

## Controller

Отвечает за HTTP API:

- принимает HTTP-запрос;
- получает параметры;
- выполняет базовую валидацию;
- вызывает Service;
- формирует HTTP-ответ.

Controller не содержит основной бизнес-логики.

---

## Service

Содержит бизнес-логику приложения.

Например, при списании топлива Service:

1. получает текущий остаток;
2. проверяет наличие достаточного количества топлива;
3. уменьшает остаток;
4. сохраняет изменения;
5. обрабатывает конкурентное изменение записи.

Именно на уровне Service была обнаружена и исправлена проблема конкурентного обновления.

---

## Repository

Repository отвечает за взаимодействие с базой данных.

Используется:

```text
Spring Data JPA
        +
Hibernate
```

Repository скрывает детали SQL/JPA от Service.

---

## PostgreSQL

PostgreSQL используется как основное постоянное хранилище данных.

В базе хранятся:

- АЗС;
- виды топлива;
- остатки топлива;
- цены;
- версии записей для optimistic locking;
- связанные сущности проекта.

---

# Технологический стек

### Backend

- Java
- Spring Boot
- Spring Web / Spring MVC
- Spring Data JPA
- Hibernate
- Maven

### Database

- PostgreSQL

### Testing

- JUnit 5
- Mockito
- многопоточные тесты через `ExecutorService`
- `CountDownLatch`

### Other

- Lombok
- Git
- GitHub
- REST API
- SLF4J / Logback

---

# Модель данных

Основная сущность для работы с остатками топлива:

```text
FuelStock
```

Логически она содержит:

```text
id
station
fuelType
price
quantity
updatedAt
version
```

Где:

| Поле | Назначение |
|---|---|
| `id` | идентификатор записи |
| `station` | АЗС |
| `fuelType` | вид топлива |
| `price` | текущая цена |
| `quantity` | текущий остаток |
| `updatedAt` | время последнего изменения |
| `version` | версия записи для optimistic locking |

Связь выглядит примерно следующим образом:

```text
Station
   |
   +---- FuelStock ---- FuelType
              |
              +---- quantity
              +---- price
              +---- version
```

---

# REST API

## Получение списка АЗС

```http
GET /stations
```

Возвращает список доступных АЗС.

---

## Получение конкретной АЗС

```http
GET /stations/{stationId}
```

Пример:

```http
GET /stations/1
```

---

## Получение топлива на АЗС

```http
GET /stations/{stationId}/fuel
```

Позволяет получить информацию о доступном топливе.

Например:

```json
[
  {
    "fuelType": "AI-92",
    "price": 62.50,
    "quantity": 4850
  },
  {
    "fuelType": "AI-95",
    "price": 67.20,
    "quantity": 3200
  }
]
```

---

# Внутренний API изменения состояния

Для моделирования работы интеграционной системы предусмотрены endpoints для изменения состояния топлива.

## Списание топлива

```http
POST /stations/{stationId}/fuel/{fuelTypeId}/consume
```

Например:

```http
POST /stations/1/fuel/92/consume
```

Тело запроса:

```json
{
  "quantity": 30
}
```

Смысл операции:

```text
текущий остаток = текущий остаток - 30
```

То есть POST здесь представляет **новое событие/операцию продажи топлива**.

---

## Синхронизация текущего состояния

Для обновления текущего состояния топлива используется PUT.

Например:

```http
PUT /stations/1/fuel/92
```

Тело:

```json
{
  "price": 62.50,
  "quantity": 4850
}
```

В отличие от `POST consume`, здесь смысл операции другой:

> Источник данных сообщает backend, какое состояние топлива является актуальным.

Таким образом:

```text
POST → новое событие / операция

PUT → синхронизация текущего состояния
```

---

# Конкурентное списание топлива

Одним из основных технических challenges проекта стало конкурентное изменение остатка топлива.

## Бизнес-сценарий

На одной АЗС может одновременно работать несколько топливных колонок.

Например:

```text
Колонка 1 → продала 30 литров
Колонка 2 → продала 20 литров
```

Backend может получить два практически одновременных запроса:

```text
Request A
Request B
```

Оба работают с одной записью `FuelStock`.

---

# Проблема Lost Update

Первоначально логика была простой:

```text
1. Получить FuelStock
2. Проверить quantity
3. Вычесть количество
4. Сохранить FuelStock
```

Например, в базе:

```text
quantity = 5000
```

Одновременно приходят два запроса:

```text
Request A: -30
Request B: -20
```

Возможный сценарий:

```text
                PostgreSQL
                    |
              quantity = 5000
                    |
          +---------+---------+
          |                   |
          v                   v
      Thread A             Thread B
      read 5000            read 5000
          |                   |
      5000 - 30           5000 - 20
          |                   |
       4970                 4980
          |                   |
          +---------+---------+
                    |
                    v
              final = 4980
```

Но правильный результат:

```text
5000 - 30 - 20 = 4950
```

Получается:

```text
Expected: 4950
Actual:   4980
```

Одна операция фактически потерялась.

Это классическая проблема **Lost Update**.

---

# Воспроизведение проблемы

Для воспроизведения race condition был написан многопоточный тест.

Использовались:

```java
ExecutorService
```

и

```java
CountDownLatch
```

### ExecutorService

Позволяет запустить несколько задач параллельно.

Условно:

```text
Thread 1 → списывает топливо
Thread 2 → списывает топливо
Thread 3 → списывает топливо
...
```

### CountDownLatch

Используется для синхронизации старта.

Идея:

```text
Все потоки готовы
       |
       v
CountDownLatch
       |
       v
Общий старт
       |
       +---- Thread 1
       +---- Thread 2
       +---- Thread 3
       +---- ...
```

Это позволяет увеличить вероятность одновременного чтения одной и той же версии записи и воспроизводить race condition.

---

# Решение через Optimistic Locking

Для решения проблемы был выбран **optimistic locking**.

В сущность `FuelStock` добавлено поле:

```java
@Version
private Long version;
```

Hibernate автоматически использует это поле для контроля конкурентных изменений.

Например:

```text
id = 1
quantity = 5000
version = 7
```

Два запроса читают:

```text
version = 7
```

Первый запрос изменяет запись:

```text
quantity = 4970
version = 8
```

Второй запрос пытается сохранить изменение, используя старую версию:

```text
version = 7
```

Hibernate фактически делает операцию, логически эквивалентную:

```sql
UPDATE fuel_stock
SET quantity = ?,
    version = 8
WHERE id = ?
  AND version = 7;
```

Но запись уже имеет:

```text
version = 8
```

Поэтому условие:

```text
version = 7
```

не выполняется.

Количество изменённых строк:

```text
0
```

Hibernate понимает, что запись была изменена другим transaction, и выбрасывает optimistic locking exception.

Таким образом, второе изменение **не перезаписывает молча результат первого**.

---

# Почему не `synchronized`

Одним из вариантов решения была бы синхронизация критической секции:

```java
synchronized
```

Она действительно может защитить операцию от конкурентного доступа между потоками.

Однако есть важное ограничение.

`synchronized` работает на уровне конкретного JVM-процесса.

Например:

```text
Load Balancer
      |
 +----+----+
 |         |
 v         v
JVM 1     JVM 2
 |         |
lock 1    lock 2
 |         |
 +----+----+
      |
      v
 PostgreSQL
```

У JVM 1 и JVM 2 разные объекты блокировок.

Поэтому если приложение запущено в нескольких экземплярах, `synchronized` не обеспечивает координацию между ними.

`@Version` работает на уровне общей базы данных:

```text
JVM 1 ----\
           \
            PostgreSQL
           /
JVM 2 ----/
```

Именно поэтому optimistic locking лучше подходит для защиты общей записи в распределённом приложении.

---

# Результат после добавления `@Version`

После добавления optimistic locking многопоточный тест был запущен повторно.

До исправления:

```text
Expected quantity ≠ Actual quantity
```

и часть изменений могла теряться.

После исправления:

```text
Concurrent modification
        |
        v
version conflict
        |
        v
OptimisticLockingException
```

То есть система больше не позволяет двум конкурентным операциям молча перезаписать изменения друг друга.

В логике приложения конфликт может быть:

- обработан как ошибка конкурентного изменения;
- повторён автоматически;
- возвращён клиенту как conflict.

Конкретная стратегия зависит от бизнес-требований.

---

# Тестирование

Проект тестируется на нескольких уровнях.

## Unit-тесты

Unit-тесты Service проверяют бизнес-логику отдельно от базы данных.

Например:

### Успешное списание

```text
Initial quantity: 50
Consume:          30
Expected:         20
```

Результат:

```text
PASS
```

---

### Списание всего остатка

```text
Initial quantity: 50
Consume:          50
Expected:         0
```

Результат:

```text
PASS
```

---

### Недостаточный остаток

```text
Initial quantity: 50
Consume:          60
```

Операция должна быть отклонена.

Результат:

```text
PASS
```

---

## Почему Repository мокается в Unit-тесте

Unit-тест Service проверяет именно бизнес-логику Service.

Поэтому Repository заменяется mock-объектом:

```text
Service
   |
   +---- mock Repository
```

В таком тесте мы не проверяем PostgreSQL.

Мы проверяем:

```text
Если Repository вернул quantity = 50,
то Service должен корректно обработать consume(30).
```

Интеграционное тестирование базы данных является отдельной задачей.

---

# Многопоточное тестирование

Отдельно проверяется поведение при конкурентном доступе.

Используется:

```text
ExecutorService
+
CountDownLatch
+
PostgreSQL
+
@Version
```

Общая схема:

```text
                Test
                 |
          ExecutorService
                 |
      +----------+----------+
      |          |          |
      v          v          v
   Thread 1   Thread 2   Thread 3
      |          |          |
      +----------+----------+
                 |
                 v
             PostgreSQL
                 |
                 v
           Optimistic Lock
```

Цель теста — не просто проверить конечное число, а убедиться, что конкурентные изменения не теряются незаметно.

---

# Проверка `@Version`

Во время тестирования отслеживаются:

```text
quantity
version
```

Например:

```text
Initial:
quantity = 100
version  = 0
```

После последовательных успешных изменений:

```text
quantity = 0
version  = 10
```

При конкурентном конфликте одна операция может получить optimistic locking exception вместо того, чтобы перезаписать результат другой операции.

Это позволяет обнаружить конфликт на уровне persistence layer.

---

# Логирование

Для диагностики операций используется стандартный logging stack Spring Boot:

```text
SLF4J
+
Logback
```

Логируются ключевые события:

- начало операции;
- списание топлива;
- stationId;
- fuelTypeId;
- количество топлива;
- ошибки;
- конкурентные конфликты;
- завершение операции.

Пример логики логирования:

```text
INFO  Consume fuel: stationId=1 fuelTypeId=92 quantity=30
INFO  Fuel stock updated: stationId=1 fuelTypeId=92
WARN  Optimistic locking conflict: stationId=1 fuelTypeId=92
ERROR Failed to consume fuel
```

Логи позволяют анализировать поведение приложения без подключения debugger к production-инстансу.

---

# Кэширование

Для read-heavy операций проект предусматривает использование Redis.

Проблема:

```text
User
 |
 v
GET /stations/1/fuel
 |
 v
PostgreSQL
```

Если один и тот же endpoint запрашивается большое количество раз, база получает большое количество одинаковых запросов.

С Redis схема становится:

```text
                +-------------+
                |    Redis    |
                +-------------+
                       ^
                       |
User → Controller → Service
                       |
                       v
                  PostgreSQL
```

Первый запрос:

```text
GET
 |
 v
Redis MISS
 |
 v
PostgreSQL
 |
 v
Redis SET
 |
 v
Response
```

Следующий запрос:

```text
GET
 |
 v
Redis HIT
 |
 v
Response
```

Таким образом, PostgreSQL не используется для каждого одинакового read-запроса.

Для кэширования используются механизмы Spring Cache / Spring Data Redis.

Для изменения данных требуется инвалидировать или обновлять соответствующий cache entry.

Например:

```text
PUT /fuel
    |
    v
Update PostgreSQL
    |
    v
Evict Redis cache
```

Следующий GET снова получит актуальные данные из PostgreSQL и положит их в cache.

---

# Обработка ошибок

Ошибки бизнес-логики не должны превращаться в необработанные stack trace для клиента.

Типичные ситуации:

```text
Station not found
Fuel type not found
Fuel stock not found
Not enough fuel
Optimistic locking conflict
Invalid request
```

Для централизованной обработки ошибок может использоваться:

```java
@ControllerAdvice
```

Это позволяет сформировать единый формат HTTP-ошибок.

Например:

```json
{
  "status": 409,
  "message": "Fuel stock was modified by another request"
}
```

Для конкурентного изменения подходит HTTP:

```text
409 Conflict
```

если бизнес-логика приложения трактует ситуацию как конфликт состояния ресурса.

---

# Безопасность API

В текущем MVP API не является полноценной production-системой с authentication/authorization.

Однако логически endpoints разделяются по ролям.

Например:

```text
USER
 |
 +-- GET /stations
 +-- GET /stations/{id}
 +-- GET /stations/{id}/fuel
```

Внутренние операции:

```text
STATION_SYSTEM / ADMIN
 |
 +-- POST /consume
 +-- PUT /fuel
```

В production для этого могли бы использоваться:

- Spring Security;
- JWT;
- OAuth 2.0;
- service-to-service authentication;
- role-based authorization.

Таким образом, пользователь приложения не должен иметь возможности самостоятельно изменить остаток топлива.

---

# Как запустить проект

## Требования

Необходимо установить:

```text
Java
Maven
PostgreSQL
```

Для проекта рекомендуется использовать версию Java, указанную в `pom.xml`.

---

## Клонирование

```bash
git clone https://github.com/yadkakvodu/fuel-station-monitor.git
cd fuel-station-monitor
```

---

## Настройка PostgreSQL

Необходимо создать базу данных:

```sql
CREATE DATABASE fuel_station_monitor;
```

После этого указать параметры подключения в configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fuel_station_monitor
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

---

## Запуск

Через Maven:

```bash
./mvnw spring-boot:run
```

или:

```bash
mvn spring-boot:run
```

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

---

# Пример полного сценария

Рассмотрим АЗС №1 и АИ-92.

Начальное состояние:

```text
FuelStock

quantity = 5000
price    = 62.50
version  = 7
```

Поступает операция продажи:

```json
{
  "quantity": 30
}
```

Сервис проверяет:

```text
5000 >= 30
```

После успешного изменения:

```text
quantity = 4970
version  = 8
```

Если одновременно приходит ещё одна операция:

```json
{
  "quantity": 20
}
```

и она работала со старой версией:

```text
version = 7
```

Hibernate обнаруживает конфликт.

Вместо:

```text
4980
```

система получает контролируемый optimistic locking conflict.

---

# Основные challenges проекта

Проект развивался постепенно.

## Challenge 1 — базовый REST API

Реализованы:

- Controller;
- Service;
- Repository;
- PostgreSQL;
- CRUD-операции;
- REST endpoints.

Цель:

```text
Получение и изменение данных через HTTP API.
```

---

## Challenge 2 — бизнес-логика списания топлива

Добавлена операция:

```text
consume fuel
```

Реализованы проверки:

- наличие АЗС;
- наличие вида топлива;
- наличие записи остатка;
- достаточное количество топлива;
- корректное изменение остатка.

---

## Challenge 3 — Lost Update

Обнаружена проблема:

```text
Two requests
      |
      v
same quantity
      |
      v
both modify
      |
      v
one update overwrites another
```

Проблема была воспроизведена многопоточным тестом.

---

## Challenge 4 — Optimistic Locking

Добавлена:

```java
@Version
```

Результат:

```text
Concurrent modification
        |
        v
Version mismatch
        |
        v
Optimistic locking exception
```

Проблема Lost Update устранена на уровне persistence.

---

## Challenge 5 — многопоточное тестирование

Использованы:

```text
ExecutorService
CountDownLatch
```

Это позволило воспроизводить конкурентный доступ не вручную через Postman, а автоматически в тесте.

---

## Challenge 6 — Unit-тестирование Service

Проверены основные бизнес-сценарии:

```text
successful consume
consume entire stock
insufficient stock
invalid data
```

Repository мокается, потому что Unit-тест проверяет бизнес-логику Service, а не работу PostgreSQL.

---

## Challenge 7 — Production logging

Добавлено структурированное логирование ключевых операций.

Цель:

```text
debugging
+
monitoring
+
incident investigation
```

---

## Challenge 8 — Redis caching

Для read-heavy endpoints предусмотрено кэширование.

Цель:

```text
reduce PostgreSQL load
+
reduce response latency
+
avoid repeated identical queries
```

При изменении данных cache должен инвалидироваться, чтобы пользователь не получил устаревший остаток.

---

# Результаты

В результате разработки проект прошёл путь от обычного CRUD-приложения до backend-сервиса с отдельными инженерными задачами.

### Реализовано

- REST API;
- многослойная архитектура;
- PostgreSQL;
- Spring Data JPA;
- Hibernate;
- бизнес-логика списания топлива;
- Unit-тестирование Service;
- Mockito;
- многопоточное тестирование;
- `ExecutorService`;
- `CountDownLatch`;
- обнаружение Lost Update;
- Optimistic Locking;
- `@Version`;
- обработка конкурентных изменений;
- логирование;
- подготовка к Redis caching;
- разделение пользовательского и внутреннего API.

---

# Что было изучено на проекте

Проект использовался не только как CRUD-приложение, но и как практическая площадка для изучения backend-разработки.

Основные темы:

### Java

- OOP;
- collections;
- exceptions;
- multithreading;
- ExecutorService;
- synchronization;
- concurrency;
- atomic operations.

### Spring

- Dependency Injection;
- IoC;
- Spring MVC;
- REST;
- Spring Data JPA;
- transactions;
- configuration;
- exception handling.

### Database

- PostgreSQL;
- relational model;
- JPA;
- Hibernate;
- transactions;
- optimistic locking;
- versioning;
- concurrent updates.

### Testing

- JUnit 5;
- Mockito;
- unit testing;
- multithreaded testing;
- testing business logic.

### Backend architecture

- Controller → Service → Repository;
- separation of responsibilities;
- internal API vs public API;
- caching;
- logging;
- concurrency control.

---

# Возможное дальнейшее развитие

Проект можно развивать в сторону production-like архитектуры.

## 1. Kafka

Сделать поток событий:

```text
Fuel Station
     |
     v
   Kafka
     |
     v
Fuel Station Monitor
     |
     v
 PostgreSQL
```

Например:

```text
FuelConsumedEvent
```

Событие:

```json
{
  "stationId": 1,
  "fuelTypeId": 92,
  "quantity": 30,
  "timestamp": "2026-10-06T00:00:00"
}
```

Это позволит изучить:

- Kafka;
- partitions;
- consumer groups;
- offsets;
- retries;
- duplicate messages;
- idempotency.

---

## 2. Idempotency

При использовании Kafka или внешних интеграций одно событие может быть доставлено повторно.

Например:

```text
FuelConsumedEvent #123
       |
       +---- delivery #1
       |
       +---- delivery #2
```

Если просто дважды выполнить:

```text
quantity -= 30
```

можно ошибочно списать:

```text
60 литров
```

вместо:

```text
30 литров
```

Поэтому следующим challenge может стать идемпотентная обработка событий.

---

## 3. Database performance

Можно добавить нагрузочное исследование:

```text
EXPLAIN ANALYZE
```

и проверить индексы.

Например, для часто используемого запроса:

```text
findByStationIdAndFuelTypeId
```

может потребоваться соответствующий индекс.

Также можно исследовать:

- composite indexes;
- query plans;
- N+1;
- HikariCP;
- connection pool;
- database latency.

---

## 4. Redis

Полноценное внедрение Redis:

```text
GET request
     |
     v
   Redis
   /   \
 HIT    MISS
 |       |
 v       v
response PostgreSQL
          |
          v
        Redis
```

Можно добавить:

- TTL;
- cache eviction;
- cache invalidation;
- cache-aside pattern.

---

## 5. Observability

Следующий этап:

```text
Spring Boot Actuator
        |
        v
    Micrometer
        |
        v
   Prometheus
        |
        v
     Grafana
```

Можно отслеживать:

- RPS;
- latency;
- error rate;
- JVM memory;
- CPU;
- database connections;
- cache hit/miss;
- количество optimistic locking conflicts.

---

# Архитектура потенциальной production-версии

После дальнейшего развития архитектура может выглядеть следующим образом:

```text
                    +----------------+
                    |    Clients     |
                    +-------+--------+
                            |
                            v
                    +---------------+
                    | Load Balancer |
                    +-------+-------+
                            |
                 +----------+----------+
                 |                     |
                 v                     v
          +-------------+       +-------------+
          | Spring Boot |       | Spring Boot |
          | Instance 1  |       | Instance 2  |
          +------+------+       +------+------+
                 |                     |
                 +----------+----------+
                            |
              +-------------+-------------+
              |                           |
              v                           v
          +--------+                 +---------+
          | Redis  |                 |  Kafka  |
          +--------+                 +----+----+
                                          |
                                          v
                                   Integration events
                                          |
                                          v
                                   +-------------+
                                   | PostgreSQL  |
                                   +-------------+
```

Такой вариант позволяет масштабировать backend горизонтально и отделять:

- synchronous read API;
- state storage;
- caching;
- asynchronous events;
- external integrations.

---

# Почему проект интересен с инженерной точки зрения

Основная ценность проекта заключается не в количестве CRUD endpoints.

В процессе разработки была найдена реальная проблема, которая возникает в backend-системах:

> несколько параллельных запросов изменяют одну и ту же запись.

Проблема была:

```text
1. Обнаружена
2. Воспроизведена
3. Покрыта многопоточным тестом
4. Проанализирована
5. Исправлена через optimistic locking
6. Проверена повторным тестированием
```

Таким образом, проект позволил перейти от простого:

```text
Controller → Service → Repository → DB
```

к изучению реальных backend-задач:

```text
Concurrency
     ↓
Lost Update
     ↓
Optimistic Locking
     ↓
@Version
     ↓
Concurrent Testing
     ↓
Logging
     ↓
Caching
     ↓
Production considerations
```

---

# Заключение

**Fuel Station Monitor** — учебный backend-проект, который начинался как REST-приложение для мониторинга АЗС и постепенно развивался в сторону production-like системы.

Главным техническим challenge стала проблема конкурентного списания топлива.

Была воспроизведена ситуация, при которой несколько потоков одновременно читали один и тот же остаток топлива и могли перезаписывать изменения друг друга.

Для проверки использовались:

```text
ExecutorService
+
CountDownLatch
```

После воспроизведения проблемы была реализована защита через:

```java
@Version
```

и Hibernate optimistic locking.

В результате приложение стало корректно обнаруживать конкурентные изменения вместо того, чтобы молча терять обновления.

Дальнейшее развитие проекта направлено на:

```text
Redis
Kafka
Idempotency
Database optimization
Observability
Security
```

Главная цель проекта — не просто показать использование Spring Boot, а продемонстрировать процесс решения backend-задачи:

```text
Business problem
      ↓
Simple implementation
      ↓
Find bottleneck / bug
      ↓
Reproduce
      ↓
Test
      ↓
Analyze
      ↓
Implement solution
      ↓
Verify result
```

---

## Author

**Java Backend Developer**

GitHub: `yadkakvodu`

Проект создан как учебный pet-project для практического изучения Java backend development, Spring Boot, PostgreSQL, concurrency и production-oriented подходов.