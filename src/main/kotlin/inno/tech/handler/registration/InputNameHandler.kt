package inno.tech.handler.registration

import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.extension.getChatIdAsString
import inno.tech.extension.getMessageText
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Component
class InputNameHandler(
    private val messageService: MessageService,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && Status.REG_NAME == user.status
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("Error user state for message $update")
        }

        user.fullName = update.getMessageText()
        user.status = Status.REG_CITY

        messageService.sendMessageWithKeyboard(update.getChatIdAsString(), CITIES, Message.REG_STEP_2)
    }


    companion object {

        val CITIES = chooseCityBtn()

        private fun chooseCityBtn(): InlineKeyboardMarkup {
            return InlineKeyboardMarkup().apply {
                keyboard = listOf(
                    listOf(cityBtn("Moscow"), cityBtn("St. Petersburg")),
                    listOf(cityBtn("Novosibirsk"), cityBtn("Yekaterinburg")),
                    listOf(cityBtn("Kazan"), cityBtn("Samara")),
                    listOf(cityBtn("Nizhny Novgorod"), cityBtn("Voronezh")),
                    listOf(cityBtn("Krasnodar"), cityBtn("Tyumen")),
                    listOf(cityBtn("Izhevsk"), cityBtn("Khabarovsk")),
                    listOf(cityBtn("Vladivostok"), cityBtn("Tomsk")),
                    listOf(cityBtn("Ryazan"), cityBtn("Калининград")),
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
