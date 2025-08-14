# 📱 株式投資シミュレーター 詳細UI仕様書

## 🎨 共通デザインシステム

### **カラーパレット**
```kotlin
object StockSimColors {
    // メインカラー
    val Primary = Color(0xFF2E7D32)      // 深緑 (投資・お金)
    val PrimaryVariant = Color(0xFF1B5E20) // より深い緑
    val Secondary = Color(0xFF00BCD4)    // シアン (情報・データ)
    
    // 状態カラー
    val Profit = Color(0xFF4CAF50)       // 利益 (緑)
    val Loss = Color(0xFFF44336)         // 損失 (赤)
    val Neutral = Color(0xFF9E9E9E)      // 変動なし (グレー)
    val Warning = Color(0xFFFF9800)      // 警告 (オレンジ)
    
    // 背景・テキスト
    val Background = Color(0xFFFAFAFA)   // 薄いグレー
    val Surface = Color(0xFFFFFFFF)      // 白
    val OnPrimary = Color(0xFFFFFFFF)    // 白文字
    val OnSurface = Color(0xFF212121)    // 黒文字
    val OnSurfaceVariant = Color(0xFF757575) // グレー文字
}
```

### **タイポグラフィ**
```kotlin
object StockSimTypography {
    val Headline1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)    // タイトル
    val Headline2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold) // セクション
    val Body1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)     // 本文
    val Body2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)     // 小さな本文
    val Caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)   // キャプション
    val Price = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)       // 価格表示
    val PriceChange = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium) // 変動率
}
```

### **共通コンポーネント**
```kotlin
// 価格変動表示
@Composable
fun PriceChangeChip(
    changeAmount: Money,
    changeRate: Double,
    modifier: Modifier = Modifier
) {
    val color = when {
        changeRate > 0 -> StockSimColors.Profit
        changeRate < 0 -> StockSimColors.Loss
        else -> StockSimColors.Neutral
    }
    val icon = when {
        changeRate > 0 -> Icons.Default.TrendingUp
        changeRate < 0 -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }
}

// 株式アイテムカード
@Composable
fun StockCard(
    stock: Stock,
    holding: Holding?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// ゲーム統計表示
@Composable
fun GameStatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier
)
```

## 📄 各画面詳細仕様

### 1. **スプラッシュ画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│                                 │
│           (100dp余白)           │
│                                 │
│    📈 (88dp アイコン)          │
│                                 │
│      STOCK SIMULATOR            │ ← Headline1, Primary色
│                                 │
│           (40dp余白)            │
│                                 │
│    ● ● ●  (ローディング)        │ ← 16dp円形, Secondary色
│                                 │
│           (120dp余白)           │
│                                 │
│        Version 1.0.0            │ ← Caption, OnSurfaceVariant
│                                 │
└─────────────────────────────────┘
```

#### **Compose実装仕様**
```kotlin
@Composable
fun SplashScreen(
    onNavigateToTitle: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000)
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
        // アイコン + タイトル + ローディング
    }
}
```

### 2. **タイトル画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│                                 │
│           (80dp余白)            │
│                                 │
│      📈 STOCK SIMULATOR         │ ← Headline1
│      ～投資の世界へようこそ～     │ ← Body2, OnSurfaceVariant
│                                 │
│           (80dp余白)            │
│                                 │
│ ┌─────────────────────────────┐ │
│ │      新しくはじめる        │ │ ← 56dp高さ, Primary色
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │      つづきから            │ │ ← 56dp高さ, 有効時のみ表示
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │      設定                  │ │ ← 48dp高さ, Outlined
│ └─────────────────────────────┘ │
│                                 │
│           (フレキシブル)        │
│                                 │
│    最高記録: +125.3% 💎         │ ← Caption, 中央寄せ
│                                 │
└─────────────────────────────────┘
```

#### **状態管理**
```kotlin
data class TitleUiState(
    val hasSaveData: Boolean = false,
    val bestRecord: Double? = null,
    val isLoading: Boolean = false
)

@Composable
fun TitleScreen(
    uiState: TitleUiState,
    onStartNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onSettings: () -> Unit
)
```

