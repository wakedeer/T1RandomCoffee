package inno.tech.handler.admin

import inno.tech.TelegramProperties
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.SubscriptionService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class SuggestionHandler(
    private val telegramProperties: TelegramProperties,
    private val subscriptionService: SubscriptionService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return telegramProperties.adminId == user?.userId && command == "/ask"
    }

    override fun handle(update: Update, user: User?) {
        subscriptionService.sendSuggestions()
    }
}