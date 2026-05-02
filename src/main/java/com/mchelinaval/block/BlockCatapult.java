package com.mchelinaval.block;

import com.mchelinaval.tileentity.TileEntityCatapult;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * カタパルトブロック。
 * ブロックの向き（facing）を持ち、右クリックで発射を実行する。
 * BlockHorizontalを継承することでN/S/E/W の4方向を自動的に持てる。
 */
public class BlockCatapult extends BlockHorizontal implements ITileEntityProvider {

    public BlockCatapult() {
        super(Material.IRON);
        setTranslationKey("catapult");
        setRegistryName("mchelinaval", "catapult");
        setHardness(3.0f);

        // デフォルトの向きを北に設定
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    // -------------------------------------------------------
    // 設置時に向きを設定する
    // -------------------------------------------------------
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
                                             float hitX, float hitY, float hitZ,
                                             int meta, EntityLivingBase placer) {
        // プレイヤーが向いている方向をブロックの向きにする
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
    }

    // -------------------------------------------------------
    // 右クリックで発射
    // -------------------------------------------------------
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     net.minecraft.util.EnumFacing facing,
                                     float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true; // クライアント側では何もしない

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCatapult) {
            ((TileEntityCatapult) te).fire();
        }
        return true;
    }

    // -------------------------------------------------------
    // TileEntityを生成する
    // -------------------------------------------------------
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCatapult();
    }

    // -------------------------------------------------------
    // BlockStateの定義（向きを持つ）
    // -------------------------------------------------------
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    // TileEntityを持つブロックはhasTileEntityをtrueにする
    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }
}
