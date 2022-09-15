package inno.tech.constant

/**
 * Статусы отправки приглашения для встречи.
 */
enum class SendInvitationStatus {
    /** Сообщение отправлено успешно */
    OK,

    /** Ошибка отправки сообщения первому участнику */
    FIRST_ERROR,

    /** Ошибка отправки сообщения второму участнику */
    SECOND_ERROR,
}
