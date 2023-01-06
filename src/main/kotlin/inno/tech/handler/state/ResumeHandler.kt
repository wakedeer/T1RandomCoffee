package inno.tech.handler.state

import inno.tech.constant.Command
import inno.tech.constant.message.MessageProvider
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик возобновления отправки сообщений пользователю.
 *
 * @param messageService сервис отправки сообщений
 * @param messageProvider компонент, содержащий шаблоны сообщений
 */
@Component
class ResumeHandler(
    private val messageService: MessageService,
    private val messageProvider: MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Command.RESUME.command == command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        user.active = true

        messageService.sendMessage(user.chatId.toString(), messageProvider.statusResume)
    }
}
