package inno.tech.constant

/**
 * Команда бота.
 *
 * @param command код команды
 */
enum class Command(val command: String) {

    /** Получение информации о боте */
    INFO("/info"),

    /** Показать информацию о профиле */
    SHOW_PROFILE("/show_profile"),

    /** Редактирование профиля */
    EDIT_PROFILE("/edit_profile"),

    /** Запустить бота */
    START("/start"),

    /** Перезапустить бота. Аналогично команде START */
    RESTART("/restart"),

    /** Поставить участие на паузу */
    PAUSE("/pause"),

    /** Восстановить участие  */
    RESUME("/resume"),

    /** Готовность пользователя участвовать в жеребьёвке */
    READY("/ready"),

    /** Пропуск участия в жеребьёвке */
    SKIP("/skip"),
}