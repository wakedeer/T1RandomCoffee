package inno.tech.handler.state

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class ResumeHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Command.RESUME.command == command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.active = true

        val resumeResponse = SendMessage()
        resumeResponse.text = Message.STATUS_RESUME
        resumeResponse.parseMode = ParseMode.MARKDOWN
        resumeResponse.chatId = user.chatId.toString()
        telegramBotApi.execute(resumeResponse)
    }
}