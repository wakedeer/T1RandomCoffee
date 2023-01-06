package inno.tech.handler.registration

import inno.tech.constant.Status
import inno.tech.constant.message.MessageProvider
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.handler.Handler
import inno.tech.handler.registration.InputLevelHandler.Companion.LEVELS
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик сообщений заполнения имя пользователя.
 *
 * @param messageService сервис отправки сообщений
 * @param messageProvider компонент, содержащий шаблоны сообщений
 */
@Component
class InputNameHandler(
    private val messageService: MessageService,
    private val messageProvider: MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Status.REG_NAME == user.status
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        user.fullName = update.getMessageText()
        user.status = Status.REG_LEVEL

        messageService.sendMessageWithKeyboard(update.getChatIdAsString(), LEVELS, messageProvider.regStepLevel)
    }
}
