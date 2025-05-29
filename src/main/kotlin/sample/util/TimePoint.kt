package sample.util

import java.time.LocalDate
import java.time.LocalDateTime
import sample.model.constraints.ISODate
import sample.model.constraints.ISODateTime

/**
 * Pair of a date and the date and time.
 *
 * A business day uses it with the case which is not 0:00.
 */
data class TimePoint(
        @field:ISODate val day: LocalDate,
        @field:ISODateTime val date: LocalDateTime
) {

    /** day == targetDay */
    fun equalsDay(targetDay: LocalDate): Boolean = day == targetDay

    /** day < targetDay */
    fun beforeDay(targetDay: LocalDate): Boolean = day.isBefore(targetDay)

    /** day <= targetDay */
    fun beforeEqualsDay(targetDay: LocalDate): Boolean =
            equalsDay(targetDay) || beforeDay(targetDay)

    /** targetDay < day */
    fun afterDay(targetDay: LocalDate): Boolean = day.isAfter(targetDay)

    /** targetDay <= day */
    fun afterEqualsDay(targetDay: LocalDate): Boolean = equalsDay(targetDay) || afterDay(targetDay)

    companion object {
        fun of(day: LocalDate): TimePoint = TimePoint(day, DateUtils.dateByDay(day)!!)
    }
}
