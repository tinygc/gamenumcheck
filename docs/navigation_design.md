# 🧭 Navigation Structure Design - 株式投資シミュレーター

## 📐 Jetpack Navigation 設計

### **Navigation Graph 構造**

```kotlin
// メインナビゲーショングラフ
@Composable
fun StockSimNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // スプラッシュ・タイトルフロー
        splashNavigation(navController)
        
        // ゲーム設定フロー  
        gameSetupNavigation(navController)
        
        // メインゲームフロー
        mainGameNavigation(navController)
        
        // 結果・設定フロー
        resultNavigation(navController)
    }
}
```

### **画面定義 (Sealed Class)**

```kotlin
sealed class Screen(val route: String) {
    // スプラッシュ・タイトル系
    object Splash : Screen("splash")
    object Title : Screen("title")
    object Settings : Screen("settings")
    
    // ゲーム開始系
    object DifficultySelect : Screen("difficulty_select")
    object Tutorial : Screen("tutorial/{page}") {
        fun createRoute(page: Int) = "tutorial/$page"
    }
    
    // メインゲーム系
    object MainGame : Screen("main_game")
    object Home : Screen("home")
    object Trading : Screen("trading")
    object News : Screen("news") 
    object History : Screen("history")
    
    // 詳細画面系
    object StockDetail : Screen("stock_detail/{symbol}") {
        fun createRoute(symbol: String) = "stock_detail/$symbol"
    }
    object TradeModal : Screen("trade_modal/{symbol}/{type}") {
        fun createRoute(symbol: String, type: String) = "trade_modal/$symbol/$type"
    }
    object Portfolio : Screen("portfolio")
    object NewsDetail : Screen("news_detail/{newsId}") {
        fun createRoute(newsId: String) = "news_detail/$newsId"
    }
    object Statistics : Screen("statistics")
    
    // ゲーム終了系
    object GameResult : Screen("game_result")
}
```

## 🗂️ Navigation Module 詳細設計

### **1. スプラッシュ・タイトルナビゲーション**

```kotlin
fun NavGraphBuilder.splashNavigation(navController: NavController) {
    composable(Screen.Splash.route) {
        SplashScreen(
            onNavigateToTitle = {
                navController.navigate(Screen.Title.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        )
    }
    
    composable(Screen.Title.route) {
        TitleScreen(
            onStartNewGame = {
                navController.navigate(Screen.DifficultySelect.route)
            },
            onContinueGame = {
                navController.navigate(Screen.MainGame.route)
            },
            onSettings = {
                navController.navigate(Screen.Settings.route)
            }
        )
    }
    
    composable(Screen.Settings.route) {
        SettingsScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
```

### **2. ゲーム設定フロー**

```kotlin
fun NavGraphBuilder.gameSetupNavigation(navController: NavController) {
    composable(Screen.DifficultySelect.route) {
        DifficultySelectScreen(
            onDifficultySelected = { difficulty ->
                // 難易度をViewModelに保存
                navController.navigate(Screen.Tutorial.createRoute(0))
            },
            onBack = { navController.popBackStack() }
        )
    }
    
    composable(
        route = Screen.Tutorial.route,
        arguments = listOf(navArgument("page") { type = NavType.IntType })
    ) { backStackEntry ->
        val page = backStackEntry.arguments?.getInt("page") ?: 0
        
        TutorialScreen(
            currentPage = page,
            onNext = { nextPage ->
                if (nextPage < TUTORIAL_PAGES) {
                    navController.navigate(Screen.Tutorial.createRoute(nextPage))
                } else {
                    // チュートリアル完了、ゲーム開始
                    navController.navigate(Screen.MainGame.route) {
                        popUpTo(Screen.Title.route)
                    }
                }
            },
            onPrevious = { prevPage ->
                if (prevPage >= 0) {
                    navController.navigate(Screen.Tutorial.createRoute(prevPage))
                } else {
                    navController.popBackStack()
                }
            },
            onSkip = {
                navController.navigate(Screen.MainGame.route) {
                    popUpTo(Screen.Title.route)
                }
            }
        )
    }
}
```

### **3. メインゲームナビゲーション**

