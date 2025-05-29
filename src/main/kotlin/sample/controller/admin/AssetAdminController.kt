package sample.controller.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid
import sample.model.asset.CashInOut
import sample.model.asset.CashInOut.FindCashInOut
import sample.usecase.admin.AssetAdminService

/**
 * API controller of the asset domain in the organization.
 */
@RestController
@RequestMapping("/admin/asset")
class AssetAdminController(
        private val service: AssetAdminService
) {

    @GetMapping("/cio")
    fun findCashInOut(@Valid p: FindCashInOut): List<CashInOut> {
        return service.findCashInOut(p)
    }
} 