package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val companySymbol: String,
    val type: TransactionType,
    val shares: Int,
    val pricePerShare: Money,
    val totalAmount: Money,
    val timestamp: Long, // LocalDateTime を Long で serialization
    val day: Int,
    val commission: Money = Money(0.0)
) {
    val totalCost: Money get() = totalAmount + commission
    
    fun getFormattedTime(): String {
        val hours = (timestamp % 86400) / 3600
        val minutes = (timestamp % 3600) / 60
        return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
    
    companion object {
        fun generateId(): String = "txn_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

@Serializable
enum class TransactionType(val displayName: String, val emoji: String) {
    BUY("購入", "📥"),
    SELL("売却", "📤");
    
    fun getSign(): Int = when (this) {
        BUY -> -1  // 現金減少
        SELL -> 1  // 現金増加
    }
}

data class TransactionValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        fun valid() = TransactionValidation(true)
        fun invalid(message: String) = TransactionValidation(false, message)
        
        fun validateBuy(
            cash: Money,
            shares: Int,
            pricePerShare: Money,
            commissionRate: Double = 0.001
        ): TransactionValidation {
            if (shares <= 0) {
                return invalid("株数は1以上である必要があります")
            }
            
            val totalCost = Money(shares * pricePerShare.amount)
            val commission = totalCost * commissionRate
            val requiredCash = totalCost + commission
            
            if (cash < requiredCash) {
                val shortage = requiredCash - cash
                return invalid("現金が不足しています (不足額: ${shortage.format()})")
            }
            
            return valid()
        }
        
        fun validateSell(
            holding: Holding?,
            shares: Int
        ): TransactionValidation {
            if (shares <= 0) {
                return invalid("株数は1以上である必要があります")
            }
            
            if (holding == null) {
                return invalid("保有していない銘柄です")
            }
            
            if (holding.shares < shares) {
                return invalid("保有株数が不足しています (保有: ${holding.shares}株)")
            }
            
            return valid()
        }
    }
}