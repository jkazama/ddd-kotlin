package sample.context

import jakarta.validation.ConstraintViolation
import sample.util.Warns
import sample.util.Warns.Warn
import sample.util.Warns.WarnsBuilder

/**
 * Application's validation exception.
 *
 * ValidationException represents validation exceptions that can be recovered from, such as input
 * exceptions or invalid state transition exceptions. The output in the log is performed at WARN
 * level.
 *
 * The validation exception can hold multiple errors in global/field scope. When you handle multiple
 * validation errors, please initialize it using Warns.
 */
class ValidationException(
        /** Use this when notifying multiple validation exceptions. */
        val warns: Warns
) : RuntimeException(warns.globalError().message) {

        companion object {
                fun of(warn: Warn): ValidationException =
                        ValidationException(Warns.builder().add(warn).build())

                fun of(warns: Warns): ValidationException = ValidationException(warns)

                fun of(warnsBuilder: WarnsBuilder): ValidationException = of(warnsBuilder.build())

                fun of(errors: Set<ConstraintViolation<Any>>): ValidationException {
                        val builder = Warns.builder()
                        errors.forEach { v ->
                                builder.addField(v.propertyPath.toString(), v.message)
                        }
                        return of(builder)
                }

                fun of(message: String, vararg messageArgs: String): ValidationException =
                        of(Warns.builder().add(message, *messageArgs).build())

                fun of(message: String, messageArgs: List<String>): ValidationException =
                        of(Warns.builder().add(message, messageArgs).build())

                fun of(message: String): ValidationException =
                        of(Warns.builder().add(message).build())

                fun ofField(
                        field: String,
                        message: String,
                        vararg messageArgs: String
                ): ValidationException =
                        of(Warns.builder().addField(field, message, *messageArgs).build())

                fun ofField(
                        field: String,
                        message: String,
                        messageArgs: List<String>
                ): ValidationException =
                        of(Warns.builder().addField(field, message, messageArgs).build())
        }
}
