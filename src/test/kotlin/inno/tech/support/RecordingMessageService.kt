package inno.tech.support

import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

/**
 * Тестовая реализация сервиса сообщений, которая сохраняет отправленные сообщения в памяти.
 */
internal class RecordingMessageService : MessageService {

    val messages = mutableListOf<RecordedMessage>()
    val keyboardMessages = mutableListOf<RecordedKeyboardMessage>()
    val invitations = mutableListOf<Pair<User, User>>()
    val profiles = mutableListOf<User>()
    val errorChatIds = mutableListOf<Long>()

    override fun sendProfileInfoMessage(user: User) {
        profiles += user
    }

    override fun sendInvitationMessage(user: User, partner: User) {
        invitations += user to partner
    }

    override fun sendMessage(
        chatId: String,
        template: String,
        args: Array<String>,
        transformation: (SendMessage) -> SendMessage,
    ) {
        messages += RecordedMessage(chatId, template, args.toList())
    }

    override fun sendMessageWithKeyboard(
        chatId: String,
        replyMarkup: InlineKeyboardMarkup,
        template: String,
        args: Array<String>,
    ) {
        keyboardMessages += RecordedKeyboardMessage(chatId, template, replyMarkup, args.toList())
    }

    override fun sendErrorMessage(chatId: Long) {
        errorChatIds += chatId
    }
}

/** Запись обычного сообщения, отправленного во время теста. */
internal data class RecordedMessage(
    val chatId: String,
    val template: String,
    val args: List<String>,
)

/** Запись сообщения с клавиатурой, отправленного во время теста. */
internal data class RecordedKeyboardMessage(
    val chatId: String,
    val template: String,
    val keyboard: InlineKeyboardMarkup,
    val args: List<String>,
)
