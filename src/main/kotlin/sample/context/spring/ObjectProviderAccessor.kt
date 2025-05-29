package sample.context.spring

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

/** ObjectProvider Fast Access Utility. */
@Component
class ObjectProviderAccessor {

    private val cache = ConcurrentHashMap<Class<*>, Any?>()

    @Suppress("UNCHECKED_CAST")
    fun <T> bean(target: ObjectProvider<T>, clazz: Class<T>): T {
        cache.computeIfAbsent(clazz) { target.getObject() }
        return cache[clazz] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> beanOpt(target: ObjectProvider<T>, clazz: Class<T>): Optional<T> {
        cache.computeIfAbsent(clazz) { target.getIfAvailable() }
        return Optional.ofNullable(cache[clazz]).map { it as T }
    }
}
