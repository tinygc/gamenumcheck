package com.tinygc.numcheck.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.tinygc.numcheck.data.local.dao.StockDao
import com.tinygc.numcheck.data.local.entity.PriceHistoryEntity
import com.tinygc.numcheck.data.local.entity.StockEntity

@Database(
    entities = [
        StockEntity::class,
        PriceHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StockSimDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    
    companion object {
        @Volatile
        private var INSTANCE: StockSimDatabase? = null
        
        fun getDatabase(context: Context): StockSimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockSimDatabase::class.java,
                    "stock_sim_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}