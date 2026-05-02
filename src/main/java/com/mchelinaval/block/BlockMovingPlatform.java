package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.tileentity.TileEntityMovingPlatform;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 移動プラットフォームのコントローラーブロック。
 *
 * 【操作方法】
 *   右クリック → GUIを開く
 *     GUIから「▲ 上へ」「▼ 下へ」（ELEVATORモード）
 *     GUIから「展開/格納」（JBDモード）
 *     GUIから「モード切替」（ELEVATOR ⇔ JBD）
 */
public class BlockMovingPlatform extends Block implements ITileEntityProvider {

    public BlockMovingPlatform() {
        super(Material.IRON);
        setTranslationKey("moving_platform");
        setRegistryName("mchelinaval", "moving_platform");
        setHardness(3.0f);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        // サーバー側だけGUIを開く処理をする（クライアントは false で早期リターン）
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityMovingPlatform)) return true;

        // GUIを開く（NavalGuiHandlerが呼ばれてGuiMovingPlatformが表示される）
        player.openGui(McHeliNavalAddon.instance,
                       NavalGuiHandler.GUI_MOVING_PLATFORM,
                       world,
                       pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMovingPlatform();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
