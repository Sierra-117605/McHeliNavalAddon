package com.mchelinaval.gui;

import com.mchelinaval.McHeliNavalAddon;
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
        McHeliNavalAddon.logger.info("[GuiHandler] getServerGuiElement ID={} @ {} TE={}",
            ID, pos, te == null ? "null" : te.getClass().getSimpleName());

        if (ID == GUI_ELEVATOR && te instanceof TileEntityElevatorController) {
            McHeliNavalAddon.logger.info("[GuiHandler] サーバー: ContainerMovingPlatform を返す (Elevator)");
            return new ContainerMovingPlatform();
        }
        if (ID == GUI_JBD && te instanceof TileEntityJBDController) {
            McHeliNavalAddon.logger.info("[GuiHandler] サーバー: ContainerMovingPlatform を返す (JBD)");
            return new ContainerMovingPlatform();
        }
        McHeliNavalAddon.logger.warn("[GuiHandler] サーバー: 対応するコンテナなし → null を返す (ID={} TE={})",
            ID, te == null ? "null" : te.getClass().getSimpleName());
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
                                       int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        McHeliNavalAddon.logger.info("[GuiHandler] getClientGuiElement ID={} @ {} TE={}",
            ID, pos, te == null ? "null" : te.getClass().getSimpleName());

        if (ID == GUI_ELEVATOR && te instanceof TileEntityElevatorController) {
            McHeliNavalAddon.logger.info("[GuiHandler] クライアント: GuiMovingPlatform を返す");
            return new GuiMovingPlatform((TileEntityElevatorController) te, pos);
        }
        if (ID == GUI_JBD && te instanceof TileEntityJBDController) {
            McHeliNavalAddon.logger.info("[GuiHandler] クライアント: GuiJBDController を返す");
            return new GuiJBDController((TileEntityJBDController) te, pos);
        }
        McHeliNavalAddon.logger.warn("[GuiHandler] クライアント: 対応するGUIなし → null を返す (ID={} TE={})",
            ID, te == null ? "null" : te.getClass().getSimpleName());
        return null;
    }
}
