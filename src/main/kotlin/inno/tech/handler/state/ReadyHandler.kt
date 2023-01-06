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
 * Обработчик сообщения о готовности участвовать в жеребьёвке.
 *
 * @param messageService сервис отправки сообщений
 */
@Component
class ReadyHandler(
    private val messageService: MessageService,
    private val messageProvider : Message,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && user.status == Status.ASKED && command == Command.READY.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.status = Status.READY

        messageService.sendMessage(user.chatId.toString(), messageProvider.READY_TO_MATCH)
    }
}
