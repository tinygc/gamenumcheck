package com.tinygc.numcheck.domain.usecase.trading

import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.Transaction
import com.tinygc.numcheck.domain.model.TransactionType
import com.tinygc.numcheck.domain.model.TransactionValidation
import com.tinygc.numcheck.domain.repository.GameStateRepository
import com.tinygc.numcheck.domain.repository.TransactionRepository
import javax.inject.Inject

class BuyStockUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val transactionRepository: TransactionRepository
) {
    
    suspend operator fun invoke(
        gameState: GameState,
        symbol: String,
        shares: Int
    ): Result<BuyStockResult> {
        return try {
            // 株式が利用可能かチェック
            val stock = gameState.getUnlockedStocks().find { it.company.symbol == symbol }
                ?: return Result.failure(StockNotAvailableException("株式が見つかりません: $symbol"))
            
            // 取引の妥当性をチェック
            val validation = TransactionValidation.validateBuy(
                cash = gameState.portfolio.cash,
                shares = shares,
                pricePerShare = stock.currentPrice
            )
            
            if (!validation.isValid) {
                return Result.failure(InvalidTransactionException(validation.errorMessage ?: "不明なエラー"))
            }
            
            // 取引を実行
            val nowEpoch = System.currentTimeMillis() / 1000
            val totalAmount = Money(shares * stock.currentPrice.amount)
            val commission = totalAmount * 0.001 // 0.1%手数料
            
            val transaction = Transaction(
                id = "txn_${System.currentTimeMillis()}_${(1000..9999).random()}",
                companySymbol = symbol,
                type = TransactionType.BUY,
                shares = shares,
                pricePerShare = stock.currentPrice,
                totalAmount = totalAmount,
                timestamp = nowEpoch,
                day = gameState.currentDay,
                commission = commission
            )
            
            // ポートフォリオを更新
            val updatedPortfolio = gameState.portfolio.addHolding(
                symbol = symbol,
                shares = shares,
                price = stock.currentPrice
            )
            
            // ゲーム状態を更新（取引数も更新）
            val updatedGameState = gameState
                .updatePortfolio(updatedPortfolio)
                .addTrade(isWinning = false) // 購入時点では勝敗は未確定
            
            // データを保存
            transactionRepository.saveTransaction(transaction)
            gameStateRepository.saveGameState(updatedGameState)
            
            Result.success(
                BuyStockResult(
                    gameState = updatedGameState,
                    transaction = transaction
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class BuyStockResult(
    val gameState: GameState,
    val transaction: Transaction
)

class StockNotAvailableException(message: String) : Exception(message)
class InvalidTransactionException(message: String) : Exception(message)