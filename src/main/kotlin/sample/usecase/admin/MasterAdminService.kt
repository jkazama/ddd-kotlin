package sample.usecase.admin

import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import sample.context.Timestamper
import sample.context.audit.AuditHandler
import sample.context.orm.OrmRepository

/**
 * The use case processing for the master domain in the organization.
 */
@Service
class MasterAdminService(
        private val rep: OrmRepository,
        @Suppress("unused")
        private val txm: PlatformTransactionManager,
        private val audit: AuditHandler
) {

    fun processDay() {
        audit.audit("Forward day.") {
            val time: Timestamper = rep.dh().time()
            time.daySet(time.dayPlus(1))
        }
    }
} 