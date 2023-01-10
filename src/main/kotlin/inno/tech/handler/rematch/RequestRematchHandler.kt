package inno.tech.handler.rematch

import inno.tech.constant.Command
import inno.tech.constant.Status
import inno.tech.constant.message.MessageProvider
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import inno.tech.service.subscription.SubscriptionService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик сообщения о готовности поменять партнёра.
 *
 * @param messageService сервис отправки сообщений
 * @param messageProvider компонент, содержащий шаблоны сообщений
 * @param userRepository репозиторий для работы с информацией о пользователях
 * @param subscriptionService сервис отправки уведомлений
 */
@Component
class RequestRematchHandler(
    private val messageService: MessageService,
    private val messageProvider: MessageProvider,
    private val userRepository: UserRepository,
    private val subscriptionService: SubscriptionService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return Status.ASKED == user?.status && command == Command.REQUEST_REMATCH.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }
        //try to find a partner for the new user
        val level = user.level ?: throw RandomCoffeeBotException("level should be filled")
        val readyUser = userRepository.findAllByStatusInAndLevelAndActiveTrue(listOf(Status.READY, Status.UNPAIRED), level).firstOrNull()

        if (readyUser != null) {
            // the partner has found
            subscriptionService.sendInvitation(readyUser, user)
        } else {
            // a partner hasn't found. add to wait list
            user.status = Status.READY
            messageService.sendMessage(user.chatId.toString(), messageProvider.rematchSearch)
        }
    }
}
