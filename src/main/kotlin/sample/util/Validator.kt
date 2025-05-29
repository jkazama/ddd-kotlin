package sample.util

import sample.context.ValidationException
import sample.util.Warns.WarnsBuilder

/** Construction concept for validation exceptions. */
class Validator {
    private val builder: WarnsBuilder = Warns.builder()

    /** Stacks a global exception internally if valid is false. */
    fun check(valid: Boolean, message: String): Validator {
        if (!valid) {
            builder.add(message)
        }
        return this
    }

    /** Stacks a field exception internally if valid is false. */
    fun checkField(valid: Boolean, field: String, message: String): Validator {
        if (!valid) {
            builder.addField(field, message)
        }
        return this
    }

    /** Throws a global exception if valid is false. */
    fun verify(valid: Boolean, message: String): Validator = check(valid, message).verify()

    /** Throws a field exception if valid is false. */
    fun verifyField(valid: Boolean, field: String, message: String): Validator =
            checkField(valid, field, message).verify()

    fun verify(): Validator {
        if (hasWarn()) {
            throw ValidationException.of(builder)
        }
        return this
    }

    fun hasWarn(): Boolean = builder.build().hasError()

    companion object {
        fun validate(proc: (Validator) -> Unit) {
            val validator = Validator()
            proc(validator)
            validator.verify()
        }
    }
}
