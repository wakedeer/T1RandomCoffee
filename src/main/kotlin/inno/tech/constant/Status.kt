package inno.tech.constant

/**
 * Статус пользователя.
 */
enum class Status {

    /** Регистрация. Пользователь ввёл имя */
    REG_NAME,

    /** Регистрация. Пользователь ввёл город */
    REG_CITY,

    /** Регистрация. Пользователь ввёл ссылку соцсети */
    REG_PROFILE_URL,

    /** Отправлен запрос об участии на следующей неделе */
    ASKED,

    /** Пользователь готовый к участию. Пользователь прошедший регистрацию */
    READY,

    /** Пользователь пропускает встречу на этой неделе*/
    SKIP,

    /** Для пользователя запланирована встреча. Пользователю отправлен партнёр для встречи */
    MATCHED,

    /** Пользователь не нашедший пару при жеребьёвке */
    UNPAIRED,
}

/**
 * Статусы регистрации.
 */
val REGISTRATION_STATUSES = listOf(
    Status.REG_NAME,
    Status.REG_CITY,
    Status.REG_PROFILE_URL,
)

/**
 * Основные статусы.
 */
val COMMON_STATUSES = listOf(
    Status.ASKED,
    Status.READY,
    Status.MATCHED,
    Status.UNPAIRED,
)
