package inno.tech.handler.state

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик сообщения о готовности участвовать в жеребьёвке.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 */
@Component
class ReadyHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && user.status == Status.ASKED && command == Command.READY.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.status = Status.SCHEDULED

        val pauseReply = SendMessage()
        pauseReply.text = Message.MATCH_SUCCESS
        pauseReply.parseMode = ParseMode.MARKDOWN
        pauseReply.chatId = user.chatId.toString()
        telegramBotApi.execute(pauseReply)
    }
}