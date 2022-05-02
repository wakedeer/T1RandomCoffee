package inno.tech.model

import java.time.LocalDateTime
import javax.persistence.*

/**
 * Встреча.
 */
@Entity
@Table(name = "MEETINGS")
class Meeting(

    /** Идентификатор встречи */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    /** Идентификатор первого участника */
    @Column(name = "USER_ID_1")
    var userId1: Long,

    /** Идентификатор второго участника */
    @Column(name = "USER_ID_2")
    var userId2: Long,

    /** Время создания записи (Время жеребьёвки) */
    @Column(name = "MATCH_DATE")
    var matchDate: LocalDateTime = LocalDateTime.now(),
)
