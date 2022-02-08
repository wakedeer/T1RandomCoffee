package inno.tech

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Конфигурация подключения к Telegram.
 *
 * @author Aleksei Mironov
 */
@ConfigurationProperties("telegram")
@ConstructorBinding
data class TelegramConfig(
    val name: String,
    val token: String,
    val adminId: Long,
)