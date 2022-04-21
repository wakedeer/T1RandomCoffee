package inno.tech.service

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.handler.other.ShowProfileHandler.Companion.NOT_DEFINED
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.text.MessageFormat

/**
 * Сервис для рассылки уведомлений пользователем по расписанию.
 * @param userRepository репозиторий для работы с информацией о пользователе
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 * @param meetingRepository репозиторий для работы с информацией о встречах
 */
@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val telegramBotApi: TelegramBotApi,
    private val meetingRepository: MeetingRepository,
) {

    /** Logger. */
    private val log = KotlinLogging.logger {}

    @Transactional
    @Scheduled(cron = "\${schedule.match}")
    fun matchPairs() {
        val users = userRepository.findAllByStatusAndActiveTrue(Status.SCHEDULED)

        var collisionCount = 0
        while (users.count() > 1) {
            val (firstUserIndex, secondUserIndex) = findIndexes(0 until users.count())

            val firstUser = users[firstUserIndex]
            val secondUser = users[secondUserIndex]

            if (meetingRepository.existsMeeting(firstUser.userId, secondUser.userId)) {
                if (collisionCount >= MAX_ATTEMPT) {
                    sendFailure(firstUser, PAIR_CANNOT_MATCH_DUE_TO_COLLISION_MSG)
                    sendFailure(secondUser, PAIR_CANNOT_MATCH_DUE_TO_COLLISION_MSG)
                } else {
                    collisionCount++
                    continue
                }
            } else {
                val meeting = Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId)
                meetingRepository.save(meeting)

                sendInvention(firstUser, secondUser)
                sendInvention(secondUser, firstUser)
            }

            firstUser.status = Status.UNSCHEDULED
            secondUser.status = Status.UNSCHEDULED

            collisionCount = 0
            users.remove(firstUser)
            users.remove(secondUser)
        }

        // set unscheduled status for other
        users.forEach { u: User ->
            sendFailure(u, PAIR_CANNOT_MATCH_DUE_TO_ONN_COUNT_MSG)
            u.status = Status.UNSCHEDULED
        }
    }

    @Transactional
    @Scheduled(cron = "\${schedule.suggest}")
    fun sendSuggestions() {
        val users = userRepository.findAllByStatusInAndActiveTrue(listOf(Status.ASKED, Status.UNSCHEDULED))
        users.forEach { user: User ->
            user.status = Status.ASKED

            val participationQuestion = SendMessage()
            participationQuestion.text = Message.MATCH_SUGGESTION
            participationQuestion.parseMode = ParseMode.MARKDOWN
            participationQuestion.chatId = user.chatId.toString()
            participationQuestion.replyMarkup = partQuestion()
            participationQuestion.allowSendingWithoutReply = false

            try {
                telegramBotApi.execute(participationQuestion)
            } catch (ex: Exception) {
                log.error("Sending a request. Error occurred with user ${user.userId} ", ex)
            }
        }
    }

    private fun findIndexes(userIndexes: IntRange): Pair<Int, Int> {
        var firstUserIndex: Int
        var secondUserIndex: Int
        do {
            firstUserIndex = userIndexes.random()
            secondUserIndex = userIndexes.random()
        } while (firstUserIndex == secondUserIndex)

        return Pair(firstUserIndex, secondUserIndex)
    }

    private fun partQuestion(): InlineKeyboardMarkup {
        val infoBtn = InlineKeyboardButton().apply {
            text = "Да, конечно"
            callbackData = Command.READY.command
        }

        val showProfileBtn = InlineKeyboardButton().apply {
            text = "Пропущу неделю"
            callbackData = Command.SKIP.command
        }

        return InlineKeyboardMarkup().apply {
            keyboard = listOf(listOf(infoBtn, showProfileBtn))
        }
    }

    private fun sendInvention(user: User, partner: User) {
        val matchMessage = SendMessage()

        val profileUrl = partner.profileUrl ?: NOT_DEFINED
        val fullName = partner.fullName ?: NOT_DEFINED
        val city = partner.city ?: NOT_DEFINED

        matchMessage.text = MessageFormat.format(Message.MATCH_INVITATION, fullName, city, profileUrl)
        matchMessage.parseMode = ParseMode.MARKDOWN
        matchMessage.chatId = user.userId.toString()
        matchMessage.replyMarkup = contactPartnerBtn(partner)

        try {
            telegramBotApi.execute(matchMessage)
        } catch (ex: Exception) {
            log.error("Sending an invitation. Error occurred with user ${user.userId} ", ex)
        }
    }

    private fun sendFailure(user: User, reason: String) {
        val failureMessage = SendMessage()

        failureMessage.text = MessageFormat.format(Message.MATCH_FAILURE, reason)
        failureMessage.parseMode = ParseMode.MARKDOWN
        failureMessage.chatId = user.userId.toString()

        try {
            telegramBotApi.execute(failureMessage)
        } catch (ex: Exception) {
            log.error("Sending a cause of invitation failure. Error occurred with user ${user.userId} ", ex)
        }
    }

    private fun contactPartnerBtn(partner: User): InlineKeyboardMarkup {
        val contactPartner = InlineKeyboardButton().apply {
            text = "Telegram ${partner.fullName}"
            url = "tg://user?id=${partner.userId}"
        }
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                listOf(contactPartner),
            )
        }
    }

    companion object {

        /** Количество попыток решить коллизию участников встречи. */
        const val MAX_ATTEMPT = 10

        /** URL Random Coffee бота. */
        private const val BOT_URL = "https://t.me/InnotechRandomCoffeeBot"

        const val PAIR_CANNOT_MATCH_DUE_TO_COLLISION_MSG = "Кажется, ты встретился со всеми участниками \uD83D\uDC4D" +
                " Продолжай в том же духе и не забывай приглашать коллег $BOT_URL \uD83D\uDE4C"

        const val PAIR_CANNOT_MATCH_DUE_TO_ONN_COUNT_MSG =
            "В этот раз количество участников Random Coffee оказалось нечётное."
    }
}