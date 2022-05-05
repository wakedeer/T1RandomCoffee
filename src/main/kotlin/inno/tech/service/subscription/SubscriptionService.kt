package inno.tech.service.subscription

import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Сервис для рассылки уведомлений пользователем по расписанию.
 * @param userRepository репозиторий для работы с информацией о пользователе
 * @param meetingRepository репозиторий для работы с информацией о встречах
 * @param messageService сервис отправки сообщений
 */
@Service
class SubscriptionService(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val messageService: MessageService,
) {

    /** Logger. */
    private val log = KotlinLogging.logger {}

    @Transactional
    @Scheduled(cron = "\${schedule.match}")
    fun matchPairs() {
        val participants = userRepository.findAllByStatusAndActiveTrue(Status.READY)

        var collisionCount = 0
        while (participants.count() > 1) {
            val (firstUserIndex, secondUserIndex) = findIndexes(0 until participants.count())

            val firstUser = participants[firstUserIndex]
            val secondUser = participants[secondUserIndex]

            if (meetingRepository.existsMeeting(firstUser.userId, secondUser.userId)) {
                if (collisionCount > MAX_ATTEMPT) {
                    sendFailure(firstUser, Message.MATCH_FAILURE)
                    sendFailure(secondUser, Message.MATCH_FAILURE)
                    firstUser.status = Status.UNPAIRED
                    secondUser.status = Status.UNPAIRED
                } else {
                    collisionCount++
                    continue
                }
            } else {
                sendInvitation(firstUser, secondUser)
            }

            collisionCount = 0
            participants.remove(firstUser)
            participants.remove(secondUser)
        }

        // set unscheduled status for other
        participants.forEach { u: User ->
            sendFailure(u, Message.MATCH_FAILURE_ODD)
            u.status = Status.UNPAIRED
        }
    }

    fun sendInvitation(firstUser: User, secondUser: User) {
        val meeting = Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId)
        meetingRepository.save(meeting)

        messageService.sendInventionMessage(firstUser, secondUser)
        messageService.sendInventionMessage(secondUser, firstUser)
        firstUser.status = Status.MATCHED
        secondUser.status = Status.MATCHED
    }

    @Transactional
    @Scheduled(cron = "\${schedule.invite}")
    fun sendInvitation() {
        val invitationGroup = listOf(Status.MATCHED, Status.ASKED, Status.UNPAIRED, Status.SKIP)
        val participants = userRepository.findAllByStatusInAndActiveTrue(invitationGroup)
        participants.forEach { participant: User ->
            participant.status = Status.ASKED
            try {
                messageService.sendMessageWithKeyboard(participant.chatId.toString(), SUGGESTION_MENU, Message.MATCH_SUGGESTION)
            } catch (ex: Exception) {
                log.error("Sending a request. Error occurred with user ${participant.userId} ", ex)
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


    private fun sendFailure(user: User, reason: String) {
        try {
            messageService.sendMessage(user.userId.toString(), reason)
        } catch (ex: Exception) {
            log.error("Sending a cause of invitation failure. Error occurred with user ${user.userId} ", ex)
        }
    }

    companion object {

        /** Количество попыток решить коллизию участников встречи. */
        const val MAX_ATTEMPT = 10

        val SUGGESTION_MENU = createInvitationMenu()

        private fun createInvitationMenu(): InlineKeyboardMarkup {
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
    }
}
