package com.tinygc.numcheck.presentation.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinygc.numcheck.presentation.theme.StockSimColors
import com.tinygc.numcheck.presentation.theme.StockSimTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToTitle: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "splash_alpha"
    )
    
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToTitle()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StockSimColors.Background)
            .alpha(animatedAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        
        // アイコン
        Text(
            text = "💰",
            style = StockSimTypography.Headline1.copy(
                fontSize = 64.sp
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // タイトル
        Text(
            text = "STOCK SIMULATOR",
            style = StockSimTypography.Headline1.copy(
                fontSize = 28.sp,
                color = StockSimColors.Primary
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "〜投資の世界へようこそ〜",
            style = StockSimTypography.Body2.copy(
                color = StockSimColors.OnSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // ローディングインジケーター
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = StockSimColors.Secondary,
            strokeWidth = 3.dp
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // バージョン情報
        Text(
            text = "Version 1.0.0",
            style = StockSimTypography.Caption.copy(
                color = StockSimColors.OnSurfaceVariant
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}