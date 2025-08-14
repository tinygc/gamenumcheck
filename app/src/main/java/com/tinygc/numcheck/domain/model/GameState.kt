package com.tinygc.numcheck.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val currentDay: Int,
    val maxDays: Int,
    val portfolio: Portfolio,
    val availableStocks: List<Stock>,
    val unlockedCompanies: Set<String>,
    val gameStatus: GameStatus,
    val difficulty: Difficulty,
    val totalTrades: Int = 0,
    val winningTrades: Int = 0
) {
    val isGameOver: Boolean get() = currentDay >= maxDays
    val remainingDays: Int get() = maxDays - currentDay
    val winRate: Double get() = if (totalTrades > 0) winningTrades.toDouble() / totalTrades else 0.0
    
    fun nextDay(): GameState {
        return copy(
            currentDay = currentDay + 1,
            gameStatus = if (currentDay + 1 >= maxDays) GameStatus.COMPLETED else GameStatus.IN_PROGRESS
        )
    }
    
    fun addTrade(isWinning: Boolean): GameState {
        return copy(
            totalTrades = totalTrades + 1,
            winningTrades = if (isWinning) winningTrades + 1 else winningTrades
        )
    }
    
    fun updatePortfolio(newPortfolio: Portfolio): GameState {
        return copy(portfolio = newPortfolio)
    }
    
    fun updateStocks(newStocks: List<Stock>): GameState {
        return copy(availableStocks = newStocks)
    }
    
    fun unlockCompany(symbol: String): GameState {
        return copy(unlockedCompanies = unlockedCompanies + symbol)
    }
    
    fun getUnlockedStocks(): List<Stock> {
        return availableStocks.filter { stock ->
            unlockedCompanies.contains(stock.company.symbol)
        }
    }
    
    fun canUnlockNewCompanies(): Boolean {
        val currentLevel = calculateLevel()
        return availableStocks.any { stock ->
            !unlockedCompanies.contains(stock.company.symbol) && 
            stock.company.unlockLevel <= currentLevel
        }
    }
    
    private fun calculateLevel(): Int {
        // プレイヤーレベルの計算（総資産、日数、取引回数から算出）
        val assetMultiplier = portfolio.getTotalAssets().amount / Portfolio.INITIAL_CASH
        val dayBonus = currentDay / 5 // 5日ごとに+1
        val tradeBonus = totalTrades / 10 // 10取引ごとに+1
        
        return (assetMultiplier + dayBonus + tradeBonus).toInt().coerceAtLeast(1)
    }
    
    companion object {
        const val DEFAULT_MAX_DAYS = 30
        
        fun createNew(difficulty: Difficulty): GameState {
            return GameState(
                currentDay = 1,
                maxDays = DEFAULT_MAX_DAYS,
                portfolio = Portfolio.createInitial(),
                availableStocks = emptyList(), // 後でロードされる
                unlockedCompanies = emptySet(), // 初期企業は後で設定
                gameStatus = GameStatus.IN_PROGRESS,
                difficulty = difficulty
            )
        }
    }
}

@Serializable
enum class GameStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED
}

@Serializable
enum class Difficulty(val displayName: String, val emoji: String) {
    EASY("イージー", "🟢"),
    NORMAL("ノーマル", "🟡"),
    HARD("ハード", "🔴");
    
    fun getVolatilityMultiplier(): Double = when (this) {
        EASY -> 0.7
        NORMAL -> 1.0
        HARD -> 1.5
    }
    
    fun getPositiveNewsMultiplier(): Double = when (this) {
        EASY -> 1.5
        NORMAL -> 1.0
        HARD -> 0.7
    }
}