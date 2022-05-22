package inno.tech

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update

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

        //remove loading circle turning on InlineKeyboardButtons on Android and Desktop clients
        if (update.hasCallbackQuery()) {
            execute(AnswerCallbackQuery(update.callbackQuery.id))
        }

        messageHandler.handle(update)
    }
}
