# 📐 株式投資シミュレーター Clean Architecture 設計書

## 🏗️ アーキテクチャ概要

Clean Architectureの4層構造で設計：

```
📱 Presentation Layer (UI/ViewModel)
    ↕️
🎯 Use Case Layer (Business Logic)
    ↕️
🏢 Domain Layer (Entities/Repository Interfaces)
    ↕️
💾 Data Layer (Repository Implementation/Data Sources)
```

## 🎯 Domain Layer (最内層)

### エンティティ設計

#### **Company (企業エンティティ)**
```kotlin
data class Company(
    val symbol: String,          // 銘柄コード (e.g., "CYBT")
    val name: String,           // 企業名
    val category: CompanyCategory, // 業界カテゴリ
    val description: String,    // 企業説明
    val volatility: Volatility, // 変動性
    val hasDividend: Boolean,   // 配当有無
    val unlockLevel: Int        // 解放レベル
)

enum class CompanyCategory {
    TECHNOLOGY, LIFESTYLE, INFRASTRUCTURE, MATERIALS, ENTERTAINMENT
}

enum class Volatility {
    LOW, MEDIUM, HIGH
}
```

#### **Stock (株式エンティティ)**
```kotlin
data class Stock(
    val company: Company,
    val currentPrice: Money,
    val previousPrice: Money,
    val priceHistory: List<PricePoint>,
    val lastUpdated: LocalDateTime
) {
    val changeRate: Double get() = 
        (currentPrice.amount - previousPrice.amount) / previousPrice.amount
    
    val changeAmount: Money get() = 
        Money(currentPrice.amount - previousPrice.amount)
}

data class PricePoint(
    val price: Money,
    val timestamp: LocalDateTime
)
```

#### **Portfolio (ポートフォリオエンティティ)**
```kotlin
data class Portfolio(
    val cash: Money,
    val holdings: Map<String, Holding>,
    val totalValue: Money
) {
    fun getTotalAssets(): Money = cash + holdings.values.sumOf { it.currentValue }
    
    fun getProfitLoss(): Money = getTotalAssets() - Money(INITIAL_CASH)
    
    fun getProfitRate(): Double = getProfitLoss().amount / INITIAL_CASH
}

data class Holding(
    val companySymbol: String,
    val shares: Int,
    val averagePurchasePrice: Money,
    val currentPrice: Money
) {
    val currentValue: Money get() = Money(shares * currentPrice.amount)
    val profitLoss: Money get() = Money(shares * (currentPrice.amount - averagePurchasePrice.amount))
}
```

#### **GameState (ゲーム状態エンティティ)**
```kotlin
data class GameState(
    val currentDay: Int,
    val maxDays: Int,
    val portfolio: Portfolio,
    val availableStocks: List<Stock>,
    val unlockedCompanies: Set<String>,
    val gameStatus: GameStatus,
    val difficulty: Difficulty
) {
    val isGameOver: Boolean get() = currentDay >= maxDays
    val remainingDays: Int get() = maxDays - currentDay
}

enum class GameStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, PAUSED
}

enum class Difficulty {
    EASY, NORMAL, HARD
}
```

#### **NewsEvent (ニュースイベントエンティティ)**
```kotlin
data class NewsEvent(
    val id: String,
    val title: String,
    val description: String,
    val affectedCompanies: List<String>,
    val impact: MarketImpact,
    val eventType: NewsType,
    val occurredAt: LocalDateTime
)

data class MarketImpact(
    val priceChangeRange: DoubleRange,  // e.g., -0.15..0.30
    val probability: Double             // 0.0..1.0
)

enum class NewsType {
    POSITIVE, NEGATIVE, NEUTRAL, MARKET_WIDE
}
```

#### **Transaction (取引エンティティ)**
```kotlin
data class Transaction(
    val id: String,
    val companySymbol: String,
    val type: TransactionType,
    val shares: Int,
    val pricePerShare: Money,
    val totalAmount: Money,
    val timestamp: LocalDateTime,
    val day: Int
)

enum class TransactionType {
    BUY, SELL
}
```

### 共通値オブジェクト

#### **Money (金額値オブジェクト)**
```kotlin
@JvmInline
value class Money(val amount: Double) {
    operator fun plus(other: Money): Money = Money(amount + other.amount)
    operator fun minus(other: Money): Money = Money(amount - other.amount)
    operator fun times(multiplier: Double): Money = Money(amount * multiplier)
    
    fun format(): String = "¥${String.format("%,.0f", amount)}"
}
```

## 🎯 Repository Interfaces (Domain Layer)

```kotlin
interface StockRepository {
    suspend fun getAllStocks(): List<Stock>
    suspend fun getStockBySymbol(symbol: String): Stock?
    suspend fun updateStockPrice(symbol: String, newPrice: Money)
    suspend fun getStockHistory(symbol: String, days: Int): List<PricePoint>
}

interface CompanyRepository {
    suspend fun getAllCompanies(): List<Company>
    suspend fun getCompaniesByCategory(category: CompanyCategory): List<Company>
    suspend fun getCompanyBySymbol(symbol: String): Company?
    suspend fun getUnlockedCompanies(level: Int): List<Company>
}

interface GameStateRepository {
    suspend fun saveGameState(gameState: GameState)
    suspend fun loadGameState(): GameState?
    suspend fun deleteGameState()
}

interface TransactionRepository {
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun getTransactionHistory(): List<Transaction>
    suspend fun getTransactionsByDay(day: Int): List<Transaction>
}

interface NewsRepository {
    suspend fun getTodaysNews(day: Int): List<NewsEvent>
    suspend fun generateRandomNews(companies: List<Company>): NewsEvent
}
```

