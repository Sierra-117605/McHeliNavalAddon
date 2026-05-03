package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.tileentity.IHasMimic;
import com.mchelinaval.tileentity.TileEntityFloorMarker;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
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
 * フロアマーカーブロック。2つの役割を持つ。
 *
 * 【役割1: 囲むブロック】
 *   エレベーターやJBDの床を構成するブロック。
 *
 * 【役割2: 止まり目印】
 *   エレベーターコントローラーが同X/Z列のこのブロックを探して停止位置にする。
 *
 * 【テクスチャ偽装】
 *   Shift+右クリック（ブロックを持っている状態）→ そのブロックの見た目に変化
 *   Shift+右クリック（何も持っていない）→ 元の見た目に戻す
 *
 * 【右クリック（普通）】
 *   Y座標をチャットに表示（設置確認用）
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
        McHeliNavalAddon.logger.info("[Block] BlockFloorMarker コンストラクタ完了");
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
        boolean disguised = state.getValue(DISGUISED);
        return disguised ? EnumBlockRenderType.INVISIBLE : EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        McHeliNavalAddon.logger.info("[FloorMarker] onBlockActivated @ {} isRemote={} sneaking={}",
            pos, world.isRemote, player.isSneaking());

        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        McHeliNavalAddon.logger.info("[FloorMarker] TileEntity @ {} : {}",
            pos, te == null ? "null!" : te.getClass().getSimpleName());

        if (!(te instanceof TileEntityFloorMarker)) {
            McHeliNavalAddon.logger.warn("[FloorMarker] TileEntityが TileEntityFloorMarker でない → スキップ");
            return true;
        }

        if (player.isSneaking()) {
            McHeliNavalAddon.logger.info("[FloorMarker] Shift+右クリック → applyMimic 呼び出し");
            applyMimic(world, pos, state, (IHasMimic) te, player, hand);
        } else {
            McHeliNavalAddon.logger.info("[FloorMarker] 右クリック → Y座標表示");
            player.sendMessage(new TextComponentString(
                "[Naval] Floor Marker: Y=" + pos.getY() + "  (エレベーターがこのY座標で停止します)"
            ));
        }
        return true;
    }

    /**
     * テクスチャ偽装を適用する（ElevatorController・JBDControllerからも呼ばれる）。
     * ブロックを持って呼ぶ → そのブロックの見た目に変化
     * 何も持たずに呼ぶ    → 偽装を解除して元の見た目に戻る
     */
    static void applyMimic(World world, BlockPos pos, IBlockState state,
                            IHasMimic te, EntityPlayer player, EnumHand hand) {

        ItemStack held = player.getHeldItem(hand);
        McHeliNavalAddon.logger.info("[Mimic] applyMimic @ {} held={} isEmpty={}",
            pos,
            held.isEmpty() ? "（空）" : held.getDisplayName(),
            held.isEmpty());

        IBlockState mimic = null;
        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            Block mimicBlock = ((ItemBlock) held.getItem()).getBlock();
            mimic = mimicBlock.getDefaultState();
            McHeliNavalAddon.logger.info("[Mimic] 偽装ブロック決定: {}", mimicBlock.getRegistryName());
        } else {
            McHeliNavalAddon.logger.info("[Mimic] 持ちアイテムがブロックでないか空 → 偽装解除");
        }

        // TileEntityに偽装ブロック状態をセット
        te.setMimicState(mimic);

        // ブロックの描画モードを切り替える（disguised=trueでINVISIBLE → TESRが描画）
        boolean newDisguised = (mimic != null);
        IBlockState newState = state.withProperty(BlockFloorMarker.DISGUISED, newDisguised);
        McHeliNavalAddon.logger.info("[Mimic] ブロック状態変更: disguised {} → {} (変化あり={})",
            state.getValue(BlockFloorMarker.DISGUISED), newDisguised, (newState != state));

        if (newState != state) {
            world.setBlockState(pos, newState, 3);
        } else {
            McHeliNavalAddon.logger.info("[Mimic] ブロック状態に変化なし → setBlockState スキップ");
        }

        // チャットに確認メッセージ
        String msg = mimic != null
            ? "[Naval] 偽装: " + held.getDisplayName() + " の見た目に変更"
            : "[Naval] 偽装を解除しました";
        player.sendMessage(new TextComponentString(msg));
        McHeliNavalAddon.logger.info("[Mimic] 完了: {}", msg);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        McHeliNavalAddon.logger.info("[FloorMarker] createNewTileEntity @ meta={}", meta);
        return new TileEntityFloorMarker();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
