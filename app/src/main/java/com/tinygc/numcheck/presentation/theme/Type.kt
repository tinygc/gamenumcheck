package com.tinygc.numcheck.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object StockSimTypography {
    val Headline1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)    // タイトル
    val Headline2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold) // セクション
    val Body1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)     // 本文
    val Body2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)     // 小さな本文
    val Caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)   // キャプション
    val Price = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)       // 価格表示
    val PriceChange = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium) // 変動率
}

// Material3 Typography
val Typography = Typography(
    displayLarge = StockSimTypography.Headline1,
    displayMedium = StockSimTypography.Headline2,
    bodyLarge = StockSimTypography.Body1,
    bodyMedium = StockSimTypography.Body2,
    labelSmall = StockSimTypography.Caption
)