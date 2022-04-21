package inno.tech.repository

import inno.tech.constant.Status
import inno.tech.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

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
     * Возвращает список активных пользователей с определённым статусом
     * @param status статус
     * @return список активных пользователей
     */
    fun findAllByStatusAndActiveTrue(status: Status): LinkedList<User>
}