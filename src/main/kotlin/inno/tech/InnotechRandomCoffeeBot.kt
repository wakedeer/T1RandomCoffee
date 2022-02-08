package inno.tech

import inno.tech.constant.Command
import inno.tech.constant.Message
import inno.tech.constant.Status
import inno.tech.model.Meeting
import inno.tech.model.User
import inno.tech.repository.MeetingRepository
import inno.tech.repository.UserRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.text.MessageFormat

@Component
class InnotechRandomCoffeeBot(
    private val telegramConfig: TelegramConfig,
    private val userRepository: UserRepository,
    private val meetingRepository: MeetingRepository,
) : TelegramLongPollingBot() {

    override fun getBotToken() = telegramConfig.token

    override fun getBotUsername() = telegramConfig.name

    override fun onUpdateReceived(update: Update) {
        val text: String
        val chatId: Long
        val userId: Long

        if (update.hasMessage() && update.message.hasText()) {
            text = update.message.text
            chatId = update.message.chatId
            userId = update.message.from.id
        } else if (update.hasCallbackQuery()) {
            text = update.callbackQuery.data
            chatId = update.callbackQuery.from.id
            userId = update.callbackQuery.from.id
        } else {
            return
        }

        val user: User? = userRepository.findById(userId).orElse(null)

        if (text == Command.RESTART.command || text == Command.START.command || text == Command.EDIT_PROFILE.command) {
            val welcomeMsg = SendMessage()
            welcomeMsg.text = Message.WELCOME
            welcomeMsg.parseMode = ParseMode.MARKDOWN
            welcomeMsg.chatId = chatId.toString()
            execute(welcomeMsg)

            val newUser = User(
                userId = userId,
                chatId = chatId,
                status = Status.REG_NAME,
                active = true,
            )
            newUser.status = Status.REG_NAME
            userRepository.save(newUser)

            val nameQuestion = SendMessage()
            nameQuestion.text = Message.REG_STEP_1
            nameQuestion.parseMode = ParseMode.MARKDOWN
            nameQuestion.chatId = chatId.toString()
            nameQuestion.allowSendingWithoutReply = false
            execute(nameQuestion)

        } else if (Command.INFO.command == text) {

            val info = SendMessage()
            info.text = Message.INFO
            info.parseMode = ParseMode.MARKDOWN
            info.chatId = chatId.toString()
            info.allowSendingWithoutReply = false
            execute(info)

        } else if (user != null && INTERMEDIATE_REGISTRATION_STATUSES.contains(user.status)) {
            val question = SendMessage()

            when (user.status) {
                Status.REG_NAME -> {
                    user.fullName = update.message.text
                    question.text = Message.REG_STEP_2
                }
                Status.REG_CITY -> {
                    user.city = update.message.text
                    question.text = Message.REG_STEP_3
                }
                Status.REG_PROFILE_URL -> {
                    user.profileUrl = update.message.text
                    question.text = Message.REG_SUCCESS
                }
                else -> {
                    errorSend(chatId)
                    return
                }
            }

            user.status = REGISTRATION_STATUS_ORDER[REGISTRATION_STATUS_ORDER.indexOf(user.status) + 1]
            userRepository.save(user)

            question.parseMode = ParseMode.MARKDOWN
            question.chatId = chatId.toString()
            question.allowSendingWithoutReply = false
            execute(question)

        } else if (telegramConfig.adminId == user?.userId && text == "/pair") {
            val users = userRepository.findAllByStatusAndActiveTrue(Status.UNSCHEDULED)

            while (users.count() > 1) {
                val firstUserIndex = (0 until users.count()).random()
                val firstUser = users.removeAt(firstUserIndex)

                val secondUserIndex = (0 until users.count()).random()
                val secondUser = users.removeAt(secondUserIndex)

                val meeting = Meeting(userId1 = firstUser.userId, userId2 = secondUser.userId)
                meetingRepository.save(meeting)

                sendIndentation(firstUser, secondUser)
                sendIndentation(secondUser, firstUser)
            }

            // set unscheduled status for other
            users.forEach { u: User ->
                u.status = Status.UNSCHEDULED
                userRepository.save(u)
            }

        } else if (user != null && user.status == Status.ASKED && text == Command.READY.command) {
            user.status = Status.UNSCHEDULED
            userRepository.save(user)
        } else if (user != null && user.status == Status.ASKED && text == Command.SKIP.command) {
            val skipReply = SendMessage()
            skipReply.text = Message.MATCH_SKIP
            skipReply.parseMode = ParseMode.MARKDOWN
            skipReply.chatId = user.userId.toString()
            execute(skipReply)
        } else if (telegramConfig.adminId == user?.userId && text == "/ask") {
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
                execute(participationQuestion)
            }
        } else if (user != null && text == Command.PAUSE.command) {
            user.active = false
            userRepository.save(user)

            val pauseReply = SendMessage()
            pauseReply.text = Message.STATUS_PAUSE
            pauseReply.parseMode = ParseMode.MARKDOWN
            pauseReply.chatId = user.chatId.toString()
            execute(pauseReply)

        } else if (user != null && text == Command.RESUME.command) {
            user.active = true
            userRepository.save(user)

            val resumeResponse = SendMessage()
            resumeResponse.text = Message.STATUS_RESUME
            resumeResponse.parseMode = ParseMode.MARKDOWN
            resumeResponse.chatId = user.chatId.toString()
            execute(resumeResponse)

        } else if (user != null && text == Command.SHOW_PROFILE.command) {

            val showProfileReply = SendMessage()
            showProfileReply.text = MessageFormat.format(Message.PROFILE, user.fullName, user.city, user.profileUrl)
            showProfileReply.parseMode = ParseMode.MARKDOWN
            showProfileReply.chatId = user.chatId.toString()
            execute(showProfileReply)
        } else {
            errorSend(chatId)
        }
    }

    fun sendIndentation(user: User, partner: User) {
        val matchMessage = SendMessage()
        matchMessage.text =
            MessageFormat.format(Message.MATCH_INVITATION, partner.fullName, partner.city, partner.profileUrl)
        matchMessage.parseMode = ParseMode.MARKDOWN
        matchMessage.chatId = user.userId.toString()
        execute(matchMessage)
    }

    private fun errorSend(chatId: Long) {
        val response = SendMessage()
        response.text = Message.ERROR
        response.replyMarkup = mainMenu()
        response.parseMode = ParseMode.MARKDOWN
        response.chatId = chatId.toString()
        execute(response)
    }

    private fun mainMenu(): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()

        val infoBtn = InlineKeyboardButton()
        infoBtn.text = "Что такое Random coffee"
        infoBtn.callbackData = Command.INFO.command

        val showProfileBtn = InlineKeyboardButton()
        showProfileBtn.text = "Посмотреть свой профиль"
        showProfileBtn.callbackData = Command.SHOW_PROFILE.command

        val editProfileBtn = InlineKeyboardButton()
        editProfileBtn.text = "Поменять данные профиля"
        editProfileBtn.callbackData = Command.EDIT_PROFILE.command

        val pauseBtn = InlineKeyboardButton()
        pauseBtn.text = "Поставить бот на паузу"
        pauseBtn.callbackData = Command.PAUSE.command

        val resumeBtn = InlineKeyboardButton()
        resumeBtn.text = "Снять бота с паузы"
        resumeBtn.callbackData = Command.RESUME.command

        val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow1.add(infoBtn)

        val keyboardButtonsRow2: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow2.add(showProfileBtn)

        val keyboardButtonsRow3: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow3.add(editProfileBtn)

        val keyboardButtonsRow4: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow4.add(pauseBtn)

        val keyboardButtonsRow5: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow5.add(resumeBtn)


        val keyboardButtons: MutableList<List<InlineKeyboardButton>> = ArrayList()
        keyboardButtons.add(keyboardButtonsRow1)
        keyboardButtons.add(keyboardButtonsRow2)
        keyboardButtons.add(keyboardButtonsRow3)
        keyboardButtons.add(keyboardButtonsRow4)
        keyboardButtons.add(keyboardButtonsRow5)

        inlineKeyboardMarkup.keyboard = keyboardButtons
        return inlineKeyboardMarkup
    }


    private fun partQuestion(): InlineKeyboardMarkup {
        val inlineKeyboardMarkup = InlineKeyboardMarkup()

        val infoBtn = InlineKeyboardButton()
        infoBtn.text = "Да, конечно"
        infoBtn.callbackData = "OK"

        val showProfileBtn = InlineKeyboardButton()
        showProfileBtn.text = "Пропущу неделю"
        showProfileBtn.callbackData = "NOK"

        val keyboardButtonsRow1: MutableList<InlineKeyboardButton> = ArrayList()
        keyboardButtonsRow1.add(infoBtn)
        keyboardButtonsRow1.add(showProfileBtn)

        val keyboardButtons: MutableList<List<InlineKeyboardButton>> = ArrayList()
        keyboardButtons.add(keyboardButtonsRow1)

        inlineKeyboardMarkup.keyboard = keyboardButtons
        return inlineKeyboardMarkup
    }

    companion object {

        val INTERMEDIATE_REGISTRATION_STATUSES = listOf(
            Status.REG_NAME,
            Status.REG_CITY,
            Status.REG_PROFILE_URL,
        )

        val REGISTRATION_STATUS_ORDER = INTERMEDIATE_REGISTRATION_STATUSES + Status.UNSCHEDULED
    }
}