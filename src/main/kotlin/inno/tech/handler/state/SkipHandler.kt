package inno.tech.handler.state

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SkipHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && user.status == Status.ASKED && command == Command.SKIP.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.status = Status.UNSCHEDULED

        val skipReply = SendMessage()
        skipReply.text = Message.MATCH_SKIP
        skipReply.parseMode = ParseMode.MARKDOWN
        skipReply.chatId = user.userId.toString()
        telegramBotApi.execute(skipReply)
    }
}