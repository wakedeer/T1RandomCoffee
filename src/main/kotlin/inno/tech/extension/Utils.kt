package inno.tech.extension

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * Создает inline клавишу.
 */
internal fun createBtn(text: String): InlineKeyboardButton {
    return InlineKeyboardButton().apply {
        this.text = text
        callbackData = text
    }
}
