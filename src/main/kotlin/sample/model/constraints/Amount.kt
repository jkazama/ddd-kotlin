package sample.model.constraints

import jakarta.validation.Constraint
import jakarta.validation.OverridesAttribute
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import kotlin.reflect.KClass

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
@Digits(integer = 16, fraction = 4)
annotation class Amount(
        val message: String = "{error.domain.amount}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
        @get:OverridesAttribute(constraint = Digits::class, name = "integer") val integer: Int = 16,
        @get:OverridesAttribute(constraint = Digits::class, name = "fraction") val fraction: Int = 4
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
    annotation class List(val value: Array<Amount>)
}
