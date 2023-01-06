package inno.tech.service.message

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.message.MessageProvider
import inno.tech.model.Topic
import inno.tech.model.User
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.text.MessageFormat

/**
 * Сервис отправки сообщений в Telegram.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 */
@Service
class TelegramMessageService(
    private val telegramBotApi: TelegramBotApi,
    private val messageProvider : MessageProvider,
) : MessageService {

    override fun sendMessage(chatId: String, template: String, args: Array<String>) {
        val message = SendMessage()
        message.text = MessageFormat.format(template, *sanitize(args))
        message.parseMode = ParseMode.MARKDOWNV2
        message.chatId = chatId
        message.allowSendingWithoutReply = false

        telegramBotApi.execute(message)
    }

    override fun sendMessageWithKeyboard(chatId: String, replyMarkup: InlineKeyboardMarkup, template: String, args: Array<String>) {
        val message = SendMessage()
        message.text = MessageFormat.format(template, *sanitize(args))
        message.parseMode = ParseMode.MARKDOWNV2
        message.chatId = chatId
        message.replyMarkup = replyMarkup

        telegramBotApi.execute(message)
    }

    override fun sendErrorMessage(chatId: Long) {
        sendMessageWithKeyboard(chatId.toString(), MAIN_MENU, messageProvider.error)
    }

    override fun sendProfileInfoMessage(user: User) {
        val fullName = user.fullName ?: DATA_IS_NOT_DEFINED
        val city = user.city ?: DATA_IS_NOT_DEFINED
        val level = user.level?.toString() ?: DATA_IS_NOT_DEFINED
        val description = user.description ?: DATA_IS_NOT_DEFINED

        sendMessageWithKeyboard(user.chatId.toString(), contactPartnerBtn(user), messageProvider.profile, arrayOf(fullName, city, level, description))
    }

    private fun contactPartnerBtn(partner: User): InlineKeyboardMarkup {
        val contactPartner = InlineKeyboardButton().apply {
            val name = partner.fullName ?: ""
            text = "Telegram $name"
            url = "tg://user?id=${partner.userId}"
        }
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(contactPartner),
            )
        }
    }

    override fun sendInvitationMessage(user: User, partner: User, topic: Topic) {
        val description = partner.description ?: DATA_IS_NOT_DEFINED
        val fullName = partner.fullName ?: DATA_IS_NOT_DEFINED
        val level = partner.level?.name ?: DATA_IS_NOT_DEFINED
        val city = partner.city ?: DATA_IS_NOT_DEFINED
        val topicName = topic.name

        val args = arrayOf(fullName, city, level, description, topicName)
        sendMessageWithKeyboard(user.chatId.toString(), invitationKeyboard(partner, topic), messageProvider.matchInvitation, args)
    }

    private fun invitationKeyboard(partner: User, topic: Topic): InlineKeyboardMarkup {
        val contactPartner = InlineKeyboardButton().apply {
            val name = partner.fullName ?: ""
            text = "Telegram $name link"
            url = "tg://user?id=${partner.userId}"
        }
        val showTopicBtn = InlineKeyboardButton().apply {
            text = "Topic ${topic.name} questions"
            callbackData = Command.SHOW_QUESTIONS.command + topic.id
        }
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(contactPartner),
                listOf(showTopicBtn),
            )
        }
    }

    /**
     * Экранирует с '\' специальные символы разметки MarkdownV2 Telegram Bot API.
     * https://core.telegram.org/bots/api#markdownv2-style
     *
     * @param args аргументы без экранирования специальных символов
     * @return аргументы с экранированием специальных символов
     */
    private fun sanitize(args: Array<String>): Array<String> = args.map { arg -> arg.replace(MATCH_TG_ESCAPE_SYMBOLS) { "\\${it.value}" } }.toTypedArray()

    companion object {
        /** Значение по умолчанию */
        const val DATA_IS_NOT_DEFINED = "is not defined"

        /** Регулярное выражение, находящее символы: '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!' в строке */
        val MATCH_TG_ESCAPE_SYMBOLS = Regex("[\\_\\*\\[\\]\\(\\)\\~\\`\\>\\#\\+\\-\\=\\|\\{\\}\\.\\!]")

        /** Главное меню приложения */
        val MAIN_MENU: InlineKeyboardMarkup = createMainMenu()

        private fun createMainMenu() = InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(actionBtn("About Random coffee", Command.INFO)),
                listOf(actionBtn("My profile", Command.SHOW_PROFILE)),
                listOf(actionBtn("Change profile", Command.EDIT_PROFILE)),
                listOf(actionBtn("Pause bot", Command.PAUSE)),
                listOf(actionBtn("Resume bot", Command.RESUME)),
            )
        }

        private fun actionBtn(name: String, command: Command) = InlineKeyboardButton().apply {
            text = name
            callbackData = command.command
        }
    }
}
