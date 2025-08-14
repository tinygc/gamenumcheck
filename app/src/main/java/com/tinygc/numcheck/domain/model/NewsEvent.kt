package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NewsEvent(
    val id: String,
    val title: String,
    val description: String,
    val affectedCompanies: List<String>,
    val impact: MarketImpact,
    val eventType: NewsType,
    val occurredAt: Long, // LocalDateTime を Long で serialization
    val day: Int
) {
    fun getFormattedTime(): String {
        val hours = (occurredAt % 86400) / 3600
        val minutes = (occurredAt % 3600) / 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
}

@Serializable
data class MarketImpact(
    val priceChangeMin: Double,     // 最小変動率 (e.g., -0.15)
    val priceChangeMax: Double,     // 最大変動率 (e.g., 0.30)
    val probability: Double         // 発生確率 (0.0..1.0)
) {
    fun getRandomImpact(): Double {
        return if (Math.random() < probability) {
            priceChangeMin + (priceChangeMax - priceChangeMin) * Math.random()
        } else {
            0.0
        }
    }
    
    companion object {
        fun positive(min: Double = 0.05, max: Double = 0.15, probability: Double = 0.8): MarketImpact {
            return MarketImpact(min, max, probability)
        }
        
        fun negative(min: Double = -0.15, max: Double = -0.05, probability: Double = 0.8): MarketImpact {
            return MarketImpact(min, max, probability)
        }
        
        fun neutral(range: Double = 0.03, probability: Double = 0.5): MarketImpact {
            return MarketImpact(-range, range, probability)
        }
        
        fun volatile(min: Double = -0.25, max: Double = 0.25, probability: Double = 0.9): MarketImpact {
            return MarketImpact(min, max, probability)
        }
    }
}

@Serializable
enum class NewsType(val displayName: String, val emoji: String) {
    POSITIVE("好材料", "🔥"),
    NEGATIVE("悪材料", "📉"),
    NEUTRAL("中立", "💡"),
    MARKET_WIDE("市場全体", "⚡");
    
    fun getDefaultImpact(): MarketImpact = when (this) {
        POSITIVE -> MarketImpact.positive()
        NEGATIVE -> MarketImpact.negative()
        NEUTRAL -> MarketImpact.neutral()
        MARKET_WIDE -> MarketImpact.volatile(min = -0.1, max = 0.1, probability = 0.7)
    }
}