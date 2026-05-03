package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.tileentity.IHasMimic;
import com.mchelinaval.tileentity.TileEntityElevatorController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * エレベーターコントローラーブロック。
 *
 * 【使い方】
 *   1. このブロックを1つ設置する
 *   2. 止まりたい各フロアの床にフロアマーカーを敷く（プラットフォームの床になる）
 *   3. 右クリックでGUIを開き「▲ 上へ」「▼ 下へ」ボタンで移動
 *
 * 【テクスチャ偽装】
 *   Shift+右クリック（ブロックを持っている状態）→ そのブロックの見た目に変化
 *   Shift+右クリック（何も持っていない）         → 元の見た目に戻す
 */
public class BlockElevatorController extends Block implements ITileEntityProvider {

    /** 偽装モードフラグ */
    public static final PropertyBool DISGUISED = PropertyBool.create("disguised");

    public BlockElevatorController() {
        super(Material.IRON);
        setTranslationKey("elevator_controller");
        setRegistryName("mchelinaval", "elevator_controller");
        setHardness(3.0f);
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
    public boolean isOpaqueCube(IBlockState state) {
        return !state.getValue(DISGUISED); // 偽装時は非不透明（TESR描画のため）
    }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityElevatorController)) return true;

        if (player.isSneaking()) {
            // ===== Shift+右クリック：テクスチャ偽装 =====
            BlockFloorMarker.applyMimic(world, pos, state, (IHasMimic) te, player, hand);
        } else {
            // ===== 右クリック：GUIを開く =====
            player.openGui(McHeliNavalAddon.instance,
                           NavalGuiHandler.GUI_ELEVATOR,
                           world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityElevatorController();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
