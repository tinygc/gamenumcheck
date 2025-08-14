package com.tinygc.numcheck.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val currentPrice: Double,
    val previousPrice: Double,
    val lastUpdated: Long
)