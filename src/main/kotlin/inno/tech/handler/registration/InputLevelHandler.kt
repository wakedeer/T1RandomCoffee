package inno.tech.handler.registration

import inno.tech.constant.Level
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.handler.Handler
import inno.tech.handler.registration.InputCityHandler.Companion.CITIES
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class InputLevelHandler(
    private val messageService: MessageService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Status.REG_LEVEL == user.status
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        val level = try {
            Level.valueOf(update.getMessageText())
        } catch (e: IllegalArgumentException) {
            messageService.sendMessage(update.getChatIdAsString(), Message.REG_STEP_2)
            return
        }

        user.level = level
        user.status = Status.REG_CITY

        messageService.sendMessageWithKeyboard(update.getChatIdAsString(), CITIES, Message.REG_STEP_3)
    }
}
