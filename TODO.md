# McHeliNavalAddon — TODO

最終更新: 2026-05-01

---

## 現在のフェーズ: M0（MCHELI 1.14解析）

---

## 進行中

- [ ] MCHELI 1.14 jarをデコンパイルして機体エンティティのクラス名・速度フィールドを特定する

---

## 未着手

### M1: 空MODビルド
- [ ] build.gradle を作成（Forge 1.14.4 / ForgeGradle 3.x）
- [ ] gradle.properties 作成
- [ ] McHeliNavalAddon.java（@Modエントリポイント）作成
- [ ] mods.toml 作成
- [ ] `libs/` にMCHELI 1.14 jarを配置してdeobf依存として追加
- [ ] `./gradlew runClient` でForgeに読み込まれることを確認

### M2: カタパルト
- [ ] BlockCatapult.java 作成
- [ ] TileEntityCatapult.java 作成（速度付与ロジック）
- [ ] 隣接する移動プラットフォームへの通知処理（機体セット・離脱）
- [ ] スチームパーティクル・効果音
- [ ] config: 打ち出し速度

### M3: 移動プラットフォーム（手動）
- [ ] BlockMovingController.java 作成（コントローラーブロック）
- [ ] TileEntityMovingController.java（範囲内ブロック移動ロジック）
- [ ] 移動方向: Y軸（エレベーター）・X/Z軸（スライドドア）
- [ ] 移動距離・速度のconfig/GUI設定
- [ ] エンティティ（機体・プレイヤー）追従処理

### M4: 斜め移動・カタパルト連動（JBD）
- [ ] 移動方向に「Y+Z同時（斜め）」を追加
- [ ] カタパルト連動トリガーの実装
- [ ] 機体セット→展開・機体離脱→格納の自動制御

---

## 完了済み

（なし）
