package com.tinygc.numcheck.domain.repository

import com.tinygc.numcheck.domain.model.Transaction
import com.tinygc.numcheck.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun getTransactionHistory(): List<Transaction>
    suspend fun getTransactionsByDay(day: Int): List<Transaction>
    suspend fun getTransactionsByType(type: TransactionType): List<Transaction>
    suspend fun getTransactionsBySymbol(symbol: String): List<Transaction>
    suspend fun deleteAllTransactions()
    
    fun observeTransactions(): Flow<List<Transaction>>
}