package inno.tech.handler.other

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.extension.getChatIdAsString
import inno.tech.handler.Handler
import inno.tech.model.User
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class InfoHandler(
    private val telegramBotApi: TelegramBotApi,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return Command.INFO.command == command
    }

    override fun handle(update: Update, user: User?) {
        val info = SendMessage()
        info.text = Message.INFO
        info.parseMode = ParseMode.MARKDOWN
        info.chatId = update.getChatIdAsString()
        info.allowSendingWithoutReply = false
        telegramBotApi.execute(info)
    }
}