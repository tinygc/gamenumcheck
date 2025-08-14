package com.tinygc.numcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.tinygc.numcheck.presentation.screen.SplashScreen
import com.tinygc.numcheck.ui.theme.StockSimTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockSimTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StockSimApp()
                }
            }
        }
    }
}

@Composable
fun StockSimApp() {
    var showSplash by remember { mutableStateOf(true) }
    
    if (showSplash) {
        SplashScreen(
            onNavigateToTitle = {
                showSplash = false
            }
        )
    } else {
        // TODO: タイトル画面への遷移
        // 現在はスプラッシュのままループ
        LaunchedEffect(Unit) {
            delay(1000)
            showSplash = true
        }
    }
}