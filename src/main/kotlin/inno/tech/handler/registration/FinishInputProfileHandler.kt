package inno.tech.handler.registration

import inno.tech.constant.Status
import inno.tech.constant.message.MessageProvider
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import inno.tech.service.subscription.SubscriptionService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик сообщений заполнения профиля пользователя.
 *
 * @param subscriptionService сервис отправки уведомлений пользователям
 * @param userRepository репозиторий для работы с информацией о пользователях
 * @param messageService сервис отправки сообщений
 * @param messageProvider компонент, содержащий шаблоны сообщений
 */
@Component
class FinishInputProfileHandler(
    private val subscriptionService: SubscriptionService,
    private val userRepository: UserRepository,
    private val messageService: MessageService,
    private val messageProvider: MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Status.REG_PROFILE == user.status
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        user.description = update.getMessageText()

        val previousStatus = user.previousStatus
        if (previousStatus != null) {
            //just update user profile
            user.status = previousStatus
            user.previousStatus = null

            messageService.sendMessage(update.getChatIdAsString(), messageProvider.successfulChangeProfile)
            messageService.sendProfileInfoMessage(user)
        } else {
            //try to find a pair for the new user
            messageService.sendMessage(update.getChatIdAsString(), messageProvider.successfulSignUp)

            val level = user.level ?: throw RandomCoffeeBotException("level should be filled")
            val readyUser = userRepository.findAllByStatusInAndLevelAndActiveTrue(listOf(Status.READY, Status.UNPAIRED), level).firstOrNull()

            if (readyUser != null) {
                // the pair found
                subscriptionService.sendInvitation(readyUser, user)
            } else {
                // a pair hasn't found. add to wait list
                user.status = Status.READY
            }
        }
    }
}
