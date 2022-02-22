package inno.tech.exception

/**
 * Базовое исключение бота.
 */
class RandomCoffeeBotException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, ex: Exception?) : super(message, ex)
}