package inno.tech.service.subscription

import inno.tech.constant.Command
import inno.tech.constant.Level
import inno.tech.constant.Message
import inno.tech.constant.Message.MATCH_FAILURE_SEND_TO_PARTNER
import inno.tech.constant.SendInvitationStatus
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
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.util.LinkedList

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
        log.info("Pair matching is started")

        val nextLevelParticipants = mutableListOf<User>()
        for (level in Level.values()) {
            val participants = LinkedList(nextLevelParticipants)
            participants.addAll(userRepository.findAllByStatusAndLevelAndActiveTrue(Status.READY, level))
            nextLevelParticipants.clear()

            while (participants.count() > 1) {

                val firstUser = participants.removeFirst()
                val secondUser = participants.find { candidate -> meetingRepository.existsMeeting(firstUser.userId, candidate.userId).not() }

                if (secondUser == null) {
                    log.debug("Can't find pair for user ${firstUser.userId}")
                    nextLevelParticipants.add(firstUser)
                    continue
                }

                when (sendInvitation(firstUser, secondUser)) {
                    SendInvitationStatus.OK -> {
                        participants.remove(secondUser)
                        log.info("Invitation has been sent to ${firstUser.userId} and ${secondUser.userId} ")
                    }

                    SendInvitationStatus.FIRST_ERROR -> {
                        firstUser.active = false
                        log.warn("User ${firstUser.userId} has been deactivated ")
                    }

                    SendInvitationStatus.SECOND_ERROR -> {
                        secondUser.active = false
                        participants.addFirst(firstUser)
                        participants.remove(secondUser)
                        log.warn("User ${secondUser.userId} has been deactivated ")
                    }
                }
            }
            nextLevelParticipants.addAll(participants)
        }

        // set unscheduled status for other
        nextLevelParticipants.forEach { participant: User ->
            sendFailure(participant, Message.MATCH_FAILURE)
            participant.status = Status.UNPAIRED
            log.info("User ${participant.userId} hasn't been matched")
        }

        log.info("Pair matching is finished successfully")
    }

    fun sendInvitation(firstUser: User, secondUser: User): SendInvitationStatus {
        try {
            messageService.sendInvitationMessage(firstUser, secondUser)
        } catch (e: Exception) {
            log.error("Error occurred sending match message to pair ${firstUser.userId} and ${secondUser.userId}", e)
            return SendInvitationStatus.FIRST_ERROR
        }

        try {
            messageService.sendInvitationMessage(secondUser, firstUser)
        } catch (e: Exception) {
            messageService.sendMessage(firstUser.userId.toString(), MATCH_FAILURE_SEND_TO_PARTNER)
            log.error("Error occurred sending match message to pair ${firstUser.userId} and ${secondUser.userId}", e)
            return SendInvitationStatus.SECOND_ERROR
        }

        firstUser.status = Status.MATCHED
        secondUser.status = Status.MATCHED
        meetingRepository.save(Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId))

        log.info("Created pair first user id: ${firstUser.userId} and second user id: ${secondUser.userId}")

        return SendInvitationStatus.OK
    }

    @Transactional
    @Scheduled(cron = "\${schedule.invite}")
    fun sendInvitation() {
        log.info("Invention sending is started")
        val invitationGroup = listOf(Status.MATCHED, Status.ASKED, Status.UNPAIRED, Status.SKIP)
        val participants = userRepository.findAllByStatusInAndActiveTrue(invitationGroup)
        participants.forEach { participant: User ->
            participant.status = Status.ASKED
            try {
                messageService.sendMessageWithKeyboard(participant.chatId.toString(), SUGGESTION_MENU, Message.MATCH_SUGGESTION)
            } catch (ex: Exception) {
                if (ex is TelegramApiRequestException && 403 == ex.errorCode) {
                    log.warn("User ${participant.userId} unsubscribed from the bot. Deactivate user", ex)
                    participant.active = false
                } else {
                    log.error("Sending a request. Error occurred with user ${participant.userId} ", ex)
                }
            }
        }
        log.info("Invention sending has finished")
    }

    private fun sendFailure(user: User, reason: String) {
        try {
            messageService.sendMessage(user.userId.toString(), reason)
        } catch (ex: Exception) {
            log.error("Sending a cause of invitation failure. Error occurred with user ${user.userId} ", ex)
        }
    }

    companion object {

        /** Сообщение о готовности участвовать в жеребьевке. */
        private const val READY_MESSAGE = "Yes, sure"

        /** Сообщение о пропуске участия в жеребьёвке. */
        private const val SKIP_MESSAGE = "Skip one week"

        /** Меню выбора участия в жеребьёвке. */
        val SUGGESTION_MENU = run {
            val infoBtn = InlineKeyboardButton().apply {
                text = READY_MESSAGE
                callbackData = Command.READY.command
            }
            val showProfileBtn = InlineKeyboardButton().apply {
                text = SKIP_MESSAGE
                callbackData = Command.SKIP.command
            }
            InlineKeyboardMarkup().apply {
                keyboard = listOf(listOf(infoBtn, showProfileBtn))
            }
        }
    }
}
