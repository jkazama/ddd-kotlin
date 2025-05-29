package sample.context.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

/**
 * Send and receive mail.
 * low: In the sample, only the interface for sending mail is created. In
 * practice, it
 * also supports receiving emails such as POP3/IMAP.
 */
@Component
class MailHandler {
    
    @Value("\${sample.mail.enabled:true}")
    private var enable: Boolean = true

    companion object {
        private val log = LoggerFactory.getLogger(MailHandler::class.java)
    }

    fun send(mail: SendMail): MailHandler {
        if (!enable) {
            log.info("Sent a dummy email. [${mail.subject}]")
            return this
        }
        // low: There should be a lot of overhead in cooperation with external
        // resources, so it should be done asynchronously.
        // low: Send the contents of the substitution mapping of bodyArgs to body by
        // JavaMail etc.
        return this
    }

    /** Mail sending parameters. */
    data class SendMail(
            val address: String,
            val subject: String,
            val body: String,
            val bodyArgs: Map<String, String>
    )
} 