### 3. **難易度選択画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│  ←  難易度を選択してください     │ ← TopAppBar
├─────────────────────────────────┤
│           (24dp余白)            │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 🟢 イージー                │ │ ← 72dp高さ
│ │ 価格変動が小さく、良いニュース│ │
│ │ が多めに発生します           │ │
│ │                 おすすめ！   │ ← 初心者バッジ
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 🟡 ノーマル                │ │
│ │ バランスの取れた難易度        │ │
│ │ リアルな投資体験             │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 🔴 ハード                  │ │
│ │ 価格変動が大きく、悪材料も    │ │
│ │ 多く発生します               │ │
│ │                     上級者向け│ ← 上級者バッジ
│ └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘
```

#### **カード仕様**
```kotlin
@Composable
fun DifficultyCard(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = if (isSelected) 8.dp else 2.dp,
        border = if (isSelected) BorderStroke(2.dp, StockSimColors.Primary) else null
    ) {
        // 難易度詳細表示
    }
}
```

### 4. **ホーム画面 (メインダッシュボード)**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ Day 15/30        💰 ¥1,250,000 │ ← ヘッダー: 56dp高さ
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 📊 資産サマリー             │ │ ← セクションヘッダー
│ │ ┌─────┬─────┬─────┬─────┐ │ │
│ │ │現金 │株式 │利益 │利益率│ │ │ ← 4列グリッド
│ │ │450K │800K │+250K│+25% │ │ │
│ │ └─────┴─────┴─────┴─────┘ │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 🔥 注目銘柄                 │ │
│ │ CYBT  ¥1,200  +12.5% 🔥    │ │ ← 変動大きい3社
│ │ FOOD  ¥800   -3.2%  📉     │ │
│ │ ENGY  ¥1,500  +0.8% →      │ │
│ │              [もっと見る >] │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📰 今日のニュース           │ │
│ │ ・サイバーテック、新AI技術...│ │ ← 最新3件のヘッドライン
│ │ ・電力料金値上げ決定...     │ │
│ │ ・外食産業回復傾向...       │ │
│ │              [もっと見る >] │ │
│ └─────────────────────────────┘ │
│                                 │
│           (フレキシブル)        │
│                                 │
│ ┌─────────────────────────────┐ │
│ │        次の日へ進む         │ │ ← 56dp高さ, Primary
│ └─────────────────────────────┘ │
│           (16dp余白)            │
└─────────────────────────────────┘
│  ホーム │ 取引 │ニュース│ 履歴 │ ← BottomNavigation
```

#### **コンポーネント詳細**
```kotlin
@Composable
fun AssetSummaryCard(
    cash: Money,
    stockValue: Money,
    profit: Money,
    profitRate: Double,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AssetColumn("現金", cash.format())
                AssetColumn("株式", stockValue.format())
                AssetColumn("利益", profit.format(), profit.amount >= 0)
                AssetColumn("利益率", "${String.format("%.1f", profitRate)}%", profitRate >= 0)
            }
        }
    }
}

@Composable
fun AssetColumn(
    label: String,
    value: String,
    isPositive: Boolean? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = StockSimTypography.Caption,
            color = StockSimColors.OnSurfaceVariant
        )
        Text(
            text = value,
            style = StockSimTypography.Body1.copy(fontWeight = FontWeight.Bold),
            color = when (isPositive) {
                true -> StockSimColors.Profit
                false -> StockSimColors.Loss
                null -> StockSimColors.OnSurface
            }
        )
    }
}
```

