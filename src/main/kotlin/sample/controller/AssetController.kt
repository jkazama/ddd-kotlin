package sample.controller

import jakarta.validation.Valid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sample.ActionStatusType
import sample.context.Dto
import sample.context.actor.ActorSession
import sample.model.asset.CashInOut
import sample.model.asset.CashInOut.RegCashOut
import sample.model.constraints.AbsAmount
import sample.model.constraints.Currency
import sample.usecase.AssetService

/** API controller of the asset domain. */
@RestController
@RequestMapping("/asset")
class AssetController(private val service: AssetService) {

    @GetMapping("/cio/unprocessedOut")
    fun findUnprocessedCashOut(): List<UserCashOut> =
            service.findUnprocessedCashOut().map { UserCashOut.of(it) }

    @PostMapping("/cio/withdraw")
    fun withdraw(@RequestBody @Valid p: UserRegCashOut): ResponseEntity<Map<String, String>> {
        val accountId = ActorSession.actor().id
        return ResponseEntity.ok(mapOf("id" to service.withdraw(p.toParam(accountId))))
    }

    data class UserRegCashOut(
            @field:Currency val currency: String,
            @field:AbsAmount val absAmount: BigDecimal
    ) : Dto {
        fun toParam(accountId: String): RegCashOut =
                RegCashOut(
                        accountId = accountId,
                        currency = this.currency,
                        absAmount = this.absAmount
                )
    }

    data class UserCashOut(
            val id: String,
            val currency: String,
            val absAmount: BigDecimal,
            val requestDay: LocalDate,
            val requestDate: LocalDateTime,
            val eventDay: LocalDate,
            val valueDay: LocalDate,
            val statusType: ActionStatusType,
            val updateDate: LocalDateTime,
            val cashflowId: String
    ) : Dto {
        companion object {
            fun of(cio: CashInOut): UserCashOut =
                    UserCashOut(
                            id = cio.id,
                            currency = cio.currency,
                            absAmount = cio.absAmount,
                            requestDay = cio.requestDay,
                            requestDate = cio.requestDate,
                            eventDay = cio.eventDay,
                            valueDay = cio.valueDay,
                            statusType = cio.statusType,
                            updateDate = cio.updateDate,
                            cashflowId = cio.cashflowId ?: ""
                    )
        }
    }
}
