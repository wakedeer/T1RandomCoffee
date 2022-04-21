package inno.tech.model

import inno.tech.constant.Status
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

    /** Статус пользователя*/
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    var status: Status,

    /** Постановка бота на паузу */
    @Column(name = "ACTIVE")
    var active: Boolean = false,
)