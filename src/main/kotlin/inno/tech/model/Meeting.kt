package inno.tech.model

import inno.tech.constant.MeetingFormat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

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

    /** Формат встречи */
    @Enumerated(EnumType.STRING)
    @Column(name = "MEETING_FORMAT")
    var meetingFormat: MeetingFormat = MeetingFormat.ONLINE,
)
