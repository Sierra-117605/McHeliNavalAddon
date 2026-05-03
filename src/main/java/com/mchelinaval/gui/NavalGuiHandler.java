package com.mchelinaval.gui;

import com.mchelinaval.tileentity.TileEntityElevatorController;
import com.mchelinaval.tileentity.TileEntityJBDController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * GUIを開くときにForgeが呼び出すハンドラ。
 *
 * GUI ID:
 *   1 = エレベーターコントローラー
 *   2 = JBDコントローラー
 */
public class NavalGuiHandler implements IGuiHandler {

    public static final int GUI_ELEVATOR        = 1;
    public static final int GUI_JBD             = 2;
    /** @deprecated 旧BlockMovingPlatformとの互換性維持用 */
    @Deprecated
    public static final int GUI_MOVING_PLATFORM = GUI_ELEVATOR;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
                                       int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (ID == GUI_ELEVATOR && te instanceof TileEntityElevatorController) {
            return new ContainerMovingPlatform();
        }
        if (ID == GUI_JBD && te instanceof TileEntityJBDController) {
            return new ContainerMovingPlatform();
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
                                       int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);

        if (ID == GUI_ELEVATOR && te instanceof TileEntityElevatorController) {
            return new GuiMovingPlatform((TileEntityElevatorController) te, pos);
        }
        if (ID == GUI_JBD && te instanceof TileEntityJBDController) {
            return new GuiJBDController((TileEntityJBDController) te, pos);
        }
        return null;
    }
}
