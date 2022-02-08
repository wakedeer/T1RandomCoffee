package inno.tech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class InnotechRandomCoffeeApplication

fun main(args: Array<String>) {
	runApplication<InnotechRandomCoffeeApplication>(*args)
}
