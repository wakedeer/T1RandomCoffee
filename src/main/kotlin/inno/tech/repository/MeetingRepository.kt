package inno.tech.repository

import inno.tech.model.Meeting
import org.springframework.data.repository.CrudRepository

/**
 * Репозиторий для работы с информацией о встречах.
 */
interface MeetingRepository : CrudRepository<Meeting, Long>