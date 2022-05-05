package inno.tech

import inno.tech.constant.Command
import inno.tech.constant.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Компонент, предоставляющий доступ к Telegram Bot API
 *
 * @param telegramProperties конфигурации подключения к Telegram Bot API
 */
@Component
class TelegramBotApi(
    private val telegramProperties: TelegramProperties,
) : TelegramLongPollingBot() {

    /** Обработчик входящий сообщений. (Lazy для избежания рекурсивной зависимости) */
    @Lazy
    @Autowired
    private lateinit var messageHandler: MessageHandler

    override fun getBotToken() = telegramProperties.token

    override fun getBotUsername() = telegramProperties.name

    override fun onUpdateReceived(update: Update) {
        messageHandler.handle(update)
    }
}
