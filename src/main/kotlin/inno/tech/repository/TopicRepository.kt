package inno.tech.repository

import inno.tech.model.Topic
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

/**
 * Репозиторий для работы с топиками вопросов.
 */
interface TopicRepository : CrudRepository<Topic, Long> {

    /**
     * Возвращает случайную тему вопросов для обсуждения на встрече.
     */
    @Query(nativeQuery = true, value = "SELECT *  FROM TOPICS ORDER BY random() LIMIT 1")
    fun getRandomTopic(): Topic
}
