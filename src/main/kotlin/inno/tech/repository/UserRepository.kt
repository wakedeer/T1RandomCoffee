package inno.tech.repository

import inno.tech.constant.Status
import inno.tech.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

/**
 * Репозиторий для работы с информацией о пользователях.
 */
interface UserRepository : CrudRepository<User, Long> {

    fun findAllByStatusInAndActiveTrue(statuses: Collection<Status>): LinkedList<User>

    fun findAllByStatusAndActiveTrue(status: Status): LinkedList<User>
}