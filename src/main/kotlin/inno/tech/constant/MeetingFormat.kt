package inno.tech.constant

/**
 * Формат встречи, выбранный пользователем при согласии участвовать.
 */
enum class MeetingFormat(val displayName: String) {

    /** Онлайн-встреча */
    ONLINE("🖥 Онлайн"),

    /** Офлайн-встреча в своём городе */
    OFFLINE("☕ Офлайн"),
}

