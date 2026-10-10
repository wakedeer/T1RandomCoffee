package inno.tech.handler

import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.handler.registration.FinishInputProfileHandler
import inno.tech.handler.registration.InputCityHandler
import inno.tech.handler.registration.InputNameHandler
import inno.tech.handler.registration.StartInputProfileHandler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.subscription.SubscriptionService
import inno.tech.support.RecordingMessageService
import inno.tech.support.telegramMessageUpdate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.LinkedList

/**
 * Фиксирует текущее поведение обработчиков регистрации и изменения профиля.
 */
internal class RegistrationHandlerTest {

    @Test
    fun `new registration stores telegram identity and requests a name`() {
        val userRepository = mock(UserRepository::class.java)
        val messageService = RecordingMessageService()
        val handler = StartInputProfileHandler(userRepository, messageService)
        val update = telegramMessageUpdate(
            text = "/start",
            userId = 42,
            chatId = 420,
            firstName = "Иван",
            lastName = "Иванов",
            username = "ivan",
        )

        handler.handle(update, null)

        val userCaptor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(userCaptor.capture())
        assertThat(userCaptor.value.userId).isEqualTo(42)
        assertThat(userCaptor.value.chatId).isEqualTo(420)
        assertThat(userCaptor.value.username).isEqualTo("ivan")
        assertThat(userCaptor.value.status).isEqualTo(Status.REG_NAME)
        assertThat(userCaptor.value.active).isTrue()
        assertThat(messageService.messages.single().template).isEqualTo(Message.WELCOME)
        assertThat(messageService.keyboardMessages.single().template).isEqualTo(Message.REG_STEP_1)
    }

    @Test
    fun `name and city handlers advance registration state`() {
        val messageService = RecordingMessageService()
        val user = user(status = Status.REG_NAME)

        InputNameHandler(messageService).handle(
            telegramMessageUpdate(text = "Анна Смирнова", userId = user.userId, chatId = user.chatId),
            user,
        )

        assertThat(user.fullName).isEqualTo("Анна Смирнова")
        assertThat(user.status).isEqualTo(Status.REG_CITY)
        assertThat(messageService.keyboardMessages.single().template).isEqualTo(Message.REG_STEP_2)

        InputCityHandler(messageService).handle(
            telegramMessageUpdate(text = "Москва", userId = user.userId, chatId = user.chatId),
            user,
        )

        assertThat(user.city).isEqualTo("Москва")
        assertThat(user.status).isEqualTo(Status.REG_PROFILE_URL)
        assertThat(messageService.messages.last().template).isEqualTo(Message.REG_STEP_3)
    }

    @Test
    fun `profile completion without available partner moves user to ready`() {
        val userRepository = mock(UserRepository::class.java)
        val subscriptionService = mock(SubscriptionService::class.java)
        val messageService = RecordingMessageService()
        val handler = FinishInputProfileHandler(subscriptionService, userRepository, messageService)
        val user = user(status = Status.REG_PROFILE_URL)
        `when`(userRepository.findAllByStatusInAndActiveTrue(listOf(Status.READY, Status.UNPAIRED)))
            .thenReturn(LinkedList())

        handler.handle(
            telegramMessageUpdate(text = "https://example.test/profile", userId = user.userId, chatId = user.chatId),
            user,
        )

        assertThat(user.profileUrl).isEqualTo("https://example.test/profile")
        assertThat(user.status).isEqualTo(Status.READY)
        assertThat(messageService.messages.single().template).isEqualTo(Message.SUCCESSFUL_SIGN_UP)
    }

    @Test
    fun `profile editing restores previous status`() {
        val userRepository = mock(UserRepository::class.java)
        val subscriptionService = mock(SubscriptionService::class.java)
        val messageService = RecordingMessageService()
        val handler = FinishInputProfileHandler(subscriptionService, userRepository, messageService)
        val user = user(status = Status.REG_PROFILE_URL).apply {
            previousStatus = Status.MATCHED
        }

        handler.handle(
            telegramMessageUpdate(text = "https://example.test/new-profile", userId = user.userId, chatId = user.chatId),
            user,
        )

        assertThat(user.status).isEqualTo(Status.MATCHED)
        assertThat(user.previousStatus).isNull()
        assertThat(messageService.messages.single().template).isEqualTo(Message.SUCCESSFUL_CHANGE_PROFILE)
        assertThat(messageService.profiles).containsExactly(user)
    }

    private fun user(status: Status) = User(
        userId = 7,
        chatId = 70,
        fullName = "Test User",
        status = status,
        active = true,
    )
}
