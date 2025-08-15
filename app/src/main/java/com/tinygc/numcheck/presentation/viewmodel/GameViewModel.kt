package com.tinygc.numcheck.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinygc.numcheck.domain.model.*
import com.tinygc.numcheck.domain.usecase.game.LoadGameUseCase
import com.tinygc.numcheck.domain.usecase.game.NextDayUseCase
import com.tinygc.numcheck.domain.usecase.game.StartNewGameUseCase
import com.tinygc.numcheck.domain.usecase.trading.BuyStockUseCase
import com.tinygc.numcheck.domain.usecase.trading.SellStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val startNewGameUseCase: StartNewGameUseCase,
    private val loadGameUseCase: LoadGameUseCase,
    private val nextDayUseCase: NextDayUseCase,
    private val buyStockUseCase: BuyStockUseCase,
    private val sellStockUseCase: SellStockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame(difficulty: Difficulty = Difficulty.NORMAL) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = startNewGameUseCase(difficulty)
                result.onSuccess { gameState ->
                    _gameState.value = gameState
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "ゲーム開始に失敗しました"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "不明なエラー"
                )
            }
        }
    }

    fun buyStock(symbol: String, shares: Int) {
        val currentGameState = _gameState.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = buyStockUseCase(currentGameState, symbol, shares)
                result.onSuccess { buyResult ->
                    _gameState.value = buyResult.gameState
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "株式購入が完了しました：${symbol} ${shares}株",
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "株式購入に失敗しました"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "不明なエラー"
                )
            }
        }
    }

    fun sellStock(symbol: String, shares: Int) {
        val currentGameState = _gameState.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = sellStockUseCase(currentGameState, symbol, shares)
                result.onSuccess { sellResult ->
                    _gameState.value = sellResult.gameState
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "株式売却が完了しました：${symbol} ${shares}株",
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "株式売却に失敗しました"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "不明なエラー"
                )
            }
        }
    }

    fun nextDay() {
        val currentGameState = _gameState.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = nextDayUseCase(currentGameState)
                result.onSuccess { newGameState ->
                    _gameState.value = newGameState
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "第${newGameState.currentDay}日が開始されました",
                        errorMessage = null
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "日次進行に失敗しました"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "不明なエラー"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun getStockList(): List<Stock> {
        return _gameState.value?.getUnlockedStocks() ?: emptyList()
    }

    fun getPortfolioHoldings(): List<Holding> {
        return _gameState.value?.portfolio?.holdings?.values?.toList() ?: emptyList()
    }

    fun getTotalAssets(): Money {
        return _gameState.value?.portfolio?.getTotalAssets() ?: Money(1000000.0)
    }

    fun getCurrentCash(): Money {
        return _gameState.value?.portfolio?.cash ?: Money(1000000.0)
    }

    fun getStockValue(): Money {
        return _gameState.value?.portfolio?.getTotalStockValue() ?: Money(0.0)
    }

    fun getProfitRate(): Double {
        val portfolio = _gameState.value?.portfolio ?: return 0.0
        val initialCash = Portfolio.INITIAL_CASH
        val currentAssets = portfolio.getTotalAssets().amount
        return ((currentAssets - initialCash) / initialCash.toDouble()) * 100
    }

    fun getCurrentDay(): Int {
        return _gameState.value?.currentDay ?: 1
    }

    fun getMaxDays(): Int {
        return _gameState.value?.maxDays ?: 30
    }

    fun isGameOver(): Boolean {
        return _gameState.value?.isGameOver ?: false
    }
}

data class GameUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)