package sample.context.audit

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import sample.context.InvocationException
import sample.context.ValidationException
import sample.context.actor.Actor
import sample.context.actor.ActorSession

/**
 * Handles user auditing and EDP audit (scheduled batch processing or daily batch processing). <p>
 * When you expect implicit application behavior, please consider integration with AOP. <p> The
 * target log is written to both Logger and the audit table of the system schema. (You can detect
 * unresponsive states by using separate transactions for start and completion.)
 */
@Component
class AuditHandler {

    companion object {
        val loggerActor: Logger = LoggerFactory.getLogger("Audit.Actor")
        val loggerEvent: Logger = LoggerFactory.getLogger("Audit.Event")
    }

    fun <T> audit(message: String, callable: () -> T): T {
        this.logger().trace(message(message, "[Start]", null))
        val start = System.currentTimeMillis()
        return try {
            val v = callable()
            this.logger().info(message(message, "[ End ]", start))
            v
        } catch (e: ValidationException) {
            this.logger().warn(message(message, "[Warning]", start))
            throw e
        } catch (e: RuntimeException) {
            this.logger().error(message(message, "[Exception]", start))
            throw e
        } catch (e: Exception) {
            this.logger().error(message(message, "[Exception]", start))
            throw InvocationException("error.Exception", e)
        }
    }

    private fun logger(): Logger =
            if (ActorSession.actor().roleType.isSystem()) loggerEvent else loggerActor

    private fun message(message: String, prefix: String, startMillis: Long?): String {
        val actor: Actor = ActorSession.actor()
        val sb = StringBuilder("$prefix ")
        when {
            actor.roleType.isAnonymous() -> sb.append("[${actor.source}] ")
            actor.roleType.notSystem() -> sb.append("[${actor.id}] ")
        }
        sb.append(message)
        if (startMillis != null) {
            sb.append(" [${System.currentTimeMillis() - startMillis}ms]")
        }
        return sb.toString()
    }
}
