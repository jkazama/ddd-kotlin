package sample.context

import java.util.*
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource
import org.springframework.stereotype.Component
import sample.util.Warns.Warn

/** Retrieves strings from message resources based on keys. */
interface MessageAccessor {

    /**
     * Retrieves a message with the default locale.
     *
     * Returns the key as-is when no matching key is found.
     *
     * @param messageKey message key
     * @param messageArgs message arguments
     * @return message
     */
    fun load(messageKey: String, vararg messageArgs: String): String =
            load(Locale.getDefault(), messageKey, *messageArgs)

    /**
     * Retrieves a message with the specified locale.
     *
     * Returns the key as-is when no matching key is found.
     *
     * @param locale locale
     * @param messageKey message key
     * @param messageArgs message arguments
     * @return message
     */
    fun load(locale: Locale, messageKey: String, vararg messageArgs: String): String =
            load(locale, messageKey, messageArgs.toList())

    /**
     * Retrieves a message with the default locale.
     *
     * Returns the key as-is when no matching key is found.
     *
     * @param messageKey message key
     * @param messageArgs message arguments
     * @return message
     */
    fun load(messageKey: String, messageArgs: List<String>): String =
            load(Locale.getDefault(), messageKey, messageArgs)

    /**
     * Retrieves a message with the specified locale.
     *
     * Returns the key as-is when no matching key is found.
     *
     * @param locale locale
     * @param messageKey message key
     * @param messageArgs message arguments
     * @return message
     */
    fun load(locale: Locale, messageKey: String, messageArgs: List<String>): String

    /**
     * Retrieves the message for the specified validation exception.
     *
     * Returns the validation exception message as-is when no message exists for the validation
     * exception.
     *
     * @param locale locale
     * @param warn validation exception
     * @return message
     */
    fun load(locale: Locale, warn: Warn): String

    /**
     * Retrieves the message for the specified validation exception.
     *
     * Returns the validation exception message as-is when no message exists for the validation
     * exception.
     *
     * @param warn validation exception
     * @return message
     */
    fun load(warn: Warn): String = load(Locale.getDefault(), warn)

    /** Standard implementation of MessageAccessor. */
    @Component
    class MessageAccessorImpl(private val msg: MessageSource) : MessageAccessor {

        /** {@inheritDoc} */
        override fun load(locale: Locale, messageKey: String, messageArgs: List<String>): String =
                msg.getMessage(messageKey, messageArgs.toTypedArray(), messageKey, locale)!!

        /** {@inheritDoc} */
        override fun load(locale: Locale, warn: Warn): String =
                load(locale, warn.message, warn.messageArgs)

        companion object {
            fun of(msg: MessageSource): MessageAccessorImpl = MessageAccessorImpl(msg)
        }
    }

    /** Mock implementation of MessageAccessor. */
    class MessageAccessorMock : MessageAccessor {
        private val msgOrigin = StaticMessageSource()

        /** {@inheritDoc} */
        override fun load(locale: Locale, messageKey: String, messageArgs: List<String>): String =
                msg().load(locale, messageKey, messageArgs)

        private fun msg(): MessageAccessor = MessageAccessorImpl.of(msgOrigin)

        /** {@inheritDoc} */
        override fun load(locale: Locale, warn: Warn): String = msg().load(locale, warn)

        fun put(messageKey: String, message: String): MessageAccessorMock {
            msgOrigin.addMessage(messageKey, Locale.getDefault(), message)
            return this
        }

        fun put(messageKey: String, message: String, locale: Locale): MessageAccessorMock {
            msgOrigin.addMessage(messageKey, locale, message)
            return this
        }
    }
}
