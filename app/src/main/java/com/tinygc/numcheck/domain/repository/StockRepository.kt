package com.tinygc.numcheck.domain.repository

import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.PricePoint
import com.tinygc.numcheck.domain.model.Stock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getAllStocks(): List<Stock>
    suspend fun getStockBySymbol(symbol: String): Stock?
    suspend fun updateStockPrice(symbol: String, newPrice: Money)
    suspend fun updateMultipleStockPrices(updates: Map<String, Money>)
    suspend fun getStockHistory(symbol: String, days: Int): List<PricePoint>
    suspend fun addPricePoint(symbol: String, pricePoint: PricePoint)
    
    fun observeStocks(): Flow<List<Stock>>
    fun observeStockBySymbol(symbol: String): Flow<Stock?>
}