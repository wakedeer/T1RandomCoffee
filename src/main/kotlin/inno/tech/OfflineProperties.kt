package inno.tech

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Конфигурация городов, в которых доступны офлайн-встречи.
 * @param cities список городов с поддержкой офлайн-формата
 */
@ConfigurationProperties("offline")
data class OfflineProperties(
    val cities: List<String> = emptyList(),
)

