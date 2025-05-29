package sample.usecase.event

import org.springframework.context.ApplicationEvent
import sample.usecase.event.type.AppMailType

/** Represents a mail delivery event. */
data class AppMailEvent<T>(val mailType: AppMailType, val value: T) :
        ApplicationEvent(value as Any) {

    companion object {
        fun <T> of(mailType: AppMailType, value: T): AppMailEvent<T> =
                AppMailEvent(mailType = mailType, value = value)
    }
}
