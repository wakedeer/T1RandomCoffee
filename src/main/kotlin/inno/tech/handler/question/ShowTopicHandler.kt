package inno.tech.handler.question

import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.toNullable
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.QuestionsRepository
import inno.tech.repository.TopicRepository
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик запроса топика вопросов для встречи.
 *
 * @param messageService сервис отправки сообщений
 * @param topicRepository репозиторий для работы с топиками вопросов
 * @param questionsRepository репозиторий для работы с вопросами
 */
@Component
class ShowTopicHandler(
    private val messageService: MessageService,
    private val topicRepository: TopicRepository,
    private val questionsRepository: QuestionsRepository,
) : Handler {
    override fun accept(command: String, user: User?): Boolean {
        return command.startsWith(Command.SHOW_QUESTIONS.command)
    }

    override fun handle(update: Update, user: User?) {
        val topicId = extractTopicId(update)
        val topic = topicRepository.findById(topicId).toNullable() ?: throw RandomCoffeeBotException("Topic $topicId cannot be found")
        val questions = questionsRepository.findAllByTopic(topic)
        val questionText = questions.asSequence()
            .mapIndexed { index, question -> "${index + 1}. ${question.content}" }
            .joinToString(separator = "\n")

        messageService.sendMessage(update.getChatIdAsString(), Message.QUESTIONS, arrayOf(topic.name, questionText))
    }

    /**
     * Извлекает идентификатор топика вопросов из входящей команды.
     * @param user информация о пользователе
     * @param update входящее сообщение
     * @return идентификатор топика
     */
    private fun extractTopicId(update: Update): Long =
        update.callbackQuery?.data?.split("/")?.last()?.toLong()
            ?: throw RandomCoffeeBotException("TopicId cannot be extracted from message: $update")
}
