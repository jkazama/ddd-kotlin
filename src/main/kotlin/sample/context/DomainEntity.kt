package sample.context

/**
 * The marker interface of the domain object.
 *
 * The domain object in succession to this interface has a role to carry out a business logic with
 * domain information.
 *
 * Please refer to the next book for more information about domain model.
 * - [Domain-Driven Design](http://www.amazon.co.jp/dp/0321125215/) Eric Evans
 * - [Patterns of Enterprise Application Architecture](http://www.amazon.co.jp/dp/0321127420/)
 * Martin Fowler
 * - [Pojos in Action](http://www.amazon.co.jp/dp/1932394583/) Chris Richardson
 */
interface DomainEntity {
    fun getId(): Any
}
