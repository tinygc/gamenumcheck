package com.tinygc.numcheck.data.local.dao

import androidx.room.*
import com.tinygc.numcheck.data.local.entity.PriceHistoryEntity
import com.tinygc.numcheck.data.local.entity.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    suspend fun getAllStocks(): List<StockEntity>
    
    @Query("SELECT * FROM stocks")
    fun observeAllStocks(): Flow<List<StockEntity>>
    
    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?
    
    @Query("SELECT * FROM stocks WHERE symbol = :symbol")
    fun observeStockBySymbol(symbol: String): Flow<StockEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)
    
    @Update
    suspend fun updateStock(stock: StockEntity)
    
    @Query("UPDATE stocks SET currentPrice = :price, previousPrice = currentPrice, lastUpdated = :timestamp WHERE symbol = :symbol")
    suspend fun updateStockPrice(symbol: String, price: Double, timestamp: Long)
    
    // 価格履歴関連
    @Insert
    suspend fun insertPriceHistory(priceHistory: PriceHistoryEntity)
    
    @Query("SELECT * FROM price_history WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getPriceHistory(symbol: String, limit: Int): List<PriceHistoryEntity>
    
    @Query("DELETE FROM price_history WHERE symbol = :symbol AND timestamp < :cutoffTime")
    suspend fun deleteOldPriceHistory(symbol: String, cutoffTime: Long)
}