package inno.tech.constant.message

class InnotechEngClubMessageProvider : MessageProvider() {

    override val welcome: String = loadTemplate("/message/innotech/welcome.md")
    override val info: String = loadTemplate("/message/innotech/info.md")

    override val successfulSignUp: String = loadTemplate("/message/innotech/registration/successful_sign_up.md")
    override val successfulChangeProfile: String = loadTemplate("/message/innotech/registration/successful_change_profile.md")
}
