package inno.tech.support

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message

/**
 * Создаёт Telegram update с текстовым сообщением без обращения к Telegram API.
 */
internal fun telegramMessageUpdate(
    text: String,
    userId: Long = 1,
    chatId: Long = userId * 10,
    firstName: String = "Test",
    lastName: String? = "User",
    username: String? = "test_user",
): Update {
    val update = mock(Update::class.java)
    val message = mock(Message::class.java)
    val telegramUser = mock(User::class.java)

    `when`(update.hasMessage()).thenReturn(true)
    `when`(update.message).thenReturn(message)
    `when`(message.hasText()).thenReturn(true)
    `when`(message.text).thenReturn(text)
    `when`(message.chatId).thenReturn(chatId)
    `when`(message.from).thenReturn(telegramUser)
    `when`(telegramUser.id).thenReturn(userId)
    `when`(telegramUser.firstName).thenReturn(firstName)
    `when`(telegramUser.lastName).thenReturn(lastName)
    `when`(telegramUser.userName).thenReturn(username)

    return update
}
