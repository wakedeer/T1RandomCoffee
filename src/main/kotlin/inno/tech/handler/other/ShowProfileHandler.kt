package inno.tech.handler.other

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
import java.text.MessageFormat

@Component
class ShowProfileHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Command.SHOW_PROFILE.command == command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        val showProfileReply = SendMessage()
        showProfileReply.text = MessageFormat.format(Message.PROFILE, user.fullName, user.city, user.profileUrl)
        showProfileReply.parseMode = ParseMode.MARKDOWN
        showProfileReply.chatId = user.chatId.toString()
        telegramBotApi.execute(showProfileReply)
    }
}