package inno.tech.constant.message

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("innotech")
class InnotechEngClubMessageProvider : MessageProvider() {

    override val welcome: String = loadTemplate("/message/welcome.md")
    override val info: String = loadTemplate("/message/info.md")

    override val successfulSignUp: String = loadTemplate("/message/registration/successful_sign_up.md")
    override val successfulChangeProfile: String = loadTemplate("/message/registration/successful_change_profile.md")
}
