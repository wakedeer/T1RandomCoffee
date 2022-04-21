package inno.tech.handler.registration

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.extension.getChatId
import inno.tech.extension.getUserId
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Обработчик регистрации/перерегистрации пользователя.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 * @param userRepository репозиторий для работы с информацией о пользователе
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class StartHandler(
    private val telegramBotApi: TelegramBotApi,
    private val userRepository: UserRepository,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return command == Command.RESTART.command || command == Command.START.command || command == Command.EDIT_PROFILE.command
    }

    override fun handle(update: Update, user: User?) {
        val chatId = update.getChatId()

        val welcomeMsg = SendMessage()
        welcomeMsg.text = Message.WELCOME
        welcomeMsg.parseMode = ParseMode.MARKDOWN
        welcomeMsg.chatId = chatId.toString()
        telegramBotApi.execute(welcomeMsg)

        val newUser = User(
            userId = update.getUserId(),
            chatId = chatId,
            username = update.message?.from?.userName,
            status = Status.REG_NAME,
            active = true
        )

        userRepository.save(newUser)

        val nameQuestion = SendMessage()
        nameQuestion.text = Message.REG_STEP_1
        nameQuestion.parseMode = ParseMode.MARKDOWN
        nameQuestion.chatId = chatId.toString()
        nameQuestion.allowSendingWithoutReply = false

        val firstName = update.message?.from?.firstName?.let { "$it " } ?: ""
        val lastName = update.message?.from?.lastName ?: ""
        val fullName = firstName + lastName
        val resumeBtn = InlineKeyboardButton().apply {
            text = fullName
            callbackData = fullName
        }
        nameQuestion.replyMarkup = InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(resumeBtn),
            )
        }

        telegramBotApi.execute(nameQuestion)
    }
}