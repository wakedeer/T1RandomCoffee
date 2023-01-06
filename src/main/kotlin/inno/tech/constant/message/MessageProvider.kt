package inno.tech.constant.message

/**
 * Шаблоны сообщений.
 */
abstract class MessageProvider {

    abstract val welcome: String
    val profile: String = loadTemplate("/message/profile.md")
    val questions: String = loadTemplate("/message/questions.md")
    val error: String = loadTemplate("/message/error.md")
    abstract val info: String

    val regStepName: String = loadTemplate("/message/registration/step1_name.md")
    val regStepLevel: String = loadTemplate("/message/registration/step2_level.md")
    val regStepCity: String = loadTemplate("/message/registration/step3_city.md")
    val regStepProfile: String = loadTemplate("/message/registration/step4_profile.md")
    abstract val successfulSignUp: String
    abstract val successfulChangeProfile: String

    val matchInvitation: String = loadTemplate("/message/match/invitation.md")
    val matchSkip: String = loadTemplate("/message/match/skip.md")
    val matchSuggestion: String = loadTemplate("/message/match/suggestion.md")
    val readyToMatch: String = loadTemplate("/message/match/success.md")
    val matchFailure: String = loadTemplate("/message/match/failure.md")
    val matchFailureSendToPartner: String = loadTemplate("/message/match/failure_send_msg_to_partner.md")

    val statusPause: String = loadTemplate("/message/status/pause.md")
    val statusResume: String = loadTemplate("/message/status/resume.md")

    protected fun loadTemplate(path: String) = MessageProvider::class.java.getResource(path)?.readText()
        ?: throw IllegalArgumentException("Cannot load message template by path: $path")
}
