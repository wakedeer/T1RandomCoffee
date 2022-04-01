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

        while (users.count() > 1) {
            val firstUserIndex = (0 until users.count()).random()
            val firstUser = users.removeAt(firstUserIndex)

            val secondUserIndex = (0 until users.count()).random()
            val secondUser = users.removeAt(secondUserIndex)

            val meeting = Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId)
            meetingRepository.save(meeting)

            sendInvention(firstUser, secondUser)
            sendInvention(secondUser, firstUser)
        }

        // set unscheduled status for other
        users.forEach { u: User ->
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
}