package sample.context.orm

import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import sample.context.ValidationException
import sample.model.DomainErrorKeys

/**
 * Simple Accessor for JdbcAggregateTemplate operations. (you are formed every session, and please
 * use it) <p> This provides JDBC-based operations using Spring Data JDBC's Criteria API for dynamic
 * query construction and JdbcAggregateTemplate for execution.
 */
class OrmTemplate(
        private val template: JdbcAggregateTemplate,
        private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    /**
     * Get a single entity using Criteria-based query.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return Entity or null if not found
     */
    fun <T> get(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria): T? {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria)

        return template.findOne(query, clazz).orElse(null)
    }

    /**
     * Get a single entity by property equality.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @return Entity or null if not found
     */
    fun <T> get(clazz: Class<T>, propertyName: String, value: Any?): T? {
        if (value == null) {
            return null
        }

        val criteria = Criteria.where(propertyName).`is`(value)
        val query = Query.query(criteria)

        return template.findOne(query, clazz).orElse(null)
    }

    /**
     * Get a single entity by multiple conditions.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @return Entity or null if not found
     */
    fun <T> get(clazz: Class<T>, conditions: Map<String, Any?>): T? {
        if (conditions.isEmpty()) {
            return null
        }

        val criteria = buildCriteriaFromConditions(conditions)
        val query = Query.query(criteria)

        return template.findOne(query, clazz).orElse(null)
    }

    /**
     * Load a single entity using Criteria-based query. Throws ValidationException if not found.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    fun <T> load(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria): T {
        return get(clazz, criteriaBuilder)
                ?: throw ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND)
    }

    /**
     * Load a single entity by property equality. Throws ValidationException if not found.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    fun <T> load(clazz: Class<T>, propertyName: String, value: Any?): T {
        return get(clazz, propertyName, value)
                ?: throw ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND, value.toString())
    }

    /**
     * Load a single entity by multiple conditions. Throws ValidationException if not found.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @return Entity instance
     * @throws ValidationException if entity not found
     */
    fun <T> load(clazz: Class<T>, conditions: Map<String, Any?>): T {
        return get(clazz, conditions)
                ?: throw ValidationException.of(DomainErrorKeys.ENTITY_NOT_FOUND)
    }

    /**
     * Find entities using JdbcAggregateTemplate with Criteria-based Query. This method demonstrates
     * how to use Criteria API for dynamic queries.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @return List of entities
     */
    fun <T> find(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria): List<T> {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria)

        return template.findAll(query, clazz)
    }

    /**
     * Find entities by a simple property equality condition.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @return List of entities
     */
    fun <T> find(clazz: Class<T>, propertyName: String, value: Any?): List<T> {
        if (value == null) {
            return template.findAll(clazz)
        }

        val criteria = Criteria.where(propertyName).`is`(value)
        val query = Query.query(criteria)

        return template.findAll(query, clazz)
    }

    /**
     * Find entities with multiple conditions.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @return List of entities
     */
    fun <T> find(clazz: Class<T>, conditions: Map<String, Any?>): List<T> {
        if (conditions.isEmpty()) {
            return template.findAll(clazz)
        }

        val criteria = buildCriteriaFromConditions(conditions)
        val query = Query.query(criteria)

        return template.findAll(query, clazz)
    }

    /**
     * Find with pagination using Criteria.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria
     * @param pageable Pagination information
     * @return Page of entities
     */
    fun <T> find(
            clazz: Class<T>,
            criteriaBuilder: (Criteria) -> Criteria,
            pageable: Pageable
    ): Page<T> {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria).with(pageable)

        val content = template.findAll(query, clazz)
        val total = template.count(Query.query(criteria), clazz)

        return PageImpl(content, pageable, total)
    }

    /**
     * Find with pagination using property equality.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @param pageable Pagination information
     * @return Page of entities
     */
    fun <T> find(clazz: Class<T>, propertyName: String, value: Any?, pageable: Pageable): Page<T> {
        if (value == null) {
            return template.findAll(clazz, pageable)
        }

        val criteria = Criteria.where(propertyName).`is`(value)
        val query = Query.query(criteria).with(pageable)

        val content = template.findAll(query, clazz)
        val total = template.count(Query.query(criteria), clazz)

        return PageImpl(content, pageable, total)
    }

    /**
     * Find with pagination using multiple conditions.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @param pageable Pagination information
     * @return Page of entities
     */
    fun <T> find(clazz: Class<T>, conditions: Map<String, Any?>, pageable: Pageable): Page<T> {
        if (conditions.isEmpty()) {
            return template.findAll(clazz, pageable)
        }

        val criteria = buildCriteriaFromConditions(conditions)
        val query = Query.query(criteria).with(pageable)

        val content = template.findAll(query, clazz)
        val total = template.count(Query.query(criteria), clazz)

        return PageImpl(content, pageable, total)
    }

    /**
     * Find all entities of a specific type.
     *
     * @param clazz Entity class
     * @return List of all entities
     */
    fun <T> findAll(clazz: Class<T>): List<T> = template.findAll(clazz)

    /**
     * Find all entities with pagination.
     *
     * @param clazz Entity class
     * @param pageable Pagination information
     * @return Page of entities
     */
    fun <T> findAll(clazz: Class<T>, pageable: Pageable): Page<T> =
            template.findAll(clazz, pageable)

    /**
     * Count entities by criteria.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria
     * @return Count of entities
     */
    fun <T> count(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria): Long {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria)

        return template.count(query, clazz)
    }

    /**
     * Count entities by property equality.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @return Count of entities
     */
    fun <T> count(clazz: Class<T>, propertyName: String, value: Any?): Long {
        if (value == null) {
            return template.count(clazz)
        }

        val criteria = Criteria.where(propertyName).`is`(value)
        val query = Query.query(criteria)

        return template.count(query, clazz)
    }

    /**
     * Count entities by multiple conditions.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @return Count of entities
     */
    fun <T> count(clazz: Class<T>, conditions: Map<String, Any?>): Long {
        if (conditions.isEmpty()) {
            return template.count(clazz)
        }

        val criteria = buildCriteriaFromConditions(conditions)
        val query = Query.query(criteria)

        return template.count(query, clazz)
    }

    /**
     * Check if entities exist by criteria.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria
     * @return true if entities exist
     */
    fun <T> exists(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria): Boolean {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria)

        return template.exists(query, clazz)
    }

    /**
     * Check if entities exist by property equality.
     *
     * @param clazz Entity class
     * @param propertyName Property name
     * @param value Property value
     * @return true if entities exist
     */
    fun <T> exists(clazz: Class<T>, propertyName: String, value: Any?): Boolean {
        if (value == null) {
            return false
        }

        val criteria = Criteria.where(propertyName).`is`(value)
        val query = Query.query(criteria)

        return template.exists(query, clazz)
    }

    /**
     * Check if entities exist by multiple conditions.
     *
     * @param clazz Entity class
     * @param conditions Map of property names and values
     * @return true if entities exist
     */
    fun <T> exists(clazz: Class<T>, conditions: Map<String, Any?>): Boolean {
        if (conditions.isEmpty()) {
            return false
        }

        val criteria = buildCriteriaFromConditions(conditions)
        val query = Query.query(criteria)

        return template.exists(query, clazz)
    }

    /**
     * Execute update/insert/delete operations using NamedParameterJdbcTemplate.
     *
     * @param sql SQL statement
     * @param args Query arguments
     * @return Number of affected rows
     */
    fun execute(sql: String, vararg args: Any?): Int {
        val paramMap = createParameterMap(*args)
        return namedParameterJdbcTemplate.update(sql, paramMap)
    }

    /**
     * Build Criteria from conditions map.
     *
     * @param conditions Map of property names and values
     * @return Criteria object
     */
    private fun buildCriteriaFromConditions(conditions: Map<String, Any?>): Criteria {
        var criteria = Criteria.empty()
        for ((key, value) in conditions) {
            if (value != null) {
                criteria = criteria.and(Criteria.where(key).`is`(value))
            }
        }
        return criteria
    }

    /**
     * Create parameter map from positional arguments. Supports both positional parameters
     * (converted to named) and Map parameters.
     *
     * @param args Query arguments
     * @return Parameter map
     */
    private fun createParameterMap(vararg args: Any?): Map<String, Any?> {
        val paramMap = mutableMapOf<String, Any?>()

        args.forEachIndexed { index, arg ->
            when (arg) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST") val argNamed = arg as Map<String, Any?>
                    paramMap.putAll(argNamed)
                }
                else -> {
                    // Convert positional parameters to named parameters (param1, param2, etc.)
                    paramMap["param${index + 1}"] = arg
                }
            }
        }

        return paramMap
    }

    /**
     * Find entities using JdbcAggregateTemplate with Criteria-based Query and Sort.
     *
     * @param clazz Entity class
     * @param criteriaBuilder Function to build criteria dynamically
     * @param sort Sort specification
     * @return List of entities
     */
    fun <T> find(clazz: Class<T>, criteriaBuilder: (Criteria) -> Criteria, sort: Sort): List<T> {
        val criteria = criteriaBuilder(Criteria.empty())
        val query = Query.query(criteria).sort(sort)

        return template.findAll(query, clazz)
    }

    companion object {
        fun of(
                template: JdbcAggregateTemplate,
                namedParameterJdbcTemplate: NamedParameterJdbcTemplate
        ): OrmTemplate = OrmTemplate(template, namedParameterJdbcTemplate)
    }
}
