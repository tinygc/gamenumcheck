package com.tinygc.numcheck.domain.usecase.game

import com.tinygc.numcheck.domain.model.Difficulty
import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.NewsEvent
import com.tinygc.numcheck.domain.model.PricePoint
import com.tinygc.numcheck.domain.model.Stock
import com.tinygc.numcheck.domain.repository.GameStateRepository
import com.tinygc.numcheck.domain.repository.NewsRepository
import com.tinygc.numcheck.domain.repository.StockRepository
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

class NextDayUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val stockRepository: StockRepository,
    private val newsRepository: NewsRepository
) {
    
    suspend operator fun invoke(gameState: GameState): Result<GameState> {
        return try {
            // ゲーム終了チェック
            if (gameState.isGameOver) {
                return Result.failure(GameAlreadyEndedException("ゲームは既に終了しています"))
            }
            
            // 1. ニュースを生成
            val todaysNews = newsRepository.generateRandomNews(
                companies = gameState.availableStocks.map { it.company },
                day = gameState.currentDay + 1,
                difficulty = gameState.difficulty
            )
            
            // 2. 株価を更新
            val updatedStocks = updateStockPrices(gameState.availableStocks, todaysNews, gameState.difficulty)
            
            // 3. ポートフォリオの株価を更新
            val updatedPortfolio = updatedStocks.fold(gameState.portfolio) { portfolio, stock ->
                portfolio.updateStockPrice(stock.company.symbol, stock.currentPrice)
            }
            
            // 4. 新企業の解放チェック
            val newUnlockedCompanies = checkForNewUnlocks(gameState, updatedStocks)
            
            // 5. ゲーム状態を更新
            val nextGameState = gameState
                .nextDay()
                .updatePortfolio(updatedPortfolio)
                .updateStocks(updatedStocks)
                .copy(unlockedCompanies = gameState.unlockedCompanies + newUnlockedCompanies)
            
            // 6. データを保存
            todaysNews.forEach { newsRepository.saveNews(it) }
            updatedStocks.forEach { stock ->
                stockRepository.updateStockPrice(stock.company.symbol, stock.currentPrice)
                stockRepository.addPricePoint(
                    stock.company.symbol,
                    PricePoint(stock.currentPrice, System.currentTimeMillis() / 1000)
                )
            }
            gameStateRepository.saveGameState(nextGameState)
            
            Result.success(nextGameState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun updateStockPrices(
        stocks: List<Stock>,
        news: List<NewsEvent>,
        difficulty: Difficulty
    ): List<Stock> {
        return stocks.map { stock ->
            val baseVolatility = stock.company.volatility.multiplier
            val difficultyMultiplier = difficulty.getVolatilityMultiplier()
            
            // ニュースの影響を計算
            val newsImpact = news
                .filter { it.affectedCompanies.contains(stock.company.symbol) }
                .fold(0.0) { acc, newsEvent -> acc + newsEvent.impact.getRandomImpact() }
            
            // ランダムな市場変動
            val randomChange = generateRandomPriceChange(baseVolatility * difficultyMultiplier)
            
            // 総合的な価格変動
            val totalChange = (newsImpact + randomChange).coerceIn(-0.3, 0.3) // 最大±30%の制限
            val newPrice = Money(stock.currentPrice.amount * (1.0 + totalChange))
            
            // 価格履歴を更新
            val nowEpoch = System.currentTimeMillis() / 1000
            val newPricePoint = PricePoint(newPrice, nowEpoch)
            val updatedHistory = (stock.priceHistory + newPricePoint).takeLast(30) // 過去30日分を保持
            
            stock.copy(
                currentPrice = newPrice,
                previousPrice = stock.currentPrice,
                priceHistory = updatedHistory,
                lastUpdated = nowEpoch
            )
        }
    }
    
    private fun generateRandomPriceChange(volatility: Double): Double {
        // 正規分布に近い変動を生成
        val random1 = Random.nextDouble()
        val random2 = Random.nextDouble()
        val normalRandom = kotlin.math.sqrt(-2.0 * kotlin.math.ln(random1)) * kotlin.math.cos(2.0 * kotlin.math.PI * random2)
        
        return normalRandom * volatility * 0.05 // 基本変動幅5%
    }
    
    private fun checkForNewUnlocks(gameState: GameState, updatedStocks: List<Stock>): Set<String> {
        val currentLevel = calculatePlayerLevel(gameState)
        return updatedStocks
            .filter { stock ->
                !gameState.unlockedCompanies.contains(stock.company.symbol) &&
                stock.company.unlockLevel <= currentLevel
            }
            .map { it.company.symbol }
            .toSet()
    }
    
    private fun calculatePlayerLevel(gameState: GameState): Int {
        val assetMultiplier = gameState.portfolio.getTotalAssets().amount / 1_000_000.0
        val dayBonus = gameState.currentDay / 5
        val tradeBonus = gameState.totalTrades / 10
        
        return (assetMultiplier + dayBonus + tradeBonus).toInt().coerceAtLeast(1)
    }
}

class GameAlreadyEndedException(message: String) : Exception(message)