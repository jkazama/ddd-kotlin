package sample.context

/**
 * Application's runtime exception.
 *
 * use it for the purpose of wrapping the system exception that cannot restore.
 */
class InvocationException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
}
