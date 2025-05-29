package sample.model.account

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.test.context.ActiveProfiles
import javax.sql.DataSource
import sample.context.ValidationException
import sample.model.DataFixtures
import sample.model.DomainTester
import sample.model.account.Account.AccountStatusType

@DataJdbcTest
@ActiveProfiles("test")
class AccountTest {
    
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
    fun loadActive() {
        tester.tx { rep ->
            rep.save(DataFixtures.acc("normal"))
            val account = Account.loadActive(rep, "normal")
            assertEquals("normal", account.getId())
            assertEquals(AccountStatusType.NORMAL, account.statusType)

            // Verification when withdrawal account is loaded
            val withdrawal = DataFixtures.acc("withdraw").copy(statusType = AccountStatusType.WITHDRAWAL)
            rep.save(withdrawal)
            try {
                Account.loadActive(rep, "withdraw")
            } catch (e: ValidationException) {
                assertEquals("error.Account.loadActive", e.message)
            }
        }
    }
} 