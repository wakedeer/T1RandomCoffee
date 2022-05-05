package inno.tech.handler.other

import inno.tech.constant.Command
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик запроса информации о профиле пользователя.
 *
 * @param messageService сервис отправки сообщений
 */
@Component
class ShowProfileHandler(
    private val messageService: MessageService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Command.SHOW_PROFILE.command == command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }
        messageService.sendProfileInfoMessage(user)
    }
}
