package inno.tech.constant

/**
 * Доступные команды бота.
 */
enum class Command(val command: String) {
    INFO("/info"),

    SHOW_PROFILE("/show_profile"),
    EDIT_PROFILE("/edit_profile"),

    START("/start"),
    RESTART("/restart"),

    PAUSE("/pause"),
    RESUME("/resume"),

    READY("/ready"),
    SKIP("/skip"),
}