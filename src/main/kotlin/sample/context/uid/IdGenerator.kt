package sample.context.uid

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import org.springframework.stereotype.Component

/**
 * Utility component for ID generation. low: It is memory-based because it is a sample, but actually
 * it depends on resource because there is a need for persistence.
 */
@Component
class IdGenerator {

    private val values = ConcurrentHashMap<String, AtomicLong>()

    fun generate(key: String): String =
            when (key) {
                "CashInOut" -> formatCashInOut(nextValue(key))
                else -> UUID.randomUUID().toString().replace("-", "")
            }

    private fun formatCashInOut(v: Long): String {
        // low: Correct code formatting including fixed digitization and 0 padding is
        // required.
        return "CIO$v"
    }

    @Synchronized
    private fun nextValue(key: String): Long {
        return values.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
    }
}
