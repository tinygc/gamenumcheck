package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Company(
    val symbol: String,          // 銘柄コード (e.g., "CYBT")
    val name: String,           // 企業名
    val category: CompanyCategory, // 業界カテゴリ
    val description: String,    // 企業説明
    val volatility: Volatility, // 変動性
    val hasDividend: Boolean,   // 配当有無
    val unlockLevel: Int        // 解放レベル
) {
    companion object {
        const val INITIAL_COMPANIES_COUNT = 10
    }
}

@Serializable
enum class CompanyCategory(val displayName: String, val emoji: String) {
    TECHNOLOGY("テクノロジー", "🏭"),
    LIFESTYLE("生活関連", "🏪"),
    INFRASTRUCTURE("インフラ", "🏥"),
    MATERIALS("素材", "🏗️"),
    ENTERTAINMENT("エンタメ", "🎮");
    
    companion object {
        fun fromString(value: String): CompanyCategory {
            return values().find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown category: $value")
        }
    }
}

@Serializable
enum class Volatility(val displayName: String, val multiplier: Double) {
    LOW("低", 0.5),
    MEDIUM("中", 1.0),
    HIGH("高", 1.8);
    
    companion object {
        fun fromString(value: String): Volatility {
            return values().find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown volatility: $value")
        }
    }
}