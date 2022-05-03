package inno.tech.handler.other

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.extension.getChatIdAsString
import inno.tech.handler.Handler
import inno.tech.model.User
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.text.MessageFormat

/**
 * Обработчик запроса информации о приложении.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 * @param buildProperties параметры сборки приложения
 */
@Component
class InfoHandler(
    private val telegramBotApi: TelegramBotApi,
    private val buildProperties: BuildProperties,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return Command.INFO.command == command
    }

    override fun handle(update: Update, user: User?) {
        val info = SendMessage()
        info.text = MessageFormat.format(Message.INFO, buildProperties.version)
        info.parseMode = ParseMode.MARKDOWN
        info.chatId = update.getChatIdAsString()
        info.allowSendingWithoutReply = false
        telegramBotApi.execute(info)
    }
}
