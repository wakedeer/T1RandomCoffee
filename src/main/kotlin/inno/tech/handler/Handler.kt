package inno.tech.handler

import inno.tech.model.User
import org.springframework.transaction.annotation.Transactional
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * Обработчик команд пользователя.
 */
interface Handler {

    /**
     * Условие обработки сообщения обработчиком.
     * @param command команда или текст поступающий от пользователя
     * @param user  информация о пользователе
     * @return true - данные обработчик обработает входящее сообщение
     */
    fun accept(command: String, user: User?): Boolean

    /**
     * Обрабатывает входящее сообщение.
     * @param update входящее сообщение
     * @param user информация о пользователе
     */
    @Transactional
    fun handle(update: Update, user: User?)
}