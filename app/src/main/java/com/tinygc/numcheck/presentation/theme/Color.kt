package com.tinygc.numcheck.presentation.theme

import androidx.compose.ui.graphics.Color

object StockSimColors {
    // メインカラー
    val Primary = Color(0xFF2E7D32)      // 深緑 (投資・お金)
    val PrimaryVariant = Color(0xFF1B5E20) // より深い緑
    val Secondary = Color(0xFF00BCD4)    // シアン (情報・データ)
    val SecondaryVariant = Color(0xFF0097A7)
    
    // 状態カラー
    val Profit = Color(0xFF4CAF50)       // 利益 (緑)
    val Loss = Color(0xFFF44336)         // 損失 (赤)
    val Neutral = Color(0xFF9E9E9E)      // 変動なし (グレー)
    val Warning = Color(0xFFFF9800)      // 警告 (オレンジ)
    
    // 背景・テキスト
    val Background = Color(0xFFFAFAFA)   // 薄いグレー
    val Surface = Color(0xFFFFFFFF)      // 白
    val OnPrimary = Color(0xFFFFFFFF)    // 白文字
    val OnSecondary = Color(0xFFFFFFFF)  // 白文字
    val OnSurface = Color(0xFF212121)    // 黒文字
    val OnSurfaceVariant = Color(0xFF757575) // グレー文字
    val OnBackground = Color(0xFF212121) // 黒文字
    
    // エラー
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
}