package com.tinygc.numcheck.domain.repository

import com.tinygc.numcheck.domain.model.GameState
import kotlinx.coroutines.flow.Flow

interface GameStateRepository {
    suspend fun saveGameState(gameState: GameState)
    suspend fun loadGameState(): GameState?
    suspend fun deleteGameState()
    suspend fun hasSavedGame(): Boolean
    
    fun observeGameState(): Flow<GameState?>
}