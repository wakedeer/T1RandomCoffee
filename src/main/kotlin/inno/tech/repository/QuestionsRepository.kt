package inno.tech.repository

import inno.tech.model.Question
import inno.tech.model.Topic
import org.springframework.data.repository.CrudRepository

/**
 * Репозиторий для работы с вопросами.
 */
interface QuestionsRepository : CrudRepository<Question, Long> {

    /**
     * Возвращает список вопросов топика.
     * @param topic топик вопросов
     */
    fun findAllByTopic(topic: Topic): List<Question>
}
