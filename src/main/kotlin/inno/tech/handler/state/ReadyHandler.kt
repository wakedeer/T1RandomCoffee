package inno.tech.handler.state

import inno.tech.OfflineProperties
import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.MeetingFormat
import inno.tech.constant.Status
import inno.tech.exception.RandomCoffeeBotException
import inno.tech.handler.Handler
import inno.tech.model.User
import inno.tech.service.message.MessageService
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

/**
 * Обработчик сообщения о готовности участвовать в жеребьёвке.
 * Если город пользователя поддерживает офлайн — предлагает выбор формата.
 * Иначе сразу регистрирует как онлайн-участника.
 *
 * @param messageService сервис отправки сообщений
 * @param offlineProperties конфигурация городов с поддержкой офлайн-встреч
 */
@Component
class ReadyHandler(
    private val messageService: MessageService,
    private val offlineProperties: OfflineProperties,
) : Handler {

    override fun accept(command: String, user: User?): Boolean {
        return user != null && user.status == Status.ASKED && command == Command.READY.command
    }

    override fun handle(update: Update, user: User?) {
        if (user == null) {
            throw RandomCoffeeBotException("user cannot be null")
        }

        if (user.city != null && offlineProperties.cities.contains(user.city)) {
            user.status = Status.CHOOSING_FORMAT
            messageService.sendMessageWithKeyboard(user.chatId.toString(), FORMAT_MENU, Message.MATCH_CHOOSE_FORMAT)
        } else {
            user.meetingFormat = MeetingFormat.ONLINE
            user.status = Status.READY
            messageService.sendMessage(user.chatId.toString(), Message.READY_TO_MATCH)
        }
    }

    companion object {

        val FORMAT_MENU: InlineKeyboardMarkup = run {
            val onlineBtn = InlineKeyboardButton.builder()
                .text(MeetingFormat.ONLINE.displayName)
                .callbackData(Command.READY_ONLINE.command)
                .build()
            val offlineBtn = InlineKeyboardButton.builder()
                .text(MeetingFormat.OFFLINE.displayName)
                .callbackData(Command.READY_OFFLINE.command)
                .build()
            InlineKeyboardMarkup.builder()
                .keyboardRow(InlineKeyboardRow(onlineBtn, offlineBtn))
                .build()
        }
    }
}
