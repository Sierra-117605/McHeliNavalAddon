package com.mchelinaval.block;

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
 *   コントローラーの周りに敷き詰めてプラットフォームの面を作る。
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

    /**
     * 偽装モードフラグ。
     * false = 通常描画（自分のテクスチャ）
     * true  = INVISIBLE（TESRが偽装テクスチャを描画）
     */
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

    /**
     * 通常時 → MODEL（自分のテクスチャで描画）
     * 偽装時 → INVISIBLE（TESRに任せる）
     */
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
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityFloorMarker)) return true;

        if (player.isSneaking()) {
            // ===== Shift+右クリック：テクスチャ偽装 =====
            applyMimic(world, pos, state, (IHasMimic) te, player, hand);
        } else {
            // ===== 右クリック：Y座標を表示 =====
            player.sendMessage(new TextComponentString(
                "[Naval] Floor Marker: Y=" + pos.getY() + "  (エレベーターがこのY座標で停止します)"
            ));
        }
        return true;
    }

    /**
     * テクスチャ偽装を適用する。
     * ブロックを持って呼ぶ → そのブロックの見た目に変化
     * 何も持たずに呼ぶ    → 偽装を解除して元の見た目に戻る
     */
    static void applyMimic(World world, BlockPos pos, IBlockState state,
                            IHasMimic te, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);

        net.minecraft.block.state.IBlockState mimic = null;
        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            Block mimicBlock = ((ItemBlock) held.getItem()).getBlock();
            mimic = mimicBlock.getDefaultState();
        }

        // TileEntityに偽装ブロック状態をセット
        te.setMimicState(mimic);

        // ブロックの描画モードを切り替える（偽装あり=INVISIBLE、なし=MODEL）
        IBlockState newState = state.withProperty(DISGUISED, mimic != null);
        if (newState != state) {
            world.setBlockState(pos, newState, 3);  // 3 = クライアントへ通知
        }

        // 確認メッセージ
        if (player instanceof EntityPlayer) {
            String msg = mimic != null
                ? "[Naval] 偽装: " + held.getDisplayName() + " の見た目に変更"
                : "[Naval] 偽装を解除しました";
            ((EntityPlayer) player).sendMessage(new TextComponentString(msg));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFloorMarker();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
