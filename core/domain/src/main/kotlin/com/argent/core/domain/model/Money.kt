package com.argent.core.domain.model

/**
 * A money amount held in the currency's smallest unit (pence, cents, …).
 *
 * Money is never a [Double] in this codebase — rounding error on currency is a
 * correctness bug, not an approximation. Arithmetic across currencies fails fast.
 *
 * @property minorUnits amount in the smallest unit; may be negative
 * @property currency ISO 4217 code, uppercase (e.g. "GBP")
 */
data class Money(
    val minorUnits: Long,
    val currency: String,
) {
    init {
        require(currency.length == 3 && currency == currency.uppercase()) {
            "currency must be an uppercase ISO 4217 code, was '$currency'"
        }
    }

    val isPositive: Boolean get() = minorUnits > 0L
    val isZero: Boolean get() = minorUnits == 0L
    val isNegative: Boolean get() = minorUnits < 0L

    operator fun plus(other: Money): Money =
        copy(minorUnits = minorUnits + other.sameCurrencyAs(this))

    operator fun minus(other: Money): Money =
        copy(minorUnits = minorUnits - other.sameCurrencyAs(this))

    operator fun compareTo(other: Money): Int =
        minorUnits.compareTo(other.sameCurrencyAs(this))

    private fun sameCurrencyAs(reference: Money): Long {
        require(currency == reference.currency) {
            "cannot combine $currency with ${reference.currency}"
        }
        return minorUnits
    }

    companion object {
        fun gbp(minorUnits: Long): Money = Money(minorUnits, "GBP")
        fun zero(currency: String): Money = Money(0L, currency)
    }
}
