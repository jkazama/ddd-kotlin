package sample.model.master

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import sample.context.DomainEntity
import sample.context.orm.OrmRepository
import sample.model.constraints.AccountId
import sample.model.constraints.Category
import sample.model.constraints.Currency
import sample.model.constraints.IdStr

/**
 * The settlement financial institution of the service company. low: It is a sample, a branch and a
 * name, and considerably originally omit required information.
 */
@Table("SELF_FI_ACCOUNT")
data class SelfFiAccount(
        @Id val id: String,
        @Category val category: String,
        @Currency val currency: String,
        @IdStr val fiCode: String,
        @AccountId val fiAccountId: String
) : DomainEntity {

    override fun getId(): Any = id

    companion object {
        fun load(rep: OrmRepository, category: String, currency: String): SelfFiAccount =
                rep.tmpl().load(SelfFiAccount::class.java) { criteria ->
                    criteria.and("category").`is`(category).and("currency").`is`(currency)
                }
    }
}
