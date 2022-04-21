package inno.tech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * Основной класс приложения.
 *
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class InnotechRandomCoffeeApplication

/**
 * Начальная загрузка и запуск приложения.
 *
 * @param args аргументы командной строки
 */
fun main(args: Array<String>) {
    runApplication<InnotechRandomCoffeeApplication>(*args)
}
