package sample.model.constraints

import jakarta.validation.Constraint
import jakarta.validation.OverridesAttribute
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.NotNull
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
@NotNull
@Size
@Pattern(regexp = "")
annotation class Category(
        val message: String = "{error.domain.category}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = [],
        @get:OverridesAttribute(constraint = Size::class, name = "max") val max: Int = 30,
        @get:OverridesAttribute(constraint = Pattern::class, name = "regexp")
        val regexp: String = "^\\p{ASCII}*$",
        @get:OverridesAttribute(constraint = Pattern::class, name = "flags")
        val flags: Array<Pattern.Flag> = []
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
    annotation class List(val value: Array<Category>)
}
