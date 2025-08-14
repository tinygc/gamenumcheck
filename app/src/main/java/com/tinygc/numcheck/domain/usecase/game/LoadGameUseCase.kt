package com.tinygc.numcheck.domain.usecase.game

import com.tinygc.numcheck.domain.model.GameState
import com.tinygc.numcheck.domain.repository.GameStateRepository
import javax.inject.Inject

class LoadGameUseCase @Inject constructor(
    private val gameStateRepository: GameStateRepository
) {
    
    suspend operator fun invoke(): Result<GameState> {
        return try {
            val gameState = gameStateRepository.loadGameState()
            if (gameState != null) {
                Result.success(gameState)
            } else {
                Result.failure(NoSaveDataException("保存されたゲームデータが見つかりません"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun hasSavedGame(): Boolean {
        return gameStateRepository.hasSavedGame()
    }
}

class NoSaveDataException(message: String) : Exception(message)