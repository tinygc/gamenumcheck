package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Stock(
    val company: Company,
    val currentPrice: Money,
    val previousPrice: Money,
    val priceHistory: List<PricePoint>,
    val lastUpdated: Long // LocalDateTime を Long で serialization
) {
    val changeRate: Double get() = 
        if (previousPrice.amount != 0.0) {
            (currentPrice.amount - previousPrice.amount) / previousPrice.amount
        } else 0.0
    
    val changeAmount: Money get() = 
        Money(currentPrice.amount - previousPrice.amount)
    
    val isPositiveChange: Boolean get() = changeRate > 0
    val isNegativeChange: Boolean get() = changeRate < 0
    val isNoChange: Boolean get() = changeRate == 0.0
    
    fun formatChangeRate(): String = "${String.format("%+.1f", changeRate * 100)}%"
    
    fun getHighestPrice(days: Int = 7): Money {
        return priceHistory.takeLast(days).maxByOrNull { it.price.amount }?.price ?: currentPrice
    }
    
    fun getLowestPrice(days: Int = 7): Money {
        return priceHistory.takeLast(days).minByOrNull { it.price.amount }?.price ?: currentPrice
    }
    
    fun getTrend(days: Int = 3): PriceTrend {
        if (priceHistory.size < days) return PriceTrend.NEUTRAL
        
        val recentPrices = priceHistory.takeLast(days).map { it.price.amount }
        val increasing = recentPrices.zipWithNext().count { (prev, next) -> next > prev }
        val decreasing = recentPrices.zipWithNext().count { (prev, next) -> next < prev }
        
        return when {
            increasing > decreasing -> PriceTrend.UPWARD
            decreasing > increasing -> PriceTrend.DOWNWARD
            else -> PriceTrend.NEUTRAL
        }
    }
}

@Serializable
data class PricePoint(
    val price: Money,
    val timestamp: Long // LocalDateTime を Long で serialization
)

enum class PriceTrend {
    UPWARD, DOWNWARD, NEUTRAL
}