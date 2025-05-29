package sample.model

import java.math.BigDecimal
import java.time.LocalDate
import sample.ActionStatusType
import sample.context.DomainHelper
import sample.model.account.Account
import sample.model.account.Account.AccountStatusType
import sample.model.account.FiAccount
import sample.model.asset.CashBalance
import sample.model.asset.CashInOut
import sample.model.asset.Cashflow
import sample.model.asset.Cashflow.CashflowType
import sample.model.asset.Cashflow.RegCashflow
import sample.model.master.SelfFiAccount
import sample.util.TimePoint

/**
 * A support component for the data generation. <p> It is aimed for master data generation at the
 * time of a test and the development, Please do not use it in the production.
 */
object DataFixtures {

    // account

    fun acc(id: String): Account {
        return Account(
                id = id,
                name = id,
                mail = "hoge@example.com",
                statusType = AccountStatusType.NORMAL
        )
    }

    fun fiAcc(
            helper: DomainHelper,
            accountId: String,
            category: String,
            currency: String
    ): FiAccount {
        return FiAccount(
                id = helper.uid().generate(FiAccount::class.java.simpleName),
                accountId = accountId,
                category = category,
                currency = currency,
                fiCode = "$category-$currency",
                fiAccountId = "FI$accountId"
        )
    }

    // asset

    fun cb(
            helper: DomainHelper,
            accountId: String,
            baseDay: LocalDate,
            currency: String,
            amount: String
    ): CashBalance {
        val now = helper.time().date()
        return CashBalance(
                id = helper.uid().generate(CashBalance::class.java.simpleName),
                accountId = accountId,
                baseDay = baseDay,
                currency = currency,
                amount = BigDecimal(amount),
                updateDate = now
        )
    }

    fun cf(
            helper: DomainHelper,
            accountId: String,
            amount: String,
            eventDay: LocalDate,
            valueDay: LocalDate
    ): Cashflow {
        val now = helper.time().date()
        return Cashflow(
                id = helper.uid().generate(Cashflow::class.java.simpleName),
                accountId = accountId,
                currency = "JPY",
                amount = BigDecimal(amount),
                cashflowType = CashflowType.CASH_IN,
                remark = "cashIn",
                eventDay = eventDay,
                eventDate = now,
                valueDay = valueDay,
                statusType = ActionStatusType.UNPROCESSED,
                updateActor = "dummy",
                updateDate = now
        )
    }

    fun cfReg(accountId: String, amount: String, valueDay: LocalDate): RegCashflow {
        return RegCashflow(
                accountId = accountId,
                currency = "JPY",
                amount = BigDecimal(amount),
                cashflowType = CashflowType.CASH_IN,
                remark = "cashIn",
                eventDay = null,
                valueDay = valueDay
        )
    }

    // eventDay(T+1) / valueDay(T+3)
    fun cio(
            id: String,
            accountId: String,
            absAmount: String,
            withdrawal: Boolean,
            now: TimePoint
    ): CashInOut {
        return CashInOut(
                id = id,
                accountId = accountId,
                currency = "JPY",
                absAmount = BigDecimal(absAmount),
                withdrawal = withdrawal,
                requestDay = now.day,
                requestDate = now.date,
                eventDay = now.day.plusDays(1),
                valueDay = now.day.plusDays(3),
                targetFiCode = "tFiCode",
                targetFiAccountId = "tFiAccId",
                selfFiCode = "sFiCode",
                selfFiAccountId = "sFiAccId",
                statusType = ActionStatusType.UNPROCESSED,
                updateActor = "dummy",
                updateDate = now.date,
                cashflowId = null
        )
    }

    // master

    fun selfFiAcc(helper: DomainHelper, category: String, currency: String): SelfFiAccount {
        return SelfFiAccount(
                id = helper.uid().generate(SelfFiAccount::class.java.simpleName),
                category = category,
                currency = currency,
                fiCode = "$category-$currency",
                fiAccountId = "xxxxxx"
        )
    }
}
