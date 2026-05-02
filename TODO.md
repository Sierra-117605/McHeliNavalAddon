# McHeliNavalAddon — TODO

最終更新: 2026-05-03

---

## 現在のフェーズ: 動作確認

---

## 次にやること（ゲームで確認）

- [ ] `./gradlew runClient` でゲームを起動する
- [ ] クリエイティブタブ「McHeli Naval Addon」にブロックが表示されることを確認
- [ ] カタパルトブロックを設置 → MCHELI機体を乗せて右クリック → 打ち出されることを確認
- [ ] 移動プラットフォームを設置 → 右クリックで動作確認（エレベーターモード）
- [ ] 移動プラットフォームをカタパルト隣接に設置 → JBD連動の動作確認
- [ ] カタパルト速度など `config/mchelinaval.cfg` で値が変更できることを確認

## 調整が必要になりそうな箇所

- カタパルトの `LAUNCH_SPEED`（デフォルト3.0）— 実際に試して適切な値に調整
- 移動プラットフォームの `DEFAULT_MOVE_SPEED`（デフォルト0.05）— 速すぎ/遅すぎなら調整
- 移動プラットフォームの `DEFAULT_TRAVEL_DISTANCE`（デフォルト5ブロック）— JBDとして使う場合の距離調整

---

## 完了済み

- [x] M0: MCHELI 1.1.4 jar解析（MCH_EntityAircraft・速度フィールド特定）
- [x] M1: 空MODビルド（BUILD SUCCESSFUL確認）
- [x] M2: カタパルト実装（BlockCatapult + TileEntityCatapult）
- [x] M3: 移動プラットフォーム実装（BlockMovingPlatform + TileEntityMovingPlatform）
- [x] クリエイティブタブ追加
- [x] config対応（mchelinaval.cfg でカタパルト速度・プラットフォーム設定を変更可能）
- [x] テクスチャJSON + PNG生成（カタパルト: グレー+黄ストライプ、プラットフォーム: グレー+白ストライプ）
- [x] 最終ビルド成功 → McHeliNavalAddon-1.0.0.jar 生成
