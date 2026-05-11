package inno.tech.handler.state

import inno.tech.constant.Command
import inno.tech.constant.MeetingFormat
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import inno.tech.service.message.MessageService
import inno.tech.service.subscription.SubscriptionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDateTime

/**
 * Обработчик ответа «Нет ответа» — партнёр не ответил, ищем нового собеседника.
 *
 * Логика поиска партнёра:
 * - Офлайн-участник: ищем READY-пользователя с офлайн-форматом в том же городе.
 *   Если нашли — метчим. Если нет — кладём в пул READY (без фолбэка на онлайн).
 * - Онлайн-участник: ищем любого READY-пользователя.
 *   Если нашли — метчим. Если нет — кладём в пул READY.
 *
 * @param userRepository репозиторий пользователей
 * @param meetingRepository репозиторий встреч
 * @param messageService сервис отправки сообщений
 * @param subscriptionService сервис жеребьёвки (для отправки приглашения паре)
 */
@Component
class RequestRematchHandler(
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
    private val messageService: MessageService,
    private val subscriptionService: SubscriptionService,
) : Handler {

    private val log = KotlinLogging.logger {}

    override fun accept(command: String, user: User?): Boolean {
        return user?.status == Status.SUGGEST_REMATCH && command == Command.REQUEST_REMATCH.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        val fromDate = LocalDateTime.now().minusMonths(6)
        val partner = findPartner(user, fromDate)

        if (partner != null) {
            log.info { "Rematch: found partner ${partner.userId} for user ${user.userId}" }
            subscriptionService.sendMatchResult(user, partner)
        } else {
            val reason = if (user.meetingFormat == MeetingFormat.OFFLINE) {
                "no offline partner in city '${user.city}'"
            } else {
                "no READY users available"
            }
            log.info { "Rematch: $reason for user ${user.userId}, adding to READY pool" }
            user.status = Status.READY
            messageService.sendMessage(user.chatId.toString(), Message.REMATCH_SEARCH)
        }
    }

    /**
     * Ищет подходящего партнёра из пула READY-пользователей.
     *
     * Офлайн-участник: только офлайн-партнёр в том же городе, фолбэк на онлайн не делается.
     * Онлайн-участник: любой READY-пользователь.
     */
    private fun findPartner(user: User, fromDate: LocalDateTime): User? {
        return if (user.meetingFormat == MeetingFormat.OFFLINE && user.city != null) {
            // Офлайн: ищем только в своём городе
            userRepository
                .findAllByStatusAndMeetingFormatAndCityAndActiveTrue(Status.READY, MeetingFormat.OFFLINE, user.city!!)
                .firstOrNull { candidate ->
                    candidate.userId != user.userId &&
                        !meetingRepository.existsMeetingAfter(user.userId, candidate.userId, fromDate)
                }
        } else {
            // Онлайн: любой READY-пользователь
            userRepository
                .findAllByStatusAndActiveTrue(Status.READY)
                .firstOrNull { candidate ->
                    candidate.userId != user.userId &&
                        !meetingRepository.existsMeetingAfter(user.userId, candidate.userId, fromDate)
                }
        }
    }
}

