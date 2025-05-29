package sample.usecase.report

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import sample.context.orm.OrmRepository
import sample.context.report.ReportHandler
import sample.model.asset.CashInOut.FindCashInOut

/**
 * Report exporter of the application layer. <p> Manages transactions independently, please be
 * careful not to call it within a service transaction.
 */
@Component
@Suppress("unused")
class ServiceReportExporter(
        private val rep: OrmRepository,
        private val tx: PlatformTransactionManager,
        private val report:
                ReportHandler // low: It is not used because it is not implemented in the sample
) {

    fun exportCashInOut(p: FindCashInOut): ByteArrayResource {
        // low: Binary generation. Assumes an online download that allows you to specify
        // conditions.
        return ByteArrayResource(ByteArray(0))
    }

    fun exportFileCashInOut(baseDay: String) {
        // low: File output to a specific directory. Assume use in jobs etc
    }
}
