package com.mchelinaval.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * フロアマーカーブロック。
 * エレベーターが止まりたい高さに設置する目印ブロック。
 * 半透明の薄いブロックで、設置するだけで機能する。
 * 右クリックで何階かを確認できる。
 */
public class BlockFloorMarker extends Block {

    public BlockFloorMarker() {
        super(Material.GLASS);
        setTranslationKey("floor_marker");
        setRegistryName("mchelinaval", "floor_marker");
        setHardness(0.3f);
        setLightOpacity(0); // 光を通す（半透明ブロック扱い）
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        // 右クリックでY座標を表示（デバッグ用）
        player.sendMessage(new TextComponentString(
            "[Naval] Floor Marker: Y=" + pos.getY() + "  (このY座標でエレベーターが停止します)"
        ));
        return true;
    }

    // フルサイズのままガラスのように透明にする
    // → ブロックを上に積める・エレベーターのシャフト内で普通に使える
    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }
}
