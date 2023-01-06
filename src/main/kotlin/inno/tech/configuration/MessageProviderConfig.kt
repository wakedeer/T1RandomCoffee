package inno.tech.configuration

import inno.tech.constant.message.DefaultMessageProvider
import inno.tech.constant.message.InnotechEngClubMessageProvider
import inno.tech.constant.message.MessageProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class MessageProviderConfig {

    @Bean
    @Profile("innotech")
    fun innotechMessageProvider(): MessageProvider = InnotechEngClubMessageProvider()

    @Bean
    @ConditionalOnMissingBean(value = [MessageProvider::class])
    fun defaultMessageProvider(): MessageProvider = DefaultMessageProvider()
}
