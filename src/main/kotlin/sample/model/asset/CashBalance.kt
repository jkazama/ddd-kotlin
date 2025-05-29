package sample.model.asset

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.mapping.Table
import sample.context.DomainEntity
import sample.context.orm.OrmRepository
import sample.model.constraints.AccountId
import sample.model.constraints.Amount
import sample.model.constraints.Currency
import sample.model.constraints.ISODate
import sample.model.constraints.ISODateTime
import sample.util.Calculator

/** The account balance. */
@Table("CASH_BALANCE")
data class CashBalance(
        @Id val id: String,
        @AccountId val accountId: String,
        @ISODate val baseDay: LocalDate,
        @Currency val currency: String,
        @Amount val amount: BigDecimal,
        @ISODateTime val updateDate: LocalDateTime
) : DomainEntity {

    override fun getId(): Any = id

    /**
     * low: Use Currency here, but the real number of the currency figures and fraction processing
     * definition are managed with DB or the configuration file.
     */
    fun add(rep: OrmRepository, addAmount: BigDecimal): CashBalance {
        val scale = java.util.Currency.getInstance(currency).defaultFractionDigits
        val mode = RoundingMode.DOWN
        val newAmount = Calculator.init(amount).scale(scale, mode).add(addAmount).decimal()
        return rep.update(this.copy(amount = newAmount, updateDate = rep.dh().time().date()))
    }

    companion object {
        /**
         * Acquire the balance of the designated account. (when I do not exist, acquire it after
         * carrying forward preservation) low: The appropriate consideration and examination of
         * multiple currencies are omitted.
         */
        fun getOrNew(rep: OrmRepository, accountId: String, currency: String): CashBalance {
            val baseDay = rep.dh().time().day()
            val list =
                    rep.tmpl().find(CashBalance::class.java) { criteria ->
                        criteria.and("accountId")
                                .`is`(accountId)
                                .and("currency")
                                .`is`(currency)
                                .and("baseDay")
                                .`is`(baseDay)
                    }

            return if (list.isEmpty()) {
                create(rep, accountId, currency)
            } else {
                list.maxByOrNull { it.baseDay } ?: list[0]
            }
        }

        private fun create(rep: OrmRepository, accountId: String, currency: String): CashBalance {
            val id = rep.dh().uid().generate(CashBalance::class.java.simpleName)
            val now = rep.dh().time().tp()
            val pageable: Pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "baseDay"))
            val list =
                    rep.tmpl()
                            .find(
                                    CashBalance::class.java,
                                    { criteria ->
                                        criteria.and("accountId")
                                                .`is`(accountId)
                                                .and("currency")
                                                .`is`(currency)
                                    },
                                    pageable
                            )
                            .content

            return if (list.isEmpty()) {
                rep.save(
                        CashBalance(
                                id = id,
                                accountId = accountId,
                                baseDay = now.day,
                                currency = currency,
                                amount = BigDecimal.ZERO,
                                updateDate = now.date
                        )
                )
            } else { // roll over
                val prev = list[0]
                rep.save(
                        CashBalance(
                                id = id,
                                accountId = accountId,
                                baseDay = now.day,
                                currency = currency,
                                amount = prev.amount,
                                updateDate = now.date
                        )
                )
            }
        }
    }
}
