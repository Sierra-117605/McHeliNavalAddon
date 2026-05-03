# McHeliNavalAddon — KNOWLEDGE

一度ハマった問題には二度とハマらない。学びを蓄積するドキュメント。

---

## ★重要★ onBlockActivated とスニーク+アイテムの Vanilla 仕様

### 問題
スニーク中にアイテムを持っていると、Minecraft はサーバー側の
`onBlockActivated` を**呼ばない**。

```java
// PlayerInteractionManager.processRightClickBlock（Vanilla コード）
if (!player.isSneaking() || player.getHeldItemMainhand().isEmpty() && player.getHeldItemOffhand().isEmpty()) {
    block.onBlockActivated(...);  // ← スニーク+アイテム持ちのとき呼ばれない
}
```

### 結果
「Shift+右クリックでテクスチャ偽装」という設計が根本的に動かない。
クライアント側は `return true` するが、サーバー側の処理が実行されない。

### 正しい設計
- ブロックを持って右クリック（スニーク不要） → テクスチャ偽装
- 何も持たずに右クリック → GUI / 情報表示

---

## ★重要★ IBlockState.withProperty() はブロック間で共有できない

### 問題
`PropertyBool.create("disguised")` を3ブロックそれぞれで呼ぶと
**3つの別インスタンス**ができる。
FloorMarker の DISGUISED インスタンスを ElevatorController の
`IBlockState.withProperty()` に渡すと `IllegalArgumentException`。

### 正しい取得方法
プロパティを名前で動的検索する：

```java
PropertyBool disguisedProp = null;
for (IProperty<?> p : state.getPropertyKeys()) {
    if ("disguised".equals(p.getName()) && p instanceof PropertyBool) {
        disguisedProp = (PropertyBool) p;
        break;
    }
}
if (disguisedProp != null) {
    world.setBlockState(pos, state.withProperty(disguisedProp, newValue), 3);
}
```

---

## ★重要★ TESRCamouflage の renderModel 座標

### 問題
`renderModel(world, model, state, te.getPos(), buf, false)` と書くと
ワールド座標（例: 100,64,200）分だけ頂点がオフセットされる。
GL行列もカメラ相対オフセット分 translate 済みなので **二重オフセット** になり
偽装テクスチャが遥か遠くに描画される。

### 正しい書き方
```java
brd.getBlockModelRenderer().renderModel(
    world, model, state,
    BlockPos.ORIGIN,   // ← te.getPos() ではなく ORIGIN
    buf, false
);
```

---

## Forge 1.12.2 / stable_39 マッピング 対応表

| 間違い（古い名前）| 正しい（stable_39）|
|---|---|
| `setUnlocalizedName()` | `setTranslationKey()` |
| `net.minecraft.util.SoundEvents` | `net.minecraft.init.SoundEvents` |
| `EnumFacing.getHorizontal(int)` | `EnumFacing.byHorizontalIndex(int)` |
| `getFrontOffsetX/Z()` | `getXOffset()` / `getZOffset()` |
| `CreativeTabs.getTabIconItem()` | `createIcon()` returning `ItemStack` |

---

## MCHELI 1.1.4 注意事項

- jarのファイル名: `mcheli-1.1.4.jar`（"MCHELI 1.14" という表記はバージョン1.1.4を指す。MC 1.14ではない）
- 速度フィールド: `velocityX`, `velocityY`, `velocityZ`（`MCH_EntityAircraft` クラス）
- リフレクション必須: フィールドはprivateなので `setAccessible(true)` でアクセス

---

## ブロック・TileEntityの設計パターン

### TileEntity付きブロック
```java
// ブロック破壊時にTileEntityをリセットしない
@Override
public boolean shouldRefresh(World world, BlockPos pos, IBlockState old, IBlockState nw) {
    return old.getBlock() != nw.getBlock();
}
```

