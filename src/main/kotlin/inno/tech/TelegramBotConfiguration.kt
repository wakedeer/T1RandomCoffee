package inno.tech

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

/**
 * Конфигурация Telegram-клиента для отправки сообщений.
 * Регистрация бота выполняется автоматически стартером telegrambots-springboot-longpolling-starter.
 */
@Configuration
class TelegramBotConfiguration(
    private val telegramProperties: TelegramProperties,
) {

    @Bean
    fun telegramClient(): TelegramClient =
        OkHttpTelegramClient(telegramProperties.token)
}
