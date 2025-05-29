package sample.util

import jakarta.validation.ConstraintViolation
import org.springframework.util.StringUtils
import sample.context.ErrorKeys

/** Validation exception information. */
data class Warns(val errors: List<Warn>) {

    /** Returns the global validation exception. */
    fun globalError(): Warn = globalErrorOpt() ?: Warn.ofGlobal(ErrorKeys.Exception)

    /** Returns the global validation exception. */
    fun globalErrorOpt(): Warn? = errors.firstOrNull { it.isGlobal() }

    /** Returns the field validation exception list. */
    fun fieldErrors(): List<Warn> = errors.filter { !it.isGlobal() }

    /** Returns the field validation exception. */
    fun fieldError(field: String): Warn? =
            errors.firstOrNull { !it.isGlobal() && it.field == field }

    /** Returns whether a validation exception exists. */
    fun hasError(): Boolean = errors.isNotEmpty()

    /** Returns whether a field validation exception exists. */
    fun hasFieldError(): Boolean = fieldErrors().isNotEmpty()

    companion object {
        /** Returns the validation exception information builder. */
        fun builder(): WarnsBuilder = WarnsBuilder()
    }

    /** Validation exception information builder. */
    class WarnsBuilder {
        private val warns = mutableListOf<Warn>()

        /** Adds a validation exception message. */
        fun add(message: String, vararg messageArgs: String): WarnsBuilder {
            warns.add(Warn.ofGlobal(message, messageArgs.toList()))
            return this
        }

        /** Adds a validation exception message. */
        fun add(message: String, messageArgs: List<String>): WarnsBuilder {
            warns.add(Warn.ofGlobal(message, messageArgs))
            return this
        }

        /** Adds a validation exception message. */
        fun add(warn: Warn): WarnsBuilder {
            warns.add(warn)
            return this
        }

        /** Adds a validation exception message to the field. */
        fun addField(field: String, message: String, messageArgs: List<String>): WarnsBuilder {
            warns.add(Warn.ofField(field, message, messageArgs))
            return this
        }

        /** Adds a validation exception message to the field. */
        fun addField(field: String, message: String, vararg messageArgs: String): WarnsBuilder {
            warns.add(Warn.ofField(field, message, messageArgs.toList()))
            return this
        }

        /** Adds a validation exception message. */
        fun addConstraint(error: ConstraintViolation<*>): WarnsBuilder =
                addField(error.propertyPath.toString(), error.message)

        /** Adds a validation exception message. */
        fun addAll(errors: List<Warn>): WarnsBuilder {
            warns.addAll(errors)
            return this
        }

        /** Adds a validation exception message. */
        fun addAll(warns: Warns): WarnsBuilder {
            this.warns.addAll(warns.errors)
            return this
        }

        /** Adds a validation exception message. */
        fun addAllConstraint(errors: Set<ConstraintViolation<*>>): WarnsBuilder {
            errors.forEach { addConstraint(it) }
            return this
        }

        /** Returns the validation exception information. */
        fun build(): Warns = Warns(warns.toList())
    }

    /**
     * Represents a validation exception token in a field scope.
     *
     * If the field is null, it is treated as a global exception.
     */
    data class Warn(
            /** Validation exception field key */
            val field: String? = null,
            /** Validation exception message */
            val message: String,
            /** Validation exception message arguments */
            val messageArgs: List<String> = emptyList()
    ) {

        /** When the field is not associated, it is true for a global exception. */
        fun isGlobal(): Boolean = !StringUtils.hasText(field)

        companion object {
            fun ofGlobal(message: String, vararg messageArgs: String): Warn =
                    ofGlobal(message, messageArgs.toList())

            fun ofGlobal(message: String, messageArgs: List<String>): Warn =
                    Warn(message = message, messageArgs = messageArgs)

            fun ofField(field: String, message: String, vararg messageArgs: String): Warn =
                    ofField(field, message, messageArgs.toList())

            fun ofField(field: String, message: String, messageArgs: List<String>): Warn =
                    Warn(field = field, message = message, messageArgs = messageArgs)
        }
    }
}
