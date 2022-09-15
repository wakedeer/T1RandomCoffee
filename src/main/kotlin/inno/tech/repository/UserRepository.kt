package inno.tech.repository

import inno.tech.constant.Level
import inno.tech.constant.Status
import inno.tech.model.User
import org.springframework.data.repository.CrudRepository
import java.util.LinkedList

/**
 * Репозиторий для работы с информацией о пользователях.
 */
interface UserRepository : CrudRepository<User, Long> {

    /**
     * Возвращает список активных пользователей, имеющих статус входящий в список искомых
     * @param statuses список статусов для поиска
     * @return список активных пользователей
     */
    fun findAllByStatusInAndActiveTrue(statuses: Collection<Status>): LinkedList<User>

    /**
     * Возвращает список активных пользователей, имеющих статус входящий в список искомых и уровнем владения языка
     * @param statuses список статусов для поиска
     * @param level уровень владения языком
     * @return список активных пользователей
     */
    fun findAllByStatusInAndLevelAndActiveTrue(statuses: Collection<Status>, level: Level): LinkedList<User>

    /**
     * Возвращает список активных пользователей с определённым статусом и уровнем владения языка
     * @param status статус
     * @param level уровень владения языком
     * @return список активных пользователей
     */
    fun findAllByStatusAndLevelAndActiveTrue(status: Status, level: Level): LinkedList<User>
}
