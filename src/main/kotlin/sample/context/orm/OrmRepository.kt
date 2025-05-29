package sample.context.orm

import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import javax.sql.DataSource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import sample.context.DomainEntity
import sample.context.DomainHelper
import sample.context.Repository
import sample.context.ValidationException
import sample.model.DomainErrorKeys

/**
 * Repository base implementation of JDBC. <p> This component provides simple JDBC implementation in
 * form not to use a base of Spring Data to realize 1-n relations of Repository and Entity.
 */
@Component
class OrmRepository(
        private val dh: DomainHelper,
        private val dataSource: DataSource,
        private val jdbcTemplate: JdbcAggregateTemplate
) : Repository {

    override fun dh(): DomainHelper = dh

    fun tmpl(): OrmTemplate = OrmTemplate.of(this.jdbcTemplate, this.tmplJdbc())

    fun tmplJdbc(): NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(this.dataSource)

    override fun <T : DomainEntity> get(clazz: Class<T>, id: Any): T? =
            jdbcTemplate.findById(id, clazz)

    override fun <T : DomainEntity> load(clazz: Class<T>, id: Any): T {
        val entity = get(clazz, id)
        return entity
                ?: throw ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND, id.toString())
    }

    override fun <T : DomainEntity> exists(clazz: Class<T>, id: Any): Boolean =
            jdbcTemplate.existsById(id, clazz)

    override fun <T : DomainEntity> findAll(clazz: Class<T>): List<T> {
        val entities = jdbcTemplate.findAll(clazz)
        return StreamSupport.stream(entities.spliterator(), false).collect(Collectors.toList())
    }

    override fun <T : DomainEntity> save(entity: T): T = jdbcTemplate.insert(entity)

    override fun <T : DomainEntity> saveOrUpdate(entity: T): T {
        return if (this.exists(entity.javaClass as Class<T>, entity.getId())) {
            this.update(entity)
        } else {
            this.save(entity)
        }
    }

    override fun <T : DomainEntity> update(entity: T): T = jdbcTemplate.save(entity)

    override fun <T : DomainEntity> delete(entity: T): T {
        jdbcTemplate.delete(entity)
        return entity
    }

    // Additional convenience methods for Spring Data JDBC style operations

    /** Save multiple entities. */
    fun <T : DomainEntity> saveAll(entities: Iterable<T>): Iterable<T> =
            jdbcTemplate.saveAll(entities)

    /** Find entity by ID with Optional. */
    fun <T : DomainEntity> findById(clazz: Class<T>, id: Any): Optional<T> =
            Optional.ofNullable(jdbcTemplate.findById(id, clazz))

    /** Find all entities with sorting. */
    fun <T : DomainEntity> findAll(clazz: Class<T>, sort: Sort): Iterable<T> =
            jdbcTemplate.findAll(clazz, sort)

    /** Find all entities with pagination. */
    fun <T : DomainEntity> findAll(clazz: Class<T>, pageable: Pageable): Page<T> {
        val entities = jdbcTemplate.findAll(clazz, pageable.sort)
        val content =
                StreamSupport.stream(entities.spliterator(), false)
                        .skip(pageable.offset)
                        .limit(pageable.pageSize.toLong())
                        .collect(Collectors.toList())

        val total = jdbcTemplate.count(clazz)
        return PageImpl(content, pageable, total)
    }

    /** Count all entities. */
    fun <T : DomainEntity> count(clazz: Class<T>): Long = jdbcTemplate.count(clazz)

    /** Delete entity by ID. */
    fun <T : DomainEntity> deleteById(clazz: Class<T>, id: Any) {
        jdbcTemplate.deleteById(id, clazz)
    }

    /** Delete multiple entities. */
    fun <T : DomainEntity> deleteAll(entities: Iterable<T>) {
        jdbcTemplate.deleteAll(entities)
    }

    /** Delete all entities of specified type. */
    fun <T : DomainEntity> deleteAll(clazz: Class<T>) {
        jdbcTemplate.deleteAll(clazz)
    }

    companion object {
        fun of(
                dh: DomainHelper,
                dataSource: DataSource,
                jdbcTemplate: JdbcAggregateTemplate
        ): OrmRepository = OrmRepository(dh, dataSource, jdbcTemplate)
    }
}
