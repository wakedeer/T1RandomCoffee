package inno.tech.handler.admin

import inno.tech.TelegramProperties
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.subscription.SubscriptionService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик ручного запуска жеребьёвки администратором.
 *
 * @param telegramProperties конфигурации подключения к Telegram Bot API
 * @param subscriptionService сервис отправки уведомлений
 */
@Component
@Profile("dev")
class ManualMatchHandler(
    private val telegramProperties: TelegramProperties,
    private val subscriptionService: SubscriptionService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return telegramProperties.adminId == user?.userId && "/pair" == command
    }

    override fun handle(update: Update, user: User?) {
        subscriptionService.matchPairs()
    }
}
