package sample.usecase.mail

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import sample.context.InvocationException
import sample.context.mail.MailHandler
import sample.context.mail.MailHandler.SendMail
import sample.context.orm.OrmRepository
import sample.context.orm.TxTemplate
import sample.model.account.Account
import sample.model.asset.CashInOut
import sample.usecase.event.AppMailEvent
import sample.usecase.event.type.AppMailType

/**
 * Mail delivery service of the application layer. <p> Manages transactions independently, please be
 * careful not to call it within a service transaction.
 */
@Component
class ServiceMailDeliver(
        private val rep: OrmRepository,
        private val tx: PlatformTransactionManager,
        private val mail: MailHandler
) {

    @EventListener(AppMailEvent::class)
    fun handleEvent(event: AppMailEvent<*>) {
        val mailType = event.mailType
        when (mailType) {
            AppMailType.FINISH_REQUEST_WITHDRAW ->
                    sendFinishRequestWithdraw(event.value as CashInOut)
        }
    }

    fun sendFinishRequestWithdraw(cio: CashInOut) {
        send(cio.accountId) { account ->
            // low: Actual title and text are acquired from setting information
            val subject = "[${cio.getId()}] Notification of withdrawal request acceptance"
            val body = "{name} …"
            val bodyArgs = mapOf("name" to account.name)
            SendMail(account.mail, subject, body, bodyArgs)
        }
    }

    private fun send(accountId: String, creator: ServiceMailCreator) {
        TxTemplate.of(tx).tx {
            try {
                mail.send(creator.create(Account.load(rep, accountId)))
            } catch (e: RuntimeException) {
                throw e
            } catch (e: Exception) {
                throw InvocationException("errors.MailException", e)
            }
        }
    }

    fun interface ServiceMailCreator {
        fun create(account: Account): SendMail
    }
}
