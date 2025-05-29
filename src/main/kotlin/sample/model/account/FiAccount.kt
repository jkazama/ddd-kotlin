package sample.model.account

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import sample.context.DomainEntity
import sample.context.orm.OrmRepository
import sample.model.constraints.AccountId
import sample.model.constraints.Category
import sample.model.constraints.Currency
import sample.model.constraints.IdStr

/**
 * the financial institution account in an account. <p> Use it by an account activity. low: The
 * minimum columns with this sample.
 */
@Table("FI_ACCOUNT")
data class FiAccount(
        @Id val id: String,
        @AccountId val accountId: String,
        @Category val category: String,
        @Currency val currency: String,
        @IdStr val fiCode: String,
        @AccountId val fiAccountId: String
) : DomainEntity {

    override fun getId(): Any = id

    companion object {
        fun load(
                rep: OrmRepository,
                accountId: String,
                category: String,
                currency: String
        ): FiAccount {
            val conditions =
                    mapOf("accountId" to accountId, "category" to category, "currency" to currency)
            return rep.tmpl().load(FiAccount::class.java, conditions)
        }
    }
}
