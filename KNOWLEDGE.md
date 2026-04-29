# McHeliNavalAddon — KNOWLEDGE

一度ハマった問題には二度とハマらない。学びを蓄積するドキュメント。

---

## MCHELI内部情報（解析結果を随時追記）

### 機体エンティティ
- クラス名: 未確認（M0フェーズで特定予定）
- 速度フィールド: 未確認
- 速度書き換えの影響: 未確認

---

## Forge 1.14.4での注意点

- 設定ファイルは `.cfg` ではなく `.toml`（ForgeConfigSpec を使用）
- ブロック登録は `DeferredRegister<Block>` を使う（1.12.2の GameRegistry と異なる）
- TileEntity登録も `DeferredRegister<TileEntityType<?>>` を使う
- `@Mod.EventBusSubscriber` の `bus` 引数を忘れないこと（MOD_BUS vs FORGE_BUS）

---

## リフレクションの注意点

- MCHELIのフィールドアクセスは必ず `try-catch (Exception e)` で保護する
- `Field.setAccessible(true)` はフィールドごとに初回のみ呼び出してキャッシュする
- MCHELIのバージョンアップでフィールド名が変わった場合、ここに新旧対応を記録する
