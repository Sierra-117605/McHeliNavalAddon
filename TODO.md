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
- [ ] TileEntityCatapult.java 作成（速度付与ロジック・JBDへの通知）
- [ ] スチームパーティクル・効果音
- [ ] config: 打ち出し速度

### M3: アレスティングワイヤー
- [ ] BlockArrestingWire.java 作成
- [ ] TileEntityArrestingWire.java 作成（制動ロジック）
- [ ] キャッチエフェクト・効果音

### M4: JBD
- [ ] BlockJBD.java 作成（BlockState: 格納 / 立ち上がり）
- [ ] カタパルトからの「機体セット」「機体離脱」通知受信ロジック
- [ ] 立ち上がり中のコリジョン切替

### M5: 移動プラットフォーム
- [ ] BlockMovingController.java 作成（コントローラーブロック）
- [ ] TileEntityMovingController.java（範囲内ブロック移動ロジック）
- [ ] 移動方向・距離・速度のconfig/GUI設定
- [ ] エンティティ（機体・プレイヤー）を乗せたまま移動する処理

---

## 完了済み

（なし）
