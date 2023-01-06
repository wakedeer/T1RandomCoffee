package inno.tech.handler.registration

import inno.tech.constant.COMMON_STATUSES
import inno.tech.constant.Command
import inno.tech.constant.message.MessageProvider
import inno.tech.constant.Status
import inno.tech.extension.getChatId
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getUserId
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Обработчик регистрации/перерегистрации пользователя.
 *
 * @param userRepository репозиторий для работы с информацией о пользователе
 * @param messageService сервис отправки сообщений
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class StartInputProfileHandler(
    private val userRepository: UserRepository,
    private val messageService: MessageService,
    private val messageProvider : MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return command == Command.RESTART.command || command == Command.START.command || command == Command.EDIT_PROFILE.command
    }

    override fun handle(update: Update, user: User?) {
        val chatId = update.getChatIdAsString()
        messageService.sendMessage(chatId, messageProvider.welcome)

        val telegramUsername = update.message?.from?.userName
        val u = if (user != null && user.status in COMMON_STATUSES) {
            user.previousStatus = user.status
            user.status = Status.REG_NAME
            user.username = telegramUsername
            user
        } else {
            User(
                userId = update.getUserId(),
                chatId = update.getChatId(),
                username = telegramUsername,
                status = Status.REG_NAME,
                active = true,
            )
        }

        userRepository.save(u)

        val fullName = extractFullName(update)
        if (fullName != null) {
            val keyboard = createSuggestedNameKeyboard(fullName)
            messageService.sendMessageWithKeyboard(chatId, keyboard, messageProvider.regStepName)
        } else {
            messageService.sendMessage(chatId, messageProvider.regStepName)
        }
    }

    /**
     * Возвращает клавиатуру с предлагаемым именем и фамилией пользователя.
     * @param fullName имя и фамилия
     * @return клавиатура с именем и фамилией
     */
    private fun createSuggestedNameKeyboard(fullName: String): InlineKeyboardMarkup {
        val suggestedNameBtn = InlineKeyboardButton().apply {
            text = fullName
            callbackData = fullName
        }
        return InlineKeyboardMarkup().apply {
            this.keyboard = listOf(
                listOf(suggestedNameBtn),
            )
        }
    }

    /**
     * Возвращает имя и фамилию пользователя из входящего сообщения.
     * @param update входящее сообщение
     * @return имя и фамилия
     */
    private fun extractFullName(update: Update): String? {
        val userInfo = when {
            update.hasMessage() -> update.message.from
            update.hasCallbackQuery() -> update.callbackQuery.from
            else -> return null
        }

        val firstName = userInfo.firstName.let { "$it " }
        val lastName = userInfo.lastName ?: ""
        return firstName + lastName
    }

}
