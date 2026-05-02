package inno.tech

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient

/**
 * Компонент, предоставляющий доступ к Telegram Bot API
 *
 * @param telegramProperties конфигурации подключения к Telegram Bot API
 */
@Component
class TelegramBotApi(
    private val telegramProperties: TelegramProperties,
    private val telegramClient: TelegramClient,
) : SpringLongPollingBot {

    /** Обработчик входящий сообщений. (Lazy для избежания рекурсивной зависимости) */
    @Lazy
    @Autowired
    private lateinit var messageHandler: MessageHandler

    override fun getBotToken() = telegramProperties.token

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = LongPollingUpdateConsumer { updates ->
        updates.forEach { update ->
            //remove loading circle turning on InlineKeyboardButtons on Android and Desktop clients
            if (update.hasCallbackQuery()) {
                telegramClient.execute(
                    AnswerCallbackQuery.builder()
                        .callbackQueryId(update.callbackQuery.id)
                        .build()
                )
            }
            messageHandler.handle(update)
        }
    }
}
