# 🏦 Android Stock Investment Simulator

株式投資シミュレーターゲーム - Clean Architecture + TDD で実装

## 📱 プロジェクト概要

### 🎮 ゲーム内容
- **リアルな株式投資体験**: 30日間の投資ゲーム
- **10社の株式**: 5つのカテゴリ（テクノロジー、生活用品、エネルギー、金融、エンタメ）
- **市場変動**: ランダムイベント＋ニュースによる株価変動
- **段階的解放**: プレイヤーレベルに応じて企業が解放
- **3つの難易度**: Easy/Normal/Hard

### 🏗️ アーキテクチャ
Clean Architecture + MVVM + Repository パターンで実装

```
presentation/     - UI層 (Jetpack Compose)
domain/          - ビジネスロジック層
  ├── model/     - エンティティ
  ├── usecase/   - ユースケース  
  └── repository/ - リポジトリIF
data/            - データ層
  ├── local/     - Room Database
  └── repository/ - リポジトリ実装
```

## 🛠️ 技術スタック

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Hilt
- **Database**: Room + SQLite
- **Navigation**: Navigation Compose
- **Serialization**: Kotlinx Serialization
- **Build System**: Gradle (Kotlin DSL)
- **Testing**: JUnit + Espresso

## 🚀 開発状況

### ✅ 完了済み機能
- [x] Clean Architecture基盤設計（Domain/Data/Presentation層分離）
- [x] Domain層完全実装（エンティティ、ユースケース、リポジトリIF）
- [x] Data層基盤（Room Database、DAO、Entity）
- [x] Use Case層（ゲーム開始、売買、日次進行ロジック）
- [x] UI テーマ・カラー設計（Material 3準拠）
- [x] スプラッシュ画面実装
- [x] タイトル画面実装
- [x] メインゲーム画面完全実装
- [x] 株式売買機能（買い・売り・保有管理）
- [x] リアルタイム株価変動システム
- [x] ポートフォリオ画面
- [x] 日次進行システム（30日制限）
- [x] 利益率計算・表示機能
- [x] エラーメッセージのオーバーレイ表示
- [x] UI更新最適化（即座反映システム）
- [x] 時間管理システム（9:00〜17:00取引時間・3秒で15分経過）
- [x] マーケットオープン・クローズ機能
- [x] 複数株数取引システム（1株・100株・1000株・全売却）
- [x] リアルタイム株価自動更新（取引時間中のみ）
- [x] 企業データ外部JSON対応（companies.json）
- [x] 型安全なMoney値オブジェクト（演算子オーバーロード）
- [x] ビルドエラー完全修正（0件達成）
- [x] Kotlinx Serialization完全対応
- [x] TDD基盤（テスト構造整備）
- [x] 詳細設計書完成（docs/フォルダ）

### 📝 設計ドキュメント
- [x] [株式ゲーム仕様書](docs/stock_game_specification.md)
- [x] [アーキテクチャ設計](docs/architecture_design.md)
- [x] [UI詳細仕様](docs/detailed_ui_specifications.md)
- [x] [ナビゲーション設計](docs/navigation_design.md)
- [x] [UI/UXフロー](docs/ui_flow_design.md)

### 🔄 実装予定
- [ ] ニュース・イベント機能（UI実装済み・ロジック未実装）
- [ ] ゲーム結果画面
- [ ] レベルシステム（企業段階解放）
- [ ] セーブ・ロード機能
- [ ] 音効・BGM
- [ ] アニメーション効果

## 🎯 ゲーム設計

### 💰 収益システム
- **初期資金**: 1,000,000円
- **取引手数料**: 0.1%
- **目標**: 30日で資産を増やす

### 📊 企業カテゴリ
1. **テクノロジー**: 高ボラティリティ・高成長性
2. **生活用品**: 安定・低リスク  
3. **エネルギー**: 中リスク・市況連動
4. **金融**: 経済全体に連動
5. **エンタメ**: 高ボラティリティ・トレンド敏感

## 🔧 開発環境

### 必要な環境
- Android Studio (最新版推奨)
- JDK 11以上
- Android SDK API 24以上 (Android 7.0+)

### ビルド方法
```bash
# プロジェクトのクリーンビルド
./gradlew clean assembleDebug

# テスト実行
./gradlew test

# Android Studioでの実行推奨
```

## 📋 開発ルール

- **TDD**: テスト駆動開発で進行
- **Clean Architecture**: 責任分離を徹底
- **Kotlin DSL**: Gradle設定はKotlin DSLで記述
- **Material 3**: 最新のマテリアルデザイン適用

## 🎨 UI/UX設計

### カラーテーマ
- **Primary**: 緑系（#2E7D32）- 利益・成長
- **Secondary**: オレンジ系（#FF9800）- 警告・注意
- **Error**: 赤系（#D32F2F）- 損失・エラー
- **Success**: 深緑（#388E3C）- 成功・完了

### 画面構成
1. **スプラッシュ** → **タイトル** → **ゲーム選択** → **メインゲーム**
2. レスポンシブデザイン対応
3. アクセシビリティ配慮

## 📱 対応デバイス
- **最小SDK**: API 24 (Android 7.0)
- **ターゲットSDK**: API 36 (Android 15)
- **画面サイズ**: Phone/Tablet対応

---
*This project demonstrates Clean Architecture implementation in Android with Jetpack Compose.*

🤖 Generated with [Claude Code](https://claude.ai/code)