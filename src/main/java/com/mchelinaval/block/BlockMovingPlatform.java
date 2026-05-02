package com.mchelinaval.block;

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
 * 右クリックで移動開始/停止をトグルする。
 * 移動方向・距離・速度はTileEntityで管理する。
 */
public class BlockMovingPlatform extends Block implements ITileEntityProvider {

    public BlockMovingPlatform() {
        super(Material.IRON);
        setTranslationKey("moving_platform");
        setRegistryName("mchelinaval", "moving_platform");
        setHardness(3.0f);
    }

    // -------------------------------------------------------
    // 右クリックで移動トグル
    // -------------------------------------------------------
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityMovingPlatform) {
            TileEntityMovingPlatform platform = (TileEntityMovingPlatform) te;

            if (player.isSneaking()) {
                // スニーク右クリック → 設定サイクル（移動方向を切り替え）
                platform.cycleMode();
                player.sendMessage(new net.minecraft.util.text.TextComponentString(
                    "[Naval] Mode: " + platform.getModeDescription()));
            } else {
                // 通常右クリック → 移動トグル
                platform.toggle();
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMovingPlatform();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
