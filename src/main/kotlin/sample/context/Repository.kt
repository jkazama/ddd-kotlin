package sample.context

/**
 * It is general-purpose Repository which does not depend on the specific domain object.
 *
 * You can use it as Repository where a type is not safe.
 */
interface Repository {

    /**
     * Return a helper utility to provide the access to an infrastructure layer component in the
     * domain layer.
     */
    fun dh(): DomainHelper

    fun <T : DomainEntity> get(clazz: Class<T>, id: Any): T?

    fun <T : DomainEntity> load(clazz: Class<T>, id: Any): T

    fun <T : DomainEntity> exists(clazz: Class<T>, id: Any): Boolean

    fun <T : DomainEntity> findAll(clazz: Class<T>): List<T>

    fun <T : DomainEntity> save(entity: T): T

    fun <T : DomainEntity> saveOrUpdate(entity: T): T

    fun <T : DomainEntity> update(entity: T): T

    fun <T : DomainEntity> delete(entity: T): T
}
