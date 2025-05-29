package sample.model.account

import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import sample.context.DomainEntity
import sample.context.ValidationException
import sample.context.orm.OrmRepository
import sample.model.constraints.AccountId
import sample.model.constraints.Email
import sample.model.constraints.Name

/** Account. low: The minimum columns with this sample. */
@Table("ACCOUNT")
data class Account(
        @Id @AccountId val id: String,
        @Name val name: String,
        @Email val mail: String,
        @NotNull val statusType: AccountStatusType
) : DomainEntity {

    override fun getId(): Any = id

    companion object {
        fun load(rep: OrmRepository, id: String): Account {
            return rep.load(Account::class.java, id)
        }

        fun loadActive(rep: OrmRepository, id: String): Account {
            val acc = load(rep, id)
            if (acc.statusType.inactive()) {
                throw ValidationException.of("error.Account.loadActive")
            }
            return acc
        }
    }

    enum class AccountStatusType {
        NORMAL,
        WITHDRAWAL;

        fun inactive(): Boolean {
            return this == WITHDRAWAL
        }
    }
}
