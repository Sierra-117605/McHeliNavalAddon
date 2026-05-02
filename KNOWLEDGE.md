# McHeliNavalAddon — KNOWLEDGE

一度ハマった問題には二度とハマらない。学びを蓄積するドキュメント。

---

## MCHELI 1.1.4 内部情報（解析済み）

### 基本情報
- jarファイル: `mcheli-1.1.4.jar`
- 対象MCバージョン: **1.12.2**（"MCHELI 1.14"はMCHELIのバージョン1.1.4のこと。MC 1.14とは別物）

### 機体エンティティ
- **基底クラス**: `mcheli.aircraft.MCH_EntityAircraft`（abstract）
- パッケージ: `mcheli.aircraft`
- 継承: `mcheli.wrapper.W_EntityContainer` → Forgeエンティティ

### 速度フィールド（リフレクションで直接操作可能）
| フィールド名 | 型 | アクセス修飾子 | 内容 |
|---|---|---|---|
| `velocityX` | `double` | `protected` | X方向の速度 |
| `velocityY` | `double` | `protected` | Y方向の速度 |
| `velocityZ` | `double` | `protected` | Z方向の速度 |
| `currentSpeed` | `double` | `public` | 現在の速度スカラー値 |
| `currentThrottle` | `double` | `private` | 現在のスロットル値 |

### 姿勢フィールド
| フィールド名 | 型 | 内容 |
|---|---|---|
| `aircraftYaw` | `double` | 機首方位（ヨー角）|
| `aircraftPitch` | `double` | 仰俯角（ピッチ角）|
| `rotationRoll` | `float` | ロール角 |

### UAV判定
- `public boolean isUAV()` — UAVかどうかのメソッド
- `MCH_AircraftInfo.isUAV` — JSONで設定されるフラグ
- `private mcheli.uav.MCH_EntityUavStation uavStation` — UAVステーション参照

### カタパルト実装方針
- `velocityX` / `velocityY` / `velocityZ` をリフレクションで書き換えて打ち出し速度を設定する
- ブロックのfacing方向からXZ速度成分を計算して代入する
- `protected` フィールドなので `setAccessible(true)` が必要

---

## Forge 1.12.2 での注意点

- 設定ファイルは `.cfg`（`@Config` アノテーションまたは `Configuration` クラス）
- ブロック登録は `GameRegistry.register()` またはイベント経由
- TileEntity登録も `GameRegistry.registerTileEntity()`
- `@Mod.EventBusSubscriber` はそのまま使える

### stable_39 マッピングでよくある名前の違い（ハマりポイント）
| 間違えやすい名前 | 正しい名前（stable_39）|
|---|---|
| `setUnlocalizedName()` | `setTranslationKey()` |
| `EnumFacing.getHorizontal(int)` | `EnumFacing.byHorizontalIndex(int)` |
| `facing.getFrontOffsetX()` | `facing.getXOffset()` |
| `facing.getFrontOffsetZ()` | `facing.getZOffset()` |
| `net.minecraft.util.SoundEvents` | `net.minecraft.init.SoundEvents` |
| `CreativeTabs.getTabIconItem()` | `CreativeTabs.createIcon()` （戻り値はItemStackに変更）|

---

## リフレクションの注意点

- MCHELIのフィールドアクセスは必ず `try-catch (Exception e)` で保護する
- `Field.setAccessible(true)` はフィールドごとに初回のみ呼び出してキャッシュする
- `protected` フィールドも `setAccessible(true)` で外部からアクセス可能
- MCHELIのバージョンアップでフィールド名が変わった場合、ここに新旧対応を記録する
