package inno.tech.handler.state

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик приостановки пользователем получений сообщений от бота.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 */
@Component
class PauseHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?) = user != null && Command.PAUSE.command == command

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.active = false

        val pauseReply = SendMessage()
        pauseReply.text = Message.STATUS_PAUSE
        pauseReply.parseMode = ParseMode.MARKDOWN
        pauseReply.chatId = user.chatId.toString()
        telegramBotApi.execute(pauseReply)
    }
}