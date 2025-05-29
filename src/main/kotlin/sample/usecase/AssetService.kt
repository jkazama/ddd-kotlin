package sample.usecase

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import sample.context.actor.Actor
import sample.context.audit.AuditHandler
import sample.context.lock.IdLockHandler
import sample.context.lock.IdLockType
import sample.context.orm.OrmRepository
import sample.context.orm.TxTemplate
import sample.model.asset.CashInOut
import sample.model.asset.CashInOut.RegCashOut
import sample.usecase.event.AppMailEvent
import sample.usecase.event.type.AppMailType

/** The customer use case processing for the asset domain. */
@Service
class AssetService(
        private val rep: OrmRepository,
        private val txm: PlatformTransactionManager,
        private val audit: AuditHandler,
        private val idLock: IdLockHandler,
        private val event: ApplicationEventPublisher
) {

    fun findUnprocessedCashOut(): List<CashInOut> {
        val accId = actor().id
        return idLock.call(accId, IdLockType.READ) {
            TxTemplate.of(txm).readOnly().tx { CashInOut.findUnprocessed(rep, accId) }
        }
    }

    private fun actor(): Actor = rep.dh().actor()

    fun withdraw(p: RegCashOut): String =
            audit.audit("Requesting a withdrawal") {
                // low: Take account ID lock (WRITE) and transaction and handle transfer
                val cio =
                        idLock.call(actor().id, IdLockType.WRITE) {
                            TxTemplate.of(txm).tx { CashInOut.withdraw(rep, p) }
                        }
                // low: this service e-mail it and notify user.
                this.event.publishEvent(AppMailEvent.of(AppMailType.FINISH_REQUEST_WITHDRAW, cio))
                cio.id
            }
}
