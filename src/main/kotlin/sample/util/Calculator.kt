package sample.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicReference

/**
 * Calculation utility.
 *
 * this calculation is not thread safe.
 */
class Calculator private constructor(v: BigDecimal) {

    private val value = AtomicReference<BigDecimal>()
    private var scale = 0
    private var mode = RoundingMode.DOWN
    /** When do fraction processing each calculation. */
    private var roundingAlways = false
    private val defaultScale = 18

    init {
        this.value.set(v)
    }

    private constructor(
            v: Number
    ) : this(
            try {
                BigDecimal(v.toString())
            } catch (e: NumberFormatException) {
                BigDecimal.ZERO
            }
    )

    /**
     * Set scale value.
     *
     * call it before a calculation.
     */
    fun scale(scale: Int): Calculator = scale(scale, RoundingMode.DOWN)

    /**
     * Set scale value.
     *
     * call it before a calculation.
     */
    fun scale(scale: Int, mode: RoundingMode): Calculator {
        this.scale = scale
        this.mode = mode
        return this
    }

    /**
     * Set roundingAlways value.
     *
     * call it before a calculation.
     */
    fun roundingAlways(roundingAlways: Boolean): Calculator {
        this.roundingAlways = roundingAlways
        return this
    }

    fun add(v: Number): Calculator {
        try {
            add(BigDecimal(v.toString()))
        } catch (e: NumberFormatException) {
            // ignore
        }
        return this
    }

    fun add(v: BigDecimal): Calculator {
        value.set(rounding(value.get().add(v)))
        return this
    }

    private fun rounding(v: BigDecimal): BigDecimal =
            if (roundingAlways) v.setScale(scale, mode) else v

    fun subtract(v: Number): Calculator {
        try {
            subtract(BigDecimal(v.toString()))
        } catch (e: NumberFormatException) {
            // ignore
        }
        return this
    }

    fun subtract(v: BigDecimal): Calculator {
        value.set(rounding(value.get().subtract(v)))
        return this
    }

    fun multiply(v: Number): Calculator {
        try {
            multiply(BigDecimal(v.toString()))
        } catch (e: NumberFormatException) {
            // ignore
        }
        return this
    }

    fun multiply(v: BigDecimal): Calculator {
        value.set(rounding(value.get().multiply(v)))
        return this
    }

    fun divideBy(v: Number): Calculator {
        try {
            divideBy(BigDecimal(v.toString()))
        } catch (e: NumberFormatException) {
            // ignore
        }
        return this
    }

    fun divideBy(v: BigDecimal): Calculator {
        val ret =
                if (roundingAlways) {
                    value.get().divide(v, scale, mode)
                } else {
                    value.get().divide(v, defaultScale, mode)
                }
        value.set(ret)
        return this
    }

    /** Return a calculation result. */
    fun intValue(): Int = decimal().toInt()

    /** Return a calculation result. */
    fun longValue(): Long = decimal().toLong()

    /** Return a calculation result. */
    fun decimal(): BigDecimal {
        val v = value.get()
        return v?.setScale(scale, mode) ?: BigDecimal.ZERO
    }

    companion object {
        fun init(): Calculator = Calculator(BigDecimal.ZERO)

        fun init(v: Number): Calculator = Calculator(v)

        fun init(v: BigDecimal): Calculator = Calculator(v)
    }
}
