package sample.model.asset

import java.math.BigDecimal
import java.time.LocalDate
import javax.sql.DataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.test.context.ActiveProfiles
import sample.ActionStatusType
import sample.context.ValidationException
import sample.model.DataFixtures
import sample.model.DomainErrorKeys
import sample.model.DomainTester

@DataJdbcTest
@ActiveProfiles("test")
class CashflowTest {

    @Autowired private lateinit var dataSource: DataSource

    @Autowired private lateinit var jdbcTemplate: JdbcAggregateTemplate

    private lateinit var tester: DomainTester

    @BeforeEach
    fun before() {
        tester = DomainTester.create(jdbcTemplate, dataSource)
    }

    @Test
    fun register() {
        tester.tx { rep ->
            // It is cashflow outbreak by the delivery of the past date.
            // [ValidationException]
            try {
                Cashflow.register(
                        rep,
                        DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 17))
                )
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(
                        "error.Cashflow.beforeEqualsDay",
                        e.warns.fieldError("valueDay")?.message
                )
            }
            // Cashflow occurs by delivery the next day.
            val cf =
                    Cashflow.register(
                            rep,
                            DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 19))
                    )
            assertEquals(BigDecimal("1000"), cf.amount.setScale(0))
            assertEquals(ActionStatusType.UNPROCESSED, cf.statusType)
            assertEquals(LocalDate.of(2014, 11, 18), cf.eventDay)
            assertEquals(LocalDate.of(2014, 11, 19), cf.valueDay)
        }
    }

    @Test
    fun realize() {
        tester.tx { rep ->
            CashBalance.getOrNew(rep, "test1", "JPY")

            // Value day of non-arrival. [ValidationException]
            val cfFuture =
                    rep.save(
                            DataFixtures.cf(
                                    rep.dh(),
                                    "test1",
                                    "1000",
                                    LocalDate.of(2014, 11, 18),
                                    LocalDate.of(2014, 11, 19)
                            )
                    )
            try {
                cfFuture.realize(rep)
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(AssetErrorKeys.CF_REALIZE_DAY, e.message)
            }

            // Balance reflection inspection of the cashflow. 0 + 1000 = 1000
            val cfNormal =
                    rep.save(
                            DataFixtures.cf(
                                    rep.dh(),
                                    "test1",
                                    "1000",
                                    LocalDate.of(2014, 11, 17),
                                    LocalDate.of(2014, 11, 18)
                            )
                    )
            val cfNormalRealized = cfNormal.realize(rep)
            assertEquals(ActionStatusType.PROCESSED, cfNormalRealized.statusType)
            assertEquals(
                    BigDecimal("1000"),
                    CashBalance.getOrNew(rep, "test1", "JPY").amount.setScale(0)
            )

            // Re-realization of the treated cashflow. [ValidationException]
            try {
                cfNormalRealized.realize(rep)
                fail<Unit>()
            } catch (e: ValidationException) {
                assertEquals(DomainErrorKeys.STATUS_PROCESSING, e.message)
            }

            // Balance reflection inspection of the other day cashflow. 1000 + 2000 = 3000
            val cfPast =
                    rep.save(
                            DataFixtures.cf(
                                    rep.dh(),
                                    "test1",
                                    "2000",
                                    LocalDate.of(2014, 11, 16),
                                    LocalDate.of(2014, 11, 17)
                            )
                    )
            assertEquals(ActionStatusType.PROCESSED, cfPast.realize(rep).statusType)
            assertEquals(
                    BigDecimal("3000"),
                    CashBalance.getOrNew(rep, "test1", "JPY").amount.setScale(0)
            )
        }
    }

    @Test
    fun registerWithRealize() {
        tester.tx { rep ->
            CashBalance.getOrNew(rep, "test1", "JPY")
            // Cashflow is realized immediately
            Cashflow.register(rep, DataFixtures.cfReg("test1", "1000", LocalDate.of(2014, 11, 18)))
            assertEquals(
                    BigDecimal("1000"),
                    CashBalance.getOrNew(rep, "test1", "JPY").amount.setScale(0)
            )
        }
    }
}
