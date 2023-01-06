package inno.tech.constant.message

/**
 * Класс содержащий шаблоны сообщений по-умолчанию.
 */
class DefaultMessageProvider : MessageProvider() {
    override val welcome: String = loadTemplate("/message/common/welcome.md")
    override val info: String = loadTemplate("/message/common/info.md")

    override val successfulSignUp: String = loadTemplate("/message/common/registration/successful_sign_up.md")
    override val successfulChangeProfile: String = loadTemplate("/message/common/registration/successful_change_profile.md")
}
