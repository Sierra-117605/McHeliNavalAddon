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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * 移動プラットフォームのコントローラーブロック。
 *
 * 【ELEVATORモード】
 *   右クリック         → 上のフロアマーカーへ移動
 *   スニーク+右クリック → 下のフロアマーカーへ移動 / モード切替
 *
 * 【JBDモード】
 *   右クリック         → 手動で展開/格納トグル
 *   スニーク+右クリック → モード切替
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
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityMovingPlatform)) return true;
        TileEntityMovingPlatform platform = (TileEntityMovingPlatform) te;

        if (player.isSneaking()) {
            // スニーク+右クリック
            if (platform.getMode() == TileEntityMovingPlatform.Mode.ELEVATOR) {
                // ELEVATORモード中は下へ
                platform.goDown(player);
            } else {
                // JBDモード中はモード切替
                platform.cycleMode();
                player.sendMessage(new TextComponentString(
                    "[Naval] " + platform.getModeDescription()));
            }
        } else {
            // 通常右クリック
            if (platform.getMode() == TileEntityMovingPlatform.Mode.ELEVATOR) {
                platform.goUp(player);
            } else {
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
    public boolean hasTileEntity(IBlockState state) { return true; }
}
