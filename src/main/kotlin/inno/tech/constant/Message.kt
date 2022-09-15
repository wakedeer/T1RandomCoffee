package inno.tech.constant

/**
 * Шаблоны сообщений.
 */
object Message {

    val WELCOME: String = loadTemplate("/message/welcome.md")
    val PROFILE: String = loadTemplate("/message/profile.md")
    val ERROR: String = loadTemplate("/message/error.md")
    val INFO: String = loadTemplate("/message/info.md")

    val REG_STEP_1: String = loadTemplate("/message/registration/step1_name.md")
    val REG_STEP_2: String = loadTemplate("/message/registration/step2_level.md")
    val REG_STEP_3: String = loadTemplate("/message/registration/step3_city.md")
    val REG_STEP_4: String = loadTemplate("/message/registration/step4_profile.md")
    val SUCCESSFUL_SIGN_UP: String = loadTemplate("/message/registration/successful_sign_up.md")
    val SUCCESSFUL_CHANGE_PROFILE: String = loadTemplate("/message/registration/successful_change_profile.md")

    val MATCH_INVITATION: String = loadTemplate("/message/match/invitation.md")
    val MATCH_SKIP: String = loadTemplate("/message/match/skip.md")
    val MATCH_SUGGESTION: String = loadTemplate("/message/match/suggestion.md")
    val READY_TO_MATCH: String = loadTemplate("/message/match/success.md")
    val MATCH_FAILURE: String = loadTemplate("/message/match/failure.md")
    val MATCH_FAILURE_SEND_TO_PARTNER: String = loadTemplate("/message/match/failure_send_msg_to_partner.md")

    val STATUS_PAUSE: String = loadTemplate("/message/status/pause.md")
    val STATUS_RESUME: String = loadTemplate("/message/status/resume.md")

    private fun loadTemplate(path: String) = Message::class.java.getResource(path)?.readText()
        ?: throw IllegalArgumentException("Cannot load message template by path: $path")
}
