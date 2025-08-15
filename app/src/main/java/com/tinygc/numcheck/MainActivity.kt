package com.tinygc.numcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinygc.numcheck.domain.model.Money
import com.tinygc.numcheck.domain.model.Stock
import com.tinygc.numcheck.presentation.screen.SplashScreen
import com.tinygc.numcheck.presentation.viewmodel.GameViewModel
import com.tinygc.numcheck.ui.theme.StockSimTheme
// import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

// @AndroidEntryPoint
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
    var currentScreen by remember { mutableStateOf("splash") }
    
    when (currentScreen) {
        "splash" -> {
            SplashScreen(
                onNavigateToTitle = {
                    currentScreen = "title"
                }
            )
        }
        "title" -> {
            TitleScreen(
                onStartGame = {
                    currentScreen = "game"
                }
            )
        }
        "game" -> {
            // ViewModelは暫定的に直接インスタンス化（本来はHiltで注入）
            GameScreen()
        }
    }
}

@Composable
fun TitleScreen(onStartGame: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // タイトルロゴ
            Text(
                text = "💰",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "株式投資シミュレーター",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "〜投資の世界へようこそ〜",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // ゲーム開始ボタン
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "ゲーム開始",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 設定ボタン（暫定）
            OutlinedButton(
                onClick = { /* TODO: 設定画面へ */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "設定",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GameScreen() {
    // ゲーム状態の管理
    var gameDay by remember { mutableStateOf(1) }
    var currentCash by remember { mutableStateOf(1000000) }
    var stockValue by remember { mutableStateOf(0) }
    var totalAssets by remember { mutableStateOf(currentCash + stockValue) }
    var profitRate by remember { mutableStateOf(0.0) }
    
    // 時間管理（9:00〜17:00の取引時間）
    var currentHour by remember { mutableStateOf(9) }
    var currentMinute by remember { mutableStateOf(0) }
    var isMarketOpen by remember { mutableStateOf(true) }
    
    // 株式データの管理
    var stockList by remember { mutableStateOf(generateInitialStocks()) }
    var holdings by remember { mutableStateOf(mutableMapOf<String, Int>()) }
    
    // ポートフォリオデータ（購入履歴追跡用）
    var portfolioData by remember { mutableStateOf(mutableMapOf<String, PortfolioItem>()) }
    
    // UI状態（エラーメッセージのみ）
    var hasError by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    
    // ニュース・イベント管理
    var newsEvents by remember { mutableStateOf(generateDailyNews(gameDay)) }
    var lastNewsDay by remember { mutableStateOf(gameDay) }
    
    // 時間経過とマーケット管理（3秒ごとに15分進む）
    LaunchedEffect(gameDay) {
        while (true) {
            kotlinx.coroutines.delay(3000) // 3秒待機
            
            // 時間を15分進める
            val newMinute = currentMinute + 15
            val newHour = if (newMinute >= 60) currentHour + 1 else currentHour
            val finalMinute = newMinute % 60
            
            // 取引時間チェック（9:00〜17:00）
            val newIsMarketOpen = newHour in 9..16
            
            // 一日が終了したら次の日へ
            if (newHour >= 17) {
                // 日次処理
                val endOfDayStockList = simulateDailyMarketChange(stockList)
                val endOfDayStockValue = calculateStockValue(holdings, endOfDayStockList)
                val endOfDayTotalAssets = currentCash + endOfDayStockValue
                val endOfDayProfitRate = ((endOfDayTotalAssets - 1000000.0) / 1000000.0) * 100
                
                // 次の日の開始（9:00にリセット）
                if (gameDay < 30) {
                    val newGameDay = gameDay + 1
                    gameDay = newGameDay
                    currentHour = 9
                    currentMinute = 0
                    isMarketOpen = true
                    
                    // 新しい日のニュース生成
                    if (lastNewsDay != newGameDay) {
                        newsEvents = generateDailyNews(newGameDay)
                        lastNewsDay = newGameDay
                    }
                    
                    stockList = endOfDayStockList
                    stockValue = endOfDayStockValue
                    totalAssets = endOfDayTotalAssets
                    profitRate = endOfDayProfitRate
                } else {
                    // ゲーム終了
                    currentHour = 17
                    currentMinute = 0
                    isMarketOpen = false
                }
            } else {
                // 通常の時間進行
                currentHour = newHour
                currentMinute = finalMinute
                isMarketOpen = newIsMarketOpen
                
                // 取引時間中のみ株価変動
                if (isMarketOpen) {
                    // ニュースの影響を適用した株価変動
                    val baseStockList = updateStockPrices(stockList)
                    val newStockList = applyNewsEventImpact(baseStockList, newsEvents)
                    val newStockValue = calculateStockValue(holdings, newStockList)
                    val newTotalAssets = currentCash + newStockValue
                    val newProfitRate = ((newTotalAssets - 1000000.0) / 1000000.0) * 100
                    
                    // 状態を一括更新
                    stockList = newStockList
                    stockValue = newStockValue
                    totalAssets = newTotalAssets
                    profitRate = newProfitRate
                }
            }
        }
    }
    
    // エラーメッセージの自動消去（5秒後）
    LaunchedEffect(hasError) {
        if (hasError) {
            kotlinx.coroutines.delay(5000)
            hasError = false
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        
        // ヘッダー：ゲーム情報
        GameHeaderCard(
            gameDay = gameDay,
            currentTime = "${String.format("%02d", currentHour)}:${String.format("%02d", currentMinute)}",
            isMarketOpen = isMarketOpen,
            totalAssets = totalAssets,
            currentCash = currentCash,
            stockValue = stockValue,
            profitRate = profitRate,
            onNextDay = { 
                if (gameDay < 30) {
                    // 次の日へスキップ（時間を17:00にセット）
                    val newGameDay = gameDay + 1
                    val newStockList = simulateDailyMarketChange(stockList)
                    val newStockValue = calculateStockValue(holdings, newStockList)
                    val newTotalAssets = currentCash + newStockValue
                    val newProfitRate = ((newTotalAssets - 1000000.0) / 1000000.0) * 100
                    
                    // 状態を一括更新
                    gameDay = newGameDay
                    currentHour = 9
                    currentMinute = 0
                    isMarketOpen = true
                    stockList = newStockList
                    stockValue = newStockValue
                    totalAssets = newTotalAssets
                    profitRate = newProfitRate
                    
                    hasError = false
                }
            },
            maxDays = 30
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // タブ切り替え
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("株価一覧", "ポートフォリオ", "ニュース")
        
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // タブ内容
        when (selectedTab) {
            0 -> StockListTab(
                stockList = stockList,
                holdings = holdings,
                currentCash = currentCash,
                isMarketOpen = isMarketOpen,
                onBuyStock = { symbol, shares ->
                    val stock = stockList.find { it.symbol == symbol }
                    val totalCost = if (stock != null) stock.price * shares else 0
                    if (stock != null && currentCash >= totalCost) {
                        // 即座に全ての状態を更新
                        val newCash = currentCash - totalCost
                        val newHoldings = holdings.toMutableMap().apply {
                            this[symbol] = (this[symbol] ?: 0) + shares
                        }
                        
                        // ポートフォリオデータの更新
                        val newPortfolioData = portfolioData.toMutableMap()
                        val currentPortfolio = newPortfolioData[symbol]
                        val newPurchase = Purchase(shares, stock.price, gameDay, "${String.format("%02d", currentHour)}:${String.format("%02d", currentMinute)}")
                        
                        if (currentPortfolio == null) {
                            // 新規購入
                            newPortfolioData[symbol] = PortfolioItem(
                                symbol = symbol,
                                totalShares = shares,
                                averageCost = stock.price,
                                totalInvestment = totalCost,
                                purchases = listOf(newPurchase)
                            )
                        } else {
                            // 追加購入
                            val newTotalShares = currentPortfolio.totalShares + shares
                            val newTotalInvestment = currentPortfolio.totalInvestment + totalCost
                            val newAverageCost = newTotalInvestment / newTotalShares
                            newPortfolioData[symbol] = currentPortfolio.copy(
                                totalShares = newTotalShares,
                                averageCost = newAverageCost,
                                totalInvestment = newTotalInvestment,
                                purchases = currentPortfolio.purchases + newPurchase
                            )
                        }
                        
                        val newStockValue = calculateStockValue(newHoldings, stockList)
                        val newTotalAssets = newCash + newStockValue
                        val newProfitRate = ((newTotalAssets - 1000000.0) / 1000000.0) * 100
                        
                        // 状態を一括更新
                        currentCash = newCash
                        holdings = newHoldings
                        portfolioData = newPortfolioData
                        stockValue = newStockValue
                        totalAssets = newTotalAssets
                        profitRate = newProfitRate
                        
                        // ポップアップメッセージは表示しない
                    } else {
                        hasError = true
                        statusMessage = "現金が不足しています（必要額: ${String.format("%,d", totalCost)}円）"
                    }
                },
                onSellStock = { symbol, shares ->
                    val holdingShares = holdings[symbol] ?: 0
                    val stock = stockList.find { it.symbol == symbol }
                    if (holdingShares >= shares && stock != null && shares > 0) {
                        // 即座に全ての状態を更新
                        val totalRevenue = stock.price * shares
                        val newCash = currentCash + totalRevenue
                        val newHoldings = holdings.toMutableMap().apply {
                            val newShares = holdingShares - shares
                            if (newShares <= 0) {
                                remove(symbol)
                            } else {
                                this[symbol] = newShares
                            }
                        }
                        
                        // ポートフォリオデータの更新
                        val newPortfolioData = portfolioData.toMutableMap()
                        val currentPortfolio = newPortfolioData[symbol]
                        if (currentPortfolio != null) {
                            val newTotalShares = currentPortfolio.totalShares - shares
                            if (newTotalShares <= 0) {
                                // 全売却
                                newPortfolioData.remove(symbol)
                            } else {
                                // 部分売却（平均購入コストは維持）
                                val sellRatio = shares.toDouble() / currentPortfolio.totalShares
                                val newTotalInvestment = (currentPortfolio.totalInvestment * (1 - sellRatio)).toInt()
                                newPortfolioData[symbol] = currentPortfolio.copy(
                                    totalShares = newTotalShares,
                                    totalInvestment = newTotalInvestment
                                )
                            }
                        }
                        
                        val newStockValue = calculateStockValue(newHoldings, stockList)
                        val newTotalAssets = newCash + newStockValue
                        val newProfitRate = ((newTotalAssets - 1000000.0) / 1000000.0) * 100
                        
                        // 状態を一括更新
                        currentCash = newCash
                        holdings = newHoldings
                        portfolioData = newPortfolioData
                        stockValue = newStockValue
                        totalAssets = newTotalAssets
                        profitRate = newProfitRate
                        
                        // ポップアップメッセージは表示しない
                    } else {
                        hasError = true
                        statusMessage = "売却する株式が不足しています（保有: ${holdingShares}株, 要求: ${shares}株）"
                    }
                }
            )
            1 -> PortfolioTab(
                holdings = holdings,
                stockList = stockList,
                portfolioData = portfolioData,
                totalAssets = totalAssets,
                profitRate = profitRate,
                stockValue = stockValue,
                currentCash = currentCash
            )
            2 -> NewsTab(
                newsEvents = newsEvents,
                gameDay = gameDay,
                currentTime = "${String.format("%02d", currentHour)}:${String.format("%02d", currentMinute)}"
            )
        }
    }
        
        // エラーメッセージをオーバーレイ表示
        if (hasError) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusMessage,
                        color = Color(0xFFC62828),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { hasError = false }) {
                        Text("✕", color = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
fun GameHeaderCard(
    gameDay: Int,
    currentTime: String,
    isMarketOpen: Boolean,
    totalAssets: Int,
    currentCash: Int,
    stockValue: Int,
    profitRate: Double,
    onNextDay: () -> Unit,
    maxDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📈 投資シミュレーター",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${gameDay}日目 / 30日",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // マーケット状態表示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val marketStatusColor = if (isMarketOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
                val marketStatusText = if (isMarketOpen) "🔔 マーケット オープン" else "🔒 マーケット クローズ"
                Text(
                    text = marketStatusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = marketStatusColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 資産情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "総資産",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%,d", totalAssets)}円",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "利益率",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val profitColor = if (profitRate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Text(
                        text = "${if (profitRate >= 0) "+" else ""}${String.format("%.1f", profitRate)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = profitColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "現金: ${String.format("%,d", currentCash)}円",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "株式評価額: ${String.format("%,d", stockValue)}円",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 次の日へボタン
            Button(
                onClick = onNextDay,
                modifier = Modifier.fillMaxWidth(),
                enabled = gameDay < maxDays,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    if (gameDay < maxDays) "次の日へ（第${gameDay + 1}日）" else "ゲーム終了",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StockListTab(
    stockList: List<MockStock>,
    holdings: Map<String, Int>,
    currentCash: Int,
    isMarketOpen: Boolean,
    onBuyStock: (String, Int) -> Unit,
    onSellStock: (String, Int) -> Unit
) {
    LazyColumn {
        items(stockList) { stock ->
            val holdingShares = holdings[stock.symbol] ?: 0
            InteractiveStockCard(
                stock = stock,
                holdingShares = holdingShares,
                currentCash = currentCash,
                isMarketOpen = isMarketOpen,
                onBuyClick = { shares -> onBuyStock(stock.symbol, shares) },
                onSellClick = { shares -> onSellStock(stock.symbol, shares) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PortfolioTab(
    holdings: Map<String, Int>,
    stockList: List<MockStock>,
    portfolioData: Map<String, PortfolioItem>,
    totalAssets: Int,
    profitRate: Double,
    stockValue: Int,
    currentCash: Int
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ポートフォリオサマリーカード
        item {
            PortfolioSummaryCard(
                totalAssets = totalAssets,
                profitRate = profitRate,
                stockValue = stockValue,
                currentCash = currentCash,
                totalInvestment = portfolioData.values.sumOf { it.totalInvestment }
            )
        }
        
        if (holdings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊 ポートフォリオ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "まだ株式を保有していません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "株価一覧タブから株式を購入してみましょう",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // 保有銘柄一覧
            item {
                Text(
                    text = "保有銘柄詳細",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            items(holdings.entries.toList()) { (symbol, shares) ->
                val stock = stockList.find { it.symbol == symbol }
                val portfolioItem = portfolioData[symbol]
                if (stock != null && portfolioItem != null) {
                    EnhancedPortfolioCard(
                        stock = stock,
                        shares = shares,
                        portfolioItem = portfolioItem
                    )
                }
            }
        }
    }
}

@Composable
fun NewsTab(
    newsEvents: List<NewsEvent>,
    gameDay: Int,
    currentTime: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ヘッダーカード
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📰 市場ニュース",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${gameDay}日目",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = currentTime,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "今日の市場動向とイベント情報",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        if (newsEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "今日はまだニュースがありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "時間が経過するとニュースが配信されます",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(newsEvents) { news ->
                NewsEventCard(newsEvent = news)
            }
        }
    }
}

@Composable
fun NewsEventCard(newsEvent: NewsEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (newsEvent.isRead) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ヘッダー情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // カテゴリ表示
                    val categoryIcon = when (newsEvent.category) {
                        "テクノロジー" -> "💻"
                        "エネルギー" -> "⚡"
                        "金融" -> "🏦"
                        "生活用品" -> "🏠"
                        "エンタメ" -> "🎮"
                        "経済全般" -> "📊"
                        else -> "📰"
                    }
                    Text(
                        text = "$categoryIcon ${newsEvent.category}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // タイトル
                    Text(
                        text = newsEvent.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // 時間表示
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${newsEvent.day}日目",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = newsEvent.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ニュース本文
            Text(
                text = newsEvent.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // インパクト情報
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📈 市場への影響",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "影響セクター",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = newsEvent.impact.affectedSectors.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "影響度",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val impactRange = newsEvent.impact.priceChangeRange
                            val impactText = "${String.format("%.1f", impactRange.first)}% 〜 ${String.format("%.1f", impactRange.second)}%"
                            val impactColor = if (impactRange.second > 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            Text(
                                text = impactText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = impactColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveStockCard(
    stock: MockStock,
    holdingShares: Int,
    currentCash: Int,
    isMarketOpen: Boolean,
    onBuyClick: (Int) -> Unit,
    onSellClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 企業情報
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.categoryIcon,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stock.symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stock.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 価格情報
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%,d", stock.price)}円",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    val changeColor = if (stock.changeRate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Text(
                        text = "${if (stock.changeRate >= 0) "+" else ""}${String.format("%.1f", stock.changeRate)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor
                    )
                    if (holdingShares > 0) {
                        Text(
                            text = "保有: ${holdingShares}株",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 購入ボタン（複数の数量選択）
            Column {
                Text(
                    text = "購入",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { onBuyClick(1) },
                        enabled = isMarketOpen && currentCash >= stock.price,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("1株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { onBuyClick(100) },
                        enabled = isMarketOpen && currentCash >= stock.price * 100,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("100株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { onBuyClick(1000) },
                        enabled = isMarketOpen && currentCash >= stock.price * 1000,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("1000株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 売却ボタン（複数の数量選択）
                Text(
                    text = "売却",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { onSellClick(1) },
                        enabled = isMarketOpen && holdingShares > 0,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("1株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { onSellClick(100) },
                        enabled = isMarketOpen && holdingShares >= 100,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("100株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = { onSellClick(1000) },
                        enabled = isMarketOpen && holdingShares >= 1000,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Text("1000株", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                // 全売却ボタン
                if (holdingShares > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onSellClick(holdingShares) },
                        enabled = isMarketOpen,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        )
                    ) {
                        Text("全売却 (${holdingShares}株)", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioSummaryCard(
    totalAssets: Int,
    profitRate: Double,
    stockValue: Int,
    currentCash: Int,
    totalInvestment: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 ポートフォリオサマリー",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 総資産と利益率
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "総資産",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format("%,d", totalAssets)}円",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "利益率",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    val profitColor = if (profitRate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Text(
                        text = "${if (profitRate >= 0) "+" else ""}${String.format("%.2f", profitRate)}%",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 詳細情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "現金",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format("%,d", currentCash)}円",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "投資総額",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format("%,d", totalInvestment)}円",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "評価額",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format("%,d", stockValue)}円",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (totalInvestment > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val unrealizedPL = stockValue - totalInvestment
                val unrealizedPLColor = if (unrealizedPL >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                Text(
                    text = "含み損益: ${if (unrealizedPL >= 0) "+" else ""}${String.format("%,d", unrealizedPL)}円",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = unrealizedPLColor
                )
            }
        }
    }
}

@Composable
fun EnhancedPortfolioCard(
    stock: MockStock,
    shares: Int,
    portfolioItem: PortfolioItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ヘッダー情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stock.categoryIcon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${stock.symbol} - ${stock.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "保有: ${shares}株",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 現在の株価表示
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%,d", stock.price)}円",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val changeColor = if (stock.changeRate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    Text(
                        text = "${if (stock.changeRate >= 0) "+" else ""}${String.format("%.2f", stock.changeRate)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // パフォーマンス情報
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "平均購入単価",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%,d", portfolioItem.averageCost)}円",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "投資総額",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%,d", portfolioItem.totalInvestment)}円",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "評価額",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val currentValue = shares * stock.price
                        Text(
                            text = "${String.format("%,d", currentValue)}円",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 損益情報
                val currentValue = shares * stock.price
                val unrealizedPL = currentValue - portfolioItem.totalInvestment
                val unrealizedPLRate = if (portfolioItem.totalInvestment > 0) {
                    (unrealizedPL.toDouble() / portfolioItem.totalInvestment) * 100
                } else 0.0
                val plColor = if (unrealizedPL >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "含み損益",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (unrealizedPL >= 0) "+" else ""}${String.format("%,d", unrealizedPL)}円",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = plColor
                        )
                        Text(
                            text = "(${if (unrealizedPLRate >= 0) "+" else ""}${String.format("%.2f", unrealizedPLRate)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = plColor
                        )
                    }
                }
            }
        }
    }
}

// MockDataクラス
data class MockStock(
    val symbol: String,
    val name: String,
    val price: Int,
    val changeRate: Double,
    val categoryIcon: String,
    val holding: Int
)

// ポートフォリオアイテム（購入履歴追跡用）
data class PortfolioItem(
    val symbol: String,
    val totalShares: Int,
    val averageCost: Int,
    val totalInvestment: Int,
    val purchases: List<Purchase>
)

data class Purchase(
    val shares: Int,
    val price: Int,
    val day: Int,
    val timestamp: String
)

// ニュースイベント関連
data class NewsEvent(
    val id: String,
    val title: String,
    val content: String,
    val day: Int,
    val time: String,
    val category: String,
    val impact: NewsImpact,
    val isRead: Boolean = false
)

data class NewsImpact(
    val affectedSectors: List<String>,
    val priceChangeRange: Pair<Double, Double>, // 最小値、最大値（%）
    val probability: Double // 0.0 - 1.0
)

// 株価変動とゲームロジック関数
fun generateInitialStocks(): List<MockStock> {
    return listOf(
        MockStock("CYBT", "サイバーテック", 1000, 0.0, "🏭", 0),
        MockStock("DGTL", "デジタル工業", 1800, 0.0, "🏭", 0),
        MockStock("FOOD", "フードチェーン", 800, 0.0, "🏪", 0),
        MockStock("RETL", "リテール商事", 1100, 0.0, "🏪", 0),
        MockStock("ENGY", "エナジー電力", 1500, 0.0, "🏥", 0),
        MockStock("MDCL", "メディカル製薬", 2200, 0.0, "🏥", 0),
        MockStock("MTRL", "マテリアル工業", 1200, 0.0, "🏗️", 0),
        MockStock("CHMC", "ケミカル化学", 900, 0.0, "🏗️", 0),
        MockStock("ENTM", "エンタメ企業", 1400, 0.0, "🎮", 0),
        MockStock("SPRT", "スポーツ関連", 1600, 0.0, "🎮", 0)
    )
}

fun updateStockPrices(stocks: List<MockStock>): List<MockStock> {
    return stocks.map { stock ->
        // ランダムな変動率 (-3% ～ +3%)
        val randomChange = (kotlin.random.Random.nextDouble() - 0.5) * 6.0
        val newPrice = (stock.price * (1 + randomChange / 100)).toInt().coerceAtLeast(100)
        val actualChangeRate = ((newPrice - stock.price).toDouble() / stock.price) * 100
        
        stock.copy(
            price = newPrice,
            changeRate = actualChangeRate
        )
    }
}

fun simulateDailyMarketChange(stocks: List<MockStock>): List<MockStock> {
    return stocks.map { stock ->
        // 日次の大きな変動 (-8% ～ +8%)
        val dailyChange = (kotlin.random.Random.nextDouble() - 0.5) * 16.0
        
        // 業界別の傾向
        val sectorBonus = when (stock.categoryIcon) {
            "🏭" -> kotlin.random.Random.nextDouble() * 2.0 - 1.0 // テック：-1%～+1%
            "🏪" -> kotlin.random.Random.nextDouble() * 1.0 - 0.5 // 生活：-0.5%～+0.5%
            "🏥" -> kotlin.random.Random.nextDouble() * 1.5 - 0.75 // インフラ：-0.75%～+0.75%
            "🏗️" -> kotlin.random.Random.nextDouble() * 3.0 - 1.5 // 素材：-1.5%～+1.5%
            "🎮" -> kotlin.random.Random.nextDouble() * 4.0 - 2.0 // エンタメ：-2%～+2%
            else -> 0.0
        }
        
        val totalChange = dailyChange + sectorBonus
        val newPrice = (stock.price * (1 + totalChange / 100)).toInt().coerceAtLeast(100)
        val actualChangeRate = ((newPrice - stock.price).toDouble() / stock.price) * 100
        
        stock.copy(
            price = newPrice,
            changeRate = actualChangeRate
        )
    }
}

fun calculateStockValue(holdings: Map<String, Int>, stocks: List<MockStock>): Int {
    return holdings.entries.sumOf { (symbol, shares) ->
        val stock = stocks.find { it.symbol == symbol }
        if (stock != null) shares * stock.price else 0
    }
}

// ニュース生成とイベント管理
fun generateDailyNews(day: Int): List<NewsEvent> {
    val newsPool = listOf(
        // テクノロジー系ニュース
        NewsEvent(
            "tech_${day}_1", "AI技術革新が市場に好影響", 
            "新しいAI技術の発表により、テクノロジー株に買いが殺到しています。関連企業の株価上昇が期待されます。",
            day, "09:30", "テクノロジー",
            NewsImpact(listOf("テクノロジー"), Pair(2.0, 8.0), 0.7)
        ),
        NewsEvent(
            "tech_${day}_2", "サイバーセキュリティ懸念拡大", 
            "大手企業でのサイバー攻撃により、セキュリティ関連株に注目が集まる一方、テクノロジー企業全般には慎重な見方も。",
            day, "11:45", "テクノロジー",
            NewsImpact(listOf("テクノロジー"), Pair(-3.0, 5.0), 0.6)
        ),
        
        // エネルギー系ニュース
        NewsEvent(
            "energy_${day}_1", "原油価格が急騰", 
            "中東地域の地政学的リスクにより原油価格が上昇。エネルギー関連株に買いが集中しています。",
            day, "10:15", "エネルギー",
            NewsImpact(listOf("エネルギー"), Pair(3.0, 12.0), 0.8)
        ),
        NewsEvent(
            "energy_${day}_2", "再生可能エネルギー政策発表", 
            "政府が新たな再生可能エネルギー政策を発表。環境関連企業に期待が高まっています。",
            day, "14:20", "エネルギー",
            NewsImpact(listOf("エネルギー"), Pair(1.0, 6.0), 0.7)
        ),
        
        // 金融系ニュース
        NewsEvent(
            "finance_${day}_1", "中央銀行が金利据え置き", 
            "中央銀行が政策金利を据え置くことを発表。金融株には様子見ムードが広がっています。",
            day, "15:00", "金融",
            NewsImpact(listOf("金融"), Pair(-2.0, 3.0), 0.5)
        ),
        NewsEvent(
            "finance_${day}_2", "銀行の不良債権減少", 
            "主要銀行の不良債権比率が改善。金融セクター全体に好材料として捉えられています。",
            day, "13:30", "金融",
            NewsImpact(listOf("金融"), Pair(2.0, 7.0), 0.6)
        ),
        
        // 生活用品系ニュース
        NewsEvent(
            "consumer_${day}_1", "消費者信頼指数が改善", 
            "最新の消費者信頼指数が予想を上回る結果。生活用品関連企業の業績改善期待が高まっています。",
            day, "12:00", "生活用品",
            NewsImpact(listOf("生活用品"), Pair(1.0, 4.0), 0.6)
        ),
        
        // エンタメ系ニュース
        NewsEvent(
            "entertainment_${day}_1", "新作映画が大ヒット", 
            "話題の新作映画が興行収入記録を更新。エンタメ関連株に投資家の注目が集まっています。",
            day, "16:00", "エンタメ",
            NewsImpact(listOf("エンタメ"), Pair(2.0, 10.0), 0.7)
        ),
        
        // 全市場影響ニュース
        NewsEvent(
            "market_${day}_1", "経済指標が好調", 
            "GDP成長率が予想を上回る結果となり、市場全体に楽観的なムードが広がっています。",
            day, "08:30", "経済全般",
            NewsImpact(listOf("テクノロジー", "エネルギー", "金融", "生活用品", "エンタメ"), Pair(1.0, 5.0), 0.8)
        ),
        NewsEvent(
            "market_${day}_2", "インフレ懸念が台頭", 
            "物価上昇率が目標値を上回り、市場にインフレ懸念が広がっています。投資家は慎重な姿勢を見せています。",
            day, "14:45", "経済全般",
            NewsImpact(listOf("テクノロジー", "エネルギー", "金融", "生活用品", "エンタメ"), Pair(-4.0, 2.0), 0.6)
        )
    )
    
    // 1日に2-4個のニュースをランダム選択
    val shuffled = newsPool.shuffled()
    val newsCount = (2..4).random()
    return shuffled.take(newsCount).sortedBy { it.time }
}

// ニュースイベントの株価への影響を適用
fun applyNewsEventImpact(stockList: List<MockStock>, newsEvents: List<NewsEvent>): List<MockStock> {
    val sectorMap = mapOf(
        "💻" to "テクノロジー",
        "🏠" to "生活用品", 
        "⚡" to "エネルギー",
        "🏦" to "金融",
        "🎮" to "エンタメ"
    )
    
    return stockList.map { stock ->
        val sector = sectorMap[stock.categoryIcon] ?: ""
        var totalImpact = 0.0
        
        newsEvents.forEach { news ->
            if (news.impact.affectedSectors.contains(sector) && kotlin.random.Random.nextDouble() < news.impact.probability) {
                val impactRange = news.impact.priceChangeRange
                val impact = kotlin.random.Random.nextDouble(impactRange.first, impactRange.second)
                totalImpact += impact
            }
        }
        
        // 通常の変動に加えてニュースの影響を適用
        val normalChange = kotlin.random.Random.nextDouble(-3.0, 3.0)
        val finalChange = normalChange + totalImpact
        val newPrice = (stock.price * (1 + finalChange / 100)).toInt().coerceAtLeast(100)
        
        stock.copy(
            price = newPrice,
            changeRate = finalChange
        )
    }
}

