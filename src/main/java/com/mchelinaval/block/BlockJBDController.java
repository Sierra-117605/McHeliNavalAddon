package com.mchelinaval.block;

import com.mchelinaval.McHeliNavalAddon;
import com.mchelinaval.gui.NavalGuiHandler;
import com.mchelinaval.tileentity.IHasMimic;
import com.mchelinaval.tileentity.TileEntityJBDController;
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
import net.minecraft.world.World;

/**
 * JBD（ジェットブラストデフレクター）コントローラーブロック。
 *
 * 【操作方法】
 *   ブロックを持って右クリック → そのブロックの見た目に偽装
 *   何も持たずに右クリック    → GUIを開く（展開・格納）
 *
 * 【カタパルトとの連動】
 *   カタパルトに隣接して置くだけで自動連動（機体検知で自動展開/格納）
 */
public class BlockJBDController extends Block implements ITileEntityProvider {

    public static final PropertyBool DISGUISED = PropertyBool.create("disguised");

    public BlockJBDController() {
        super(Material.IRON);
        setTranslationKey("jbd_controller");
        setRegistryName("mchelinaval", "jbd_controller");
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
        // 常に true（偽装中も隣接ブロックの内面を非表示にする）
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        McHeliNavalAddon.logger.info("[JBD] 右クリック @ {} TE={} hand={}",
            pos,
            te == null ? "null!" : te.getClass().getSimpleName(),
            hand);

        if (!(te instanceof TileEntityJBDController)) {
            McHeliNavalAddon.logger.warn("[JBD] TileEntityJBDController でない → スキップ");
            return true;
        }

        ItemStack held = player.getHeldItem(hand);

        if (!held.isEmpty() && held.getItem() instanceof ItemBlock) {
            // ===== ブロックを持って右クリック → テクスチャ偽装 =====
            McHeliNavalAddon.logger.info("[JBD] ブロック持ち右クリック → applyMimic (held={})",
                held.getDisplayName());
            BlockFloorMarker.applyMimic(world, pos, state, (IHasMimic) te, player, hand);
        } else {
            // ===== 空手（または非ブロックアイテム）で右クリック → GUIを開く =====
            McHeliNavalAddon.logger.info("[JBD] 空手右クリック → GUI_JBD={} を openGui",
                NavalGuiHandler.GUI_JBD);
            player.openGui(McHeliNavalAddon.instance,
                           NavalGuiHandler.GUI_JBD,
                           world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        McHeliNavalAddon.logger.info("[JBD] createNewTileEntity");
        return new TileEntityJBDController();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }
}
