package com.tinygc.numcheck.domain.usecase.trading

import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.Transaction
import com.tinygc.numcheck.domain.model.TransactionType
import com.tinygc.numcheck.domain.model.TransactionValidation
import com.tinygc.numcheck.domain.repository.GameStateRepository
import com.tinygc.numcheck.domain.repository.TransactionRepository
import java.time.LocalDateTime
import javax.inject.Inject

class SellStockUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository,
    private val transactionRepository: TransactionRepository
) {
    
    suspend operator fun invoke(
        gameState: GameState,
        symbol: String,
        shares: Int
    ): Result<SellStockResult> {
        return try {
            // 株式が利用可能かチェック
            val stock = gameState.getUnlockedStocks().find { it.company.symbol == symbol }
                ?: return Result.failure(StockNotAvailableException("株式が見つかりません: $symbol"))
            
            // 保有状況をチェック
            val holding = gameState.portfolio.getHolding(symbol)
            
            // 取引の妥当性をチェック
            val validation = TransactionValidation.validateSell(
                holding = holding,
                shares = shares
            )
            
            if (!validation.isValid) {
                return Result.failure(InvalidTransactionException(validation.errorMessage ?: "不明なエラー"))
            }
            
            // 損益を計算（勝敗判定用）
            val isWinning = holding?.let { h ->
                stock.currentPrice.amount > h.averagePurchasePrice.amount
            } ?: false
            
            // 取引を実行
            val nowEpoch = System.currentTimeMillis() / 1000
            val totalAmount = Money(shares * stock.currentPrice.amount)
            val commission = totalAmount * 0.001 // 0.1%手数料
            
            val transaction = Transaction(
                id = "txn_${System.currentTimeMillis()}_${(1000..9999).random()}",
                companySymbol = symbol,
                type = TransactionType.SELL,
                shares = shares,
                pricePerShare = stock.currentPrice,
                totalAmount = totalAmount,
                timestamp = nowEpoch,
                day = gameState.currentDay,
                commission = commission
            )
            
            // ポートフォリオを更新
            val updatedPortfolio = gameState.portfolio.removeHolding(
                symbol = symbol,
                shares = shares,
                price = stock.currentPrice
            )
            
            // ゲーム状態を更新（勝敗も更新）
            val updatedGameState = gameState
                .updatePortfolio(updatedPortfolio)
                .addTrade(isWinning = isWinning)
            
            // データを保存
            transactionRepository.saveTransaction(transaction)
            gameStateRepository.saveGameState(updatedGameState)
            
            Result.success(
                SellStockResult(
                    gameState = updatedGameState,
                    transaction = transaction,
                    profitLoss = holding?.let { h ->
                        stock.currentPrice - h.averagePurchasePrice
                    } ?: Money.ZERO
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class SellStockResult(
    val gameState: GameState,
    val transaction: Transaction,
    val profitLoss: Money
)