package inno.tech.handler

import inno.tech.OfflineProperties
import inno.tech.constant.MeetingFormat
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.handler.state.PauseHandler
import inno.tech.handler.state.ReadyHandler
import inno.tech.handler.state.ReadyOfflineHandler
import inno.tech.handler.state.ReadyOnlineHandler
import inno.tech.handler.state.ResumeHandler
import inno.tech.handler.state.SkipHandler
import inno.tech.model.User
import inno.tech.support.RecordingMessageService
import inno.tech.support.telegramMessageUpdate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Фиксирует переходы состояния при выборе участия, формата встречи и паузы.
 */
internal class ParticipationHandlerTest {

    @Test
    fun `offline city requires meeting format selection`() {
        val messageService = RecordingMessageService()
        val handler = ReadyHandler(messageService, OfflineProperties(listOf("Москва")))
        val user = user(status = Status.ASKED, city = "Москва")

        handler.handle(telegramMessageUpdate(text = "ready"), user)

        assertThat(user.status).isEqualTo(Status.CHOOSING_FORMAT)
        assertThat(user.meetingFormat).isNull()
        assertThat(messageService.keyboardMessages.single().template).isEqualTo(Message.MATCH_CHOOSE_FORMAT)
    }

    @Test
    fun `unsupported offline city selects online format immediately`() {
        val messageService = RecordingMessageService()
        val handler = ReadyHandler(messageService, OfflineProperties(listOf("Москва")))
        val user = user(status = Status.ASKED, city = "Казань")

        handler.handle(telegramMessageUpdate(text = "ready"), user)

        assertThat(user.status).isEqualTo(Status.READY)
        assertThat(user.meetingFormat).isEqualTo(MeetingFormat.ONLINE)
        assertThat(messageService.messages.single().template).isEqualTo(Message.READY_TO_MATCH)
    }

    @Test
    fun `format handlers store the selected format`() {
        val onlineMessages = RecordingMessageService()
        val onlineUser = user(status = Status.CHOOSING_FORMAT)
        ReadyOnlineHandler(onlineMessages).handle(telegramMessageUpdate(text = "online"), onlineUser)

        val offlineMessages = RecordingMessageService()
        val offlineUser = user(status = Status.CHOOSING_FORMAT)
        ReadyOfflineHandler(offlineMessages).handle(telegramMessageUpdate(text = "offline"), offlineUser)

        assertThat(onlineUser.status).isEqualTo(Status.READY)
        assertThat(onlineUser.meetingFormat).isEqualTo(MeetingFormat.ONLINE)
        assertThat(offlineUser.status).isEqualTo(Status.READY)
        assertThat(offlineUser.meetingFormat).isEqualTo(MeetingFormat.OFFLINE)
    }

    @Test
    fun `pause resume and skip handlers update user state`() {
        val messageService = RecordingMessageService()
        val user = user(status = Status.ASKED)
        val update = telegramMessageUpdate(text = "command", userId = user.userId, chatId = user.chatId)

        PauseHandler(messageService).handle(update, user)
        assertThat(user.active).isFalse()

        ResumeHandler(messageService).handle(update, user)
        assertThat(user.active).isTrue()

        SkipHandler(messageService).handle(update, user)
        assertThat(user.status).isEqualTo(Status.SKIP)
        assertThat(messageService.messages.map { it.template }).containsExactly(
            Message.STATUS_PAUSE,
            Message.STATUS_RESUME,
            Message.MATCH_SKIP,
        )
    }

    private fun user(status: Status, city: String? = null) = User(
        userId = 1,
        chatId = 10,
        fullName = "Test User",
        city = city,
        status = status,
        active = true,
    )
}
