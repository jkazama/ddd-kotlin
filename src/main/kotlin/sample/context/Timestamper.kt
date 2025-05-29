package sample.context

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.stereotype.Component
import sample.util.TimePoint

/** Date and time utility component. */
@Component
class Timestamper(private var day: LocalDate = LocalDate.now()) {

    fun day(): LocalDate = day

    fun date(): LocalDateTime = LocalDateTime.now()

    fun tp(): TimePoint = TimePoint(day(), date())

    fun daySet(day: LocalDate): Timestamper {
        this.day = day
        return this
    }

    // low: T + n calculation method for sample. In fact, it is necessary to
    // consider business days including holidays
    fun dayPlus(i: Int): LocalDate = this.day.plusDays(i.toLong())
}
