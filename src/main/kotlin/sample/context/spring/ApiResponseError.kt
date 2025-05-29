package sample.context.spring

import jakarta.validation.ConstraintViolation
import java.util.*
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import sample.context.MessageAccessor
import sample.util.Warns
import sample.util.Warns.WarnsBuilder

/**
 * Represents an API response error.
 *
 * Can include global errors and field errors in ProblemDetail format.
 */
data class ApiResponseError(val problem: ProblemDetail) {

    companion object {
        /**
         * Returns an API response error builder.
         *
         * @param msg message accessor
         * @return response error builder
         */
        fun builder(): ApiResponseErrorBuilder = ApiResponseErrorBuilder()
    }

    /** API response error builder. */
    class ApiResponseErrorBuilder {
        private val warnsBuilder: WarnsBuilder = Warns.builder()

        /** Adds validation exception information. */
        fun addAll(warns: Warns): ApiResponseErrorBuilder {
            warnsBuilder.addAll(warns)
            return this
        }

        /** Adds validation exception information. */
        fun addAllConstraint(errors: Set<ConstraintViolation<*>>): ApiResponseErrorBuilder {
            warnsBuilder.addAllConstraint(errors)
            return this
        }

        /** Adds a global error. */
        fun add(message: String, vararg messageArgs: String): ApiResponseErrorBuilder {
            warnsBuilder.add(message, *messageArgs)
            return this
        }

        /** Adds a global error. */
        fun add(message: String, messageArgs: List<String>): ApiResponseErrorBuilder {
            warnsBuilder.add(message, messageArgs)
            return this
        }

        /** Adds a field error. */
        fun addField(
                field: String,
                message: String,
                vararg messageArgs: String
        ): ApiResponseErrorBuilder {
            warnsBuilder.addField(field, message, *messageArgs)
            return this
        }

        /** Adds a field error. */
        fun addField(
                field: String,
                message: String,
                messageArgs: List<String>
        ): ApiResponseErrorBuilder {
            warnsBuilder.addField(field, message, messageArgs)
            return this
        }

        /** Builds the response error. */
        fun build(msg: MessageAccessor, status: HttpStatus): ApiResponseError =
                build(msg, status, Locale.getDefault())

        /** Builds the response error. */
        fun build(msg: MessageAccessor, status: HttpStatus, locale: Locale): ApiResponseError {
            val warns = warnsBuilder.build()
            val message = msg.load(locale, warns.globalError())
            val problem = ProblemDetail.forStatusAndDetail(status, message)

            if (warns.hasFieldError()) {
                problem.setProperty(
                        "errors",
                        warns.fieldErrors().map { v ->
                            mapOf(
                                    "field" to v.field,
                                    "messageKey" to v.message,
                                    "messageArgs" to v.messageArgs,
                                    "message" to msg.load(locale, v)
                            )
                        }
                )
            }
            return ApiResponseError(problem)
        }
    }
}
