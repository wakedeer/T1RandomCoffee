package inno.tech.handler.registration

import inno.tech.TelegramBotApi
import inno.tech.constant.Message
import inno.tech.constant.REGISTRATION_STATUSES
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.extension.getUserId
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.repository.UserRepository
import inno.tech.service.SubscriptionService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Обработчик сообщений заполнения профиля пользователя.
 *
 * @param telegramBotApi компонент, предоставляющий доступ к Telegram Bot API
 */
@Component
class FillProfileHandle(
    private val telegramBotApi: TelegramBotApi,
    private val subscriptionService: SubscriptionService,
    private val userRepository: UserRepository,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && REGISTRATION_STATUSES.contains(user.status)
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        val message = SendMessage()

        when (user.status) {
            Status.REG_NAME -> {
                user.fullName = update.getMessageText()
                message.text = Message.REG_STEP_2
                message.replyMarkup = CITIES
                user.status = getNextStatus(user.status)
            }
            Status.REG_CITY -> {
                user.city = update.getMessageText()
                message.text = Message.REG_STEP_3
                user.status = getNextStatus(user.status)
            }
            Status.REG_PROFILE_URL -> {
                user.profileUrl = update.getMessageText()
                message.text = Message.REG_SUCCESS
                val previousStatus = user.previousStatus
                if (previousStatus != null) {
                    //update profile
                    user.status = previousStatus
                } else {
                    //new user
                    val readyUser = userRepository.findAllByStatusAndActiveTrue(Status.READY).firstOrNull()
                    if (readyUser != null) {
                        sendMessage(message, update)
                        subscriptionService.sendInvitation(readyUser, user)
                        return
                    } else {
                        user.status = Status.READY
                    }
                }
            }
            else -> {
                telegramBotApi.errorSend(update.getUserId())
                return
            }
        }

        sendMessage(message, update)
    }

    private fun sendMessage(
        question: SendMessage,
        update: Update,
    ) {
        question.parseMode = ParseMode.MARKDOWN
        question.chatId = update.getChatIdAsString()
        question.allowSendingWithoutReply = false
        telegramBotApi.execute(question)
    }

    /**
     * Возвращает следующий шаг регистрации.
     * @param currentStatus текущий шаг регистрации
     * @return следующий шаг регистрации
     */
    private fun getNextStatus(currentStatus: Status) =
        REGISTRATION_STATUSES[REGISTRATION_STATUSES.indexOf(currentStatus) + 1]

    companion object {

        val CITIES = chooseCityBtn()

        private fun chooseCityBtn(): InlineKeyboardMarkup {
            return InlineKeyboardMarkup().apply {
                keyboard = listOf(
                    listOf(cityBtn("Москва"), cityBtn("Санкт-Петербург")),
                    listOf(cityBtn("Новосибирск"), cityBtn("Екатеринбург")),
                    listOf(cityBtn("Казань"), cityBtn("Самара")),
                    listOf(cityBtn("Нижний Новгород"), cityBtn("Воронеж")),
                    listOf(cityBtn("Краснодар"), cityBtn("Тюмень")),
                    listOf(cityBtn("Ижевск"), cityBtn("Хабаровск")),
                    listOf(cityBtn("Владивосток"), cityBtn("Томск")),
                    listOf(cityBtn("Рязань"), cityBtn("Севастополь")),
                    listOf(cityBtn("Калининград")),
                )
            }
        }

        private fun cityBtn(city: String): InlineKeyboardButton {
            val contactPartner = InlineKeyboardButton().apply {
                text = city
                callbackData = city
            }
            return contactPartner
        }
    }
}
