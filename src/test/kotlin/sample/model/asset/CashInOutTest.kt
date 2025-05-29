package sample.model.asset

import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import javax.sql.DataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.test.context.ActiveProfiles
import sample.ActionStatusType
import sample.context.ValidationException
import sample.context.actor.Actor
import sample.context.actor.ActorSession
import sample.model.DataFixtures
import sample.model.DomainErrorKeys
import sample.model.DomainTester
import sample.model.asset.CashInOut.FindCashInOut
import sample.model.asset.CashInOut.RegCashOut
import sample.model.asset.Cashflow.CashflowType

@DataJdbcTest
@ActiveProfiles("test")
class CashInOutTest {

    @Autowired private lateinit var dataSource: DataSource

    @Autowired private lateinit var jdbcTemplate: JdbcAggregateTemplate

    companion object {
        private const val ccy = "JPY"
        private const val accId = "test"
        private val baseDay = LocalDate.of(2014, 11, 18)
    }

    private lateinit var tester: DomainTester

    @BeforeEach
    fun before() {
        tester = DomainTester.create(jdbcTemplate, dataSource)
        tester.txInitializeData { rep ->
            rep.save(DataFixtures.acc(accId))
            rep.save(DataFixtures.fiAcc(rep.dh(), accId, Remarks.CASH_OUT, ccy))
            rep.save(DataFixtures.cb(rep.dh(), accId, baseDay, ccy, "1000"))
        }
    }

    @Test
    fun find() {
        tester.tx { rep ->
            val now = rep.dh().time().tp()
            val cio =
                    DataFixtures.cio("1", accId, "300", true, now)
                            .copy(eventDay = LocalDate.of(2014, 11, 18))
            rep.save(cio)

            assertEquals(
                    1,
                    CashInOut.find(
                                    rep,
                                    findParam(
                                            LocalDate.of(2014, 11, 18),
                                            LocalDate.of(2014, 11, 19)
                                    )
                            )
                            .size
            )
            assertEquals(
                    1,
                    CashInOut.find(
                                    rep,
                                    findParam(
                                            LocalDate.of(2014, 11, 18),
                                            LocalDate.of(2014, 11, 19),
                                            ActionStatusType.UNPROCESSED
                                    )
                            )
                            .size
            )
            assertTrue(
                    CashInOut.find(
                                    rep,
                                    findParam(
                                            LocalDate.of(2014, 11, 18),
                                            LocalDate.of(2014, 11, 19),
                                            ActionStatusType.PROCESSED
                                    )
                            )
                            .isEmpty()
            )
            assertTrue(
                    CashInOut.find(
                                    rep,
                                    findParam(
                                            LocalDate.of(2014, 11, 19),
                                            LocalDate.of(2014, 11, 20),
                                            ActionStatusType.UNPROCESSED
                                    )
                            )
                            .isEmpty()
            )
        }
    }

    private fun findParam(
            fromDay: LocalDate,
            toDay: LocalDate,
            vararg statusTypes: ActionStatusType?
    ): FindCashInOut {
        return FindCashInOut(
                currency = ccy,
                statusTypes =
                        if (statusTypes.isNotEmpty()) statusTypes.filterNotNull().toSet() else null,
                updFromDay = fromDay,
                updToDay = toDay
        )
    }

