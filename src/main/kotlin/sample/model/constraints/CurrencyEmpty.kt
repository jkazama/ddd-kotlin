package sample.model.constraints

import jakarta.validation.Constraint
import jakarta.validation.OverridesAttribute
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
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
@Size
@Pattern(regexp = "")
annotation class CurrencyEmpty(
        val message: String = "{error.domain.currency}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
        @get:OverridesAttribute(constraint = Size::class, name = "max") val max: Int = 3,
        @get:OverridesAttribute(constraint = Pattern::class, name = "regexp")
        val regexp: String = "^[a-zA-Z]{3}$"
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
    annotation class List(val value: Array<CurrencyEmpty>)
}
