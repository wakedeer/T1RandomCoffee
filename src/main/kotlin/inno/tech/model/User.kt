package inno.tech.model

import inno.tech.constant.Status
import javax.persistence.*

/**
 * Пользователь.
 */
@Entity
@Table(name = "USERS")
class User(
    @Id
    @Column(name = "USER_ID")
    var userId: Long,

    @Column(name = "CHAT_ID")
    var chatId: Long,

    @Column(name = "FULL_NAME")
    var fullName: String? = null,

    @Column(name = "USERNAME")
    var username: String? = null,

    @Column(name = "CITY")
    var city: String? = null,

    @Column(name = "PROFILE_URL")
    var profileUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    var status: Status,

    @Column(name = "ACTIVE")
    var active: Boolean = false,
)