package sample.model.asset

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import javax.sql.DataSource
import sample.model.DataFixtures
import sample.model.DomainTester

@DataJdbcTest
@ActiveProfiles("test")
class CashBalanceTest {
    
    @Autowired
    private lateinit var dataSource: DataSource
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcAggregateTemplate

    private lateinit var tester: DomainTester

    @BeforeEach
    fun before() {
        tester = DomainTester.create(jdbcTemplate, dataSource)
    }

    @Test
    fun add() {
        tester.tx { rep ->
            var cb = rep.save(DataFixtures.cb(rep.dh(), "test1", LocalDate.of(2014, 11, 18), "USD", "10.02"))

            // 10.02 + 11.51 = 21.53
            cb = cb.add(rep, BigDecimal("11.51"))
            assertEquals(BigDecimal("21.53"), cb.amount.setScale(2))

            // 21.53 + 11.516 = 33.04 (check roundingMode)
            cb = cb.add(rep, BigDecimal("11.516"))
            assertEquals(BigDecimal("33.04"), cb.amount.setScale(2))

            // 33.04 - 41.51 = -8.47 (check minus)
            cb = cb.add(rep, BigDecimal("-41.51"))
            assertEquals(BigDecimal("-8.47"), cb.amount.setScale(2))
        }
    }

    @Test
    fun getOrNew() {
        tester.tx { rep ->
            rep.save(DataFixtures.cb(rep.dh(), "test1", LocalDate.of(2014, 11, 18), "JPY", "1000"))
            rep.save(DataFixtures.cb(rep.dh(), "test2", LocalDate.of(2014, 11, 17), "JPY", "3000"))

            // Check the existing balance.
            val cbNormal = CashBalance.getOrNew(rep, "test1", "JPY")
            assertEquals("test1", cbNormal.accountId)
            assertEquals(LocalDate.of(2014, 11, 18), cbNormal.baseDay)
            assertEquals(BigDecimal("1000"), cbNormal.amount.setScale(0))

            // Carrying forward inspection of the balance that does not exist in a basic
            // date.
            val cbRoll = CashBalance.getOrNew(rep, "test2", "JPY")
            assertEquals("test2", cbRoll.accountId)
            assertEquals(LocalDate.of(2014, 11, 18), cbRoll.baseDay)
            assertEquals(BigDecimal("3000"), cbRoll.amount.setScale(0))

            // Create inspection of the account which does not hold the balance.
            val cbNew = CashBalance.getOrNew(rep, "test3", "JPY")
            assertEquals("test3", cbNew.accountId)
            assertEquals(LocalDate.of(2014, 11, 18), cbNew.baseDay)
            assertEquals(BigDecimal.ZERO, cbNew.amount)
        }
    }
} 