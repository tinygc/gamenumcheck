package com.tinygc.numcheck.domain.usecase.game

import com.tinygc.numcheck.domain.model.Difficulty
import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.model.Stock
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.PricePoint
import com.tinygc.numcheck.domain.model.Company
import com.tinygc.numcheck.domain.repository.CompanyRepository
import com.tinygc.numcheck.domain.repository.GameStateRepository
import com.tinygc.numcheck.domain.repository.StockRepository
import com.tinygc.numcheck.domain.repository.TransactionRepository
import com.tinygc.numcheck.domain.repository.NewsRepository
import java.time.LocalDateTime
import javax.inject.Inject

class StartNewGameUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val companyRepository: CompanyRepository,
    private val stockRepository: StockRepository,
    private val transactionRepository: TransactionRepository,
    private val newsRepository: NewsRepository
) {
    
    suspend operator fun invoke(difficulty: Difficulty): Result<GameState> {
        return try {
            // 既存のデータをクリア
            gameStateRepository.deleteGameState()
            transactionRepository.deleteAllTransactions()
            newsRepository.deleteAllNews()
            
            // 新しいゲーム状態を作成
            val initialCompanies = companyRepository.getInitialCompanies()
            val unlockedSymbols = initialCompanies.take(INITIAL_UNLOCKED_COMPANIES).map { it.symbol }.toSet()
            
            val gameState = GameState.createNew(difficulty).copy(
                unlockedCompanies = unlockedSymbols
            )
            
            // 初期株価を設定（基準価格からランダムに生成）
            val initialStocks = initialCompanies.map { company ->
                val basePrice = getBasePriceForCompany(company.symbol)
                createInitialStock(company, basePrice)
            }
            
            // 株価データを保存
            initialStocks.forEach { stock ->
                stockRepository.updateStockPrice(stock.company.symbol, stock.currentPrice)
            }
            
            val finalGameState = gameState.copy(availableStocks = initialStocks)
            
            // ゲーム状態を保存
            gameStateRepository.saveGameState(finalGameState)
            
            Result.success(finalGameState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getBasePriceForCompany(symbol: String): Double {
        // 企業ごとの基準価格を設定
        return when (symbol) {
            "CYBT" -> 1000.0
            "DGTL" -> 1800.0
            "FOOD" -> 800.0
            "RETL" -> 1100.0
            "ENGY" -> 1500.0
            "MDCL" -> 2200.0
            "MTRL" -> 950.0
            "CHMC" -> 1350.0
            "ENTM" -> 780.0
            "SPRT" -> 920.0
            else -> 1000.0
        }
    }
    
    private fun createInitialStock(company: Company, basePrice: Double): Stock {
        // 基準価格から±5%の範囲でランダムに初期価格を設定
        val variation = 0.05
        val randomFactor = 1.0 + (Math.random() - 0.5) * 2 * variation
        val initialPrice = Money(basePrice * randomFactor)
        
        val nowEpoch = System.currentTimeMillis() / 1000
        val pricePoint = PricePoint(initialPrice, nowEpoch)
        
        return Stock(
            company = company,
            currentPrice = initialPrice,
            previousPrice = initialPrice, // 初日は変動なし
            priceHistory = listOf(pricePoint),
            lastUpdated = nowEpoch
        )
    }
    
    companion object {
        private const val INITIAL_UNLOCKED_COMPANIES = 6 // 最初から利用可能な企業数
    }
}