package inno.tech.repository

import inno.tech.model.Meeting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

/**
 * Репозиторий для работы с информацией о встречах.
 */
interface MeetingRepository : CrudRepository<Meeting, Long> {

    /**
     * Проверяет ранее была ли встреча между участиниками.
     * @param userId1 идентификатор пользователя 1
     * @param userId2 идентификатор пользователя 2
     * @return true - если ранее участники уже встречались, false - участники не встречались
     */
    @Query("select case when count(m)> 0 then true else false end from Meeting m" +
            " where (m.userId1 = :userId1 and m.userId2 = :userId2) or (m.userId1 = :userId2 and m.userId2= :userId1)")
    fun existsMeeting(@Param("userId1") userId1: Long, @Param("userId2") userId2: Long): Boolean
}