package inno.tech.service.subscription

import inno.tech.constant.MeetingFormat
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions

/**
 * Проверяет основную бизнес-логику формирования пары без обращения к Telegram API.
 */
internal class SubscriptionServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var meetingRepository: MeetingRepository
    private lateinit var messageService: MessageService
    private lateinit var subscriptionService: SubscriptionService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        meetingRepository = mock(MeetingRepository::class.java)
        messageService = mock(MessageService::class.java)
        subscriptionService = SubscriptionService(userRepository, meetingRepository, messageService)
    }

    @Test
    fun `successful offline match updates statuses and stores meeting`() {
        val firstUser = user(id = 1, meetingFormat = MeetingFormat.OFFLINE)
        val secondUser = user(id = 2, meetingFormat = MeetingFormat.OFFLINE)

        val result = subscriptionService.sendMatchResult(firstUser, secondUser)

        assertThat(result).isTrue()
        assertThat(firstUser.status).isEqualTo(Status.MATCHED)
        assertThat(secondUser.status).isEqualTo(Status.MATCHED)
        verify(messageService).sendInvitationMessage(firstUser, secondUser)
        verify(messageService).sendInvitationMessage(secondUser, firstUser)

        val meetingCaptor = ArgumentCaptor.forClass(Meeting::class.java)
        verify(meetingRepository).save(meetingCaptor.capture())
        assertThat(meetingCaptor.value.userId1).isEqualTo(firstUser.userId)
        assertThat(meetingCaptor.value.userId2).isEqualTo(secondUser.userId)
        assertThat(meetingCaptor.value.meetingFormat).isEqualTo(MeetingFormat.OFFLINE)
    }

    @Test
    fun `match is not stored when second notification fails`() {
        val firstUser = user(id = 1, meetingFormat = MeetingFormat.ONLINE)
        val secondUser = user(id = 2, meetingFormat = MeetingFormat.ONLINE)
        doThrow(IllegalStateException("Telegram is unavailable"))
            .`when`(messageService)
            .sendInvitationMessage(secondUser, firstUser)

        val result = subscriptionService.sendMatchResult(firstUser, secondUser)

        assertThat(result).isFalse()
        assertThat(firstUser.status).isEqualTo(Status.READY)
        assertThat(secondUser.status).isEqualTo(Status.READY)
        verifyNoInteractions(meetingRepository)
    }

    private fun user(id: Long, meetingFormat: MeetingFormat) = User(
        userId = id,
        chatId = id * 10,
        fullName = "User $id",
        status = Status.READY,
        active = true,
        meetingFormat = meetingFormat,
    )
}
