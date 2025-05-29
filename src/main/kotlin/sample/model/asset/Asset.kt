package sample.model.asset

import java.math.BigDecimal
import java.time.LocalDate
import sample.context.orm.OrmRepository
import sample.util.Calculator

/** The asset of the account. */
data class Asset(
        /** account ID */
        val id: String
) {

    fun canWithdraw(
            rep: OrmRepository,
            currency: String,
            absAmount: BigDecimal,
            valueDay: LocalDate
    ): Boolean {
        val calc = Calculator.init(CashBalance.getOrNew(rep, id, currency).amount)
        Cashflow.findUnrealize(rep, id, currency, valueDay).map { it.amount }.forEach {
            calc.add(it)
        }
        CashInOut.findUnprocessed(rep, id, currency, true).map { it.absAmount.negate() }.forEach {
            calc.add(it)
        }
        calc.add(absAmount.negate())
        return 0 <= calc.decimal().signum()
    }

    companion object {
        fun of(accountId: String): Asset = Asset(id = accountId)
    }
}
