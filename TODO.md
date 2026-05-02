# McHeliNavalAddon — TODO

最終更新: 2026-05-03

---

## 現在のフェーズ: M4（カタパルト連動・動作確認）

---

## 進行中

- [ ] `./gradlew runClient` でゲーム内にブロックが出ることを確認する
- [ ] カタパルトブロックを設置→機体を乗せて右クリックで打ち出されることを確認する
- [ ] 移動プラットフォームブロックを設置→右クリックで動作確認する

---

## 未着手

### M4: 動作確認・調整
- [ ] カタパルトの打ち出し速度を実際に試して`LAUNCH_SPEED`の値を調整する
- [ ] 移動プラットフォームのJBDモード（Y+Z斜め）をカタパルト隣接で動作確認する
- [ ] エレベーターモード（Y軸）の移動距離・速度を確認する

### 残課題
- [ ] テクスチャを追加する（現状はデフォルトの紫チェック柄）
  - `src/main/resources/assets/mchelinaval/textures/blocks/` にPNG画像を置く
  - `src/main/resources/assets/mchelinaval/blockstates/` にJSONを置く
  - `src/main/resources/assets/mchelinaval/models/block/` にJSONを置く
- [ ] `config/mchelinaval.cfg` でカタパルト速度・プラットフォーム速度を変更できるようにする

---

## 完了済み

- [x] M0: MCHELI 1.1.4 jar解析（MCH_EntityAircraft・速度フィールド特定）
- [x] M1: 空MODビルド（BUILD SUCCESSFUL確認）
- [x] M2: カタパルト実装（BlockCatapult + TileEntityCatapult）
- [x] M3: 移動プラットフォーム実装（BlockMovingPlatform + TileEntityMovingPlatform）
