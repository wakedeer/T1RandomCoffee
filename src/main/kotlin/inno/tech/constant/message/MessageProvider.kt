package inno.tech.constant.message

/**
 * Шаблоны сообщений.
 */
abstract class MessageProvider {

    abstract val welcome: String
    val profile: String = loadTemplate("/message/common/profile.md")
    val questions: String = loadTemplate("/message/common/questions.md")
    val error: String = loadTemplate("/message/common/error.md")
    abstract val info: String

    val regStepName: String = loadTemplate("/message/common/registration/step1_name.md")
    val regStepLevel: String = loadTemplate("/message/common/registration/step2_level.md")
    val regStepCity: String = loadTemplate("/message/common/registration/step3_city.md")
    val regStepProfile: String = loadTemplate("/message/common/registration/step4_profile.md")
    abstract val successfulSignUp: String
    abstract val successfulChangeProfile: String

    val matchInvitation: String = loadTemplate("/message/common/match/invitation.md")
    val matchSkip: String = loadTemplate("/message/common/match/skip.md")
    val matchSuggestion: String = loadTemplate("/message/common/match/suggestion.md")
    val readyToMatch: String = loadTemplate("/message/common/match/success.md")
    val matchFailure: String = loadTemplate("/message/common/match/failure.md")
    val matchFailureSendToPartner: String = loadTemplate("/message/common/match/failure_send_msg_to_partner.md")

    val statusPause: String = loadTemplate("/message/common/status/pause.md")
    val statusResume: String = loadTemplate("/message/common/status/resume.md")

    protected fun loadTemplate(path: String) = MessageProvider::class.java.getResource(path)?.readText()
        ?: throw IllegalArgumentException("Cannot load message template by path: $path")
}
