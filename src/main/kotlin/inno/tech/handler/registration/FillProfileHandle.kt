package inno.tech.handler.registration

import inno.tech.TelegramBotApi
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.extension.getUserId
import inno.tech.handler.Handler
import inno.tech.model.User
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
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && INTERMEDIATE_REGISTRATION_STATUSES.contains(user.status)
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        val question = SendMessage()

        when (user.status) {
            Status.REG_NAME -> {
                user.fullName = update.getMessageText()
                question.text = Message.REG_STEP_2
                question.replyMarkup = CITIES
            }
            Status.REG_CITY -> {
                user.city = update.getMessageText()
                question.text = Message.REG_STEP_3
            }
            Status.REG_PROFILE_URL -> {
                user.profileUrl = update.getMessageText()
                question.text = Message.REG_SUCCESS
            }
            else -> {
                telegramBotApi.errorSend(update.getUserId())
                return
            }
        }

        user.status = REGISTRATION_STATUS_ORDER[REGISTRATION_STATUS_ORDER.indexOf(user.status) + 1]

        question.parseMode = ParseMode.MARKDOWN
        question.chatId = update.getChatIdAsString()
        question.allowSendingWithoutReply = false
        telegramBotApi.execute(question)
    }

    companion object {

        val INTERMEDIATE_REGISTRATION_STATUSES = listOf(
            Status.REG_NAME,
            Status.REG_CITY,
            Status.REG_PROFILE_URL,
        )

        val REGISTRATION_STATUS_ORDER = INTERMEDIATE_REGISTRATION_STATUSES + Status.SCHEDULED

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