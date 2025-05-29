package sample.model.constraints

import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.format.annotation.DateTimeFormat.ISO

@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.FIELD,
        AnnotationTarget.ANNOTATION_CLASS,
        AnnotationTarget.CONSTRUCTOR,
        AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@DateTimeFormat(iso = ISO.DATE)
annotation class ISODateEmpty(
        val message: String = "{error.domain.ISODate}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
) {
    @Target(
            AnnotationTarget.FUNCTION,
            AnnotationTarget.FIELD,
            AnnotationTarget.ANNOTATION_CLASS,
            AnnotationTarget.CONSTRUCTOR,
            AnnotationTarget.VALUE_PARAMETER
    )
    @Retention(AnnotationRetention.RUNTIME)
    @MustBeDocumented
    annotation class List(val value: Array<ISODateEmpty>)
}
