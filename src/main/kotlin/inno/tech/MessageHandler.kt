package inno.tech

import inno.tech.extension.getChatId
import inno.tech.extension.getMessageTextOrNull
import inno.tech.extension.getUserIdOrNull
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик входящих сообщений.
 *
 * @param userRepository репозиторий для работы с информацией о пользователе
 * @param handlers список обработчиков сообщений
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 */
@Component
class MessageHandler(
    private val userRepository: UserRepository,
    private val handlers: List<Handler>,
    private val telegramBotApi: TelegramBotApi,
) {

    /**
     * Обрабатывает входящее сообщение
     *
     * @param update сообщение
     */
    @Transactional
    fun handle(update: Update) {
        val command: String? = update.getMessageTextOrNull()
        val userId: Long? = update.getUserIdOrNull()

        if (command == null || userId == null) {
            return
        }

        val user: User? = userRepository.findById(userId).orElse(null)

        handlers.firstOrNull { it.accept(command, user) }?.handle(update, user)
            ?: telegramBotApi.errorSend(update.getChatId())
    }
}