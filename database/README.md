# База данных

PostgreSQL 17. Схему создаёт Flyway из файлов в `database/migration`. Ручных `CREATE TABLE` нет.

Миграции лежат тут, рядом с БД. При старте бэкенда папка `database/migration` монтируется в контейнер, и Flyway накатывает её сам. Сейчас есть один файл: `V1__init.sql`.

Две таблицы.

## obligations

Обязательство: подписка, счёт, гарантия или страховка.

- `id` — UUID, первичный ключ.
- `title` — название, строка.
- `amount` — сумма, число с 2 знаками.
- `currency` — валюта, 3 буквы (ISO 4217).
- `category` — категория: `SUBSCRIPTION`, `WARRANTY`, `BILL`, `INSURANCE`.
- `recurrence` — период: `MONTHLY`, `QUARTERLY`, `YEARLY` или `NULL` для разового.
- `next_payment_date` — дата следующего платежа или истечения.
- `status` — статус: `ACTIVE`, `CANCELLED`, `EXPIRED`.
- `created_at`, `updated_at` — метки времени, ставятся кодом.

Enum-поля хранятся как строки. Есть индекс по `next_payment_date` — по нему идёт выборка ближайших списаний и сортировка списка.

## payments

История оплат. Одна строка = один факт оплаты.

- `id` — UUID, первичный ключ.
- `obligation_id` — ссылка на `obligations.id`.
- `amount` — уплаченная сумма.
- `currency` — валюта платежа.
- `paid_at` — момент оплаты.

Внешний ключ на `obligations` с `ON DELETE CASCADE`: удалили обязательство — его платежи удаляются сами. Есть индекс по `obligation_id`.
