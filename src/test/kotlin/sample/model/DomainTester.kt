package sample.model

import javax.sql.DataSource
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import sample.context.DomainEntity
import sample.context.orm.OrmRepository
import sample.context.orm.TxTemplate

class DomainTester
private constructor(jdbcTemplate: JdbcAggregateTemplate, dataSource: DataSource) {

    private val rep: OrmRepository
    private val txm: PlatformTransactionManager
    private val dh: MockDomainHelper

    init {
        this.txm = JdbcTransactionManager(dataSource)
        this.dh = MockDomainHelper()
        this.rep = OrmRepository(dh, dataSource, jdbcTemplate)
    }

    fun <T> tx(fn: (OrmRepository) -> T): T {
        return TxTemplate.of(txm).tx {
            val ret = fn(rep)
            if (ret is DomainEntity) {
                ret.hashCode() // for lazy loading
            }
            ret
        }
    }

    fun tx(consume: (OrmRepository) -> Unit) {
        tx<Boolean> { rep ->
            consume(rep)
            true
        }
    }

    fun txInitializeData(consume: InitializeDataConsumer) {
        tx { rep -> consume.initialize(rep) }
    }

    companion object {
        fun create(jdbcTemplate: JdbcAggregateTemplate, dataSource: DataSource): DomainTester {
            return DomainTester(jdbcTemplate, dataSource)
        }
    }

    fun interface InitializeDataConsumer {
        fun initialize(rep: OrmRepository)
    }
}
