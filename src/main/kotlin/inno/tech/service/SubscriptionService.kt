package inno.tech.service

import inno.tech.TelegramBotApi
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.text.MessageFormat

@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val telegramBotApi: TelegramBotApi,
    private val meetingRepository: MeetingRepository,
) {

    @Transactional
    @Scheduled(cron = "\${schedule.match}")
    fun matchPairs() {
        val users = userRepository.findAllByStatusAndActiveTrue(Status.UNSCHEDULED)

        var collisionCount = 0
        while (users.count() > 1) {
            val userIndexes = 0 until users.count()
            val firstUserIndex = userIndexes.random()
            val secondUserIndex = userIndexes.filter { it == firstUserIndex }.random()

            val firstUser = users[firstUserIndex]
            val secondUser = users[secondUserIndex]

            if (meetingRepository.existsMeeting(firstUser.userId, secondUser.userId)) {
                if (collisionCount >= MAX_ATTEMPT) {
                    sendFailure(firstUser, PARTNER_NOT_FOUNT_MSG)
                    sendFailure(secondUser, PARTNER_NOT_FOUNT_MSG)
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

            collisionCount = 0
            users.removeAt(firstUserIndex)
            users.removeAt(secondUserIndex)
        }

        // set unscheduled status for other
        users.forEach { u: User ->
            sendFailure(u, LAST_ODD_USER_MSG)
            u.status = Status.UNSCHEDULED
            userRepository.save(u)
        }
    }

    @Transactional
    @Scheduled(cron = "\${schedule.suggest}")
    fun sendSuggestions() {
        val users = userRepository.findAllByStatusInAndActiveTrue(listOf(Status.SCHEDULED, Status.UNSCHEDULED))
        users.forEach { u: User ->
            u.status = Status.ASKED
            userRepository.save(u)

            val participationQuestion = SendMessage()
            participationQuestion.text = Message.MATCH_SUGGESTION
            participationQuestion.parseMode = ParseMode.MARKDOWN
            participationQuestion.chatId = u.chatId.toString()
            participationQuestion.replyMarkup = partQuestion()
            participationQuestion.allowSendingWithoutReply = false

            telegramBotApi.execute(participationQuestion)
        }
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
        val username = if (partner.username.isNullOrBlank()) partner.profileUrl else "@${partner.username}"

        matchMessage.text =
            MessageFormat.format(Message.MATCH_INVITATION,
                partner.fullName,
                partner.city,
                partner.profileUrl,
                username)
        matchMessage.parseMode = ParseMode.MARKDOWN
        matchMessage.chatId = user.userId.toString()
        matchMessage.replyMarkup = contactPartnerBtn(partner)

        telegramBotApi.execute(matchMessage)
    }

    private fun sendFailure(user: User, reason: String) {
        val failureMessage = SendMessage()

        failureMessage.text = MessageFormat.format(Message.MATCH_FAILURE, reason)
        failureMessage.parseMode = ParseMode.MARKDOWN
        failureMessage.chatId = user.userId.toString()

        telegramBotApi.execute(failureMessage)
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

        const val PARTNER_NOT_FOUNT_MSG = "мы не смогли найти тебе нового партнёра для встречи"

        const val LAST_ODD_USER_MSG = "количество участников встречи нечётное"
    }
}