package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.tileentity.TileEntityElevatorController;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
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
 *   2. 止まりたい各フロアの床にフロアマーカーを敷く（これがプラットフォームの床になる）
 *   3. 右クリックでGUIを開き「▲ 上へ」「▼ 下へ」ボタンで移動
 *
 * 【テクスチャ偽装】
 *   左クリック時に持っているブロックの見た目に変化する（艦船の床と馴染ませるため）
 */
public class BlockElevatorController extends Block implements ITileEntityProvider {

    public BlockElevatorController() {
        super(Material.IRON);
        setTranslationKey("elevator_controller");
        setRegistryName("mchelinaval", "elevator_controller");
        setHardness(3.0f);
    }

    /** 右クリック：GUIを開く */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (!(world.getTileEntity(pos) instanceof TileEntityElevatorController)) return true;

        player.openGui(McHeliNavalAddon.instance,
                       NavalGuiHandler.GUI_ELEVATOR,
                       world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    /** TESRで描画するため通常レンダリングを無効にする */
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityElevatorController();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
