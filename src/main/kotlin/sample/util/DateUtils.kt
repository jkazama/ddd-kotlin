package sample.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalQuery
import java.util.*
import org.springframework.util.StringUtils

/** Support Utils for sample. Please use libraries such as commons-lang in actual projects. */
object DateUtils {

        private val weekendQuery = WeekendQuery()

        fun day(dayStr: String?): LocalDate? = dayOpt(dayStr).orElse(null)

        fun day(dateStr: String?, formatter: DateTimeFormatter): LocalDate? =
                dayOpt(dateStr, formatter).orElse(null)

        fun day(dateStr: String?, format: String): LocalDate? =
                day(dateStr, DateTimeFormatter.ofPattern(format))

        fun dayOpt(dayStr: String?): Optional<LocalDate> {
                if (!StringUtils.hasText(dayStr)) {
                        return Optional.empty()
                }
                return Optional.of(
                        LocalDate.parse(dayStr!!.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
                )
        }

        fun dayOpt(dateStr: String?, formatter: DateTimeFormatter): Optional<LocalDate> {
                if (!StringUtils.hasText(dateStr)) {
                        return Optional.empty()
                }
                return Optional.of(LocalDate.parse(dateStr!!.trim(), formatter))
        }

        fun dayOpt(dateStr: String?, format: String): Optional<LocalDate> =
                dayOpt(dateStr, DateTimeFormatter.ofPattern(format))

        fun date(date: Date): LocalDateTime =
                LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())

        fun date(date: LocalDateTime): Date =
                Date.from(date.atZone(ZoneId.systemDefault()).toInstant())

        fun date(dateStr: String?, formatter: DateTimeFormatter): LocalDateTime? =
                dateOpt(dateStr, formatter).orElse(null)

        fun date(dateStr: String?, format: String): LocalDateTime? =
                date(dateStr, DateTimeFormatter.ofPattern(format))

        fun dateOpt(date: Date?): Optional<LocalDateTime> =
                if (date == null) Optional.empty() else Optional.of(date(date))

        fun dateOpt(date: LocalDateTime?): Optional<Date> =
                if (date == null) Optional.empty() else Optional.of(date(date))

        fun dateOpt(dateStr: String?, formatter: DateTimeFormatter): Optional<LocalDateTime> {
                if (!StringUtils.hasText(dateStr)) {
                        return Optional.empty()
                }
                return Optional.of(LocalDateTime.parse(dateStr!!.trim(), formatter))
        }

        fun dateOpt(dateStr: String?, format: String): Optional<LocalDateTime> =
                dateOpt(dateStr, DateTimeFormatter.ofPattern(format))

        fun dateByDay(day: LocalDate?): LocalDateTime? = dateByDayOpt(day).orElse(null)

        fun dateByDayOpt(day: LocalDate?): Optional<LocalDateTime> =
                Optional.ofNullable(day).map { it.atStartOfDay() }

        fun dateTo(day: LocalDate?): LocalDateTime? = dateToOpt(day).orElse(null)

        fun dateToOpt(day: LocalDate?): Optional<LocalDateTime> =
                Optional.ofNullable(day).map { it.atTime(23, 59, 59) }

        fun dayFormat(day: LocalDate?): String? = dayFormatOpt(day).orElse(null)

        fun dayFormatOpt(day: LocalDate?): Optional<String> =
                Optional.ofNullable(day).map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) }

        fun dateFormat(date: LocalDateTime?, formatter: DateTimeFormatter): String? =
                dateFormatOpt(date, formatter).orElse(null)

        fun dateFormatOpt(date: LocalDateTime?, formatter: DateTimeFormatter): Optional<String> =
                Optional.ofNullable(date).map { it.format(formatter) }

        fun dateFormat(date: LocalDateTime?, format: String): String? =
                dateFormatOpt(date, format).orElse(null)

        fun dateFormatOpt(date: LocalDateTime?, format: String): Optional<String> =
                Optional.ofNullable(date).map { it.format(DateTimeFormatter.ofPattern(format)) }

        fun between(start: LocalDate?, end: LocalDate?): Optional<Period> =
                if (start == null || end == null) Optional.empty()
                else Optional.of(Period.between(start, end))

        fun between(start: LocalDateTime?, end: LocalDateTime?): Optional<Duration> =
                if (start == null || end == null) Optional.empty()
                else Optional.of(Duration.between(start, end))

        fun isWeekend(day: LocalDate): Boolean {
                return day.query(weekendQuery)
        }

        fun dayTo(year: Int): LocalDate =
                LocalDate.ofYearDay(year, if (Year.of(year).isLeap) 366 else 365)

        class WeekendQuery : TemporalQuery<Boolean> {
                override fun queryFrom(temporal: TemporalAccessor): Boolean {
                        val dayOfWeek = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK))
                        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
                }
        }
}
