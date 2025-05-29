package sample.model.constraints

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.NotNull
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
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
@Constraint(validatedBy = [])
@ReportAsSingleViolation
@NotNull
@DateTimeFormat(iso = ISO.DATE)
annotation class ISODate(
        val message: String = "{error.domain.ISODate}",
        val groups: Array<kotlin.reflect.KClass<*>> = [],
        val payload: Array<kotlin.reflect.KClass<out Payload>> = []
)
