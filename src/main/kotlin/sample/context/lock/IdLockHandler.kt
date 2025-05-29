package sample.context.lock

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.springframework.stereotype.Component
import sample.context.InvocationException

/**
 * The lock of the ID unit. low: It is simple and targets only the ID lock of the account unit here.
 * low: You take the pessimistic lock by "for update" demand on a lock table of DB, but usually do
 * it to memory lock because it is a sample.
 */
@Component
class IdLockHandler {

    private val lockMap: ConcurrentMap<Any, ReentrantReadWriteLock> = ConcurrentHashMap()

    fun <T> call(id: Any?, lockType: IdLockType, block: () -> T): T {
        if (lockType.isWrite()) {
            this.writeLock(id)
        } else {
            this.readLock(id)
        }
        return try {
            block()
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw InvocationException("error.Exception", e)
        } finally {
            this.unlock(id)
        }
    }

    private fun writeLock(id: Any?) {
        if (id == null) {
            return
        }
        this.idLock(id).writeLock().lock()
    }

    private fun idLock(id: Any): ReentrantReadWriteLock =
            lockMap.computeIfAbsent(id) { ReentrantReadWriteLock() }

    private fun readLock(id: Any?) {
        if (id == null) {
            return
        }
        this.idLock(id).readLock().lock()
    }

    private fun unlock(id: Any?) {
        if (id == null) {
            return
        }

        val idLock = idLock(id)
        if (idLock.isWriteLockedByCurrentThread) {
            idLock.writeLock().unlock()
        } else {
            idLock.readLock().unlock()
        }
    }

    data class IdLockPair(val id: Any?, val lockType: IdLockType)
}
