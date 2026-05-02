package inno.tech

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

/**
 * Конфигурация регистрации Telegram-бота.
 * Заменяет авторегистрацию из telegrambots-spring-boot-starter,
 * которая использует механизм spring.factories, не поддерживаемый Spring Boot 3.
 */
@Configuration
class TelegramBotConfiguration(
    private val telegramBotApi: TelegramBotApi,
) {

    @Bean
    @Throws(TelegramApiException::class)
    fun telegramBotsApi(): TelegramBotsApi =
        TelegramBotsApi(DefaultBotSession::class.java).also {
            it.registerBot(telegramBotApi)
        }
}