### 5. **取引画面 (株式一覧)**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ 💰 現金: ¥450,000              │ ← ヘッダー、右端にフィルターアイコン
├─────────────────────────────────┤
│ [全部][🏭][🏪][🏥][🎮][素材]     │ ← 横スクロール可能なチップフィルター
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │ ← LazyColumn
│ │ CYBT サイバーテック    🔥   │ │ ← 変動大きい場合は炎アイコン
│ │ ¥1,200  +¥30  (+2.5%)      │ │
│ │ 保有: 100株 (¥120,000)     │ │
│ │        [詳細]    [売買]     │ │ ← アクションボタン
│ └─────────────────────────────┘ │
│           (8dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ FOOD フードチェーン         │ │
│ │ ¥800   -¥10   (-1.2%) 📉   │ │
│ │ 保有: 50株 (¥40,000)       │ │
│ │        [詳細]    [売買]     │ │
│ └─────────────────────────────┘ │
│           (8dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ ENGY エナジー電力      🔒   │ │ ← 未解放の場合はロックアイコン
│ │ ¥1,500  +¥12  (+0.8%) →   │ │
│ │ 保有: 0株                  │ │
│ │     [詳細]  [レベル3で解放] │ │ ← 解放条件表示
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### **フィルター仕様**
```kotlin
@Composable
fun CategoryFilterChips(
    selectedCategory: CompanyCategory?,
    onCategorySelected: (CompanyCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("全部") }
            )
        }
        items(CompanyCategory.values()) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                leadingIcon = { Icon(category.icon, contentDescription = null) }
            )
        }
    }
}
```

### 6. **株式詳細画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ ← CYBT                         │ ← TopAppBar with back
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ サイバーテック              │ │ ← 企業名、大きく表示
│ │                             │ │
│ │    ¥1,200                   │ │ ← 現在価格、超大きく
│ │ +¥30 (+2.5%) 🔥            │ │ ← 変動情報、色付き
│ │                             │ │
│ │ 前日終値: ¥1,170            │ │ ← 詳細情報
│ │ 出来高: 1,234,567株         │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📊 保有状況                 │ │
│ │ 保有株数: 100株             │ │
│ │ 平均取得単価: ¥1,150        │ │
│ │ 評価額: ¥120,000           │ │
│ │ 損益: +¥5,000 (+4.3%)      │ │ ← 色付きで表示
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📈 価格履歴 (7日間)         │ │
│ │                             │ │
│ │ [簡易チャート表示エリア]     │ │ ← 120dp高さの棒グラフ風
│ │                             │ │
│ │ 1150→1180→1170→1200→...    │ │ ← 数値の羅列も併記
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📊 企業情報                 │ │
│ │ 業界: テクノロジー 🏭        │ │
│ │ 変動性: 高                  │ │
│ │ 配当: なし                  │ │
│ │ 解放レベル: 1               │ │
│ └─────────────────────────────┘ │
│                                 │
│           (フレキシブル)        │
│                                 │
│ ┌─────────┬─────────────────┐ │
│ │   買う   │      売る        │ │ ← 固定ボタン、56dp高さ
│ └─────────┴─────────────────┘ │
└─────────────────────────────────┘
```

### 7. **売買モーダル画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│           CYBT を購入           │ ← モーダルタイトル
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 現在価格: ¥1,200            │ │ ← 価格情報カード
│ │ 利用可能現金: ¥450,000      │ │
│ │ 最大購入可能: 375株         │ │
│ └─────────────────────────────┘ │
│           (24dp余白)            │
│ 購入株数                        │ ← ラベル
│ ┌───┐ ┌───────────┐ ┌───┐    │
│ │ - │ │    100    │ │ + │    │ ← 数量調整、48dp高さ
│ └───┘ └───────────┘ └───┘    │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 必要金額: ¥120,000          │ │ ← 計算結果カード
│ │ 手数料: ¥120 (0.1%)         │ │
│ │ ─────────────────────────── │ │
│ │ 合計: ¥120,120              │ │ ← 太字、大きく
│ └─────────────────────────────┘ │
│           (24dp余白)            │
│ ⚠️ 取引後の現金残高: ¥329,880   │ ← 注意事項、小さく
│                                 │
│           (フレキシブル)        │
│                                 │
│ ┌─────────┬─────────────────┐ │
│ │キャンセル │      購入        │ │ ← アクションボタン
│ └─────────┴─────────────────┘ │
└─────────────────────────────────┘
```

#### **バリデーション仕様**
```kotlin
@Composable
fun TradeValidationMessage(
    validation: TradeValidation,
    modifier: Modifier = Modifier
) {
    when (validation) {
        is TradeValidation.InsufficientFunds -> {
            Text(
                text = "現金が不足しています (不足額: ${validation.shortage.format()})",
                color = StockSimColors.Loss,
                style = StockSimTypography.Caption
            )
        }
        is TradeValidation.InsufficientShares -> {
            Text(
                text = "保有株数が不足しています (保有: ${validation.available}株)",
                color = StockSimColors.Loss,
                style = StockSimTypography.Caption
            )
        }
        TradeValidation.Valid -> {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "有効",
                tint = StockSimColors.Profit
            )
        }
    }
}
```

### 8. **ニュース画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ 📰 本日のニュース               │ ← ヘッダー
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │ ← LazyColumn
│ │ 🔥 サイバーテック、新AI技術... │ │ ← 重要度アイコン + ヘッドライン
│ │ CYBT株に+15%の影響予想        │ │ ← 影響度予測
│ │                       15:30 │ │ ← 時刻、右寄せ
│ └─────────────────────────────┘ │
│           (8dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ ⚡ 電力料金値上げ決定          │ │
│ │ ENGY株に好影響、+5%予想      │ │
│ │                       12:00 │ │
│ └─────────────────────────────┘ │
│           (8dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ 📈 外食産業回復傾向            │ │
│ │ FOOD、RETL株に+3%予想        │ │
│ │                       09:00 │ │
│ └─────────────────────────────┘ │
│           (8dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ 💡 市場全体的に楽観            │ │
│ │ 全銘柄に小幅プラス材料        │ │
│ │                       08:00 │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### **ニュース種別デザイン**
```kotlin
@Composable
fun NewsCard(
    news: NewsEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = when (news.eventType) {
        NewsType.POSITIVE -> Icons.Default.TrendingUp to StockSimColors.Profit
        NewsType.NEGATIVE -> Icons.Default.TrendingDown to StockSimColors.Loss
        NewsType.MARKET_WIDE -> Icons.Default.Public to StockSimColors.Secondary
        NewsType.NEUTRAL -> Icons.Default.Info to StockSimColors.Neutral
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = news.title,
                        style = StockSimTypography.Body1,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = news.occurredAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = StockSimTypography.Caption,
                    color = StockSimColors.OnSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = news.description,
                style = StockSimTypography.Body2,
                color = StockSimColors.OnSurfaceVariant
            )
            
            if (news.affectedCompanies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(news.affectedCompanies) { symbol ->
                        AssistChip(
                            onClick = { /* 株式詳細画面へ */ },
                            label = { Text(symbol) }
                        )
                    }
                }
            }
        }
    }
}
```

### 9. **取引履歴画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ 📊 取引履歴          [統計]     │ ← ヘッダー、右端に統計ボタン
├─────────────────────────────────┤
│ [全て][購入][売却][今日][今週]    │ ← フィルターチップ
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │ ← LazyColumn、日付グループ
│ │ Day 15 (今日)               │ │ ← 日付ヘッダー
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 📤 CYBT 売却     15:30      │ │ ← 取引アイコン + 時刻
│ │ 50株 × ¥1,180 = ¥59,000   │ │
│ │ 損益: +¥1,500 (+2.6%) 💰   │ │ ← 損益、色付き
│ └─────────────────────────────┘ │
│           (4dp余白)             │
│ ┌─────────────────────────────┐ │
│ │ 📥 FOOD 購入     12:00      │ │
│ │ 25株 × ¥820 = ¥20,500     │ │
│ │ 手数料: ¥21                │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ Day 14                      │ │
│ └─────────────────────────────┘ │
│ ┌─────────────────────────────┐ │
│ │ 📥 ENGY 購入     09:15      │ │
│ │ 10株 × ¥1,480 = ¥14,800   │ │
│ │ 手数料: ¥15                │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

#### **取引カード仕様**
```kotlin
@Composable
fun TransactionCard(
    transaction: Transaction,
    profitLoss: Money? = null,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor) = when (transaction.type) {
        TransactionType.BUY -> Icons.Default.ShoppingCart to StockSimColors.Secondary
        TransactionType.SELL -> Icons.Default.TrendingUp to StockSimColors.Primary
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = "${transaction.companySymbol} ${transaction.type.displayName}",
                        style = StockSimTypography.Body1,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${transaction.shares}株 × ${transaction.pricePerShare.format()}",
                        style = StockSimTypography.Body2,
                        color = StockSimColors.OnSurfaceVariant
                    )
                    if (profitLoss != null) {
                        Text(
                            text = "損益: ${profitLoss.format()}",
                            style = StockSimTypography.Caption,
                            color = if (profitLoss.amount >= 0) StockSimColors.Profit else StockSimColors.Loss
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = StockSimTypography.Caption,
                    color = StockSimColors.OnSurfaceVariant
                )
                Text(
                    text = transaction.totalAmount.format(),
                    style = StockSimTypography.Body1,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

### 10. **統計画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│ ← 統計情報                      │ ← TopAppBar
├─────────────────────────────────┤
│ ┌─────────────────────────────┐ │
│ │ 📊 総合成績                 │ │
│ │ ┌─────┬─────┬─────┬─────┐ │ │
│ │ │取引回│勝率 │最大利│平均利│ │ │ ← 4列グリッド
│ │ │ 45回│67% │+120%│+12% │ │ │
│ │ └─────┴─────┴─────┴─────┘ │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 🏆 銘柄別パフォーマンス      │ │
│ │ CYBT: +120% (5勝2敗) 🥇     │ │ ← ランキング形式
│ │ ENGY: +45% (3勝1敗) 🥈      │ │
│ │ FOOD: +12% (4勝3敗) 🥉      │ │
│ │ RETL: -5% (2勝4敗) 📉       │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📈 資産推移グラフ           │ │
│ │                             │ │
│ │ [簡易折れ線グラフエリア]     │ │ ← 160dp高さ
│ │                             │ │
│ │ 1000K → 1250K (15日間)      │ │
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 🎯 投資スタイル分析         │ │
│ │ ・アクティブトレーダー       │ │ ← AI分析風コメント
│ │   頻繁に取引を行う傾向       │ │
│ │ ・テック株重視             │ │
│ │   テクノロジー銘柄中心       │ │
│ │ ・短期志向                 │ │
│ │   保有期間平均2.3日         │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

### 11. **ゲーム結果画面**

#### **レイアウト構成**
```
┌─────────────────────────────────┐
│           🎉 完了！              │ ← 大きなタイトル
│                                 │
│           (40dp余白)            │
│                                 │
│        最終資産                 │ ← ラベル
│      ¥1,580,000                │ ← 超大きく、太字
│                                 │
│      利益: +¥580,000            │ ← 色付き
│      利益率: +58.0% 🚀          │ ← 色付き、絵文字
│                                 │
│           (32dp余白)            │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 🏆 あなたの評価              │ │
│ │                             │ │
│ │     優秀な投資家！           │ │ ← 大きく、中央寄せ
│ │                             │ │
│ │ ⭐⭐⭐⭐⭐                   │ │ ← 星5個
│ └─────────────────────────────┘ │
│           (24dp余白)            │
│ ┌─────────────────────────────┐ │
│ │ 📊 詳細成績                 │ │
│ │ 取引回数: 45回              │ │
│ │ 勝率: 67%                   │ │
│ │ 最高利益銘柄: CYBT (+120%)  │ │
│ │ 最終日: 30/30               │ │
│ └─────────────────────────────┘ │
│                                 │
│           (フレキシブル)        │
│                                 │
│ ┌─────────────────────────────┐ │
│ │      詳細統計を見る         │ │ ← Outlined button
│ └─────────────────────────────┘ │
│           (16dp余白)            │
│ ┌─────────────────────────────┐ │
│ │      もう一度挑戦           │ │ ← Primary button
│ └─────────────────────────────┘ │
│           (8dp余白)            │
│ ┌─────────────────────────────┐ │
│ │     タイトルに戻る          │ │ ← Text button
│ └─────────────────────────────┘ │
│           (24dp余白)            │
└─────────────────────────────────┘
```

#### **評価ロジック**
```kotlin
enum class InvestmentRating(
    val displayName: String,
    val stars: Int,
    val emoji: String
) {
    LEGENDARY("伝説の投資家", 5, "🏆"),
    EXCELLENT("優秀な投資家", 4, "🚀"),
    GOOD("堅実な投資家", 3, "👍"),
    AVERAGE("平均的な投資家", 2, "📊"),
    BEGINNER("初心者投資家", 1, "📚")
}

fun calculateRating(profitRate: Double, winRate: Double, totalTrades: Int): InvestmentRating {
    return when {
        profitRate >= 100.0 && winRate >= 0.8 -> InvestmentRating.LEGENDARY
        profitRate >= 50.0 && winRate >= 0.7 -> InvestmentRating.EXCELLENT
        profitRate >= 25.0 && winRate >= 0.6 -> InvestmentRating.GOOD
        profitRate >= 0.0 && winRate >= 0.5 -> InvestmentRating.AVERAGE
        else -> InvestmentRating.BEGINNER
    }
}
```

## 🎨 アニメーション仕様

### **共通アニメーション**
```kotlin
object StockSimAnimations {
    val FastFadeIn = fadeIn(animationSpec = tween(300))
    val FastFadeOut = fadeOut(animationSpec = tween(200))
    val SlideFromRight = slideInHorizontally { it } + FastFadeIn
    val SlideToLeft = slideOutHorizontally { -it } + FastFadeOut
    val PriceChange = animateColorAsState(
        targetValue = color,
        animationSpec = tween(1000)
    )
}
```

### **価格変動アニメーション**
```kotlin
@Composable
fun AnimatedPriceDisplay(
    price: Money,
    changeRate: Double,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = when {
            changeRate > 0 -> StockSimColors.Profit
            changeRate < 0 -> StockSimColors.Loss
            else -> StockSimColors.OnSurface
        },
        animationSpec = tween(500)
    )
    
    val scale by animateFloatAsState(
        targetValue = if (abs(changeRate) > 5.0) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Text(
        text = price.format(),
        color = animatedColor,
        modifier = modifier.scale(scale),
        style = StockSimTypography.Price
    )
}
```

この超詳細なUI仕様で実装したら、めちゃプロ品質のアプリになるよ〜♡ 全画面のレイアウト、カラー、アニメーションまで完璧に設計したから、コーディング時に迷うことないはず✨