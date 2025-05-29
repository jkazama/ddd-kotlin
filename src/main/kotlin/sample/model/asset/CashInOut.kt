package sample.model.asset

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.mapping.Table
import sample.ActionStatusType
import sample.context.DomainEntity
import sample.context.Dto
import sample.context.orm.OrmRepository
import sample.model.DomainErrorKeys
import sample.model.account.FiAccount
import sample.model.asset.Cashflow.CashflowType
import sample.model.asset.Cashflow.RegCashflow
import sample.model.constraints.AbsAmount
import sample.model.constraints.AccountId
import sample.model.constraints.Currency
import sample.model.constraints.CurrencyEmpty
import sample.model.constraints.ISODate
import sample.model.constraints.ISODateTime
import sample.model.constraints.IdStr
import sample.model.master.SelfFiAccount
import sample.util.TimePoint
import sample.util.Validator

/**
 * Cashflow action to ask for a transfer account activity. low: It is a sample, a branch and a name,
 * and considerably originally omit required information.
 */
@Table("CASH_IN_OUT")
data class CashInOut(
        @Id @IdStr val id: String,
        @AccountId val accountId: String,
        @Currency val currency: String,
        @AbsAmount val absAmount: BigDecimal,
        val withdrawal: Boolean,
        @ISODate val requestDay: LocalDate,
        @ISODateTime val requestDate: LocalDateTime,
        @ISODate val eventDay: LocalDate,
        @ISODate val valueDay: LocalDate,
        @IdStr val targetFiCode: String,
        @AccountId val targetFiAccountId: String,
        @IdStr val selfFiCode: String,
        @AccountId val selfFiAccountId: String,
        @NotNull val statusType: ActionStatusType,
        @AccountId val updateActor: String,
        @ISODateTime val updateDate: LocalDateTime,
        /** Set only with a processed status. */
        val cashflowId: String?
) : DomainEntity {

    override fun getId(): Any = id

    /** Processed status. <p> Processed CashInOut and generate Cashflow. */
    fun process(rep: OrmRepository): CashInOut {
        val now = rep.dh().time().tp()
        Validator.validate { v ->
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING)
            v.verify(now.afterEqualsDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY)
        }

        return rep.update(
                this.copy(
                        statusType = ActionStatusType.PROCESSED,
                        updateActor = rep.dh().actor().id,
                        updateDate = now.date,
                        cashflowId = Cashflow.register(rep, regCf()).id
                )
        )
    }

    private fun regCf(): RegCashflow {
        val amount = if (withdrawal) absAmount.negate() else absAmount
        val cashflowType = if (withdrawal) CashflowType.CASH_OUT else CashflowType.CASH_IN
        val remark = if (withdrawal) Remarks.CASH_OUT else Remarks.CASH_IN
        return RegCashflow(
                accountId = accountId,
                currency = currency,
                amount = amount,
                cashflowType = cashflowType,
                remark = remark,
                eventDay = eventDay,
                valueDay = valueDay
        )
    }

    /** Cancelled status. */
    fun cancel(rep: OrmRepository): CashInOut {
        val now = rep.dh().time().tp()
        Validator.validate { v ->
            v.verify(statusType.isUnprocessing(), DomainErrorKeys.STATUS_PROCESSING)
            v.verify(now.beforeDay(eventDay), AssetErrorKeys.CIO_EVENT_DAY_BEFORE_EQUALS_DAY)
        }

        return rep.update(
                this.copy(
                        statusType = ActionStatusType.CANCELLED,
                        updateActor = rep.dh().actor().id,
                        updateDate = now.date
                )
        )
    }

    /** Mark error status. low: Actually, Take error reasons in an argument and maintain it. */
    fun error(rep: OrmRepository): CashInOut {
        Validator.validate { v ->
            v.verify(statusType.isUnprocessed(), DomainErrorKeys.STATUS_PROCESSING)
        }

        return rep.update(
                this.copy(
                        statusType = ActionStatusType.ERROR,
                        updateActor = rep.dh().actor().id,
                        updateDate = rep.dh().time().date()
                )
        )
    }

    data class FindCashInOut(
            @CurrencyEmpty val currency: String?,
            val statusTypes: Set<ActionStatusType>?,
            @ISODate val updFromDay: LocalDate?,
            @ISODate val updToDay: LocalDate?
    ) : Dto {

        @get:AssertTrue(message = DomainErrorKeys.BEFORE_EQUALS_DAY)
        val isUpdFromDay: Boolean
            get() {
                if (this.updFromDay == null || this.updToDay == null) {
                    return true
                }
                return this.updFromDay.isBefore(this.updToDay) ||
                        this.updFromDay.isEqual(this.updToDay)
            }
    }

    data class RegCashOut(
            @AccountId val accountId: String,
            @Currency val currency: String,
            @AbsAmount val absAmount: BigDecimal
    ) : Dto {

        fun create(
                now: TimePoint,
                id: String,
                eventDay: LocalDate,
                valueDay: LocalDate,
                acc: FiAccount,
                selfAcc: SelfFiAccount,
                updActor: String
        ): CashInOut =
                CashInOut(
                        id = id,
                        accountId = accountId,
                        currency = currency,
                        absAmount = absAmount,
                        withdrawal = true,
                        requestDay = now.day,
                        requestDate = now.date,
                        eventDay = eventDay,
                        valueDay = valueDay,
                        targetFiCode = acc.fiCode,
                        targetFiAccountId = acc.fiAccountId,
                        selfFiCode = selfAcc.fiCode,
                        selfFiAccountId = selfAcc.fiAccountId,
                        statusType = ActionStatusType.UNPROCESSED,
                        updateActor = updActor,
                        updateDate = now.date,
                        cashflowId = null
                )
    }

    companion object {
        fun load(rep: OrmRepository, id: String): CashInOut = rep.load(CashInOut::class.java, id)

        /** Criteria API implementation example */
        fun find(rep: OrmRepository, p: FindCashInOut): List<CashInOut> {
            val sort = Sort.by(Sort.Direction.DESC, "updateDate")
            return rep.tmpl()
                    .find(
                            CashInOut::class.java,
                            { criteria ->
                                var c = criteria
                                if (!p.currency.isNullOrEmpty()) {
                                    c = c.and("currency").`is`(p.currency)
                                }
                                if (!p.statusTypes.isNullOrEmpty()) {
                                    c = c.and("statusType").`in`(p.statusTypes)
                                }
                                if (p.updFromDay != null) {
                                    c = c.and("eventDay").greaterThanOrEquals(p.updFromDay)
                                }
                                if (p.updToDay != null) {
                                    c = c.and("eventDay").lessThanOrEquals(p.updToDay)
                                }
                                c
                            },
                            sort
                    )
        }

        fun findUnprocessed(rep: OrmRepository): List<CashInOut> {
            val sort = Sort.by(Sort.Direction.ASC, "id")
            return rep.tmpl()
                    .find(
                            CashInOut::class.java,
                            { criteria ->
                                criteria.and("eventDay")
                                        .`is`(rep.dh().time().day())
                                        .and("statusType")
                                        .`in`(ActionStatusType.UNPROCESSED_TYPES)
                            },
                            sort
                    )
        }

        fun findUnprocessed(
                rep: OrmRepository,
                accountId: String,
                currency: String,
                withdrawal: Boolean
        ): List<CashInOut> {
            val sort = Sort.by(Sort.Direction.ASC, "id")
            return rep.tmpl()
                    .find(
                            CashInOut::class.java,
                            { criteria ->
                                criteria.and("accountId")
                                        .`is`(accountId)
                                        .and("currency")
                                        .`is`(currency)
                                        .and("withdrawal")
                                        .`is`(withdrawal)
                                        .and("statusType")
                                        .`in`(ActionStatusType.UNPROCESSED_TYPES)
                            },
                            sort
                    )
        }

        fun findUnprocessed(rep: OrmRepository, accountId: String): List<CashInOut> {
            val sort = Sort.by(Sort.Direction.DESC, "updateDate")
            return rep.tmpl()
                    .find(
                            CashInOut::class.java,
                            { criteria ->
                                criteria.and("accountId")
                                        .`is`(accountId)
                                        .and("statusType")
                                        .`in`(ActionStatusType.UNPROCESSED_TYPES)
                            },
                            sort
                    )
        }

        fun withdraw(rep: OrmRepository, p: RegCashOut): CashInOut {
            val dh = rep.dh()
            val now = dh.time().tp()
            // low: It is often managed DB or properties.
            val eventDay = now.day
            // low: T+N calculation that we consider the holiday of each financial
            // institution / currency.
            val valueDay = dh.time().dayPlus(3)

            Validator.validate { v ->
                v.verifyField(
                        dh.actor().id == p.accountId,
                        "accountId",
                        DomainErrorKeys.ENTITY_NOT_FOUND
                )
                v.verifyField(0 < p.absAmount.signum(), "absAmount", "error.domain.AbsAmount.zero")
                val canWithdraw =
                        Asset.of(p.accountId).canWithdraw(rep, p.currency, p.absAmount, valueDay)
                v.verifyField(canWithdraw, "absAmount", AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT)
            }
            val uid = dh.uid().generate(CashInOut::class.java.simpleName)
            val acc = FiAccount.load(rep, p.accountId, Remarks.CASH_OUT, p.currency)
            val selfAcc = SelfFiAccount.load(rep, Remarks.CASH_OUT, p.currency)
            val updateActor = dh.actor().id
            return rep.save(p.create(now, uid, eventDay, valueDay, acc, selfAcc, updateActor))
        }
    }
}
