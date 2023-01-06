package inno.tech.configuration

import inno.tech.constant.message.DefaultMessageProvider
import inno.tech.constant.message.InnotechEngClubMessageProvider
import inno.tech.constant.message.MessageProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Конфигурация шаблонов сообщений.
 *
 */
@Configuration
class MessageProviderConfig {

    /**
     * Создаёт компонент содержащий шаблоны сообщений для бота Иннотех.
     *
     * @return компонент содержащий шаблоны сообщений для бота Иннотех
     */
    @Bean
    @Profile("innotech")
    fun innotechMessageProvider(): MessageProvider = InnotechEngClubMessageProvider()

    /**
     * Создаёт компонент содержащий шаблоны сообщений по-умолчанию.
     *
     * @return компонент содержащий шаблоны сообщений по-умолчанию
     */
    @Bean
    @ConditionalOnMissingBean(value = [MessageProvider::class])
    fun defaultMessageProvider(): MessageProvider = DefaultMessageProvider()
}
