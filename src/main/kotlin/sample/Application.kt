package sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** The start class of the application. */
@SpringBootApplication class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
