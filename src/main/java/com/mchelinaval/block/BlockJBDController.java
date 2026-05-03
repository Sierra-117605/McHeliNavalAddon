package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.tileentity.TileEntityJBDController;
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
 * JBD（ジェットブラストデフレクター）コントローラーブロック。
 *
 * 【使い方】
 *   1. このブロックを1つ設置する
 *   2. 周りにフロアマーカーを敷いてデフレクター面を作る
 *   3. 右クリックでGUIを開き「展開」「格納」ボタンで操作
 *   ※ カタパルトとは独立して手動運用する（自動連動なし）
 *
 * 【テクスチャ偽装】
 *   左クリック時に持っているブロックの見た目に変化する
 */
public class BlockJBDController extends Block implements ITileEntityProvider {

    public BlockJBDController() {
        super(Material.IRON);
        setTranslationKey("jbd_controller");
        setRegistryName("mchelinaval", "jbd_controller");
        setHardness(3.0f);
    }

    /** 右クリック：GUIを開く */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        if (!(world.getTileEntity(pos) instanceof TileEntityJBDController)) return true;

        player.openGui(McHeliNavalAddon.instance,
                       NavalGuiHandler.GUI_JBD,
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
        return new TileEntityJBDController();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
