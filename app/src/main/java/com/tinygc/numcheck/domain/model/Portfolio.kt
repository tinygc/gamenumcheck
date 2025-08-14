package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Portfolio(
    val cash: Money,
    val holdings: Map<String, Holding>,
    val initialCash: Money = Money(INITIAL_CASH)
) {
    fun getTotalStockValue(): Money {
        return holdings.values.fold(Money.ZERO) { acc, holding ->
            acc + holding.currentValue
        }
    }
    
    fun getTotalAssets(): Money = cash + getTotalStockValue()
    
    fun getProfitLoss(): Money = getTotalAssets() - initialCash
    
    fun getProfitRate(): Double = 
        if (initialCash.amount != 0.0) {
            getProfitLoss().amount / initialCash.amount
        } else 0.0
    
    fun canAfford(amount: Money): Boolean = cash >= amount
    
    fun hasHolding(symbol: String): Boolean = holdings.containsKey(symbol)
    
    fun getHolding(symbol: String): Holding? = holdings[symbol]
    
    fun addHolding(symbol: String, shares: Int, price: Money): Portfolio {
        val existingHolding = holdings[symbol]
        val newHolding = if (existingHolding != null) {
            // 平均取得単価を計算
            val totalShares = existingHolding.shares + shares
            val totalCost = Money(existingHolding.shares * existingHolding.averagePurchasePrice.amount + shares * price.amount)
            val averagePrice = totalCost / totalShares.toDouble()
            
            existingHolding.copy(
                shares = totalShares,
                averagePurchasePrice = averagePrice,
                currentPrice = price
            )
        } else {
            Holding(
                companySymbol = symbol,
                shares = shares,
                averagePurchasePrice = price,
                currentPrice = price
            )
        }
        
        return copy(
            cash = cash - Money(shares * price.amount),
            holdings = holdings + (symbol to newHolding)
        )
    }
    
    fun removeHolding(symbol: String, shares: Int, price: Money): Portfolio {
        val holding = holdings[symbol] ?: return this
        
        if (holding.shares <= shares) {
            // 全株売却
            return copy(
                cash = cash + Money(holding.shares * price.amount),
                holdings = holdings - symbol
            )
        } else {
            // 一部売却
            val updatedHolding = holding.copy(
                shares = holding.shares - shares,
                currentPrice = price
            )
            return copy(
                cash = cash + Money(shares * price.amount),
                holdings = holdings + (symbol to updatedHolding)
            )
        }
    }
    
    fun updateStockPrice(symbol: String, newPrice: Money): Portfolio {
        val holding = holdings[symbol] ?: return this
        val updatedHolding = holding.copy(currentPrice = newPrice)
        return copy(holdings = holdings + (symbol to updatedHolding))
    }
    
    companion object {
        const val INITIAL_CASH = 1_000_000.0 // 100万円
        
        fun createInitial(): Portfolio {
            return Portfolio(
                cash = Money(INITIAL_CASH),
                holdings = emptyMap()
            )
        }
    }
}

@Serializable
data class Holding(
    val companySymbol: String,
    val shares: Int,
    val averagePurchasePrice: Money,
    val currentPrice: Money
) {
    val currentValue: Money get() = Money(shares * currentPrice.amount)
    val profitLoss: Money get() = Money(shares * (currentPrice.amount - averagePurchasePrice.amount))
    val profitLossRate: Double get() = 
        if (averagePurchasePrice.amount != 0.0) {
            (currentPrice.amount - averagePurchasePrice.amount) / averagePurchasePrice.amount
        } else 0.0
    
    fun formatProfitLossRate(): String = "${String.format("%+.1f", profitLossRate * 100)}%"
}