## 🎯 Use Case Layer

### 主要ユースケース

#### **ゲーム管理系**
```kotlin
class StartNewGameUseCase(
    private val gameStateRepository: GameStateRepository,
    private val companyRepository: CompanyRepository
)

class LoadGameUseCase(
    private val gameStateRepository: GameStateRepository
)

class NextDayUseCase(
    private val gameStateRepository: GameStateRepository,
    private val stockRepository: StockRepository,
    private val newsRepository: NewsRepository,
    private val priceUpdateUseCase: UpdateStockPricesUseCase
)
```

#### **株式取引系**
```kotlin
class BuyStockUseCase(
    private val gameStateRepository: GameStateRepository,
    private val transactionRepository: TransactionRepository
)

class SellStockUseCase(
    private val gameStateRepository: GameStateRepository,
    private val transactionRepository: TransactionRepository
)

class ValidateTransactionUseCase()
```

#### **情報取得系**
```kotlin
class GetPortfolioSummaryUseCase(
    private val gameStateRepository: GameStateRepository
)

class GetMarketOverviewUseCase(
    private val stockRepository: StockRepository,
    private val gameStateRepository: GameStateRepository
)

class GetTransactionHistoryUseCase(
    private val transactionRepository: TransactionRepository
)
```

#### **価格更新系**
```kotlin
class UpdateStockPricesUseCase(
    private val stockRepository: StockRepository,
    private val newsRepository: NewsRepository
)

class ApplyNewsImpactUseCase()

class GenerateRandomPriceFluctuationUseCase()
```

## 📱 Presentation Layer

### ViewModel設計

#### **MainViewModel**
```kotlin
class MainViewModel(
    private val startNewGameUseCase: StartNewGameUseCase,
    private val loadGameUseCase: LoadGameUseCase,
    private val nextDayUseCase: NextDayUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    fun startNewGame(difficulty: Difficulty)
    fun loadGame()
    fun nextDay()
}

data class MainUiState(
    val gameState: GameState? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

#### **TradingViewModel**
```kotlin
class TradingViewModel(
    private val buyStockUseCase: BuyStockUseCase,
    private val sellStockUseCase: SellStockUseCase,
    private val getMarketOverviewUseCase: GetMarketOverviewUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()
    
    fun buyStock(symbol: String, shares: Int)
    fun sellStock(symbol: String, shares: Int)
    fun refreshMarket()
}
```

### Screen構成

#### **Compose Screen設計**
```kotlin
// メイン画面
@Composable
fun MainScreen(viewModel: MainViewModel)

// 取引画面  
@Composable
fun TradingScreen(viewModel: TradingViewModel)

// ポートフォリオ画面
@Composable
fun PortfolioScreen(viewModel: PortfolioViewModel)

// ニュース画面
@Composable
fun NewsScreen(viewModel: NewsViewModel)

// 履歴画面
@Composable  
fun HistoryScreen(viewModel: HistoryViewModel)
```

## 💾 Data Layer

### Repository実装

#### **LocalStockRepository**
```kotlin
class LocalStockRepository(
    private val stockDao: StockDao,
    private val priceGenerator: PriceGenerator
) : StockRepository {
    // Room DBを使用した実装
}
```

#### **LocalCompanyRepository** 
```kotlin
class LocalCompanyRepository(
    private val companyDao: CompanyDao,
    private val assetManager: AssetManager  // JSON読み込み用
) : CompanyRepository {
    // JSON + Room DBを使用した実装
}
```

### データソース

#### **Room Database**
```kotlin
@Entity
data class StockEntity(
    @PrimaryKey val symbol: String,
    val currentPrice: Double,
    val previousPrice: Double,
    val lastUpdated: Long
)

@Entity
data class CompanyEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val category: String,
    val description: String,
    val volatility: String,
    val hasDividend: Boolean,
    val unlockLevel: Int
)
```

## 🔧 依存性注入 (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideStockRepository(
        stockDao: StockDao,
        priceGenerator: PriceGenerator
    ): StockRepository = LocalStockRepository(stockDao, priceGenerator)
}

@Module  
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    
    @Provides
    fun provideBuyStockUseCase(
        gameStateRepository: GameStateRepository,
        transactionRepository: TransactionRepository
    ): BuyStockUseCase = BuyStockUseCase(gameStateRepository, transactionRepository)
}
```

## 🧪 テスト戦略

### テスト構造
```
domain/
  entities/ - エンティティの単体テスト
  usecases/ - ユースケースの単体テスト
data/
  repositories/ - リポジトリのテスト (Fake実装使用)
presentation/
  viewmodels/ - ViewModelのテスト
  ui/ - Composeのテスト
```

この設計でClean Architectureの原則を守りつつ、拡張しやすい株式投資シミュレーターが作れるよ〜✨