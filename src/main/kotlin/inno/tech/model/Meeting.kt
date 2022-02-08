package inno.tech.model

import javax.persistence.*

/**
 * Встреча.
 */
@Entity
@Table(name = "MEETINGS")
class Meeting(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "USER_ID_1")
    var userId1: Long,

    @Column(name = "USER_ID_2")
    var userId2: Long,
)