    @Test
    fun withdrawal() {
        tester.tx { rep ->
            ActorSession.bind(
                    Actor(
                            id = accId,
                            name = accId,
                            roleType = sample.context.actor.ActorRoleType.USER,
                            locale = Locale.getDefault()
                    )
            )

            try {
                CashInOut.withdraw(rep, RegCashOut(accId, ccy, BigDecimal("1001")))
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(
                        AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT,
                        e.warns.fieldError("absAmount")?.message
                )
            }

            try {
                CashInOut.withdraw(rep, RegCashOut(accId, ccy, BigDecimal.ZERO))
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(
                        "error.domain.AbsAmount.zero",
                        e.warns.fieldError("absAmount")?.message
                )
            }

            val normal = CashInOut.withdraw(rep, RegCashOut(accId, ccy, BigDecimal("300")))
            assertEquals(accId, normal.accountId)
            assertEquals(ccy, normal.currency)
            assertEquals(BigDecimal("300"), normal.absAmount.setScale(0))
            assertTrue(normal.withdrawal)
            assertEquals(baseDay, normal.requestDay)
            assertEquals(baseDay, normal.eventDay)
            assertEquals(LocalDate.of(2014, 11, 21), normal.valueDay)
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.targetFiCode)
            assertEquals("FI" + accId, normal.targetFiAccountId)
            assertEquals(Remarks.CASH_OUT + "-" + ccy, normal.selfFiCode)
            assertEquals("xxxxxx", normal.selfFiAccountId)
            assertEquals(ActionStatusType.UNPROCESSED, normal.statusType)
            assertNull(normal.cashflowId)

            // Withdrawal request considering restricted amount [Exception]
            try {
                CashInOut.withdraw(rep, RegCashOut(accId, ccy, BigDecimal("701")))
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(
                        AssetErrorKeys.CIO_WITHDRAWAL_AMOUNT,
                        e.warns.fieldError("absAmount")?.message
                )
            }
        }
    }

    @Test
    fun cancel() {
        tester.tx { rep ->
            val tp = rep.dh().time().tp()
            // Cancel a request of the CF having not yet processed
            val normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp))
            assertEquals(ActionStatusType.CANCELLED, normal.cancel(rep).statusType)

            // When Reach an event day, I cannot cancel it. [ValidationException]
            val today =
                    DataFixtures.cio("2", accId, "300", true, tp)
                            .copy(eventDay = LocalDate.of(2014, 11, 18))
            rep.save(today)
            try {
                today.cancel(rep)
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_BEFORE_EQUALS_DAY, e.message)
            }
        }
    }

    @Test
    fun error() {
        tester.tx { rep ->
            val tp = rep.dh().time().tp()
            val normal = rep.save(DataFixtures.cio("1", accId, "300", true, tp))
            val errored = normal.error(rep)
            assertEquals(ActionStatusType.ERROR, errored.statusType)

            // When it is processed, an error cannot do it. [ValidationException]
            val today =
                    DataFixtures.cio("2", accId, "300", true, tp)
                            .copy(eventDay = LocalDate.of(2014, 11, 18))
                            .copy(statusType = ActionStatusType.PROCESSED)
            rep.save(today)
            try {
                today.error(rep)
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.message)
            }
        }
    }

    @Test
    fun process() {
        tester.tx { rep ->
            val tp = rep.dh().time().tp()
            // It is handled non-arrival on an event day [ValidationException]
            val future = rep.save(DataFixtures.cio("1", accId, "300", true, tp))
            try {
                future.process(rep)
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(AssetErrorKeys.CIO_EVENT_DAY_AFTER_EQUALS_DAY, e.message)
            }

            // Event day arrival processing.
            val normal =
                    DataFixtures.cio("2", accId, "300", true, tp)
                            .copy(eventDay = LocalDate.of(2014, 11, 18))
            rep.save(normal)
            val processed = normal.process(rep)
            assertEquals(ActionStatusType.PROCESSED, processed.statusType)
            assertNotNull(processed.cashflowId)

            // Check the Cashflow that CashInOut produced.
            val cf = Cashflow.load(rep, processed.cashflowId!!)
            assertEquals(accId, cf.accountId)
            assertEquals(ccy, cf.currency)
            assertEquals(BigDecimal("-300"), cf.amount.setScale(0))
            assertEquals(CashflowType.CASH_OUT, cf.cashflowType)
            assertEquals(Remarks.CASH_OUT, cf.remark)
            assertEquals(LocalDate.of(2014, 11, 18), cf.eventDay)
            assertEquals(LocalDate.of(2014, 11, 21), cf.valueDay)
            assertEquals(ActionStatusType.UNPROCESSED, cf.statusType)
        }
    }
}