```kotlin
fun NavGraphBuilder.mainGameNavigation(navController: NavController) {
    composable(Screen.MainGame.route) {
        // ボトムナビゲーション付きのメイン画面
        MainGameScreen(
            onGameComplete = {
                navController.navigate(Screen.GameResult.route) {
                    popUpTo(Screen.MainGame.route) { inclusive = true }
                }
            }
        )
    }
    
    // 株式詳細画面
    composable(
        route = Screen.StockDetail.route,
        arguments = listOf(navArgument("symbol") { type = NavType.StringType })
    ) { backStackEntry ->
        val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
        
        StockDetailScreen(
            symbol = symbol,
            onBack = { navController.popBackStack() },
            onTrade = { symbol, tradeType ->
                navController.navigate(Screen.TradeModal.createRoute(symbol, tradeType))
            }
        )
    }
    
    // 取引モーダル
    composable(
        route = Screen.TradeModal.route,
        arguments = listOf(
            navArgument("symbol") { type = NavType.StringType },
            navArgument("type") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
        val type = backStackEntry.arguments?.getString("type") ?: ""
        
        TradeModalScreen(
            symbol = symbol,
            tradeType = TradeType.valueOf(type.uppercase()),
            onDismiss = { navController.popBackStack() },
            onTradeComplete = { 
                // 取引完了後、詳細画面に戻る
                navController.popBackStack()
            }
        )
    }
    
    // ポートフォリオ詳細
    composable(Screen.Portfolio.route) {
        PortfolioScreen(
            onBack = { navController.popBackStack() }
        )
    }
    
    // ニュース詳細
    composable(
        route = Screen.NewsDetail.route,
        arguments = listOf(navArgument("newsId") { type = NavType.StringType })
    ) { backStackEntry ->
        val newsId = backStackEntry.arguments?.getString("newsId") ?: ""
        
        NewsDetailScreen(
            newsId = newsId,
            onBack = { navController.popBackStack() }
        )
    }
    
    // 統計画面
    composable(Screen.Statistics.route) {
        StatisticsScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
```

### **4. ボトムナビゲーション (メインゲーム内)**

```kotlin
@Composable
fun MainGameScreen(
    onGameComplete: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                bottomNavItems.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStockClick = { symbol ->
                        // 外側のnavControllerを使用
                        // 実際は依存性注入で解決
                    },
                    onPortfolioClick = { /* ポートフォリオ画面へ */ },
                    onNextDay = { isGameComplete ->
                        if (isGameComplete) {
                            onGameComplete()
                        }
                    }
                )
            }
            
            composable(Screen.Trading.route) {
                TradingScreen(
                    onStockClick = { symbol ->
                        // 株式詳細画面へ
                    }
                )
            }
            
            composable(Screen.News.route) {
                NewsScreen(
                    onNewsClick = { newsId ->
                        // ニュース詳細画面へ
                    }
                )
            }
            
            composable(Screen.History.route) {
                HistoryScreen(
                    onStatisticsClick = {
                        // 統計画面へ
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, Icons.Default.Home, "ホーム"),
    BottomNavItem(Screen.Trading.route, Icons.Default.TrendingUp, "取引"),
    BottomNavItem(Screen.News.route, Icons.Default.Article, "ニュース"),
    BottomNavItem(Screen.History.route, Icons.Default.History, "履歴")
)
```

## 🔄 画面遷移のパターン

### **1. Replace (置き換え)**
```kotlin
// タイトル → メインゲーム (戻れない)
navController.navigate(Screen.MainGame.route) {
    popUpTo(Screen.Title.route) { inclusive = true }
}
```

### **2. Push (スタック追加)**
```kotlin
// ホーム → 株式詳細 (戻るボタンで戻れる)
navController.navigate(Screen.StockDetail.createRoute(symbol))
```

### **3. Modal (モーダル表示)**
```kotlin
// 株式詳細 → 取引モーダル
navController.navigate(Screen.TradeModal.createRoute(symbol, "BUY"))
```

### **4. Bottom Navigation (タブ切り替え)**
```kotlin
// メインゲーム内のタブ間移動
navController.navigate(Screen.Trading.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

## 🎯 DeepLink 対応

```kotlin
// 特定の株式詳細画面への直接遷移
composable(
    route = Screen.StockDetail.route,
    deepLinks = listOf(navDeepLink { uriPattern = "stocksim://stock/{symbol}" })
) { ... }

// ゲーム状態復帰用
composable(
    route = Screen.MainGame.route,
    deepLinks = listOf(navDeepLink { uriPattern = "stocksim://game/day/{day}" })
) { ... }
```

## 🔒 Navigation Guard (画面遷移制御)

```kotlin
@Composable
fun NavigationGuard(
    gameState: GameState?,
    onNavigateToTitle: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        gameState == null -> {
            // ゲーム状態なし → タイトルへ
            LaunchedEffect(Unit) {
                onNavigateToTitle()
            }
        }
        gameState.gameStatus == GameStatus.COMPLETED -> {
            // ゲーム完了 → 結果画面へ
            LaunchedEffect(gameState) {
                // Result画面への遷移処理
            }
        }
        else -> {
            content()
        }
    }
}
```

## 📱 状態保存・復元

```kotlin
// ViewModelでのナビゲーション状態管理
class NavigationViewModel : ViewModel() {
    private val _navigationState = MutableStateFlow<NavigationState?>(null)
    val navigationState = _navigationState.asStateFlow()
    
    fun saveNavigationState(route: String, arguments: Bundle?) {
        _navigationState.value = NavigationState(route, arguments)
    }
    
    fun restoreNavigation(): NavigationState? {
        return _navigationState.value
    }
}

data class NavigationState(
    val route: String,
    val arguments: Bundle?
)
```

この設計でスムーズで直感的なナビゲーションが実現できるよ〜♡ Jetpack Navigationの機能をフル活用してユーザビリティ抜群になりそう✨