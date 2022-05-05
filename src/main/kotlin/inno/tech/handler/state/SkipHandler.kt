package inno.tech.handler.state

import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update


/**
 * Обработчик сообщения о пропуске пользователем участия в жеребьёвке.
 *
 * @param messageService сервис отправки сообщений
 */
@Component
class SkipHandler(
    private val messageService: MessageService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && user.status == Status.ASKED && command == Command.SKIP.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.status = Status.SKIP

        messageService.sendMessage(user.userId.toString(), Message.MATCH_SKIP)
    }
}
