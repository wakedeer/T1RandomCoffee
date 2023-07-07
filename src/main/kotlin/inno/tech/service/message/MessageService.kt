package inno.tech.service.message

import inno.tech.model.User
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

/**
 * Сервис отправки сообщений
 */
interface MessageService {

    /**
     * Отправляет сообщение пользователю с информацией профиля пользователя.
     * @param user о пользователе
     */
    fun sendProfileInfoMessage(user: User)

    /**
     * Отправляет приглашение участнику.
     * @param user участник, которому отправляется приглашение
     * @param partner информация о партнёре участника
     */
    fun sendInvitationMessage(user: User, partner: User)

    /**
     * Отправляет шаблонизированное сообщение.
     * @param chatId идентификатор чата адресата
     * @param template шаблон сообщения
     * @param args список аргументов для подстановки в шаблон
     * @param transformation дополнительные трансформации сообщения
     */
    fun sendMessage(chatId: String, template: String, args: Array<String> = emptyArray(), transformation: (SendMessage) -> SendMessage = { it })

    /**
     * Отправляет шаблонизированное сообщение с Inline клавиатурой
     * @param chatId идентификатор чата адресата
     * @param replyMarkup inline клавиатура
     * @param template шаблон сообщения
     * @param args список аргументов для подстановки в шаблон
     */
    fun sendMessageWithKeyboard(chatId: String, replyMarkup: InlineKeyboardMarkup, template: String, args: Array<String> = emptyArray())

    /**
     * Отправляет главное меню бота, если обработчик сообщения не найден.
     * @param chatId идентификатор чата адресата
     */
    fun sendErrorMessage(chatId: Long)
}
