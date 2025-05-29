package sample.usecase.admin

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import sample.context.audit.AuditHandler
import sample.context.lock.IdLockHandler
import sample.context.lock.IdLockType
import sample.context.orm.OrmRepository
import sample.context.orm.TxTemplate
import sample.model.asset.CashInOut
import sample.model.asset.CashInOut.FindCashInOut
import sample.model.asset.Cashflow

/** The use case processing for the asset domain in the organization. */
@Service
class AssetAdminService(
        private val rep: OrmRepository,
        private val txm: PlatformTransactionManager,
        private val audit: AuditHandler,
        private val idLock: IdLockHandler
) {

    companion object {
        private val log = LoggerFactory.getLogger(AssetAdminService::class.java)
    }
    fun findCashInOut(p: FindCashInOut): List<CashInOut> {
        return TxTemplate.of(txm).readOnly().tx { CashInOut.find(rep, p) }
    }

    fun closingCashOut() {
        audit.audit("Closing cash out.") { TxTemplate.of(txm).tx { closingCashOutInTx() } }
    }

    private fun closingCashOutInTx() {
        // low: It is desirable to handle it to an account unit in a mass.
        // low: Divide paging by id sort and carry it out for a difference
        // because heaps overflow when just do it in large quantities.
        CashInOut.findUnprocessed(rep).forEach { cio ->
            idLock.call(cio.accountId, IdLockType.WRITE) {
                try {
                    cio.process(rep)
                } catch (e: Exception) {
                    log.error("[${cio.getId()}] Failure closing cash out.", e)
                    try {
                        cio.error(rep)
                    } catch (ex: Exception) {
                        // low: Keep it for a mention only for logger which is a double obstacle.
                        // (probably DB is caused)
                    }
                }
            }
        }
    }

    /** Reflect the cashflow that reached an account day in the balance. */
    fun realizeCashflow() {
        audit.audit("Realize cashflow.") { TxTemplate.of(txm).tx { realizeCashflowInTx() } }
    }

    private fun realizeCashflowInTx() {
        // low: Expect the practice after the rollover day.
        val day = rep.dh().time().day()
        Cashflow.findDoRealize(rep, day).forEach { cf ->
            idLock.call(cf.accountId, IdLockType.WRITE) {
                try {
                    cf.realize(rep)
                } catch (e: Exception) {
                    log.error("[${cf.getId()}] Failure realize cashflow.", e)
                    try {
                        cf.error(rep)
                    } catch (ex: Exception) {
                        // ignore
                    }
                }
            }
        }
    }
}
