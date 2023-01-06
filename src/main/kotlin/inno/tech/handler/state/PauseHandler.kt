package inno.tech.handler.state

import inno.tech.constant.Command
import inno.tech.constant.message.MessageProvider
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик приостановки пользователем получений сообщений от бота.
 *
 * @param messageService сервис отправки сообщений
 * @param messageProvider компонент, содержащий шаблоны сообщений
 */
@Component
class PauseHandler(
    private val messageService: MessageService,
    private val messageProvider: MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?) = user != null && Command.PAUSE.command == command

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.active = false

        messageService.sendMessage(update.getChatIdAsString(), messageProvider.statusPause)
    }
}
