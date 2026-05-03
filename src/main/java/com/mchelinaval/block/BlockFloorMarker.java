package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.tileentity.IHasMimic;
import com.mchelinaval.tileentity.TileEntityFloorMarker;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * フロアマーカーブロック。
 *
 * 【テクスチャ偽装】
 *   ブロックを持って右クリック → そのブロックの見た目に変化
 *   何も持たずに右クリック    → Y座標表示 + 偽装解除
 *
 * 【注意】スニーク+右クリックにしてはいけない。
 *   Minecraft はスニーク中にアイテムを持っている場合
 *   サーバー側の onBlockActivated を呼ばない仕様がある。
 */
public class BlockFloorMarker extends Block implements ITileEntityProvider {

    public static final PropertyBool DISGUISED = PropertyBool.create("disguised");

    public BlockFloorMarker() {
        super(Material.GLASS);
        setTranslationKey("floor_marker");
        setRegistryName("mchelinaval", "floor_marker");
        setHardness(0.3f);
        setLightOpacity(0);
        setDefaultState(blockState.getBaseState().withProperty(DISGUISED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DISGUISED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(DISGUISED, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(DISGUISED) ? 1 : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return state.getValue(DISGUISED)
            ? EnumBlockRenderType.INVISIBLE
            : EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        // クライアント側はサーバーに処理を委ねるだけ（return true で「消費済み」とする）
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        McHeliNavalAddon.logger.info("[FloorMarker] 右クリック @ {} TE={} hand={}",
            pos,
            te == null ? "null!" : te.getClass().getSimpleName(),
            hand);

        if (!(te instanceof TileEntityFloorMarker)) {
            McHeliNavalAddon.logger.warn("[FloorMarker] TileEntityFloorMarker でない → スキップ");
            return true;
        }

        ItemStack held = player.getHeldItem(hand);

        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            // ===== ブロックを持って右クリック → テクスチャ偽装 =====
            McHeliNavalAddon.logger.info("[FloorMarker] ブロック持ち右クリック → applyMimic (held={})",
                held.getDisplayName());
            applyMimic(world, pos, state, (IHasMimic) te, player, hand);
        } else {
            // ===== 空手（または非ブロックアイテム）で右クリック → Y座標表示 + 偽装解除 =====
            McHeliNavalAddon.logger.info("[FloorMarker] 空手右クリック → Y座標表示 + 偽装解除");
            applyMimic(world, pos, state, (IHasMimic) te, player, hand); // held が空なら mimic=null → 解除
            player.sendMessage(new TextComponentString(
                "[Naval] Floor Marker: Y=" + pos.getY() + "  (エレベーターがこのY座標で停止します)"
            ));
        }
        return true;
    }

    /**
     * テクスチャ偽装を適用する。
     * ブロックを持っている → そのブロックの見た目に変化
     * 空手           → 偽装を解除して元の見た目に戻る
     *
     * 【重要】
     *   DISGUISED プロパティは BlockFloorMarker / BlockElevatorController / BlockJBDController が
     *   それぞれ別インスタンスで作っているため、state から動的に取得する必要がある。
     *   state.withProperty(BlockFloorMarker.DISGUISED, ...) を他ブロックの state に使うと例外が出る。
     */
    static void applyMimic(World world, BlockPos pos, IBlockState state,
                            IHasMimic te, EntityPlayer player, EnumHand hand) {

        ItemStack held = player.getHeldItem(hand);
        McHeliNavalAddon.logger.info("[Mimic] applyMimic @ {} block={} held={}",
            pos,
            state.getBlock().getRegistryName(),
            held.isEmpty() ? "（空）" : held.getDisplayName());

        // 偽装先のブロック状態を決める
        IBlockState mimic = null;
        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            Block mimicBlock = ((ItemBlock) held.getItem()).getBlock();
            mimic = mimicBlock.getDefaultState();
            McHeliNavalAddon.logger.info("[Mimic] 偽装ブロック: {}", mimicBlock.getRegistryName());
        } else {
            McHeliNavalAddon.logger.info("[Mimic] 空手 → 偽装解除");
        }

        // TileEntityに偽装状態をセット
        te.setMimicState(mimic);

        // ブロックの "disguised" プロパティを state から動的に取得する
        // ※ 各ブロックが別インスタンスで PropertyBool を持っているため、名前で検索する
        PropertyBool disguisedProp = null;
        for (IProperty<?> p : state.getPropertyKeys()) {
            if ("disguised".equals(p.getName()) && p instanceof PropertyBool) {
                disguisedProp = (PropertyBool) p;
                break;
            }
        }

        if (disguisedProp == null) {
            McHeliNavalAddon.logger.error("[Mimic] 'disguised' プロパティが見つからない！ block={}",
                state.getBlock().getRegistryName());
            return;
        }

        boolean newDisguised = (mimic != null);
        boolean oldDisguised = state.getValue(disguisedProp);
        McHeliNavalAddon.logger.info("[Mimic] disguised: {} → {}", oldDisguised, newDisguised);

        if (newDisguised != oldDisguised) {
            world.setBlockState(pos, state.withProperty(disguisedProp, newDisguised), 3);
            McHeliNavalAddon.logger.info("[Mimic] setBlockState 完了");
        } else {
            McHeliNavalAddon.logger.info("[Mimic] 状態変化なし → setBlockState スキップ");
        }

        // チャット確認メッセージ
        String msg = mimic != null
            ? "[Naval] 偽装: " + held.getDisplayName() + " の見た目に変更"
            : "[Naval] 偽装を解除しました";
        player.sendMessage(new TextComponentString(msg));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFloorMarker();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
