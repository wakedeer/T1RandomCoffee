package inno.tech.model

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

    /** Идентификатор первого участика */
    @Column(name = "USER_ID_1")
    var userId1: Long,

    /** Идентификатор второго участика */
    @Column(name = "USER_ID_2")
    var userId2: Long,
)
