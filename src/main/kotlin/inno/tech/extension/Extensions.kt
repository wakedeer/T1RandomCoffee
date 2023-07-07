package inno.tech.extension

import org.telegram.telegrambots.meta.api.objects.Update
import java.util.Optional

/**
 * Возвращает идентификатор чата из входного сообщения.
 *
 * @return идентификатор чата
 */
fun Update.getChatId(): Long {
    return getChatIdOrNull() ?: throw IllegalArgumentException("Cannot get chatId from message: $this")
}

/**
 * Возвращает ник пользователя.
 *
 * @return ник пользователя
 */
fun Update.getUserName(): String? {
    return when {
        this.hasMessage() -> this.message?.from?.userName
        this.hasCallbackQuery() -> callbackQuery?.from?.userName
        else -> null
    }
}

/**
 * Возвращает идентификатор чата из входного сообщения.
 *
 * @return идентификатор чата
 */
fun Update.getChatIdOrNull(): Long? {
    return when {
        this.hasMessage() -> this.message.chatId
        this.hasCallbackQuery() -> callbackQuery.from.id
        else -> null
    }
}

/**
 * Возвращает идентификатор чата в текстовом виде из входного сообщения.
 *
 * @return идентификатор чата
 */
fun Update.getChatIdAsString() = this.getChatId().toString()

/**
 * Возвращает идентификатор пользователя из входного сообщения.
 *
 * @return идентификатор пользователя
 */
fun Update.getUserId(): Long {
    return getUserIdOrNull() ?: throw IllegalArgumentException("Cannot get userId from message: $this")
}

/**
 * Возвращает идентификатор пользователя из входного сообщения.
 *
 * @return идентификатор пользователя
 */
fun Update.getUserIdOrNull(): Long? {
    return when {
        this.hasMessage() -> this.message.from.id
        this.hasCallbackQuery() -> this.callbackQuery.from.id
        else -> null
    }
}

/**
 * Возвращает текст входного сообщения.
 *
 * @return идентификатор пользователя
 */
fun Update.getMessageTextOrNull(): String? {
    return when {
        this.hasMessage() && this.message.hasText() -> this.message.text
        this.hasCallbackQuery() -> this.callbackQuery.data
        else -> null
    }
}

/**
 * Возвращает текст входного сообщения.
 *
 * @return идентификатор пользователя
 */
fun Update.getMessageText(): String {
    return getMessageTextOrNull() ?: throw IllegalArgumentException("Cannot get text from message: $this")
}

/**
 * Конвертирует Java Optional в Kotlin Nullable.
 *
 * @return Kotlin Nullable значение
 */
fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null);
