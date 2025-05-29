package sample.controller

import jakarta.validation.ConstraintViolationException
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import java.util.Optional
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSourceResolvable
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.HttpMediaTypeException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.support.WebExchangeBindException
import sample.context.ErrorKeys
import sample.context.MessageAccessor
import sample.context.ValidationException
import sample.context.actor.ActorSession
import sample.context.spring.ApiResponseError
import sample.util.Warns

/**
 * Exception mapping conversion support for RestController. <p> Inserts exception handling through
 * AOP advice.
 */
@ControllerAdvice(annotations = [RestController::class])
class RestErrorAdvice(private val msg: MessageAccessor) {

    companion object {
        private val log = LoggerFactory.getLogger(RestErrorAdvice::class.java)

        @Suppress("UNCHECKED_CAST")
        fun convert(errors: List<ObjectError>): Warns {
            val builder = Warns.builder()
            errors.forEach { oe ->
                val field =
                        Optional.ofNullable(oe.codes)
                                .filter { codes -> codes.isNotEmpty() }
                                .map { codes -> bindField(codes[0]) }
                                .orElse("")
                val messageArgs =
                        Arrays.stream(oe.arguments)
                                .filter { arg -> arg !is MessageSourceResolvable }
                                .map { it.toString() }
                                .toList()
                var message = oe.defaultMessage
                if (field.contains("typeMismatch")) {
                    message = oe.codes?.get(2)
                }
                builder.addField(field, message ?: "", messageArgs)
            }
            return builder.build()
        }

        fun bindField(field: String?): String =
                Optional.ofNullable(field).map { v -> v.substring(v.indexOf('.') + 1) }.orElse("")
    }

    /** Servlet request binding exception. */
    @ExceptionHandler(ServletRequestBindingException::class)
    fun handleServletRequestBinding(e: ServletRequestBindingException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .add("error.ServletRequestBinding")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    private fun locale(): Locale = ActorSession.actor().locale

    /** Message not readable exception. */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .add("error.HttpMessageNotReadable")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** Media type exception. */
    @ExceptionHandler(HttpMediaTypeException::class)
    fun handleHttpMediaTypeException(e: HttpMediaTypeException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .add("error.HttpMediaTypeException")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** Media type not acceptable exception. */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    fun handleHttpMediaTypeNotAcceptable(e: HttpMediaTypeNotAcceptableException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .add("error.HttpMediaTypeNotAcceptable")
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** BeanValidation(JSR303) constraint exception. */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .addAllConstraint(e.constraintViolations)
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** Controller request binding exception. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .addAll(convert(e.bindingResult))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** Controller request binding exception. */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleExchangeBind(e: WebExchangeBindException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .addAll(convert(e))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    /** Controller request binding exception. */
    @ExceptionHandler(BindException::class)
    fun handleBind(e: BindException): ProblemDetail {
        log.warn(e.message)
        return ApiResponseError.builder()
                .addAll(convert(e))
                .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                .problem
    }

    private fun convert(e: BindingResult): Warns {
        val builder = Warns.builder()
        e.fieldErrors.forEach { err ->
            val messageArgs =
                    Arrays.stream(err.arguments)
                            .filter { arg -> arg !is MessageSourceResolvable }
                            .map { it.toString() }
                            .toList()
            builder.addField(err.field, err.defaultMessage ?: "", messageArgs)
        }
        return builder.build()
    }

    /** Application exception. */
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(e: ValidationException): ProblemDetail =
            ApiResponseError.builder()
                    .addAll(e.warns)
                    .build(this.msg, HttpStatus.BAD_REQUEST, this.locale())
                    .problem

    /**
     * IO exception (Broken pipe by Tomcat is not the server's responsibility, so it is excluded.)
     * <p> If the client side is terminated by a broken pipe, it is handled as a successful
     * response. </p>
     */
    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException): ResponseEntity<ProblemDetail> =
            if (e.message != null && e.message!!.contains("Broken pipe")) {
                log.info("Client-side processing was terminated.")
                ResponseEntity(HttpStatus.OK)
            } else {
                ResponseEntity.internalServerError().body(handleException(e))
            }

    /** Generic exception. */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ProblemDetail {
        log.error("An unexpected exception occurred.", e)
        return ApiResponseError.builder()
                .add(ErrorKeys.Exception)
                .build(this.msg, HttpStatus.INTERNAL_SERVER_ERROR, this.locale())
                .problem
    }
}
