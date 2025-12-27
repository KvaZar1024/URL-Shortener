# URL Shortener Service

Консольный сервис сокращения ссылок с управлением лимитами переходов и временем жизни ссылок.

## Описание проекта

URL Shortener Service — это консольное приложение на Java, которое позволяет:
- Создавать короткие ссылки из длинных URL
- Управлять лимитом переходов по каждой ссылке
- Автоматически удалять ссылки после истечения срока жизни
- Отслеживать статистику использования ссылок
- Открывать ссылки в браузере прямо из консоли

## Требования

- **Java**: 17 или выше
- **Maven**: 3.6+ (для сборки)
- **ОС**: Windows, Linux, macOS (с поддержкой Desktop API для открытия браузера)

## Использование

### Доступные команды

После запуска приложения доступны следующие команды:

| Команда | Описание | Пример использования |
|---------|----------|---------------------|
| `create` | Создать короткую ссылку | `create https://example.com` |
| `create` | Создать ссылку с лимитом | `create https://example.com 20` |
| `use` | Открыть ссылку в браузере | `use 3DZHeG` |
| `list` | Показать все ваши ссылки | `list` |
| `info` | Информация о ссылке | `info 3DZHeG` |
| `delete` | Удалить ссылку | `delete 3DZHeG` |
| `help` | Показать справку | `help` |
| `exit` | Выйти из приложения | `exit` |

### Примеры использования

#### Создание короткой ссылки

```
> create https://www.baeldung.com/java-9-http-client
✓ Short link created successfully!
  Short URL: clck.ru/aBcDeF
  Original URL: https://www.baeldung.com/java-9-http-client
  Click limit: 10
  Expires: 2025-12-28T15:30:00
```

#### Создание ссылки с кастомным лимитом

```
> create https://github.com/anthropics/claude-code 50
✓ Short link created successfully!
  Short URL: clck.ru/xYzAbC
  Original URL: https://github.com/anthropics/claude-code
  Click limit: 50
  Expires: 2025-12-28T15:30:00
```

#### Использование ссылки (переход в браузер)

```
> use aBcDeF
✓ Redirecting to: https://www.baeldung.com/java-9-http-client
Opening URL in browser: https://www.baeldung.com/java-9-http-client
```

#### Просмотр информации о ссылке

```
> info aBcDeF

  Short URL: clck.ru/aBcDeF
  Original URL: https://www.baeldung.com/java-9-http-client
  Owner ID: 550e8400-e29b-41d4-a716-446655440000
  Created: 2025-12-27T15:30:00
  Expires: 2025-12-28T15:30:00
  Clicks: 5/10 (Remaining: 5)
  Status: Active
```

#### Список всех ссылок

```
> list

  Short Code: aBcDeF
  Original URL: https://www.baeldung.com/java-9-http-client
  Clicks: 5/10 (Active)
  Expires: 2025-12-28T15:30:00

  Short Code: xYzAbC
  Original URL: https://github.com/anthropics/claude-code
  Clicks: 2/50 (Active)
  Expires: 2025-12-28T15:30:00

Total: 2 link(s)
```



## Конфигурация

Все параметры настраиваются в файле [src/main/resources/application.properties](src/main/resources/application.properties):

```properties
# Время жизни ссылки (в часах)
link.ttl.hours=24

# Лимит переходов по умолчанию
link.default.click.limit=10

# Длина короткого кода
link.short.code.length=6

# Домен для коротких ссылок
link.short.domain=clck.ru

# Интервал автоматической очистки (в минутах)
cleanup.interval.minutes=5

# Включение уведомлений
notifications.enabled=true
```


### Запуск тестов с отчетом о покрытии

```bash
mvn clean test jacoco:report
```

Отчет будет доступен в `target/site/jacoco/index.html`

### Структура тестов

- **Unit тесты**:
    - `LinkTest` - тестирование доменной модели Link
    - `UserTest` - тестирование доменной модели User
    - `ShortCodeGeneratorTest` - тестирование генерации кодов
    - `LinkServiceTest` - тестирование сервиса ссылок
    - `UserServiceTest` - тестирование сервиса пользователей
    - `InMemoryLinkRepositoryTest` - тестирование репозитория

- **Integration тесты**:
    - `IntegrationTest` - end-to-end тестирование всех компонентов

