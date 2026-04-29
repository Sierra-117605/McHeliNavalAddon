# McHeliNavalAddon — TODO

最終更新: 2026-04-30

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
- [ ] mcmod.info / mods.toml 作成
- [ ] `libs/` にMCHELI 1.14 jarを配置してdeobf依存として追加
- [ ] `./gradlew runClient` でForgeに読み込まれることを確認

### M2: カタパルト
- [ ] BlockCatapult.java 作成
- [ ] TileEntityCatapult.java 作成（速度付与ロジック）
- [ ] スチームパーティクル・効果音
- [ ] config: 打ち出し速度

### M3: アレスティングワイヤー
- [ ] BlockArrestingWire.java 作成
- [ ] TileEntityArrestingWire.java 作成（制動ロジック）
- [ ] キャッチエフェクト・効果音

### M4: JBD・IFLOLS
- [ ] BlockJBD.java 作成（BlockState: 格納/立ち上がり）
- [ ] カタパルトとの連動ロジック
- [ ] BlockIFLOLS.java 作成
- [ ] TileEntityIFLOLS.java（角度計算・発光色切替）

### M5: デッキエレベーター
- [ ] BlockElevatorPlatform.java 作成
- [ ] BlockElevatorControl.java 作成
- [ ] TileEntityElevator.java（エンティティ移動ロジック）

### M6: 格納庫扉
- [ ] BlockHangarDoor.java 作成
- [ ] BlockHangarControl.java 作成
- [ ] TileEntityHangarDoor.java（スライドアニメーションロジック）

---

## 完了済み

（なし）