### テクスチャ偽装（カモフラージュ）機能
- ブロックの `getRenderType()` を `INVISIBLE` にする
- TileEntitySpecialRenderer（TESR）に `BlockModelRenderer.renderModel()` で描画させる
- TileEntityに偽装ブロック状態（`IBlockState mimicState`）を保持・NBT保存する
- 左クリックはクライアント側 `PlayerInteractEvent.LeftClickBlock` で捕捉 → `PacketSetMimic` 送信
- TESR登録は `@SidedProxy` + `ClientProxy` で行う（サーバーで `@SideOnly(CLIENT)` コードを実行しないため）

### TESRレンダリング（BlockModelRenderer）
```java
brd.getBlockModelRenderer().renderModel(
    world,                                             // ワールド（光計算用）
    brd.getBlockModelShapes().getModelForState(state), // モデル
    state,                                             // ブロック状態
    te.getPos(),                                       // ワールド座標
    bufferBuilder,
    false                                              // AO計算スキップ
);
```

---

## イベントバス

| 用途 | 方法 |
|---|---|
| ブロック・アイテム登録 | `@Mod.EventBusSubscriber` + `@SubscribeEvent` (MinecraftForge EVENT_BUS) |
| クライアント専用Forgeイベント | `@Mod.EventBusSubscriber(value = Side.CLIENT)` |
| TESR登録 | `ClientProxy.registerTileEntityRenderers()` via `@SidedProxy` |
| GUIハンドラ登録 | `NetworkRegistry.INSTANCE.registerGuiHandler()` in `init()` |

---

## ネットワーク通信

- `SimpleNetworkWrapper` を `NetworkRegistry.INSTANCE.newSimpleChannel(MODID)` で作成
- パケットIDは登録順（0, 1, 2...）で重複禁止
- `addScheduledTask()` でサーバーメインスレッドで実行する

---

## ビルド環境

- `gradlew.bat` は `C:\dev\McHeliWingman\` からコピーして使用
- MCHELI jarはGitHub管理外（`.gitignore` に `libs/*.jar`）
- `build.finalizedBy copyToMods` で `C:\.minecraft\mods` に自動コピー

---

## フロアマーカー注意

- `isFullCube = true` にしないとブロックを上に積めない（過去にハマった）
- `getRenderType = INVISIBLE` でもモデルは登録される（TESR用に必要）
- TileEntityが必要（偽装機能のため）→ `ITileEntityProvider` を実装すること

---

## 囲むブロック設計（v2以降）

- **エレベーター**: コントローラー1個 + フロアマーカーを各フロアの床に敷く
  - コントローラーが同X/Z列のフロアマーカーYを検索して停止位置にする
  - エンティティはコントローラー周辺の `range` 内のものを全部移動（プレイヤー + MCHELI機体）
- **JBD**: コントローラー1個 + フロアマーカーをデフレクター面に敷く
  - GUIから手動展開/格納も可能
  - カタパルトと隣接させると自動連動（下記参照）

---

## カタパルト ↔ JBD 隣接自動連動

```java
// TileEntityCatapult.update() — 10tickごとに機体検知
boolean aircraftNow = getAircraftOnBlock() != null;
if (aircraftNow != wasAircraftPresent) {
    wasAircraftPresent = aircraftNow;
    notifyAdjacentJBDs(aircraftNow);  // 隣接6面を全スキャン
}

// 隣接TileEntityがJBDControllerなら deploy/retract を呼ぶ
for (EnumFacing face : EnumFacing.VALUES) {
    TileEntity te = world.getTileEntity(pos.offset(face));
    if (te instanceof TileEntityJBDController) {
        if (deploy) ((TileEntityJBDController) te).deploy();
        else        ((TileEntityJBDController) te).retract();
    }
}
```

- **ポイント**: NBT登録なし・距離制限なし（隣接1ブロックのみ）
- カタパルトに隣接して置くだけで自動連動。GUIによる手動操作も引き続き可能。
- テクスチャ偽装で見た目を変えてもTileEntityの型は変わらないので連動は維持される。

---

## 旧コードとの互換性

- `BlockMovingPlatform` / `TileEntityMovingPlatform` はv2でも削除していない（コンパイルのみ、未登録）
- `NavalGuiHandler.GUI_MOVING_PLATFORM` は `@Deprecated` として残存（旧ファイルのコンパイル通過のため）
