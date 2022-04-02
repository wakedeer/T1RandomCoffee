package inno.tech.repository

import inno.tech.model.Meeting
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

/**
 * Репозиторий для работы с информацией о встречах.
 */
interface MeetingRepository : CrudRepository<Meeting, Long> {

    @Query("select case when count(m)> 0 then true else false end from Meeting m" +
            " where (m.userId1 = :userId1 and m.userId2 = :userId2) or (m.userId1 = :userId2 and m.userId2= :userId1)")
    fun existsMeeting(@Param("userId1") userId1: Long, @Param("userId2") userId2: Long): Boolean
}