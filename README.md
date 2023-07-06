# 🔵🔵🔵 Иннотех. Leaders Coffee
[Forked from wakedeer/InnotechRandomCoffee](https://github.com/wakedeer/InnotechRandomCoffee)

## Общая информация

Проект построен на Kotlin и Spring Framework.

## Сборка и запуск на локальной машине
1. Создать бота в BotFather https://t.me/BotFather
2. Запустить БД Postgres. Например, через ``docker-compose up -d``
3. Установить переменные:
    - ``TELEGRAM_NAME=<NAME>`` - имя бота.
    - ``TELEGRAM_TOKEN=<TOKEN>`` - токен.
4. Собрать и запустить проект
    - ``gradlew clean assemble`` - сборка.
    - ``gradlew bootRun`` - запуск.

## Spring-профили проекта

- ``dev`` - профиль для разработки.