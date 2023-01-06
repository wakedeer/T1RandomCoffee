package inno.tech.handler.other

import inno.tech.constant.Command
import inno.tech.constant.message.MessageProvider
import inno.tech.extension.getChatIdAsString
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик запроса информации о приложении.
 *
 * @param messageService сервис отправки сообщений
 * @param buildProperties параметры сборки приложения
 * @param messageProvider компонент, содержащий шаблоны сообщений
 */
@Component
class InfoHandler(
    private val messageService: MessageService,
    private val buildProperties: BuildProperties,
    private val userRepository: UserRepository,
    private val messageProvider: MessageProvider,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return Command.INFO.command == command
    }

    override fun handle(update: Update, user: User?) {
        messageService.sendMessage(update.getChatIdAsString(), messageProvider.info, arrayOf(buildProperties.version, userRepository.count().toString()))
    }
}
