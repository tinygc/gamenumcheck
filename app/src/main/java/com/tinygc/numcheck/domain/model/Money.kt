package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
@JvmInline
value class Money(val amount: Double) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun minus(other: Money): Money = Money(amount - other.amount)
    operator fun times(multiplier: Double): Money = Money(amount * multiplier)
    operator fun div(divisor: Double): Money = Money(amount / divisor)
    
    operator fun compareTo(other: Money): Int = amount.compareTo(other.amount)
    
    fun format(): String = "¥${String.format("%,.0f", amount)}"
    
    fun formatWithSign(): String {
        val prefix = when {
            amount > 0 -> "+"
            amount < 0 -> ""
            else -> ""
        }
        return "$prefix${format()}"
    }
    
    fun isPositive(): Boolean = amount > 0
    fun isNegative(): Boolean = amount < 0
    fun isZero(): Boolean = amount == 0.0
    
    fun abs(): Money = Money(abs(amount))
    
    companion object {
        val ZERO = Money(0.0)
        
        fun fromYen(yen: Long): Money = Money(yen.toDouble())
        fun fromYen(yen: Int): Money = Money(yen.toDouble())
    }
}