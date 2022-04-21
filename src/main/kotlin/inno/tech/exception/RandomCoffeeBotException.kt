package inno.tech.exception

/**
 * Базовое исключение бота.
 */
class RandomCoffeeBotException : RuntimeException {

    /**
     * Конструктор.
     * @param message текст ошибки
     */
    constructor(message: String) : super(message)

    /**
     * Конструктор.
     * @param message текст ошибки
     * @param ex исключение
     */
    constructor(message: String, ex: Exception?) : super(message, ex)
}