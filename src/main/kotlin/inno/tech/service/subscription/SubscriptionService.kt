package inno.tech.service.subscription

import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.MeetingFormat
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.time.LocalDateTime

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
        log.info { "Pair matching is started" }
        val participants = userRepository.findAllByStatusAndActiveTrue(Status.READY)
        val fromDate = LocalDateTime.now().minusMonths(6)

        // 1. Матчим офлайн-участников внутри каждого города
        val offlineParticipants = participants.filter { it.meetingFormat == MeetingFormat.OFFLINE }.toMutableList()
        val byCity = offlineParticipants.groupBy { it.city }
        byCity.forEach { (city, cityGroup) ->
            val cityParticipants = cityGroup.toMutableList()
            matchGroup(cityParticipants, fromDate)
            // Нечётные офлайн-участники, которым не нашлась пара в городе — переводим в онлайн-пул
            cityParticipants.forEach { u ->
                log.info { "No offline pair found in city '$city' for user ${u.userId}. Falling back to online." }
                sendFailure(u, Message.MATCH_FAILURE_OFFLINE)
                u.meetingFormat = MeetingFormat.ONLINE
            }
            participants.removeAll(cityGroup.toSet())
            participants.addAll(cityParticipants) // добавляем упавших обратно в общий пул
        }

        // 2. Матчим оставшихся (онлайн + упавшие из офлайна) случайно
        matchGroup(participants, fromDate)

        // 3. Нечётные без пары
        participants.forEach { u: User ->
            sendFailure(u, Message.MATCH_FAILURE_ODD)
            u.status = Status.UNPAIRED
        }
        log.info { "Pair matching is finished successfully" }
    }

    private fun matchGroup(participants: MutableList<User>, fromDate: LocalDateTime) {
        var collisionCount = 0
        while (participants.count() > 1) {
            val (firstUserIndex, secondUserIndex) = findIndexes(0 until participants.count())
            val firstUser = participants[firstUserIndex]
            val secondUser = participants[secondUserIndex]

            if (meetingRepository.existsMeetingAfter(firstUser.userId, secondUser.userId, fromDate)) {
                if (collisionCount >= MAX_ATTEMPT) {
                    sendFailure(firstUser, Message.MATCH_FAILURE)
                    sendFailure(secondUser, Message.MATCH_FAILURE)
                    firstUser.status = Status.UNPAIRED
                    secondUser.status = Status.UNPAIRED
                    collisionCount = 0
                    participants.remove(firstUser)
                    participants.remove(secondUser)
                } else {
                    collisionCount++
                }
            } else {
                val isSuccess = sendMatchResult(firstUser, secondUser)
                if (!isSuccess) {
                    participants.removeIf { !it.active }
                    collisionCount++
                    continue
                }
                collisionCount = 0
                participants.remove(firstUser)
                participants.remove(secondUser)
            }
        }
    }

    fun sendMatchResult(firstUser: User, secondUser: User): Boolean {
        if (!sendMatch(firstUser, secondUser)) {
            return false
        }
        if (!sendMatch(secondUser, firstUser)) {
            log.warn { "User ${firstUser.userId} already received match notification, but match failed for ${secondUser.userId}" }
            return false
        }

        firstUser.status = Status.MATCHED
        secondUser.status = Status.MATCHED
        val meetingFormat = if (firstUser.meetingFormat == MeetingFormat.OFFLINE && secondUser.meetingFormat == MeetingFormat.OFFLINE) {
            MeetingFormat.OFFLINE
        } else {
            MeetingFormat.ONLINE
        }
        meetingRepository.save(Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId, meetingFormat = meetingFormat))
        log.debug { "Created pair first user id: ${firstUser.userId} and second user id: ${secondUser.userId}" }
        return true
    }

    fun sendMatch(firstUser: User, secondUser: User): Boolean {
        try {
            messageService.sendInvitationMessage(firstUser, secondUser)
            return true
        } catch (ex: Exception) {
            if (ex is TelegramApiRequestException && 403 == ex.errorCode) {
                log.warn(ex) { "User ${firstUser.userId} unsubscribed from the bot. Deactivate user" }
                firstUser.active = false
            } else {
                log.error(ex) { "Sending a request. Error occurred with user ${firstUser.userId} " }
            }
        }
        return false
    }

    @Transactional
    @Scheduled(cron = "\${schedule.invite}")
    fun sendInvitation() {
        log.info { "Invention sending is started" }
        val invitationGroup = listOf(Status.MATCHED, Status.ASKED, Status.UNPAIRED, Status.SKIP)
        val participants = userRepository.findAllByStatusInAndActiveTrue(invitationGroup)
        participants.forEach { participant: User ->
            participant.status = Status.ASKED
            participant.meetingFormat = null  // сбрасываем формат предыдущей недели
            try {
                messageService.sendMessageWithKeyboard(participant.chatId.toString(), SUGGESTION_MENU, Message.MATCH_SUGGESTION)
            } catch (ex: Exception) {
                if (ex is TelegramApiRequestException && 403 == ex.errorCode) {
                    log.warn(ex) { "User ${participant.userId} unsubscribed from the bot. Deactivate user" }
                    participant.active = false
                } else {
                    log.error(ex) { "Sending a request. Error occurred with user ${participant.userId} " }
                }
            }
        }
        log.info { "Invention sending is started has finished" }
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
            log.error(ex) { "Sending a cause of invitation failure. Error occurred with user ${user.userId} " }
        }
    }

    companion object {

        /** Количество попыток решить коллизию участников встречи. */
        const val MAX_ATTEMPT = 10

        /** Сообщение о готовности участвовать в жеребьевке. */
        private const val READY_MESSAGE = "Да, конечно"

        /** Сообщение о пропуске участия в жеребьёвке. */
        private const val SKIP_MESSAGE = "Пропущу неделю"

        /** Меню выбора участия в жеребьёвке. */
        val SUGGESTION_MENU: InlineKeyboardMarkup = run {
            val infoBtn = InlineKeyboardButton.builder()
                .text(READY_MESSAGE)
                .callbackData(Command.READY.command)
                .build()
            val showProfileBtn = InlineKeyboardButton.builder()
                .text(SKIP_MESSAGE)
                .callbackData(Command.SKIP.command)
                .build()
            InlineKeyboardMarkup.builder()
                .keyboardRow(InlineKeyboardRow(infoBtn, showProfileBtn))
                .build()
        }
    }
}
