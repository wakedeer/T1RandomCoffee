package inno.tech.model

import inno.tech.constant.Status
import java.time.LocalDateTime
import javax.persistence.*

/**
 * Пользователь.
 */
@Entity
@Table(name = "USERS")
class User(

    /** Идентификатор */
    @Id
    @Column(name = "USER_ID")
    var userId: Long,

    /** Идентификатор чата  */
    @Column(name = "CHAT_ID")
    var chatId: Long,

    /** Имя */
    @Column(name = "FULL_NAME")
    var fullName: String? = null,

    /** Никнейм */
    @Column(name = "USERNAME")
    var username: String? = null,

    /** Город */
    @Column(name = "CITY")
    var city: String? = null,

    /** Ссылка на страницу в соцсети */
    @Column(name = "PROFILE_URL")
    var profileUrl: String? = null,

    /** Текущий статус пользователя */
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    var status: Status,

    /** Предыдущий статус пользователя */
    @Enumerated(EnumType.STRING)
    @Column(name = "PREVIOUS_STATUS")
    var previousStatus: Status? = null,

    /** Постановка бота на паузу */
    @Column(name = "ACTIVE")
    var active: Boolean = false,

    /** Дата регистрации пользователя */
    @Column(name = "REG_DATE")
    var regDate: LocalDateTime = LocalDateTime.now(),
)
