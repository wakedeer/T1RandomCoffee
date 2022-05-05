package inno.tech.service.message

import inno.tech.model.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

/**
 * Cервис отправки сообщений
 */
interface MessageService {

    fun sendProfileInfoMessage(user: User)

    fun sendInventionMessage(user: User, partner: User)
    fun sendMessage(chatId: String, template: String, args: Array<Any> = emptyArray())

    /**
     * Отправляет главное меню бота, если обработчик сообщения не найден.
     * @param chatId идентификатор чата адресата
     */
    fun sendErrorMessage(chatId: Long)
    fun sendMessageWithKeyboard(chatId: String, replyMarkup: InlineKeyboardMarkup, template: String, args: Array<Any> = emptyArray())
}
