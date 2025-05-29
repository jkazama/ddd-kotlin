package sample.context.orm

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

/** Transaction template for managing database transactions. */
class TxTemplate private constructor(private val transactionManager: PlatformTransactionManager) {

    private var readOnly: Boolean = false

    companion object {
        fun of(transactionManager: PlatformTransactionManager): TxTemplate {
            return TxTemplate(transactionManager)
        }
    }

    fun readOnly(): TxTemplate {
        this.readOnly = true
        return this
    }

    fun <T> tx(action: () -> T): T {
        val template = TransactionTemplate(transactionManager)
        template.isReadOnly = readOnly
        template.isolationLevel = TransactionDefinition.ISOLATION_READ_COMMITTED

        return template.execute { action() }
                ?: throw IllegalStateException("Transaction returned null")
    }
}
