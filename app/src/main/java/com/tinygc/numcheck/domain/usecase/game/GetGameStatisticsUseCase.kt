package com.tinygc.numcheck.domain.usecase.game

import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.Transaction
import com.tinygc.numcheck.domain.model.TransactionType
import com.tinygc.numcheck.domain.repository.TransactionRepository
import javax.inject.Inject

class GetGameStatisticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    
    suspend operator fun invoke(gameState: GameState): Result<GameStatistics> {
        return try {
            val transactions = transactionRepository.getTransactionHistory()
            
            val statistics = GameStatistics(
                totalAssets = gameState.portfolio.getTotalAssets(),
                initialAssets = gameState.portfolio.initialCash,
                profitLoss = gameState.portfolio.getProfitLoss(),
                profitRate = gameState.portfolio.getProfitRate(),
                totalTrades = gameState.totalTrades,
                winningTrades = gameState.winningTrades,
                winRate = gameState.winRate,
                currentDay = gameState.currentDay,
                maxDays = gameState.maxDays,
                stockPerformances = calculateStockPerformances(transactions, gameState),
                tradingFrequency = calculateTradingFrequency(transactions, gameState.currentDay),
                portfolioDiversification = calculateDiversification(gameState)
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateStockPerformances(
        transactions: List<Transaction>,
        gameState: GameState
    ): List<StockPerformance> {
        val stockGroups = transactions.groupBy { it.companySymbol }
        
        return stockGroups.map { (symbol, stockTransactions) ->
            val buys = stockTransactions.filter { it.type == TransactionType.BUY }
            val sells = stockTransactions.filter { it.type == TransactionType.SELL }
            
            val totalBought = buys.sumOf { it.shares }
            val totalSold = sells.sumOf { it.shares }
            val averageBuyPrice = if (buys.isNotEmpty()) {
                buys.sumOf { it.shares * it.pricePerShare.amount } / totalBought
            } else 0.0
            
            val currentHolding = gameState.portfolio.getHolding(symbol)
            val currentPrice = gameState.availableStocks
                .find { it.company.symbol == symbol }?.currentPrice?.amount ?: 0.0
            
            // 実現損益 + 含み損益を計算
            val realizedProfitLoss = sells.sumOf { sell ->
                val avgBuyPrice = buys.filter { it.timestamp <= sell.timestamp }
                    .let { pastBuys ->
                        if (pastBuys.isNotEmpty()) {
                            pastBuys.sumOf { it.shares * it.pricePerShare.amount } / pastBuys.sumOf { it.shares }
                        } else averageBuyPrice
                    }
                sell.shares * (sell.pricePerShare.amount - avgBuyPrice)
            }
            
            val unrealizedProfitLoss = currentHolding?.let { holding ->
                holding.shares * (currentPrice - holding.averagePurchasePrice.amount)
            } ?: 0.0
            
            val totalProfitLoss = realizedProfitLoss + unrealizedProfitLoss
            val profitRate = if (averageBuyPrice > 0) {
                totalProfitLoss / (totalBought * averageBuyPrice)
            } else 0.0
            
            StockPerformance(
                symbol = symbol,
                totalProfitLoss = Money(totalProfitLoss),
                profitRate = profitRate,
                totalTrades = stockTransactions.size,
                winningTrades = calculateWinningTrades(stockTransactions),
                currentHolding = currentHolding?.shares ?: 0
            )
        }.sortedByDescending { it.profitRate }
    }
    
    private fun calculateWinningTrades(transactions: List<Transaction>): Int {
        // 簡易的な勝敗判定（売却時の価格が購入時より高い場合を勝ちとする）
        val sells = transactions.filter { it.type == TransactionType.SELL }
        val buys = transactions.filter { it.type == TransactionType.BUY }
        
        return sells.count { sell ->
            val correspondingBuy = buys
                .filter { it.timestamp <= sell.timestamp }
                .maxByOrNull { it.timestamp }
            
            correspondingBuy?.let { buy ->
                sell.pricePerShare.amount > buy.pricePerShare.amount
            } ?: false
        }
    }
    
    private fun calculateTradingFrequency(transactions: List<Transaction>, currentDay: Int): Double {
        return if (currentDay > 0) transactions.size.toDouble() / currentDay else 0.0
    }
    
    private fun calculateDiversification(gameState: GameState): Double {
        val totalStockValue = gameState.portfolio.getTotalStockValue().amount
        if (totalStockValue == 0.0) return 0.0
        
        val holdings = gameState.portfolio.holdings.values
        val variance = holdings.fold(0.0) { acc, holding ->
            val weight = holding.currentValue.amount / totalStockValue
            acc + weight * weight
        }
        
        // ハーフィンダール指数の逆数（1に近いほど分散）
        return 1.0 - variance
    }
}

data class GameStatistics(
    val totalAssets: Money,
    val initialAssets: Money,
    val profitLoss: Money,
    val profitRate: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val winRate: Double,
    val currentDay: Int,
    val maxDays: Int,
    val stockPerformances: List<StockPerformance>,
    val tradingFrequency: Double,
    val portfolioDiversification: Double
) {
    fun getInvestmentRating(): InvestmentRating {
        return when {
            profitRate >= 1.0 && winRate >= 0.8 -> InvestmentRating.LEGENDARY
            profitRate >= 0.5 && winRate >= 0.7 -> InvestmentRating.EXCELLENT
            profitRate >= 0.25 && winRate >= 0.6 -> InvestmentRating.GOOD
            profitRate >= 0.0 && winRate >= 0.5 -> InvestmentRating.AVERAGE
            else -> InvestmentRating.BEGINNER
        }
    }
}

data class StockPerformance(
    val symbol: String,
    val totalProfitLoss: Money,
    val profitRate: Double,
    val totalTrades: Int,
    val winningTrades: Int,
    val currentHolding: Int
) {
    val winRate: Double get() = if (totalTrades > 0) winningTrades.toDouble() / totalTrades else 0.0
}

enum class InvestmentRating(
    val displayName: String,
    val stars: Int,
    val emoji: String
) {
    LEGENDARY("伝説の投資家", 5, "🏆"),
    EXCELLENT("優秀な投資家", 4, "🚀"),
    GOOD("堅実な投資家", 3, "👍"),
    AVERAGE("平均的な投資家", 2, "📊"),
    BEGINNER("初心者投資家", 1, "📚")
}