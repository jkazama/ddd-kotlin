package sample.model.asset

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
class AssetTest {
    
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
    fun canWithdraw() {
        tester.tx { rep ->
            // 10000 + (1000 - 2000) - 8000 = 1000
            rep.save(DataFixtures.acc("test"))
            rep.save(DataFixtures.cb(rep.dh(), "test", LocalDate.of(2014, 11, 18), "JPY", "10000"))
            rep.save(DataFixtures.cf(rep.dh(), "test", "1000", LocalDate.of(2014, 11, 18), LocalDate.of(2014, 11, 20)))
            rep.save(DataFixtures.cf(rep.dh(), "test", "-2000", LocalDate.of(2014, 11, 19), LocalDate.of(2014, 11, 21)))
            rep.save(DataFixtures.cio("1", "test", "8000", true, rep.dh().time().tp()))

            assertTrue(
                Asset.of("test").canWithdraw(rep, "JPY", BigDecimal("1000"), LocalDate.of(2014, 11, 21))
            )
            assertFalse(
                Asset.of("test").canWithdraw(rep, "JPY", BigDecimal("1001"), LocalDate.of(2014, 11, 21))
            )
        }
    }
} 