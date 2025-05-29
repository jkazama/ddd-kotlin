package sample.controller.system

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sample.usecase.admin.AssetAdminService
import sample.usecase.admin.MasterAdminService

/**
 * API controller of the system job.
 * <p>
 * the URL after "/system/job" assumes what is carried out from job scheduler,
 * it is necessary to make it inaccessible from the outside in L/B.
 */
@RestController
@RequestMapping("/system/job")
class JobController(
        private val asset: AssetAdminService,
        private val master: MasterAdminService
) {

    @PostMapping("/daily/processDay")
    fun processDay() {
        master.processDay()
    }

    @PostMapping("/daily/closingCashOut")
    fun closingCashOut() {
        asset.closingCashOut()
    }

    @PostMapping("/daily/realizeCashflow")
    fun realizeCashflow() {
        asset.realizeCashflow()
    }
} 