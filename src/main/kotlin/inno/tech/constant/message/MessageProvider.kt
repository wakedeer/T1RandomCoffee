package inno.tech.constant.message

/**
 * Общий класс содержащий шаблоны сообщений.
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
    abstract val regStepProfile: String
    abstract val successfulSignUp: String
    abstract val successfulChangeProfile: String

    val matchInvitation: String = loadTemplate("/message/common/match/invitation.md")
    val matchInvitationWithNick: String = loadTemplate("/message/common/match/invitation_with_nickname.md")
    val matchSkip: String = loadTemplate("/message/common/match/skip.md")
    val matchSuggestion: String = loadTemplate("/message/common/match/suggestion.md")
    val readyToMatch: String = loadTemplate("/message/common/match/success.md")
    val matchFailureSendToPartner: String = loadTemplate("/message/common/match/failure_send_msg_to_partner.md")

    val statusPause: String = loadTemplate("/message/common/status/pause.md")
    val statusResume: String = loadTemplate("/message/common/status/resume.md")

    val rematchSuggestion: String = loadTemplate("/message/common/match/rematch_suggestion.md")
    val rematchSearch: String = loadTemplate("/message/common/match/rematch_search.md")
    val rematchSkip: String = loadTemplate("/message/common/match/rematch_skip.md")

    protected fun loadTemplate(path: String) = MessageProvider::class.java.getResource(path)?.readText()
        ?: throw IllegalArgumentException("Cannot load message template by path: $path")
}
